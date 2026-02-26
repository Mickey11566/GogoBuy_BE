package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.example.demo.constants.GroupbuyStatusEnum;
import com.example.demo.constants.PaymentStatus;
import com.example.demo.constants.PickupStatusEnum;
import com.example.demo.constants.ResMessage;
import com.example.demo.dao.GroupbuyEventsDao;
import com.example.demo.dao.OrdersDao;
import com.example.demo.dao.PersonalOrderDao;
import com.example.demo.dao.StoresSearchDao;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.OrderHistoryDTO;
import com.example.demo.dto.OrdersDTO;
import com.example.demo.entity.GroupbuyEvents;
import com.example.demo.entity.Menu;
import com.example.demo.entity.Orders;
import com.example.demo.entity.PersonalOrder;
import com.example.demo.entity.Stores;
import com.example.demo.entity.User;
import com.example.demo.request.OredersReq;
import com.example.demo.response.BasicRes;
import com.example.demo.response.GroupbuyEventsRes;
import com.example.demo.response.OrderHistoryRes;
import com.example.demo.response.OrdersRes;
import com.example.demo.vo.OrderMenuVo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class OrdersService {

	@Autowired
	private GroupbuyEventsDao groupbuyEventsDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private OrdersDao ordersDao;

	@Autowired
	private StoresSearchDao storesSearchDao;

	@Autowired
	private PersonalOrderDao personalOrderDao;

	ObjectMapper mapper = new ObjectMapper();

	private BasicRes checkEvent(OredersReq req) {
		// 檢查所屬團ID
		if (req.getEventsId() == 0) {
			return new BasicRes(ResMessage.EVENTS_ID_ERROR.getCode(), //
					ResMessage.EVENTS_ID_ERROR.getMessage());
		}
		// 檢查有沒有所屬團
		GroupbuyEvents events = groupbuyEventsDao.findById(req.getEventsId());
		if (events == null) {
			return new BasicRes(ResMessage.EVENTS_NOT_FOUND.getCode(), ResMessage.EVENTS_NOT_FOUND.getMessage());
		}
		// 團購是否截止
		if (events.getEndTime().isBefore(LocalDateTime.now())) {
			return new BasicRes(ResMessage.EVENT_CLOSED.getCode(), ResMessage.EVENT_CLOSED.getMessage());
		}
		// 檢查跟團者ID
		if (req.getUserId() == null || userDao.getUserById(req.getUserId()) == null) {
			return new BasicRes(ResMessage.USER_NOT_FOUND.getCode(), ResMessage.USER_NOT_FOUND.getMessage());
		}

		// 檢查菜單品項ID
		if (req.getMenuList() == null || req.getMenuList().isEmpty()) {
			return new BasicRes(400, "購物車內沒有品項");
		}

		// 遍歷清單檢查每一個品項
		for (OrderMenuVo item : req.getMenuList()) {
			if (item.getMenuId() <= 0) {
				return new BasicRes(ResMessage.MENU_ID_ERROR.getCode(), "商品 ID 錯誤：" + item.getMenuId());
			}

			// 這裡還可以順便檢查數量
			if (item.getQuantity() <= 0) {
				return new BasicRes(ResMessage.QUANTITY_FOUND.getCode(), "商品 ID " + item.getMenuId() + " 的數量必須大於 0");
			}
		}
		// 從找到的團購事件中取得商店 ID
		int storesId = events.getStoresId();
		// 根據該商店 ID 取得菜單
		List<Map<String, Object>> menuList = storesSearchDao.getMenuByStoreId(storesId);
		if (menuList == null || menuList.isEmpty()) {
			return new BasicRes(ResMessage.MENULIST_NOT_FOUND.getCode(), ResMessage.MENULIST_NOT_FOUND.getMessage());
		}
		// 遍歷前端傳來的所有商品
		for (OrderMenuVo item : req.getMenuList()) {
			int currentMenuId = item.getMenuId();
			String currentMenuIdStr = String.valueOf(currentMenuId);

			// 檢查商品是否在商店的菜單中
			boolean foundInStore = false;
			for (Map<String, Object> menu : menuList) {
				String storeMenuId = String.valueOf(menu.get("id"));
				if (storeMenuId.equals(currentMenuIdStr)) {
					foundInStore = true;
					break;
				}
			}

			if (!foundInStore) {
				return new BasicRes(404, "商店無提供商品 ID: " + currentMenuId);
			}

			// 檢查是否在團長開放的名單內 (團員才需要受限)
			if (!events.getHostId().equals(req.getUserId())) {
				String hostGiveMenuId = events.getTempMenuList();
				if (StringUtils.hasText(hostGiveMenuId)) {
					try {
						List<Integer> allowedIds = mapper.readValue(hostGiveMenuId, new TypeReference<List<Integer>>() {
						});
						if (allowedIds != null && !allowedIds.isEmpty()) {
							if (!allowedIds.contains(currentMenuId)) {
								return new BasicRes(400, "商品 ID: " + currentMenuId + " 不在團長開放名單內");
							}
						}
					} catch (Exception e) {
						// 如果解析失敗，保守起見用字串包含檢查，但要加逗號避免 1 匹配 10
						String checkStr = "," + currentMenuIdStr + ",";
						String listStr = hostGiveMenuId.replace("[", ",").replace("]", ",").replace(" ", "");
						if (!listStr.contains(checkStr)) {
							return new BasicRes(400, "商品 ID: " + currentMenuId + " 不在團長開放名單內");
						}
					}
				}
			}

			// 順便檢查該項商品的數量
			if (item.getQuantity() <= 0) {
				return new BasicRes(400, "商品 ID: " + currentMenuId + " 數量必須大於 0");
			}
		}

		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	// 硬刪除
	public BasicRes hardDelete(String userId, int eventsId) {
		int orders = ordersDao.getOrderByUserIdAndEventsId(userId, eventsId);
		if (orders == 0) {
			return new BasicRes(404, "找不到訂單或已刪除");
		}
		ordersDao.hardDelete(userId, eventsId);
		return new BasicRes(200, "成功刪除訂單");
	}

	// 計算個人subtotal
	private OrdersRes getSubtotal(OrderMenuVo item) {
		try {
			Menu menu = storesSearchDao.getMenuByMenuId(item.getMenuId());
			if (menu == null)
				return new OrdersRes(404, "找不到品項", 0);

			int basePrice = menu.getBasePrice();
			int specPrice = 0;

			String menuName = menu.getName();

			// 解析規格價格 (unusual 欄位)
			String unusualJson = menu.getUnusual();
			// 確保 unusualJson 不是 null 且前端有傳規格名稱才進行解析
			if (StringUtils.hasText(unusualJson) && item.getSpecName() != null) {
				try {
					List<Map<String, Object>> specs = mapper.readValue(unusualJson,
							new TypeReference<List<Map<String, Object>>>() {
							});
					for (Map<String, Object> spec : specs) {
						if (item.getSpecName().equals(spec.get("name"))) {
							// 使用 Number 轉型更安全，可以同時處理 Integer 或 Long
							specPrice = ((Number) spec.get("price")).intValue();
							break;
						}
					}
				} catch (Exception jsonEx) {
					System.out.println("JSON 解析規格失敗，跳過規格加價: " + jsonEx.getMessage());
				}
			}
			int totalExtraPrice = 0;
			List<Map<String, Object>> opListPrice = item.getSelectedOptionList();
			if (opListPrice != null) {
				for (Map<String, Object> op : opListPrice) {
					Object extra = op.get("extraPrice");
					if (extra != null) {
						totalExtraPrice += ((Number) extra).intValue();
					}
				}
			}
			int unitPrice = basePrice + specPrice + totalExtraPrice;
			int subtotal = unitPrice * item.getQuantity();
			OrdersRes res = new OrdersRes(200, "計算成功", subtotal);
			res.setMenuName(menuName);
			res.setBasePrice(basePrice);
			res.setSpecPrice(specPrice);
			return res;
		} catch (Exception e) {
			return new OrdersRes(500, "商品 " + item.getMenuId() + " 金額計算失敗: " + e.getMessage(), 0);
		}
	}

	// 新增
	@Transactional
	public BasicRes addOrders(OredersReq req) {
		BasicRes checkResult = checkEvent(req);
		if (checkResult.getCode() != ResMessage.SUCCESS.getCode()) {
			return checkResult;
		}
		try {
			if (ordersDao.existsByUserIdAndEventsId(req.getUserId(), req.getEventsId())) {
				ordersDao.hardDelete(req.getUserId(), req.getEventsId());
			}
			for (OrderMenuVo item : req.getMenuList()) {
				Orders orders = new Orders();
				// 金額計算
				OrdersRes subtotalRes = getSubtotal(item);
				if (subtotalRes.getCode() != 200) {
					throw new RuntimeException("商品 " + item.getMenuId() + " 金額計算失敗");
				}

				// 快照欄位
				orders.setMenuName(subtotalRes.getMenuName());
				orders.setBasePrice(subtotalRes.getBasePrice());
				orders.setSpecPrice(subtotalRes.getSpecPrice());

				// 基礎欄位賦值
				orders.setSubtotal(subtotalRes.getSubtotal());
				orders.setEventsId(req.getEventsId());
				orders.setUserId(req.getUserId());
				orders.setPersonalMemo(req.getPersonalMemo());
				orders.setMenuId(item.getMenuId());
				orders.setQuantity(item.getQuantity());
				orders.setSpecName(item.getSpecName());
				orders.setOrderTime(LocalDateTime.now());
				orders.setWeight(req.getWeight());
				orders.setSelectedOption(mapper.writeValueAsString(item.getSelectedOptionList()));
				orders.setPickupStatus(PickupStatusEnum.NOT_PICKED_UP);

				// 寫入資料庫
				ordersDao.save(orders);
			}
			updateSubtotal(req.getEventsId());
			return new BasicRes(200, "成功");
		} catch (Exception e) {
			return new BasicRes(500, "錯誤: " + e.getMessage());
		}
	}

	// 更新
	@Transactional
	public BasicRes updateOrders(OredersReq req) {
		BasicRes checkResult = checkEvent(req);
		if (checkResult.getCode() != ResMessage.SUCCESS.getCode()) {
			return checkResult;
		}
		try {
			ordersDao.hardDelete(req.getUserId(), req.getEventsId());
			return addOrders(req);
		} catch (Exception e) {
			return new BasicRes(400, "更新過程發生錯誤");
		}
	}

	// 回傳的訂單紀錄
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

	// 軟刪除
	public BasicRes deleteOrderByUserIdAndEventsId(String userId, int eventsId) {
		if (!StringUtils.hasText(userId) || eventsId <= 0) {
			return new BasicRes(400, "請提供正確的使用者 ID 與團購 ID");
		}
		int deleteRow = ordersDao.deleteOrderByUserIdAndEventsId(userId, eventsId);
		// 判斷處理結果
		if (deleteRow == 0) {
			return new BasicRes(404, "在此團購中找不到該使用者的有效訂單");
		}
		updateSubtotal(eventsId);
		return new BasicRes(200, "已成功取消該使用者在本次團購中的 " + deleteRow + " 筆訂單");
	}

	// 更新總金額
	private void updateSubtotal(int eventId) {
		// SQL 有 ifnull，沒訂單時總金額就會是 0
		int totalAmount = ordersDao.sumSubtotalByEventId(eventId);
		groupbuyEventsDao.updateTotalAmount(totalAmount, eventId);
	}

	// 查詢跟團者有的開團
	public GroupbuyEventsRes getOrdersByUserId(String userId) {
		try {
			List<Orders> ordersList = ordersDao.getOrdersByUserId(userId);

			if (CollectionUtils.isEmpty(ordersList)) {
				return new GroupbuyEventsRes(404, "找不到訂單資料");
			}

			// 用 Map 來當「分類櫃」，Key 是 Integet (eventsId)，Value 是對應的 DTO
			//
			Map<Integer, OrdersDTO> groupMap = new HashMap<>();
			for (Orders order : ordersList) {
				int currentEventId = order.getEventsId();
				// 檢查 eventsId 是不是第一次遇到
				// ! 代表「不」，所以這整句是：「如果還沒有 currentEventId 號的櫃子」。
				if (!groupMap.containsKey(currentEventId)) {
					// 初始化新的
					OrdersDTO newDto = new OrdersDTO();
					// 設定這團的「共用資訊」
					newDto.setEventsId(order.getEventsId());
					newDto.setUserId(order.getUserId());
					newDto.setPersonalMemo(order.getPersonalMemo());
					// 初始化這團的商品箱子，避免後續 add 報錯
					newDto.setMenuList(new ArrayList<>());
					// 將這個新建立的 DTO 箱子放進 Map 分類櫃中，以 currentEventId 為標籤
					groupMap.put(currentEventId, newDto);
				}
				// 無論是新箱子還是舊箱子，我們都根據 currentEventId 把對應的 DTO 拿出來
				OrdersDTO currentDto = groupMap.get(currentEventId);
				// 處理商品資訊 (VO)，存放這筆訂單點了什麼
				OrderMenuVo item = new OrderMenuVo();
				item.setMenuId(order.getMenuId());
				item.setQuantity(order.getQuantity());
				item.setSpecName(order.getSpecName());
				item.setMenuName(order.getMenuName());
				// 解析 JSON 選項
				String jsonStr = order.getSelectedOption();
				if (StringUtils.hasText(jsonStr)) {
					try {
						// 將 JSON 字串轉為 Java 的 List<Map> 結構
						List<Map<String, Object>> options = mapper.readValue(jsonStr,
								new TypeReference<List<Map<String, Object>>>() {
								});
						item.setSelectedOptionList(options);
					} catch (Exception e) {
						System.err.println("JSON 解析失敗: " + e.getMessage());
					}
				}
				// 將處理好的商品細項，塞進該活動箱子的商品清單中
				currentDto.getMenuList().add(item);
			}
			// 最後把 Map 裡所有的 DTO 拿出來，變成一個 List 回傳
			List<OrdersDTO> resultList = new ArrayList<>(groupMap.values());
			return new GroupbuyEventsRes(200, "成功找到", resultList, null, null, null, null, null);
		} catch (Exception e) {
			return new GroupbuyEventsRes(500, "系統錯誤: " + e.getMessage());
		}
	}

	// 查詢跟團者的特定訂單
	public GroupbuyEventsRes getEventIdByUserId(String userId, int eventsId) {
		try {
			List<Orders> ordersList = ordersDao.getOrderByEventIdAndUserId(userId, eventsId);

			if (CollectionUtils.isEmpty(ordersList)) {
				System.out.println(ordersList);
				return new GroupbuyEventsRes(404, "找不到訂單資料");
			}

			OrdersDTO responseDto = new OrdersDTO();
			Orders orderInfo = ordersList.get(0);
			responseDto.setUserId(orderInfo.getUserId());
			responseDto.setEventsId(orderInfo.getEventsId());
			responseDto.setPersonalMemo(orderInfo.getPersonalMemo());
			responseDto.setWeight(orderInfo.getWeight());

			List<OrderMenuVo> menuList = new ArrayList<>();
			for (Orders order : ordersList) {
				OrderMenuVo item = new OrderMenuVo();
				item.setMenuId(order.getMenuId());
				item.setQuantity(order.getQuantity());
				item.setSpecName(order.getSpecName());
				item.setMenuName(order.getMenuName());
				String jsonStr = order.getSelectedOption();
				if (StringUtils.hasText(jsonStr)) {
					try {
						/*
						 * jsonStr：這是包裹外殼，裡面裝著拆散的零件（字串）。 mapper.readValue：這是你的「組裝說明書」。 new
						 * TypeReference<...>() {}：這是包裹上的「內容物標籤」，告訴說明書要把零件組裝成什麼。
						 */
						List<Map<String, Object>> options = mapper.readValue(jsonStr,
								new TypeReference<List<Map<String, Object>>>() {
								});
						item.setSelectedOptionList(options);
					} catch (Exception e) {
						e.printStackTrace();
						return new GroupbuyEventsRes(500, e.getMessage());
					}
				}
				menuList.add(item);
			}
			responseDto.setMenuList(menuList);

			return new GroupbuyEventsRes(200, "成功找到", responseDto);
		} catch (Exception e) {
			return new GroupbuyEventsRes(500, "系統錯誤: " + e.getMessage());
		}
	}

	public BasicRes deleteCartByOrderId(int orderId) {
		int deletedCount = ordersDao.deleteOrderById(orderId);

		// 如果受影響的筆數大於 0，表示至少有一份問卷被成功軟刪除
		if (deletedCount > 0) {
			return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
		} else {
			// 如果一筆都沒改到，表示傳入的 ID 在資料庫都找不到（或是已被刪除）
			return new BasicRes(ResMessage.ORDER_ERROR.getCode(), ResMessage.ORDER_ERROR.getMessage());
		}
	}

	public OrderHistoryRes getHistoryOrdersByUserId(String userId) {
		try {
			if (!StringUtils.hasText(userId)) {
				return new OrderHistoryRes(400, "User ID is required");
			}

			List<Orders> ordersList = ordersDao.getOrdersByUserId(userId);
			if (CollectionUtils.isEmpty(ordersList)) {
				return new OrderHistoryRes(404, "No orders found for this user");
			}

			Map<Integer, OrderHistoryDTO> historyMap = new HashMap<>();

			for (Orders order : ordersList) {
				int eventId = order.getEventsId();

				if (!historyMap.containsKey(eventId)) {
					OrderHistoryDTO dto = new OrderHistoryDTO();
					dto.setEventsId(eventId);
					dto.setOrderCode("訂單編號 # " + eventId); // Simple code generation, could be improved
					dto.setCreatedAt(order.getOrderTime());
					dto.setPersonalMemo(order.getPersonalMemo()); // 新增：整筆訂單的備註

					GroupbuyEvents event = groupbuyEventsDao.findById(eventId);
					if (event != null) {
						dto.setEventName(event.getEventName());
						dto.setEventStatus(event.getStatus());
						dto.setEventPickupTime(event.getPickupTime());

						// Determine status label based on event status and pickup status
						String statusLabel = "進行中";
						if (order.getPickupStatus() == PickupStatusEnum.PICKED_UP) {
							statusLabel = "已完成";
						} else if (event.getStatus() == GroupbuyStatusEnum.FINISHED) {
							statusLabel = "待取餐";
						}
						dto.setStatusLabel(statusLabel);

						Stores store = storesSearchDao.getStoreById(event.getStoresId());
						if (store != null) {
							dto.setStoreName(store.getName());
						}

						User host = userDao.getUserById(event.getHostId());
						if (host != null) {
							dto.setHostName(host.getNickname());
						}
					}

					User receiver = userDao.getUserById(userId);
					if (receiver != null) {
						dto.setReceiverName(receiver.getNickname());
						dto.setPhone(receiver.getPhone());
					}

					PersonalOrder personalOrder = personalOrderDao.findByEventsIdAndUserId(eventId, userId);
					if (personalOrder != null) {
						dto.setTotalAmount(personalOrder.getTotalSum());
						dto.setPersonFee(personalOrder.getPersonFee());
						dto.setPaymentStatus(personalOrder.getPaymentStatus());
					} else {
						dto.setTotalAmount(0);
						dto.setPersonFee(0);
						dto.setPaymentStatus(PaymentStatus.UNPAID);
					}

					dto.setPickupStatus(
							order.getPickupStatus() != null ? order.getPickupStatus().name() : "NOT_PICKED_UP");
					dto.setPickupTime(order.getPickupTime());
					dto.setPickLocation(event != null ? event.getPickLocation() : null);

					dto.setItems(new ArrayList<>());
					historyMap.put(eventId, dto);
				}

				OrderHistoryDTO currentDto = historyMap.get(eventId);

				// Update total amount if relying on sum of subtotals and no personal_order
				// found
				PersonalOrder personalOrder = personalOrderDao.findByEventsIdAndUserId(eventId, userId);
				if (personalOrder == null) {
					currentDto.setTotalAmount(currentDto.getTotalAmount() + order.getSubtotal());
				}

				OrderMenuVo item = new OrderMenuVo();
				item.setMenuId(order.getMenuId());
				item.setMenuName(order.getMenuName());
				item.setQuantity(order.getQuantity());
				item.setSpecName(order.getSpecName());
				item.setPersonalMemo(order.getPersonalMemo());
				item.setSubtotal(order.getSubtotal());

				String jsonStr = order.getSelectedOption();
				if (StringUtils.hasText(jsonStr)) {
					try {
						List<Map<String, Object>> options = mapper.readValue(jsonStr,
								new TypeReference<List<Map<String, Object>>>() {
								});
						item.setSelectedOptionList(options);
					} catch (Exception e) {
						System.err.println("JSON parse fail: " + e.getMessage());
					}
				}
				currentDto.getItems().add(item);
			}

			List<OrderHistoryDTO> resultList = new ArrayList<>(historyMap.values());
			resultList.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())); // Sort newest first

			return new OrderHistoryRes(200, "Success", resultList);
		} catch (Exception e) {
			e.printStackTrace();
			return new OrderHistoryRes(500, "System Error: " + e.getMessage());
		}
	}
}