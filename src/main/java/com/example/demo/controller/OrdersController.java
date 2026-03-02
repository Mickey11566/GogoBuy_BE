package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.request.OredersReq;
import com.example.demo.response.BasicRes;
import com.example.demo.response.GroupbuyEventsRes;
import com.example.demo.response.OrderHistoryRes;
import com.example.demo.service.OrdersService;

import jakarta.validation.Valid;

@CrossOrigin
@RestController
public class OrdersController {

	@Autowired
	private OrdersService ordersService;

	// 新增訂單
	@PostMapping("gogobuy/event/addOrders")
	public BasicRes addOrders(@Valid @RequestBody OredersReq req) {
		return ordersService.addOrders(req);
	}

	// // 更新訂單
	// @PostMapping("gogobuy/event/updateOrders")
	// public BasicRes updateOrders(@Valid @RequestBody OredersReq req) {
	// return ordersService.updateOrders(req);
	// }

	// 查詢跟團者的開團
	@GetMapping("gogobuy/event/getOrdersIdByUserId")
	public GroupbuyEventsRes getOrdersIdByUserId(@Valid @RequestParam(name = "user_id") String userId) {
		return ordersService.getOrdersByUserId(userId);
	}

	// 查詢跟團者的訂單
	@GetMapping("gogobuy/event/getAllOrdersByUserIdAndEventsId")
	public GroupbuyEventsRes getAllOrdersByUserIdAndEventsId(@Valid @RequestParam(name = "user_id") String userId,
			@RequestParam(name = "events_id") int eventsId) {
		return ordersService.getEventIdByUserId(userId, eventsId);
	}

	// 回傳計算完的平均運費
	// @GetMapping("gogobuy/getShippingFeeByEventId")
	// public ShippingFeeRes getShippingFeeByEventId(@RequestParam(name =
	// "events_id") int eventsId) {
	// return ordersService.getShippingFeeByEventId(eventsId);
	// }

	// 軟刪除
	@PostMapping("gogobuy/event/deleteOrderByUserIdAndEventsId")
	public BasicRes deleteOrderByUserIdAndEventsId(@RequestParam(name = "user_id") String userId, //
			@RequestParam(name = "events_id") int eventsId,
			@RequestParam(name = "acting_user_id", required = false) String actingUserId) {
		return ordersService.deleteOrderByUserIdAndEventsId(userId, eventsId, actingUserId);
	}

	// 硬刪除
	@PostMapping("gogobuy/event/deleteOrder")
	private BasicRes deleteOrder(@Valid @RequestParam(name = "user_id") String userId,
			@RequestParam(name = "events_id") int eventsId) {
		return ordersService.hardDelete(userId, eventsId);
	}

	// 根據order id 軟刪除該筆資料
	@PostMapping("gogobuy/order/deleteOrderById")
	public BasicRes deleteOrderById(@RequestParam(name = "order_id") int orderId,
			@RequestParam(name = "acting_user_id", required = false) String actingUserId) {
		return ordersService.deleteCartByOrderId(orderId, actingUserId);
	}

	// 取得使用者的歷史訂單
	@GetMapping("gogobuy/orders/history")
	public OrderHistoryRes getHistoryOrdersByUserId(@Valid @RequestParam(name = "user_id") String userId) {
		return ordersService.getHistoryOrdersByUserId(userId);
	}

}
