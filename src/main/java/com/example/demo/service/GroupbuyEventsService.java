package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.example.demo.constants.GroupbuyStatusEnum;
import com.example.demo.constants.PaymentStatus;
import com.example.demo.constants.ResMessage;
import com.example.demo.dao.GroupbuyEventsDao;
import com.example.demo.dao.GroupsSearchViewDao;
import com.example.demo.dao.OrdersDao;
import com.example.demo.dao.PersonalOrderDao;
import com.example.demo.dao.StoresSearchDao;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.CartDTO;
import com.example.demo.entity.GroupbuyEvents;
import com.example.demo.entity.GroupsSearchView;
import com.example.demo.entity.Menu;
import com.example.demo.entity.Orders;
import com.example.demo.entity.OrdersSearchView;
import com.example.demo.entity.PersonalOrder;
import com.example.demo.entity.Stores;
import com.example.demo.entity.User;
import com.example.demo.projection.GroupbuyEventsProjection;
import com.example.demo.request.GroupbuyEventsReq;
import com.example.demo.request.personalOrderReq;
import com.example.demo.response.BasicRes;
import com.example.demo.response.GroupbuyEventsRes;
import com.example.demo.response.GroupbuyEventsResNew;
import com.example.demo.response.ShippingFeeRes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GroupbuyEventsService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private StoresSearchDao storesSearchDao;

	@Autowired
	private GroupbuyEventsDao groupbuyEventsDao;

	@Autowired
	private PersonalOrderDao personalOrderDao;

	@Autowired
	private GroupsSearchViewDao groupsSearchViewDao;

	@Autowired
	private OrdersDao ordersDao;

	@Autowired
	private PersonalOrderService personalOrderService;

	ObjectMapper mapper = new ObjectMapper();

	// 將重複的驗證邏輯提取出來
	private GroupbuyEventsResNew checkEvent(GroupbuyEventsReq req) {
		// 檢查團長ID
		if (!StringUtils.hasText(req.getHostId())) {
			return new GroupbuyEventsResNew(ResMessage.HOST_ID_ERROR.getCode(), //
					ResMessage.HOST_ID_ERROR.getMessage());
		}
		// 檢查有沒有團長
		User user = userDao.findById(req.getHostId()).orElse(null);
		if (user == null) {
			return new GroupbuyEventsResNew(ResMessage.HOST_ID_NOT_FOUND.getCode(),
					ResMessage.HOST_ID_NOT_FOUND.getMessage());
		}

		// 檢查商店ID
		if (req.getStoresId() == 0) {
			return new GroupbuyEventsResNew(ResMessage.STORES_ID_ERROR.getCode(), //
					ResMessage.STORES_ID_ERROR.getMessage());
		}
		// 檢查商家是否存在
		Stores stores = storesSearchDao.getStoreById(req.getStoresId());
		if (stores == null) {
			return new GroupbuyEventsResNew(ResMessage.STORES_ID_NULL.getCode(), //
					ResMessage.STORES_ID_NULL.getMessage());
		}
		// 檢查團名
		if (!StringUtils.hasText(req.getEventName())) {
			return new GroupbuyEventsResNew(400, "團名必填");
		}

		// 檢查結束時間
		if (req.getEndTime() == null) {
			return new GroupbuyEventsResNew(ResMessage.END_TIME_ERROR.getCode(), //
					ResMessage.END_TIME_ERROR.getMessage());
		}
		// 檢查拆帳模式
		if (req.getSplitType() == null) {
			return new GroupbuyEventsResNew(ResMessage.SPLIT_TYPE_ERROR.getCode(), //
					ResMessage.SPLIT_TYPE_ERROR.getMessage());
		}
		// 檢查總金額
		if (req.getTotalOrderAmount() < 0) {
			return new GroupbuyEventsResNew(ResMessage.TOTALORDERAMOUNT_ERROR.getCode(), //
					ResMessage.TOTALORDERAMOUNT_ERROR.getMessage());
		}
		// 檢查總運費
		if (req.getShippingFee() < 0) {
			return new GroupbuyEventsResNew(ResMessage.SHIPPING_FEE_ERROR.getCode(), //
					ResMessage.SHIPPING_FEE_ERROR.getMessage());
		}
		// 檢查商家類型
		if (req.getType() == null) {
			return new GroupbuyEventsResNew(ResMessage.TYPE_ERROR.getCode(), //
					ResMessage.TYPE_ERROR.getMessage());
		}
		// 金額門檻
		if (req.getLimitation() < 0) {
			return new GroupbuyEventsResNew(ResMessage.SPLIT_TYPE_ERROR.getCode(), //
					ResMessage.SPLIT_TYPE_ERROR.getMessage());
		}
		if (req.getPickupTime() == null) {
			return new GroupbuyEventsResNew(400, //
					"取貨時間為空喵!");
		}

		if (req.getPickupTime().isBefore(req.getEndTime())) {
			return new GroupbuyEventsResNew(400, //
					"取貨時間早於結單時間喵!");
		}
		return new GroupbuyEventsResNew(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	// 新增開團
	public GroupbuyEventsResNew addEvent(GroupbuyEventsReq req) {
		// 如果 checkEvent(req).getCode() 不等於 SUCCESS.getCode() 就會回傳 錯誤的訊息跟代碼
		if (checkEvent(req).getCode() != ResMessage.SUCCESS.getCode()) {
			return checkEvent(req);
		}
		int check = groupbuyEventsDao.checkEvnet(req.getHostId(), req.getStoresId());
		if (check > 0) {
			return new GroupbuyEventsResNew(400, "您已在此店家發起過團購，請勿重複新增");
		}
		/*
		 * 新增資料
		 */
		GroupbuyEvents event = new GroupbuyEvents();
		event.setHostId(req.getHostId());
		event.setStoresId(req.getStoresId());
		event.setEventName(req.getEventName());
		event.setStatus(GroupbuyStatusEnum.OPEN);
		event.setEndTime(req.getEndTime());
		event.setSplitType(req.getSplitType());
		event.setTotalOrderAmount(0);
		event.setShippingFee(req.getShippingFee());
		event.setLimitation(req.getLimitation());
		event.setAnnouncement(req.getAnnouncement());
		event.setType(req.getType());
		event.setRecommendDescription(req.getRecommendDescription());
		event.setPickupTime(req.getPickupTime());
		event.setPickLocation(req.getPickLocation());

		try {
			// 此店家的全部菜單
			List<Map<String, Object>> Menu = storesSearchDao.getMenuByStoreId(req.getStoresId());
			// 將資料庫菜單 ID 轉成 Set，方便快速比較
			Set<Integer> MenuIds = Menu.stream()
					/*
					 * 一整個 Map，我們只對 m.get("id") 感興趣，要 toString() 是因為 id 有時候是 Long 之類的
					 * Integer.valueOf() 是因為直接轉型 Integer 很容易噴錯，所以先轉成字串再轉回數字
					 */
					.map(m -> Integer.valueOf(m.get("id").toString()))
					/*
					 * 在 .collect 收集起來打包回 MenuIds .toSet 會自動確保裡面不會有重複的 ID
					 */
					.collect(Collectors.toSet());

			// 檢查飲料 ID
			List<Integer> selectedIds = new ArrayList<>();
			if (req.getTempMenuList() != null) {
				for (Integer selectedId : req.getTempMenuList()) {
					// 檢查這個 ID 是否有在商店裡

					/*
					 * .contains(selectedId) (快速比對)：這是 Set 的功能。 會瞬間檢查 selectedId（團長給的菜單的商品 ID）有沒有在
					 * MenuIds 商店菜單裡面。
					 */
					if (!MenuIds.contains(selectedId)) {
						return new GroupbuyEventsResNew(400, "品項 ID: " + selectedId + " 不屬於此店家，無法開團");
					}
					selectedIds.add(selectedId);
				}
			}

			// 檢查推薦飲料ID
			List<Integer> recommendIds = new ArrayList<>();
			if (req.getRecommendList() != null) {
				for (Integer recommendId : req.getRecommendList()) {
					// 檢查這個 ID 是否有在商店裡
					/*
					 * .contains(selectedId) (快速比對)：這是 Set 的功能。 會瞬間檢查 selectedId（團長給的菜單的商品 ID）有沒有在
					 * MenuIds 商店菜單裡面。
					 */
					if (!selectedIds.contains(recommendId)) {
						return new GroupbuyEventsResNew(400, "推薦品項 ID: " + recommendId + " 不在本次團購的選購名單內");
					}
					recommendIds.add(recommendId);
				}
			}
			// 將 ID 轉成字串
			// mapper 是負責搬運與轉換資料的工具
			// 序列化就是 Object 轉 Json
			String tempMenuJson = mapper.writeValueAsString(selectedIds);
			String recommendJson = mapper.writeValueAsString(recommendIds);

			// 存入物件
			event.setTempMenuList(tempMenuJson);
			event.setRecommendList(recommendJson);
			groupbuyEventsDao.save(event);

		} catch (Exception e) {
			return new GroupbuyEventsResNew(ResMessage.EVENT_ERROR.getCode(), ResMessage.EVENT_ERROR.getMessage());
		}
		return new GroupbuyEventsResNew(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), event.getId());
	}

	// 更新
	public BasicRes updateEvent(int id, GroupbuyEventsReq req) {
		GroupbuyEvents event = groupbuyEventsDao.findById(id);
		if (event == null) {
			return new BasicRes(ResMessage.EVENTS_NOT_FOUND.getCode(), ResMessage.EVENTS_NOT_FOUND.getMessage());
		}
		try {
			// 此店家的全部菜單
			List<Map<String, Object>> Menu = storesSearchDao.getMenuByStoreId(req.getStoresId());
			// 將資料庫菜單 ID 轉成 Set，方便快速比較
			Set<Integer> MenuIds = Menu.stream()
					/*
					 * 一整個 Map，我們只對 m.get("id") 感興趣，要 toString() 是因為 id 有時候是 Long 之類的
					 * Integer.valueOf() 是因為直接轉型 Integer 很容易噴錯，所以先轉成字串再轉回數字
					 */
					.map(m -> Integer.valueOf(m.get("id").toString()))
					/* Set 會自動確保裡面不會有重複的 ID */
					// 在 .collect 收集起來打包回 MenuIds
					.collect(Collectors.toSet());

			// 檢查飲料 ID
			List<Integer> selectedIds = new ArrayList<>();
			if (req.getTempMenuList() != null) {
				for (Integer selectedId : req.getTempMenuList()) {
					// 檢查這個 ID 是否有在商店裡
					/*
					 * .contains(selectedId) (快速比對)：這是 Set 的功能。 會瞬間檢查 selectedId（團長給的菜單的商品 ID）有沒有在
					 * MenuIds 商店菜單裡面。
					 */
					if (!MenuIds.contains(selectedId)) {
						return new BasicRes(400, "品項 ID: " + selectedId + " 不屬於此店家，無法開團");
					}
					selectedIds.add(selectedId);
				}
			}

			// 檢查推薦飲料ID
			List<Integer> recommendIds = new ArrayList<>();
			if (req.getRecommendList() != null) {
				for (Integer recommendId : req.getRecommendList()) {
					// 檢查這個 ID 是否有在商店裡
					/*
					 * .contains(selectedId) (快速比對)：這是 Set 的功能。 會瞬間檢查 recommendId（團長給的推薦商品 ID）有沒有在
					 * selectedIds （團長給的菜單的商品 ID）菜單裡面。
					 */
					if (!selectedIds.contains(recommendId)) {
						return new BasicRes(400, "推薦品項 ID: " + recommendId + " 不在本次團購的選購名單內");
					}
					recommendIds.add(recommendId);
				}
			}

			// 轉成純 ID 的 JSON 字串
			String tempMenuJson = mapper.writeValueAsString(selectedIds);
			String recommendJson = mapper.writeValueAsString(recommendIds);

			groupbuyEventsDao.updateEvent(//
					req.getHostId(), //
					req.getStoresId(), //
					req.getEventName(), //
					req.getStatus().name(), //
					req.getEndTime(), //
					event.getTotalOrderAmount(), //
					req.getShippingFee(), //
					req.getSplitType().name(), //
					req.getAnnouncement(), //
					req.getType(), //
					tempMenuJson, //
					recommendJson, //
					req.getRecommendDescription(), //
					req.getLimitation(), req.getPickLocation(), req.getPickupTime(), id);
		} catch (Exception e) {
			return new BasicRes(ResMessage.EVENT_ERROR.getCode(), ResMessage.EVENT_ERROR.getMessage());
		}
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	// 團長手動結單
	@Transactional
	public BasicRes hostCloseEvent(int id, String hostId) {
		GroupbuyEvents event = groupbuyEventsDao.findById(id);
		if (event == null) {
			return new BasicRes(404, "找不到該團購活動");
		}
		if (!event.getHostId().equals(hostId)) {
			return new BasicRes(403, "權限不足，只有團長可以結單");
		}
		if (event.getStatus() == GroupbuyStatusEnum.FINISHED) {
			return new BasicRes(400, "此團已經是結單狀態");
		}
		BasicRes autoClose = closeEvent(id, hostId);
		return autoClose;
	}

	// 結單功能
	@Transactional
	public BasicRes closeEvent(int id, String userId) {
        GroupbuyEvents event = groupbuyEventsDao.findById(id);
        if (event == null) {
            return new BasicRes(404, "找不到該團購活動");
        }
        // 判斷成團門檻 (未成團邏輯)
        if (event.getTotalOrderAmount() < event.getLimitation()) {
            fakeDelete(id);
            groupbuyEventsDao.updateStatus(GroupbuyStatusEnum.CANCELLED.name(), id, userId);
            return new BasicRes(200, "人數不足，已自動取消該團");
        }
		// 更新活動狀態為 FINISHED
		groupbuyEventsDao.updateStatus(GroupbuyStatusEnum.FINISHED.name(), id, userId);
		List<String> userIdList = ordersDao.getUserIdByEventsId(id);
		if (!CollectionUtils.isEmpty(userIdList)) {
			for (String userIdStr : userIdList) {
				// 準備 Request 物件
				personalOrderReq req = new personalOrderReq();
				req.setEventsId(id);
				req.setUserId(userIdStr);
				req.setTotalSum(ordersDao.sumSubtotalByEventIdAndUserId(id, userIdStr));
				req.setTotalWeight(ordersDao.sumWeightByEventIdAndUserId(id, userIdStr));
				req.setPersonFee(0);
				// 新增
				personalOrderService.addPersonalOrder(req);
			}
		}
		// 手動結單完查詢所屬活動的跟團者做自動生產addPersonOrder資料
		List<Orders> ordersInfoList = ordersDao.getUserAllByEventsId(id);
		// 檢查
		if (CollectionUtils.isEmpty(ordersInfoList)) {
			return new BasicRes(200, "已結單沒有跟團者");
		}

		// 此用戶的運費計算
		ShippingFeeRes feeRes = personalOrderService.getShippingFeeByEventId(id, userId);
		if (feeRes.getCode() == 200) {
			return new BasicRes(200, "結單成功，帳單已產生並完成運費分攤");
		} else {
			return new BasicRes(400, "帳單已產生但運費計算出錯：" + feeRes.getMessage());
		}
	}

	// 排程結單
	@Transactional
	public BasicRes autoCloseEvent(int id, String userId) {
		BasicRes autoClose = closeEvent(id, userId);
		return autoClose;
	}

	// 回傳開團者的開團紀錄
	public GroupbuyEventsRes getGroupbuyEventById(String hostId) {
		try {
			if (!StringUtils.hasText(hostId)) {
				return new GroupbuyEventsRes(400, "輸入正確的host_id");
			}
			List<GroupbuyEvents> eventsList = groupbuyEventsDao.getGroupbuyEventById(hostId);
			GroupbuyEventsRes res = new GroupbuyEventsRes(200, "host_id 搜尋成功");
			res.setGroupbuyEvents(eventsList);
			return res;
		} catch (Exception e) {
			return new GroupbuyEventsRes(500, "host_id 搜尋失敗");
		}
	}

	// 回傳團長給的菜單
	public GroupbuyEventsRes getMenuByMenuId(List<Integer> menuList) {
		try {
			// 先去抓商店的菜單ID
			List<Menu> menus = storesSearchDao.getMenuByMenuId(menuList);
			/*
			 * 因為寫 return new GroupbuyEventsRes(200, "menuId 搜尋成功", menus);會有問題 所以改用 set
			 * 將查詢到的 menus 塞到 res 的 menuList
			 */
			GroupbuyEventsRes res = new GroupbuyEventsRes(200, "menuId 搜尋成功");
			res.setMenuList(menus);
			return res;
		} catch (Exception e) {
			return new GroupbuyEventsRes(500, "菜單搜尋失敗");
		}
	}

	// 回傳用商店ID查詢的菜單
	public GroupbuyEventsRes getMenuByStoresId(int storesId) {
		try {
			if (storesId <= 0) {
				return new GroupbuyEventsRes(400, "輸入正確的stores_id");
			}
			List<Menu> menuList = groupbuyEventsDao.getMenuByStoresId(storesId);
			if (CollectionUtils.isEmpty(menuList)) {
				return new GroupbuyEventsRes(200, "查無此店家的菜單資料");
			}
			GroupbuyEventsRes res = new GroupbuyEventsRes(200, "店家菜單搜尋成功");
			res.setMenuList(menuList);
			return res;
		} catch (Exception e) {
			return new GroupbuyEventsRes(500, "店家菜單搜尋失敗");
		}
	}

	// 回傳用商店ID查詢符合的團
	public GroupbuyEventsRes getGroupbuyEventByStoresId(int storesId) {
		try {
			if (storesId <= 0) {
				return new GroupbuyEventsRes(400, "輸入正確的stores_id");
			}
			List<GroupbuyEvents> eventsList = groupbuyEventsDao.getGroupbuyEventByStoresId(storesId);
			if (CollectionUtils.isEmpty(eventsList)) {
				return new GroupbuyEventsRes(200, "查無此店家的菜單資料");
			}
			GroupbuyEventsRes res = new GroupbuyEventsRes(200, "店家菜單搜尋成功");
			res.setGroupbuyEvents(eventsList);
			return res;
		} catch (Exception e) {
			return new GroupbuyEventsRes(500, "店家菜單搜尋失敗");
		}
	}

	// 回傳全部開團的
	public GroupbuyEventsRes getAll() {
		try {
			List<GroupbuyEventsProjection> list = groupbuyEventsDao.getAll();
			if (list == null) {
				return new GroupbuyEventsRes(400, "目前暫無任何開團資料");
			}
			GroupbuyEventsRes res = new GroupbuyEventsRes(200, "搜尋成功");
			res.setGroupbuyEvents(list);
			return res;
		} catch (Exception e) {
			return new GroupbuyEventsRes(500, "查詢失敗");
		}
	}

	// 回傳暱稱有的開團
	public GroupbuyEventsRes getGroupbuyEventByNickname(String hostNickname) {
		List<GroupsSearchView> nicknameEventsList = groupsSearchViewDao.getGroupbuyEventByNickname(hostNickname);
		if (nicknameEventsList == null) {
			return new GroupbuyEventsRes(200, "查無此開團者的開團資料");
		}
		try {
			GroupbuyEventsRes res = new GroupbuyEventsRes(200, "開團者搜尋成功");
			res.setGroupsSearchViewList(nicknameEventsList);
			return res;
		} catch (Exception e) {
			return new GroupbuyEventsRes(500, "查詢失敗");
		}
	}

	// 回傳eventsId的活動
	public GroupbuyEventsRes getEventsByEventsId(int id) {
		List<GroupsSearchView> list = groupbuyEventsDao.getEventsByEventsId(id);
		if (CollectionUtils.isEmpty(list)) {
			return new GroupbuyEventsRes(404, "查無此資料");
		}
		return new GroupbuyEventsRes(200, "成功查詢資料", list, null, null, null, null, null);
	}

	// 刪除(只做為修改資料用 不串接)
	@Transactional
	public BasicRes deleteEventPhysically(int eventId) {
		GroupbuyEvents event = groupbuyEventsDao.findById(eventId);
		if (event == null) {
			return new BasicRes(404, "找不到該筆資料");
		}

		groupbuyEventsDao.deleteEvent(eventId);

		return new BasicRes(200, "成功刪除團購喵");
	}

	// 物理刪除
	@Transactional
	public BasicRes delete(int eventsId) {
		// 檢查該活動是否存在且尚未被刪除
		GroupbuyEvents event = groupbuyEventsDao.findById(eventsId);
		if (event == null) {
			return new BasicRes(404, "找不到該團購活動或活動已被刪除");
		}
		// 軟刪除主表
        int deletedEvent = groupbuyEventsDao.deleteEvent(eventsId);
		if (deletedEvent > 0) {
			// 順便刪除子表
			ordersDao.deleteAllOrdersByEventId(eventsId);
			return new BasicRes(200, "團購活動ID: " + eventsId + "已成功刪除");
		}
		return new BasicRes(500, "刪除活動失敗，請稍後再試");
	}
    
    // 軟刪除
    @Transactional
    public BasicRes fakeDelete(int eventsId) {
        // 檢查該活動是否存在且尚未被刪除
        GroupbuyEvents event = groupbuyEventsDao.findById(eventsId);
        if (event == null) {
            return new BasicRes(404, "找不到該團購活動或活動已被刪除");
        }
        // 軟刪除主表
        int fakeDelete = groupbuyEventsDao.fakeDelete(eventsId);
        if (fakeDelete > 0) {
            // 順便刪除子表
            ordersDao.deleteAllOrdersByEventId(eventsId);
            return new BasicRes(200, "團購活動ID: " + eventsId + "已成功刪除");
        }
        return new BasicRes(500, "刪除活動失敗，請稍後再試");
    }
	// 給團長看全部的orders
	public GroupbuyEventsRes getOrdersAll(int eventId) {
		List<OrdersSearchView> ordersSearchViewList = groupbuyEventsDao.selectOrdersAll(eventId);

		if (ordersSearchViewList == null) {
			return new GroupbuyEventsRes(404, "查無此orders");
		}
		ObjectMapper mapper = new ObjectMapper();
		// 多筆所以要用迴圈 一筆一筆轉
		for (OrdersSearchView order : ordersSearchViewList) {
			try {
				String selectedOptionJson = order.getSelectedOption();
				if (!CollectionUtils.isEmpty(ordersSearchViewList)) {
					List<Map<String, Object>> list = mapper.readValue(selectedOptionJson, new TypeReference<>() {
					});
					order.setSelectedOptionList(list);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new GroupbuyEventsRes(200, "成功", null, null, null, null, null, ordersSearchViewList);
	}

	// 購物車(首頁)
	public GroupbuyEventsRes getCart(String userId) {
		try {
			if (!StringUtils.hasText(userId)) {
				return new GroupbuyEventsRes(400, "找不到該用戶");
			}

			List<GroupsSearchView> relatedViews = groupsSearchViewDao.findAllMyRelatedEvents(userId);

			if (CollectionUtils.isEmpty(relatedViews)) {
				return new GroupbuyEventsRes(400, "");
			}

			List<Orders> allVisibleOrders = new ArrayList<>();
			Map<Integer, CartDTO> cartMap = new HashMap<>();
			Set<Integer> hostEventIds = new java.util.HashSet<>();

			for (GroupsSearchView view : relatedViews) {
				int eid = view.getEventId();
				boolean isHost = userId.equals(view.getHostId());
				if (isHost) {
					hostEventIds.add(eid);
				}

				// 如果整體活動已結束，且我不是團長，就不顯示在「跟團中」 (團員部分：活動結案即移除)
				if (view.getEventStatus() == GroupbuyStatusEnum.FINISHED && !isHost) {
					continue;
				}

				// 檢查身分與處理狀態
				PersonalOrder po = personalOrderDao.findByEventsIdAndUserId(eid, userId);

				// 團員部分：如果已經點擊過「確認結算」，代表對該團員來說流程已結束
				if (!isHost && po != null && po.getPaymentStatus() == PaymentStatus.CONFIRMED) {
					continue;
				}

				// 我是團長就拿「整團單」，我是團員就拿「個人單」
				if (isHost) {
					// 團長拿整團 (拿該活動所有品項以同步統計資訊)
					List<Orders> hostOrders = ordersDao.getUserAllByEventsId(eid);
					if (!CollectionUtils.isEmpty(hostOrders)) {
						allVisibleOrders.addAll(hostOrders);
					}
				} else {
					// 團員拿自己 (getEventIdByUserId)
					List<Orders> memberOrders = ordersDao.getOrderByEventIdAndUserId(userId, eid);
					if (!CollectionUtils.isEmpty(memberOrders))
						allVisibleOrders.addAll(memberOrders);
				}

				// 預先初始化 CartDTO(View)
				CartDTO dto = new CartDTO();
				dto.setEventsId(eid);
				dto.setEventName(view.getEventName());
				dto.setStoreName(view.getStoreName());
				dto.setStoreLogo(view.getStoreImage());
				dto.setHostLogo(view.getHostAvatar());
				// 團長部分：計算未完成的統計數據
				if (isHost) {
					dto.setUnpaidCount(personalOrderDao.countUnpaidByEventsId(eid));
					dto.setUnpickedCount(ordersDao.countUnpickedByEventId(eid));
					// 修正：主揪直接拿全團總額，避免因為狀態切換導致總額跳動
					dto.setTotalAmount(ordersDao.sumSubtotalByEventId(eid));
				}
				dto.setStatus(view.getEventStatus());
				dto.setPickLocation(view.getPickLocation());
				dto.setPickupTime(view.getPickupTime() != null ? view.getPickupTime().toString() : "尚未設定");

				// 權限：團長只要沒結單都能改；團員只有 OPEN 能改
				boolean canModify = isHost ? !"FINISHED".equals(view.getEventStatus().toString())
						: "OPEN".equals(view.getEventStatus().toString());
				dto.setCanModify(canModify);
				dto.setItems(new ArrayList<>());
				cartMap.put(eid, dto);
			}

			// 將蒐集到的訂單塞入對應的 DTO 裡
			for (Orders order : allVisibleOrders) {
				CartDTO current = cartMap.get(order.getEventsId());
				if (current != null) {
					current.getItems().add(order);
					// 只有團員單才需要在此累加；團長單在構造 DTO 時已經拿過總額了
					if (!hostEventIds.contains(order.getEventsId())) {
						current.setTotalAmount(current.getTotalAmount() + order.getSubtotal());
					}
					current.setTotalQuantity(current.getItems().size());

					// 時間紀錄
					if (order.getOrderTime() != null) {
						String orderTimeStr = order.getOrderTime().toString();
						if (current.getLatestOrderTime() == null
								|| orderTimeStr.compareTo(current.getLatestOrderTime()) > 0) {
							current.setLatestOrderTime(orderTimeStr);
						}
					}
				}
			}

			GroupbuyEventsRes res = new GroupbuyEventsRes(200, "成功找到");
			res.setCartData(new ArrayList<>(cartMap.values()));
			return res;

		} catch (Exception e) {
			e.printStackTrace();
			return new GroupbuyEventsRes(500, "查詢失敗");
		}
	}
}
