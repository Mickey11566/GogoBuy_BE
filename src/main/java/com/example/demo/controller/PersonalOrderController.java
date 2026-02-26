package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.request.personalOrderReq;
import com.example.demo.response.BasicRes;
import com.example.demo.response.GroupbuyEventsRes;
import com.example.demo.response.PersonalOrdersRes;
import com.example.demo.response.ShippingFeeRes;
import com.example.demo.service.PersonalOrderService;

import jakarta.validation.Valid;

@CrossOrigin
@RestController
public class PersonalOrderController {

	@Autowired
	private PersonalOrderService personalOrderService;

	// // 新增
	// @PostMapping("gogobuy/addPersonalOrder")
	// public BasicRes addPersonalOrder(@Valid @RequestBody personalOrderReq req) {
	// return personalOrderService.addPersonalOrder(req);
	// }
	// 更新
	@PostMapping("gogobuy/event/updatePersonalOrder")
	public BasicRes updatePersonalOrder(@Valid @RequestBody personalOrderReq req) {
		return personalOrderService.updatePersonalOrder(req);
	}

	// 確認訂單
	@PostMapping("gogobuy/event/confirmPersonalOrder")
	public PersonalOrdersRes confirmPersonalOrder(@RequestParam(name = "events_id") int eventsId,
			@RequestParam(name = "user_id") String userId) {
		return personalOrderService.confirmPersonalOrder(eventsId, userId);
	}

	// //
	// public PersonalOrdersRes updateTotalSum(int eventsId, String userId,
	// personalOrderReq req) {
	// return personalOrderService.updatePersonalOrder(req);
	// }

	// 更新平均運費
	@GetMapping("gogobuy/event/getShippingFeeByEventId")
	public ShippingFeeRes getShippingFeeByEventId(@Valid @RequestParam(name = "events_id") int eventsId,
			@RequestParam(name = "user_id") String userId) {
		return personalOrderService.getShippingFeeByEventId(eventsId, userId);
	}

	// 取得該團所有人的結算單
	@GetMapping("gogobuy/event/getPersonalOrdersByEventId")
	public GroupbuyEventsRes getPersonalOrdersByEventId(@RequestParam(name = "events_id") int eventsId) {
		return personalOrderService.getPersonalOrdersByEventId(eventsId);
	}
}
