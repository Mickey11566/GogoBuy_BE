package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.example.demo.constants.PaymentStatus;
import com.example.demo.constants.PickupStatusEnum;
import com.example.demo.dao.GroupbuyEventsDao;
import com.example.demo.dao.OrdersDao;
import com.example.demo.dao.PersonalOrderDao;
import com.example.demo.dao.UserDao;
import com.example.demo.entity.Orders;
import com.example.demo.entity.PersonalOrder;
import com.example.demo.entity.User;
import com.example.demo.request.personalOrderReq;
import com.example.demo.response.GroupbuyEventsRes;
import com.example.demo.response.PersonalOrdersRes;
import com.example.demo.response.ShippingFeeRes;

@Service
@Transactional
public class PersonalOrderService {

	@Autowired
	private GroupbuyEventsDao groupbuyEventsDao;

	@Autowired
	private OrdersDao ordersDao;

	@Autowired
	private PersonalOrderDao personalOrderDao;

	@Autowired
	private SalesStatsService salesStatsService;

	@Autowired
	private UserDao userDao;

	// 結團後生成
	public void addPersonalOrder(personalOrderReq req) {
		List<Orders> item = ordersDao.getOrderByEventIdAndUserId(req.getUserId(), req.getEventsId());

		if (CollectionUtils.isEmpty(item)) {
			System.out.println("因為沒有訂單明細，跳過銷售統計更新");
			return;
		}
		int store = groupbuyEventsDao.selectStoreIdByEventId(req.getEventsId());
		for (Orders orders : item) {
			// 從每一筆訂單物件中取出資料
			Integer storeId = store;
			Integer menuId = orders.getMenuId();
			int quantity = orders.getQuantity();
			// 引用 service 的 addSalesVolume
			salesStatsService.addSalesVolume(storeId, menuId, quantity);
		}
		PersonalOrder addpo = new PersonalOrder();
		addpo.setEventsId(req.getEventsId());
		addpo.setUserId(req.getUserId());
		addpo.setTotalSum(req.getTotalSum());
		addpo.setTotalWeight(req.getTotalWeight());
		addpo.setPersonFee(req.getPersonFee());
		addpo.setPaymentStatus(PaymentStatus.UNPAID);

		personalOrderDao.save(addpo);
	}

	// 更新結單狀態
	public PersonalOrdersRes updatePersonalOrder(personalOrderReq req) {
		try {
			// 查詢該筆「結算單」是否存在
			PersonalOrder order = personalOrderDao.findByEventsIdAndUserId(req.getEventsId(), req.getUserId());
			if (order == null) {
				return new PersonalOrdersRes(404, "找不到此結算單資料");
			}

			order.setTotalWeight(req.getTotalWeight());
			order.setPersonFee(req.getPersonFee());
			order.setTotalSum(req.getTotalSum());

			// 狀態與時間邏輯處理
			PaymentStatus paymentStatus = req.getPaymentStatus();
			if (paymentStatus != null) {
				// 處理支付時間紀錄
				if (paymentStatus == PaymentStatus.PAID || paymentStatus == PaymentStatus.CONFIRMED) {
					if (order.getPaymentTime() == null) {
						order.setPaymentTime(LocalDateTime.now());
					}
				}

				order.setPaymentStatus(paymentStatus);
			}

			// 更新取餐狀態 (連動到 orders 表以同步歷史訂單)
			PickupStatusEnum pickupStatus = req.getPickupStatus();
			if (pickupStatus != null) {
				LocalDateTime pickupTime = (pickupStatus == PickupStatusEnum.PICKED_UP) ? LocalDateTime.now() : null;
				ordersDao.updatePickupStatusByEventAndUser(req.getEventsId(), req.getUserId(), pickupStatus.name(),
						pickupTime);
			}

			personalOrderDao.save(order);
			return new PersonalOrdersRes(200, "更新成功");
		} catch (Exception e) {
			return new PersonalOrdersRes(500, "更新系統異常: " + e.getMessage());
		}
	}

	// 確認訂單
	public PersonalOrdersRes confirmPersonalOrder(int eventsId, String userId) {
		try {
			PersonalOrder order = personalOrderDao.findByEventsIdAndUserId(eventsId, userId);

			// 如果找不到結算單，代表活動還沒結單但團員已經要確認了 (預先確認)
			if (order == null) {
				order = new PersonalOrder();
				order.setEventsId(eventsId);
				order.setUserId(userId);

				// 動態計算當前使用者的總金額與總重量
				Integer totalSum = ordersDao.sumSubtotalByEventIdAndUserId(eventsId, userId);
				Double totalWeight = ordersDao.sumWeightByEventIdAndUserId(eventsId, userId);

				// 避免 null (如果該使用者沒有訂單)
				if (totalSum == null)
					totalSum = 0;
				if (totalWeight == null)
					totalWeight = 0.0;

				order.setTotalSum(totalSum);
				order.setTotalWeight(totalWeight);
				order.setPersonFee(0); // 運費預設為0，結單時會再計算
			}

			// 狀態更新為已確認
			order.setPaymentStatus(PaymentStatus.CONFIRMED);
			order.setPaymentTime(LocalDateTime.now());

			personalOrderDao.save(order);
			return new PersonalOrdersRes(200, "訂單確認成功");
		} catch (Exception e) {
			return new PersonalOrdersRes(500, "訂單確認發生異常: " + e.getMessage());
		}
	}

