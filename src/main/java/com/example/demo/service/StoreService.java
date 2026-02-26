package com.example.demo.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.example.demo.constants.ResMessage;
import com.example.demo.dao.MenuCategoryRepository;
import com.example.demo.dao.ProductOptionGroupsDao;
import com.example.demo.dao.StoresCreateDao;
import com.example.demo.dao.StoresSearchDao;
import com.example.demo.dao.StoresUpdateDao;
import com.example.demo.entity.MenuCategories;
import com.example.demo.entity.ProductOptionGroups;
import com.example.demo.entity.Stores;
import com.example.demo.projection.StoreDistanceProjection;
import com.example.demo.request.StoresReq;
import com.example.demo.response.BasicRes;
import com.example.demo.response.GoogleMapRes;
import com.example.demo.response.StoresRes;
import com.example.demo.vo.FeeDescriptionVo;
import com.example.demo.vo.MenuCategoriesVo;
import com.example.demo.vo.MenuVo;
import com.example.demo.vo.PriceLevelVo;
import com.example.demo.vo.ProductOptionGroupsVo;
import com.example.demo.vo.ProductOptionItemsVo;
import com.example.demo.vo.StoreOperatingHoursVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class StoreService {

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private StoresCreateDao storesCreateDao;

	@Autowired
	private StoresUpdateDao storesUpdateDao;

	@Autowired
	private StoresSearchDao storesSearchDao;

	@Autowired
	private ProductOptionGroupsDao productOptionGroupsDao;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ImageService imageService;

	@Autowired
	private GoogleMapService googleMapService;

	@Autowired
	private MenuCategoryRepository menuCategoryRepository;

	// Spring 會自動從 application.properties 抓取對應的值注入到變數
	@Value("${gemini.api.key}")
	private String geminiApiKey;

	@Value("${gemini.api.url}")
	private String geminiUrl;

	/*
	 * 以下為檢查邏輯
	 */

//	店家
	private void checkStore(StoresReq req) throws Exception {
//		電話
		// 先處理字串：移除所有橫線，並過濾掉空白（避免前端傳入 "02- 123" 這種狀況）
		String purePhone = req.getPhone() == null ? "" : req.getPhone().replace("-", "").trim();

		// 判斷邏輯：
		// 台灣電話規格：
		// - 手機：09xxxxxxxx (共10碼)
		// - 市話：0x-xxxxxxx (共9碼) 或 0x-xxxxxxxx (共10碼，如台北/台中/高雄)
		if (!StringUtils.hasText(purePhone)) {
			throw new Exception("電話不能為空喵!");
		}
		req.setPhone(purePhone);

		// 使用正則表達式檢查格式與長度：
		// ^0：必須以 0 開頭
		// \d{8,9}：後面接 8 到 9 位數字 (總長 9~10 碼)
		if (!purePhone.matches("^0\\d{8,9}$")) {
			throw new Exception(ResMessage.PHONE_SIZE_ERROR.getMessage());
		}
//	  	category類型驗證
		if (!(req.getCategory().equals("fast") || req.getCategory().equals("slow"))) {
			throw new Exception(ResMessage.CATEGORY_ERROR.getMessage());
		}
//		檢查運費級距
		if (!CollectionUtils.isEmpty(req.getFee_description())) {
			for (FeeDescriptionVo vo : req.getFee_description()) {
				if (vo.getKm() < 0 || vo.getFee() < 0)
					throw new Exception("運費或距離不可為負喵!");
			}
		}
	}

	private void checkStoreExist(int storeId) throws Exception {
		Stores existingStore = storesSearchDao.getStoreById(storeId);
		if (existingStore == null) {
			throw new Exception(ResMessage.STORE_NOT_FOUND.getMessage());
		}
	}

//時刻
	private void checkHours(List<StoreOperatingHoursVo> list) throws Exception {
		if (CollectionUtils.isEmpty(list))
			return;
		java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
		for (StoreOperatingHoursVo vo : list) {
			try {
				// 檢查是否為 1~7
				DayOfWeek.of(vo.getWeek());
				java.time.LocalTime.parse(vo.getOpenTime(), timeFormatter);
				java.time.LocalTime.parse(vo.getCloseTime(), timeFormatter);
			} catch (DateTimeParseException e) {
				// 如果格式不對 (例如 25:00 或 12:70) 會進到這裡
				throw new Exception("時間格式錯誤喵!，請使用 HH:mm 格式喵! (例如 09:00)");
//		子類別
			} catch (DateTimeException e) {
				throw new Exception("無效的星期數值喵!: " + vo.getWeek());
//		父類別
			}
		}
	}

//品項
	private void checkMenu(StoresReq req) throws Exception {
		List<MenuCategoriesVo> categoriesList = req.getMenuCategoriesVoList();
		List<ProductOptionGroupsVo> groupList = req.getProductOptionGroupsVoList();

		if (CollectionUtils.isEmpty(categoriesList)) {
			throw new Exception("你的商品沒有類別喵!");
		}

		// 蒐集關聯ID
		java.util.Set<String> validTags = new java.util.HashSet<>();
		if (!CollectionUtils.isEmpty(groupList)) {
			for (ProductOptionGroupsVo g : groupList) {
				if (StringUtils.hasText(g.getTempId()))
					validTags.add(g.getTempId());
				if (g.getId() > 0)
					validTags.add(String.valueOf(g.getId()));
			}
		}

		for (MenuCategoriesVo vo : categoriesList) {
			// 檢查類別名稱
			if (!StringUtils.hasText(vo.getName())) {
				throw new Exception("類別名稱不可為空喵!");
			}

			// 檢查該類別下的「規格/價位級距」 (PriceLevel)
			if (!CollectionUtils.isEmpty(vo.getPriceLevel())) {
				for (PriceLevelVo priceLevel : vo.getPriceLevel()) {
					if (!StringUtils.hasText(priceLevel.getName())) {
						throw new Exception("類別 [" + vo.getName() + "] 內的規格名不可為空喵!");
					}
					if (priceLevel.getPrice() < 0) {
						throw new Exception("規格 [" + priceLevel.getName() + "] 的價格不可為負數喵!");
					}
				}
			}

			// 檢查該類別底下的「品項列表」 (MenuVo)
			List<MenuVo> menuList = vo.getMenuVo();
			if (!CollectionUtils.isEmpty(menuList)) {
				for (MenuVo menuVo : menuList) {
					// 檢查品名
					if (!StringUtils.hasText(menuVo.getName())) {
						throw new Exception("類別 [" + vo.getName() + "] 中有商品的名稱為空喵!");
					}
					// 檢查基礎價格
					if (menuVo.getBasePrice() < 0) {
						throw new Exception("商品 [" + menuVo.getName() + "] 的價格不可為負數喵!");
					}
					if (menuVo.getUnusual() instanceof List) {
						List<?> unusualList = (List<?>) menuVo.getUnusual();
						for (Object obj : unusualList) {
							if (obj instanceof Map) {
								Map<?, ?> map = (Map<?, ?>) obj;
								for (Object key : map.keySet()) {
									String refId = String.valueOf(key);
									if (!validTags.contains(refId)) {
										throw new Exception("商品 [" + menuVo.getName() + "] 關聯了不存在的選項群組喵! ID: " + refId);
									}
								}
							}
						}
					}
				}
			}
		}
	}

//選項(群組)
	private void checkOptions(List<ProductOptionGroupsVo> groupList) throws Exception {
		if (CollectionUtils.isEmpty(groupList))
			return;
		for (ProductOptionGroupsVo vo : groupList) {
			if (!StringUtils.hasText(vo.getName()))
				throw new Exception("選項群組名不可為空喵!");
			if (vo.getMaxSelection() < 0)
				throw new Exception("規格 [" + vo.getName() + "] 的最多可選項數不可為負數喵!");
			if (!CollectionUtils.isEmpty(vo.getItems())) {
				for (ProductOptionItemsVo item : vo.getItems()) {
					if (!StringUtils.hasText(item.getName()))
						throw new Exception("選項名不可為空喵!");
					if (item.getExtraPrice() != null && item.getExtraPrice() < 0)
						throw new Exception("加價不可為負喵!");
				}
			}
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveSubTables(int storeId, StoresReq req) throws JsonProcessingException {
//		填入營業時間
		List<StoreOperatingHoursVo> operatingHoursvoList = req.getOperatingHoursVoList();
		for (StoreOperatingHoursVo vo : operatingHoursvoList) {
			storesCreateDao.addOperatingHours(storeId, vo.getWeek(), vo.getOpenTime(), vo.getCloseTime());
		}

//		填入選項群組
		List<ProductOptionGroupsVo> ProductOptionGroupsVoList = req.getProductOptionGroupsVoList();
		Map<String, Integer> idMap = new HashMap<>();
		for (ProductOptionGroupsVo vo : ProductOptionGroupsVoList) {
			ProductOptionGroups group = new ProductOptionGroups();
			group.setStoresId(storeId);
			group.setName(vo.getName());
			group.setRequired(vo.isRequired());
			group.setMaxSelection(vo.getMaxSelection());

			group = productOptionGroupsDao.save(group);

			int groupId = group.getId();

			if (StringUtils.hasText(vo.getTempId())) {
				idMap.put(vo.getTempId(), group.getId());
			} else {
				// 如果是修改舊資料，可能直接傳 id，也要存入 map
				idMap.put(String.valueOf(vo.getId()), group.getId());
			}

//			storesCreateDao.addOptionGroups(storeId, vo.getName(), vo.isRequired(), vo.getMaxSelection());
//			int groupId = storesCreateDao.getLastInsertId();
//		填入選項
			List<ProductOptionItemsVo> itemVoList = vo.getItems();
			if (!CollectionUtils.isEmpty(itemVoList)) {
				for (ProductOptionItemsVo itemVo : itemVoList) {
					Integer finalPrice = (itemVo.getExtraPrice() == null) ? 0 : itemVo.getExtraPrice();
					storesCreateDao.addOptionItems(groupId, itemVo.getName(), finalPrice);
				}
			}
		}

		// 填入品項類別及其所屬品項
		List<MenuCategoriesVo> menuCategoriesVoList = req.getMenuCategoriesVoList();
		if (!CollectionUtils.isEmpty(menuCategoriesVoList)) {
			for (MenuCategoriesVo catVo : menuCategoriesVoList) {

				// 存入類別並取得資料庫自動生成的 ID
				String priceLevelListStr = mapper.writeValueAsString(catVo.getPriceLevel());
				int realCategoryId = categoryReturnId(storeId, catVo.getName(), priceLevelListStr);

				// 存入屬於此類別的品項
				List<MenuVo> items = catVo.getMenuVo();
				for (MenuVo menuVo : items) {

					List<Map<Integer, String>> finalUnusual = new ArrayList<>();

					if (menuVo.getUnusual() instanceof List) {
						List<Map<String, String>> rawUnusual = 
						mapper.convertValue(menuVo.getUnusual(), new TypeReference<List<Map<String, String>>>() {});
//								(List<Map<String, String>>) menuVo.getUnusual();
						for (Map<String, String> entry : rawUnusual) {
							for (Map.Entry<String, String> e : entry.entrySet()) {
								String tempId = e.getKey();
								String description = e.getValue();

								Integer realId = idMap.get(tempId);
								if (realId != null) {
									finalUnusual.add(Map.of(realId, description));
								}
							}
						}
					}
					// 將轉換後的 finalUnusual 序列化為 JSON 字串
					String unusualStr = mapper.writeValueAsString(finalUnusual);

					// 傳入剛剛拿到的 realCategoryId
					storesCreateDao.addMenu(storeId, realCategoryId, menuVo.getName(), menuVo.getDescription(),
							menuVo.getBasePrice(), menuVo.getImage(), menuVo.isAvailable(), unusualStr);

					// 處理圖片確認menuVo.getImage() != null && !menuVo.getImage().isEmpty()
					if (StringUtils.hasText(menuVo.getImage())) {
						imageService.confirmImage(menuVo.getImage());
					}
				}
			}
		}
	}

////		填入品項
//		List<MenuVo> menuVoList = req.getMenuVoList();
//		for (MenuVo vo : menuVoList) {
//			//	空值防呆		
//			Object unusualObj = vo.getUnusual() != null ? vo.getUnusual() : new ArrayList<>();
//			String unusualStr = mapper.writeValueAsString(unusualObj);
//			storesCreateDao.addMenu(storeId, vo.getCategoryId(), vo.getName(), vo.getDescription(), //
//					vo.getBasePrice(), vo.getImage(), vo.isAvailable(), unusualStr);
//			if (vo.getImage() != null && !vo.getImage().isEmpty()) {
//	            imageService.confirmImage(vo.getImage());
//	        }
//		}
////		填入品項類別
//		List<MenuCategoriesVo> MenuCategoriesVoList = req.getMenuCategoriesVoList();
//		for (MenuCategoriesVo vo : MenuCategoriesVoList) {
//			String priceLevelListStr = mapper.writeValueAsString(vo.getPriceLevel());
//			storesCreateDao.addCategory(storeId, vo.getName(), priceLevelListStr);
//		}

	private void getLatLng(Stores store, String address) throws Exception {
		// 呼叫 Service 取得結果
		GoogleMapRes googleMapRes = googleMapService.googleMapAddress(address);

		// 檢查狀態碼是否為 200 (成功)
		if (googleMapRes != null && googleMapRes.getCode() == 200) {
			store.setLat(googleMapRes.getLat());
			store.setLng(googleMapRes.getLng());
		} else {
			// 如果定位失敗，拋出異常讓事務回滾，或是給予預設提示
			String errorMsg = (googleMapRes != null) ? googleMapRes.getMessage() : "定位服務無回應喵!";
			throw new Exception("地址定位失敗喵!：" + errorMsg);
		}
	}

	// 專門為「用戶搜尋」服務的座標處理
	private double[] resolveUserLocation(Double lat, Double lng, String address) throws Exception {
		// 情況 A：用戶開啟定位，直接傳入座標 (優先度最高)
		if (lat != null && lat != 0 && lng != null && lng != 0) {
			return new double[] { lat, lng };
		}

		// 情況 B：用戶手動輸入搜尋地址 (如：「台北 101」、「台中火車站」)
		if (address != null && !address.trim().isEmpty()) {
			// 借用一個暫時的 Stores 物件來承接 getLatLng 的結果
			Stores tempLoc = new Stores();
			getLatLng(tempLoc, address);
			return new double[] { tempLoc.getLat(), tempLoc.getLng() };
		}

		// 情況 C：兩者都沒有，無法搜尋
		throw new Exception("請開啟定位或輸入搜尋地址喵!");
	}

	// 回滾
//	創建店家
	@Transactional(rollbackFor = Exception.class)
	public BasicRes create(StoresReq req) throws Exception {

		checkStore(req);
		checkHours(req.getOperatingHoursVoList());
		checkMenu(req);
		checkOptions(req.getProductOptionGroupsVoList());

//    店家已存在
		if (storesCreateDao.existsByPhone(req.getPhone()) > 0) {
//			防孤兒圖片
			if(StringUtils.hasText(req.getImage())) {
				imageService.deleteImage(req.getImage());
			}

			return new BasicRes(ResMessage.STORE_EXISTS.getCode(), //
					ResMessage.STORE_EXISTS.getMessage());
		}

//		傳店家表取店家ID
		Stores store = new Stores();
		String feeStr = mapper.writeValueAsString(req.getFee_description());

		store.setName(req.getStoresname());
		store.setPhone(req.getPhone().replace("-", "").trim());
		store.setAddress(req.getAddress());
		store.setCategory(req.getCategory());
		store.setType(req.getType());
		store.setMemo(req.getMemo());
		store.setImage(req.getImage());
		store.setFeeDescription(feeStr);
		store.setPublish(req.isPublish());
		store.setCreatedBy(req.getCreatedBy());

//		由地址取得經緯度
		getLatLng(store, req.getAddress());

		store = storesCreateDao.save(store);

//		storesCreateDao.addStore(req.getStoresname(), req.getPhone(), req.getAddress(), //
//				req.getCategory(), req.getType(), req.getMemo(), //
//				req.getImage(), feeStr, req.isPublish(), req.getCreatedBy());

		int storeId = store.getId();

//		填入子表
		saveSubTables(storeId, req);

//		更改圖片標籤
		if (StringUtils.hasText(req.getImage())) {
			System.out.println("準備確認圖片路徑: " + req.getImage());
			imageService.confirmImage(req.getImage());
		}

		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

	// 店家修改
	@Transactional(rollbackFor = Exception.class)
	public BasicRes update(int storeId, StoresReq req) throws Exception {
		// 檢查店家是否存在並暫存新店家
		Stores existingStore = storesSearchDao.getStoreById(storeId);
		if (existingStore == null) {
			throw new Exception(ResMessage.STORE_NOT_FOUND.getMessage());
		}

		// 新地址的經緯度(有改才查)
		if (!existingStore.getAddress().equals(req.getAddress())) {
			getLatLng(existingStore, req.getAddress());
		}
		String oldImageUrl = existingStore.getImage();
		String newImageUrl = req.getImage();

		if (oldImageUrl != null && !oldImageUrl.equals(newImageUrl)) {
			try {
				imageService.deleteImage(oldImageUrl);
			} catch (Exception e) {
				// 捕捉但不拋出，確保資料庫更新能繼續
				System.err.println("舊圖片刪除失敗，但商店更新繼續執行喵!: " + e.getMessage());
			}
		}
		// 檢查
		checkStore(req);
		checkHours(req.getOperatingHoursVoList());
		checkMenu(req);
		checkOptions(req.getProductOptionGroupsVoList());

		// 檢查電話是否已被其他店家佔用 (排除目前的 storeId)
		if (storesUpdateDao.countByPhoneExcludeSelf(req.getPhone(), storeId) > 0) {
			return new BasicRes(ResMessage.STORE_EXISTS.getCode(), //
					ResMessage.STORE_EXISTS.getMessage());
		}

		// 更新店家主表資訊
		String feeStr = mapper.writeValueAsString(req.getFee_description());
		storesUpdateDao.updateStore(storeId, req.getStoresname(), req.getPhone(), req.getAddress(), //
				req.getCategory(), req.getType(), req.getMemo(), //
				req.getImage(), feeStr, req.isPublish(), //
				existingStore.getLng(), //
				existingStore.getLat());

		// 收集舊菜單的所有圖片網址
		List<Map<String, Object>> oldMenu = storesSearchDao.getMenuByStoreId(storeId);
		List<String> oldImages = new ArrayList<>();

		// 遍歷舊菜單
		if (!CollectionUtils.isEmpty(oldMenu)) {
			for (Map<String, Object> menuMap : oldMenu) {
				Object imgObj = menuMap.get("image");
				if (imgObj != null && StringUtils.hasText(imgObj.toString())) {
					oldImages.add(imgObj.toString());
				}
			}
		}

		// 收集新 Request 裡所有的圖片網址
		List<String> newImages = new ArrayList<>();

		// 類別
		if (!CollectionUtils.isEmpty(req.getMenuCategoriesVoList())) {
			for (MenuCategoriesVo catVo : req.getMenuCategoriesVoList()) {

				// 品項清單
				List<MenuVo> menuList = catVo.getMenuVo(); // 取得該類別的品項
				if (!CollectionUtils.isEmpty(menuList)) {
					for (MenuVo menuVo : menuList) {

						// 檢查品項有沒有圖片
						String imgUrl = menuVo.getImage();
						if (StringUtils.hasText(imgUrl)) {
							newImages.add(imgUrl);
						}
					}
				}
			}
		}

		// 找出「出現在舊名單、但沒出現在新名單」的圖片（代表這些圖被換掉或刪除了）
		for (String oldImg : oldImages) {
			if (!newImages.contains(oldImg)) {
				try {
					imageService.deleteImage(oldImg);
				} catch (Exception e) {
					System.err.println("舊品項圖片刪除失敗喵!: " + oldImg);
				}
			}
		}
		// 改新圖標籤
		if (newImageUrl != null && !newImageUrl.equals(oldImageUrl)) {
			imageService.confirmImage(newImageUrl);
		}

		// 清除舊有的子表資料 (先刪除有外鍵關聯的底層資料)
		// 順序：OptionItems -> OptionGroups -> Menu -> Categories -> Hours
		storesUpdateDao.deleteOptionItemsByStoreId(storeId);
		storesUpdateDao.deleteOptionGroupsByStoreId(storeId);
		storesUpdateDao.deleteMenuByStoreId(storeId);
		storesUpdateDao.deleteMenuCategoriesByStoreId(storeId);
		storesUpdateDao.deleteOperatingHoursByStoreId(storeId);

		// 重新寫入子表資料
		saveSubTables(storeId, req);

		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

//		物理全刪
	@Transactional(rollbackFor = Exception.class)
	public BasicRes deleteFullStore(int storeId) throws Exception {
		// 檢查店是否存在
		Stores store = storesSearchDao.getStoreById(storeId);
		if (store == null) {
			return new BasicRes(ResMessage.STORE_NOT_FOUND.getCode(), ResMessage.STORE_NOT_FOUND.getMessage());
		}
		// 刪除該店的雲端圖片
		if (store.getImage() != null) {
			imageService.deleteImage(store.getImage());
		}
		// 刪除最底層的選項細項 (依賴於 groups)
		storesUpdateDao.deleteOptionItemsByStoreId(storeId);

		// 刪除選項群組
		storesUpdateDao.deleteOptionGroupsByStoreId(storeId);

		// 刪除菜單品項
		storesUpdateDao.deleteMenuByStoreId(storeId);

		// 刪除品項類別
		storesUpdateDao.deleteMenuCategoriesByStoreId(storeId);

		// 刪除營業時間
		storesUpdateDao.deleteOperatingHoursByStoreId(storeId);

		// 刪除店家主體
		int result = storesUpdateDao.deleteStoreById(storeId);

		if (result > 0) {
			return new BasicRes(ResMessage.SUCCESS.getCode(), "店家ID" + storeId + "已被物理抹除喵!");
		} else {
			return new BasicRes(ResMessage.STORE_NOT_FOUND.getCode(), ResMessage.STORE_NOT_FOUND.getMessage());
		}
	}

//		軟刪除
	@Transactional(rollbackFor = Exception.class)
	public BasicRes deleteStore(int storeId) throws Exception {
		checkStoreExist(storeId);
		// 刪除所有子表資料
		storesUpdateDao.deleteOptionItemsByStoreId(storeId);
		storesUpdateDao.deleteOptionGroupsByStoreId(storeId);
		storesUpdateDao.deleteMenuByStoreId(storeId);
		storesUpdateDao.deleteMenuCategoriesByStoreId(storeId);
		storesUpdateDao.deleteOperatingHoursByStoreId(storeId);

		// 主表執行「軟刪除」 (將 is_deleted 改為 true)
		int result = storesUpdateDao.softDeleteStoreById(storeId);

		if (result <= 0) {
			throw new Exception("找不到該店家或已被刪除喵! (ID: " + storeId + ")");
		}

		return new BasicRes(ResMessage.SUCCESS.getCode(), "店家 ID " + storeId + " 已成功軟刪除喵!");
	}

	public StoresRes getStoresByName(String name) {
		try {
			// 修正點：如果未輸入或只有空白，直接回傳空清單
			if (!StringUtils.hasText(name)) {
				return new StoresRes(ResMessage.INPUT_IS_EMPTY.getCode(), "請輸入搜尋關鍵字喵!", null);
			}

			List<Stores> storeList = storesSearchDao.findStoresByNameLike(name);

			return new StoresRes(ResMessage.SUCCESS.getCode(), "搜尋成功喵!，共 " + storeList.size() + " 筆喵!", storeList);
		} catch (Exception e) {
			return new StoresRes(500, "搜尋失敗喵!: " + e.getMessage());
		}
	}

	public StoresRes getStoreById(int storesId) {
		try {
			// 查詢店家主表 (確認是否存在且未被軟刪除)
			Stores store = storesSearchDao.getStoreById(storesId);
			if (store == null) {
				return new StoresRes(ResMessage.STORE_NOT_FOUND.getCode(), ResMessage.STORE_NOT_FOUND.getMessage(),
						null);
			}

			StoresRes res = new StoresRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(),
					List.of(store));
			// 營業時間
			List<Map<String, Object>> hoursMap = storesSearchDao.getOperatingHoursByStoreId(storesId);

			res.setOperatingHoursVoList(mapper.convertValue(hoursMap, new TypeReference<List<StoreOperatingHoursVo>>() {
			}));

			// 菜單分類
			List<Map<String, Object>> categoriesMap = storesSearchDao.getCategoriesByStoreId(storesId);
			List<MenuCategoriesVo> categoriesVoList = new ArrayList<>();
			for (Map<String, Object> map : categoriesMap) {
				MenuCategoriesVo catVo = new MenuCategoriesVo();
				catVo.setStoresId(storesId);

				if (map.get("id") != null) {
					catVo.setId((Integer) map.get("id"));
				}

				catVo.setName((String) map.get("name"));
				// 手動將 price_level 字串轉為 List
				Object priceLevel = map.get("priceLevel");
				if (priceLevel != null && StringUtils.hasText(priceLevel.toString())) {
					catVo.setPriceLevel(
							mapper.readValue(priceLevel.toString(), new TypeReference<List<PriceLevelVo>>() {
							}));
				}
				categoriesVoList.add(catVo);
			}
			res.setMenuCategoriesVoList(categoriesVoList);

			// 菜單品項
			List<Map<String, Object>> menuMap = storesSearchDao.getMenuByStoreId(storesId);
			List<MenuVo> menuVoList = new ArrayList<>();
			for (Map<String, Object> map : menuMap) {
				// 先轉簡單欄位
				Map<String, Object> tempMap = new HashMap<>(map);
				// 先移出unusual
				tempMap.remove("unusual");
				// 生成新map(不含unusual)
				MenuVo mVo = mapper.convertValue(tempMap, MenuVo.class);
				// 手動將 unusual 字串轉為 Object/Map
				String unusualStr = (String) map.get("unusual");
				if (StringUtils.hasText(unusualStr)) {
					// 解析為 Map，保留 JSON 的鍵值結構
					mVo.setUnusual(mapper.readValue(unusualStr, new TypeReference<List<Map<String, String>>>() {
					}));
				} else {
					mVo.setUnusual(java.util.Collections.emptyList());
				}
				menuVoList.add(mVo);
			}
			res.setMenuVoList(menuVoList);

			// 選項群組與細項 (兩層巢狀)
			List<Map<String, Object>> groupsMap = storesSearchDao.getOptionGroupsByStoreId(storesId);
			List<ProductOptionGroupsVo> groupsVo = mapper.convertValue(groupsMap,
					new TypeReference<List<ProductOptionGroupsVo>>() {
					});

			for (ProductOptionGroupsVo group : groupsVo) {
				List<Map<String, Object>> itemsMap = storesSearchDao.getOptionItemsByGroupId(group.getId());
				group.setItems(mapper.convertValue(itemsMap, new TypeReference<List<ProductOptionItemsVo>>() {
				}));
			}
			res.setProductOptionGroupsVoList(groupsVo);

			// 處理主表內的 JSON 字串 (如 FeeDescription)
			// store 沒有 VoList 欄位，但 res 有，解析後塞給 res
			if (StringUtils.hasText(store.getFeeDescription())) {
				List<FeeDescriptionVo> fees = mapper.readValue(store.getFeeDescription(),
						new TypeReference<List<FeeDescriptionVo>>() {
						});
				res.setFeeDescriptionVoList(fees);
			}

			for (MenuCategoriesVo cat : categoriesVoList) {
				List<MenuVo> itemsForThisCat = new ArrayList<>();
				for (MenuVo item : menuVoList) {
					if (item.getCategoryId() == cat.getId()) {
						itemsForThisCat.add(item);
					}
				}
				cat.setMenuVo(itemsForThisCat); // 把屬於該類別的品項塞進去
			}

			return res;

		} catch (Exception e) {
			return new StoresRes(500, "取得店家資料失敗喵!: " + e.getMessage(), null);
		}
	}

//	取得全部店家
	public StoresRes getAllStores() {

		List<Stores> storesList = storesSearchDao.getAllStores();
		if (CollectionUtils.isEmpty(storesList)) {
			return new StoresRes(ResMessage.STORE_NULL_ERROR.getCode(), ResMessage.STORE_NULL_ERROR.getMessage());
		}

		return new StoresRes(ResMessage.SUCCESS.getCode(), "共" + storesList.size() + "筆資料喵!", storesList);
	}

//	AI填菜單
	//

	public String callGeminiApi(byte[] imageBytes) throws Exception {

		// 1. 強制等待，避免觸發配額限制
//		Thread.sleep(2000);

		// 2. 壓縮圖片
		byte[] processedImage = compressImage(imageBytes);

		// 3. 確保使用壓縮後的圖轉 Base64
		String base64Image = Base64.getEncoder().encodeToString(processedImage);

		// 4. URL
		String cleanUrl = geminiUrl == null ? "" : geminiUrl.trim();
		String cleanKey = geminiApiKey == null ? "" : geminiApiKey.trim();
		String finalUrl = cleanUrl + "?key=" + cleanKey;

		String prompt = """
								你是一個專業的菜單辨識員。請分析圖片並「僅」以 JSON 格式回傳資料。
				            請確保 JSON 欄位完全符合以下結構：
					            {
				            "storesname": "店家名稱",
				            "phone": "電話 (請移除括號、空格與橫線，僅保留純數字，例如 033605369 或 0912345678)"
				            "address": "地址",
				            "category": "fast",
				            "type": "類型",
				            "memo": "備註",
				            "fee_description": [{ "km": 2, "fee": 30 }],
				            "operatingHoursVoList": [{ "week": 1, "openTime": "09:00", "closeTime": "21:00" }],
				            "menuCategoriesVoList": [{ "name": "類別", "menuVo": [{ "name": "品名", "description": "描述", "basePrice": 80, "unusual": [{"tempId":"value"}] }],"priceLevel": [{ "name": "規格", "price": 100 }] }]
				            "productOptionGroupsVoList": [{ "tempId": "t1", "name": "選項群組", "items": [{ "name": "項目", "extraPrice": 0 }] }]
				        }
				unusual欄位可以留空,如有產品特殊規格、價格(熱飲、與同類型商品加大不同價)視必要性填入
				商品、各欄位要盡可能完整齊全提供
				沒有的填 空陣列[] 或 空字串"" 依欄位判斷
				category 依據產業判斷：餐飲業為 fast，非餐飲業為 slow
				unusual 必須是 List<Map<String, String>> 格式
				unusual 的 Key 必須對應到 productOptionGroupsVoList 中定義的 tempId
					            """;

		Map<String, Object> requestBody = //
				Map.of("contents", List.of(// 內容層
						Map.of("parts", List.of(// 媒體陣列
								Map.of("text", prompt), // 提示詞
								Map.of("inline_data", Map.of(// 傳送圖片的標準格式
										"mime_type", "image/jpeg", // 定義格式
										"data", base64Image// 壓縮後轉成 Base64 編碼的圖片字串
								))))), "generationConfig", Map.of(// 設定層
										"response_mime_type", "application/json", // 強制 AI 回傳純 JSON 字串
										"temperature", 0.1 // 降低隨機性 範圍通常是 0.0 到 2.0
				));

		// 告訴GOOGLE伺服器要傳送的資料格式(避免415 Unsupported Media Type)
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// 將要傳送的內容貼上標籤(JSON)
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

		// AI回答
		return restTemplate.postForObject(finalUrl, entity, String.class);

	}

	public StoresReq aiAnalyzeMenu(byte[] imageBytes) throws Exception {
		String rawResponse = callGeminiApi(imageBytes);

		try {
			// 讀取樹狀結構
			JsonNode root = mapper.readTree(rawResponse);
			// Gemini 的回傳結構中，答案放在 candidates 陣列裡
			JsonNode candidate = root.path("candidates").get(0);

			// 檢查 API 是否有回傳內容（有時會因為安全過濾而回傳空內容）
			if (candidate.path("content").path("parts").isMissingNode()) {
				throw new Exception("Gemini 3 拒絕回應或圖片無法辨識，請更換圖片再試喵!");
			}

			// 確保回傳JSON符合格式
			String aiJsonText = candidate.path("content").path("parts").get(0).path("text").asText();

			// JSON 清洗邏輯
			// 尋找第一個 '{' 和最後一個 '}'，確保只抓取 JSON 本體
			int start = aiJsonText.indexOf("{");
			int end = aiJsonText.lastIndexOf("}");
			if (start == -1 || end == -1) {
				throw new Exception("AI 回傳的內容不包含合法的 JSON 結構喵!");
			}
			String cleanedJson = aiJsonText.substring(start, end + 1);

			// 4. 轉換為 StoresReq 物件
			StoresReq req = mapper.readValue(cleanedJson, StoresReq.class);

			// 5. 確保關鍵欄位有預設值
			if (req.getCategory() == null)
				req.setCategory("fast");
			if (req.getStoresname() == null)
				req.setStoresname("未命名店家");

			return req;

		} catch (Exception e) {
			// 增加詳細錯誤輸出，方便調試
			System.err.println("解析異常回傳內容喵!：" + rawResponse);
			throw new Exception("AI 辨識解析失敗喵!: " + e.getMessage());
		}
	}

//	壓縮
	private byte[] compressImage(byte[] imageBytes) throws Exception {
		try {

			// 讀取資料包裝成串流
			ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
			// 準備容器，用來裝壓縮過圖檔
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			/*
			 * 壓縮參數 size(1024, 1024) 會將長邊縮放至 1024，寬邊等比例縮小 outputQuality(0.7) 壓縮率
			 * 70%，體積會掉非常多但文字依然清晰
			 */
			Thumbnails.of(bais).size(1024, 1024)// 大小
					.outputQuality(0.7)// 畫質
					.outputFormat("jpg")// 格式
					.toOutputStream(baos);// 輸出

			return baos.toByteArray();
		} catch (Exception e) {
			// 如果壓縮失敗，就回傳原圖，不要讓程式掛掉
			return imageBytes;
		}
	}

//	O公里內店家

	public StoresRes getNearbyStores(Double lat, Double lng, String address, double radius) {
		try {
			// 解析實際搜尋的座標
			double[] targetCoords = resolveUserLocation(lat, lng, address);
			double finalLat = targetCoords[0];
			double finalLng = targetCoords[1];

			// 計算經緯度邊界框 (Bounding Box) 以優化 SQL 效能
			double latOffset = radius / 111.0;
			double lngOffset = radius / (111.0 * Math.cos(Math.toRadians(finalLat)));

			double minLat = finalLat - latOffset;
			double maxLat = finalLat + latOffset;
			double minLng = finalLng - lngOffset;
			double maxLng = finalLng + lngOffset;

			// 呼叫 DAO 進行距離排序搜尋
			List<StoreDistanceProjection> projections = storesSearchDao.findNearbyWithDistance(finalLat, finalLng,
					radius, minLat, maxLat, minLng, maxLng);

			String locationMsg = (address != null && !address.isEmpty()) ? address : "當前定位";
			return new StoresRes(ResMessage.SUCCESS.getCode(),
					"於 [" + locationMsg + "] 半徑 " + radius + " 公里內共搜尋到 " + projections.size() + " 筆喵!", projections,
					true);
		} catch (Exception e) {
			return new StoresRes(500, "搜尋失敗喵!: " + e.getMessage());
		}
	}

	private int categoryReturnId(int storeId, String name, String priceLevel) {
		MenuCategories categories = new MenuCategories(); 
		categories.setStoreId(storeId);
		categories.setName(name);
		categories.setPriceLevel(priceLevel);

		// save() 方法會回傳存檔後的實體，裡面就包含自動生成的 ID
		categories = menuCategoryRepository.save(categories);

		return categories.getId();
	}
	
	public StoresRes getOperatingStores(StoresReq req) {
	    if (CollectionUtils.isEmpty(req.getFilteredStoreIds())) {
	        return new StoresRes(200, "無篩選範圍內的店家喵！");
	    }
	    try {
	        LocalDateTime ldt = LocalDateTime.now();
	        LocalTime now = ldt.toLocalTime();
	        
	        int dayOfWeek = ldt.getDayOfWeek().getValue(); 

	        List<Map<String, Object>> storeList = storesSearchDao.findOperatingStoresByIds(
	                req.getFilteredStoreIds(), 
	                now, 
	                dayOfWeek
	        );

	        if (CollectionUtils.isEmpty(storeList)) {
	            return new StoresRes(404, "所選店家目前皆非營業時間(或今日公休)喵！");
	        }

	        StoresRes res = new StoresRes(200, "搜尋成功喵！共 " + storeList.size() + " 筆喵！");
	        res.setStoreOperatingList(storeList);
	        return res;
	    } catch (Exception e) {
	        return new StoresRes(500, "系統錯誤喵...: " + e.getMessage());
	    }
	}

}