	// 計算平分拆帳
	public ShippingFeeRes getShippingFeeByEventId(int eventsId, String userId) {
		try {
			// 找此單的運費是多少
			Integer shippingFee = ordersDao.getShippingFeeByEventId(eventsId);
			// 用list存此單的userId
			List<PersonalOrder> orderList = personalOrderDao.findUserIdByEventsId(eventsId);
			// 檢查List有沒有人
			if (CollectionUtils.isEmpty(orderList)) {
				return new ShippingFeeRes(200, "目前無人跟團，運費總額為：" + shippingFee);
			}
			// 檢查list有沒有金額是0的
			for (PersonalOrder po : orderList) {
				if (po.getTotalSum() == 0) {
					return new ShippingFeeRes(400, "錯誤：成員 " + po.getUserId() + " 的商品金額為 0");
				}
			}

			// 計算平分運費相關
			String paymentStatus = groupbuyEventsDao.getSplitTypeById(eventsId);
			if (paymentStatus.equals("EQUAL")) {
				int totalPeople = orderList.size();
				int baseFee = shippingFee / totalPeople;
				int remainder = shippingFee % totalPeople;
				// 隨機打亂名單，這樣隨機才公平
				Collections.shuffle(orderList);
				// 開始一個一個分配金額
				for (int i = 0; i < orderList.size(); i++) {
					int finalFee = 0;
					if (i < remainder) {
						finalFee = baseFee + 1;
					} else {
						finalFee = baseFee;
					}
					PersonalOrder personalOrder = orderList.get(i);
					personalOrder.setPersonFee(finalFee);
					personalOrder.setTotalSum(personalOrder.getTotalSum() + finalFee);
					personalOrder.setPaymentTime(null);
				}
			} else if (paymentStatus.equals("WEIGHT")) {
				double totalWeight = ordersDao.sumTotalWeightByEventId(eventsId);
				if (totalWeight <= 0) {
					return new ShippingFeeRes(400, "權重計算失敗：總重量為 0");
				}
				for (PersonalOrder po : orderList) {
					double userTotalWeight = ordersDao.sumWeightByEventIdAndUserId(eventsId, po.getUserId());
					// 取整數 使用 Math.round 會根據小數點第一位判斷是否進位
					int weightFee = (int) Math.round((double) userTotalWeight / totalWeight * shippingFee);

					po.setTotalWeight(userTotalWeight);
					po.setPersonFee(weightFee);
					po.setTotalSum(po.getTotalSum() + weightFee);
					po.setPaymentTime(null);
				}
			}
			personalOrderDao.saveAll(orderList);
			return new ShippingFeeRes(200, "運費計算與分配成功！");
		} catch (Exception e) {
			return new ShippingFeeRes(500, "系統計算出錯：" + e.getMessage());
		}
	}

	// 取得該團所有人的結算單
	public GroupbuyEventsRes getPersonalOrdersByEventId(int eventsId) {
		try {
			List<PersonalOrder> list = personalOrderDao.findUserIdByEventsId(eventsId);
			if (!CollectionUtils.isEmpty(list)) {
				for (PersonalOrder po : list) {
					// 暱稱
					User user = userDao.getUserById(po.getUserId());
					if (user != null) {
						po.setUserNickname(user.getNickname());
						po.setUserAvatar(user.getAvatarUrl());
						po.setUserEmail(user.getEmail());
					}

					// 取餐狀態 (從 orders 表抓取該用戶的狀態)
					List<Orders> userOrders = ordersDao.getOrderByEventIdAndUserId(po.getUserId(), eventsId);
					if (!CollectionUtils.isEmpty(userOrders)) {
						po.setPickupStatus(userOrders.get(0).getPickupStatus());
					} else {
						po.setPickupStatus(PickupStatusEnum.NOT_PICKED_UP);
					}
				}
			}
			GroupbuyEventsRes res = new GroupbuyEventsRes(200, "查詢成功");
			res.setPersonalOrder(list);
			return res;
		} catch (Exception e) {
			return new GroupbuyEventsRes(500, "查詢失敗: " + e.getMessage());
		}
	}
}