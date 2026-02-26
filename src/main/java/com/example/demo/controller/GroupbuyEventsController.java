package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.request.GroupbuyEventsReq;
import com.example.demo.response.BasicRes;
import com.example.demo.response.GroupbuyEventsRes;
import com.example.demo.service.GoogleMapService;
import com.example.demo.service.GroupbuyEventsService;

import jakarta.validation.Valid;

@CrossOrigin
@RestController
public class GroupbuyEventsController {

	@Autowired
	private GroupbuyEventsService groupbuyEventsService;

	@Autowired
	private GoogleMapService googleMapService;

	// 新增
	@PostMapping("gogobuy/event/addEvent")
	public BasicRes addEvent(@Valid @RequestBody GroupbuyEventsReq req) {
		return groupbuyEventsService.addEvent(req);
	}

	// 更新
	@PostMapping("gogobuy/event/updateEvent")
	public BasicRes updateEvent(@Valid @RequestBody GroupbuyEventsReq req) {
		return groupbuyEventsService.updateEvent(req.getId(), req);
	}

	// 查詢開團者的開團紀錄
	@GetMapping("gogobuy/event/getGroupbuyEventByHostId")
	public GroupbuyEventsRes getGroupbuyEventById(@RequestParam(name = "host_id", required = false) String hostId) {
		return groupbuyEventsService.getGroupbuyEventById(hostId);
	}

	// 透過店家ID取得對應菜單
	@GetMapping("gogobuy/event/getMenuByStoresId")
	public GroupbuyEventsRes getMenuByStoresId(@RequestParam(name = "stores_id", required = false) int storesId) {
		return groupbuyEventsService.getMenuByStoresId(storesId);
	}

	// 查詢開團菜單
	@GetMapping("gogobuy/event/getMenuByMenuId")
	public GroupbuyEventsRes getMenuByMenuId(@RequestParam(name = "temp_menu", required = false) List<Integer> menuId) {
		return groupbuyEventsService.getMenuByMenuId(menuId);
	}

	// 透過店家ID取得對應菜單
	@GetMapping("gogobuy/event/getGroupbuyEventByStoresId")
	public GroupbuyEventsRes getGroupbuyEventByStoresId(
			@RequestParam(name = "stores_id", required = false) int storesId) {
		return groupbuyEventsService.getGroupbuyEventByStoresId(storesId);
	}

	// 查詢全部開團
	@GetMapping("gogobuy/event/getAll")
	public GroupbuyEventsRes getAll() {
		return groupbuyEventsService.getAll();
	}

	// 暱稱查詢開團紀錄
	@GetMapping("gogobuy/event/getGroupbuyEventByNickname")
	public GroupbuyEventsRes getGroupbuyEventByNickname(
			@RequestParam(name = "host_nickname", required = false) String hostNickname) {
		return groupbuyEventsService.getGroupbuyEventByNickname(hostNickname);
	}

	// 用google取經緯度
	@GetMapping("gogobuy/event/googleMapAddress")
	public BasicRes googleMapAddress(@RequestParam(name = "address") String address) {
		return googleMapService.googleMapAddress(address);
	}

	// 團長手動結單
	@PostMapping("gogobuy/event/hostCloseEvent")
	public BasicRes hostCloseEvent(@Valid @RequestParam(name = "id") int id,
			@RequestParam(name = "host_id") String hostId) {
		return groupbuyEventsService.hostCloseEvent(id, hostId);
	}

	// 透過 活動Id 取得 events
	@GetMapping("gogobuy/event/getEventsByEventsId")
	public GroupbuyEventsRes getEventsByEventsId(@Valid @RequestParam(name = "id") int id) {
		return groupbuyEventsService.getEventsByEventsId(id);
	}

	// 物理全刪(只有團)
	@PostMapping("gogobuy/event/deleteEventPhysically")
	public BasicRes deleteEventPhysically(@RequestParam(name = "id") int id) {
		return groupbuyEventsService.deleteEventPhysically(id);
	}

	// 購物車
	@GetMapping("gogobuy/event/getCart")
	public GroupbuyEventsRes getCart(@Valid @RequestParam(name = "user_id") String usrerId) {
		return groupbuyEventsService.getCart(usrerId);
	}

	// 活動ID 查詢 Orders 映射表
	@GetMapping("gogobuy/event/getOrdersView")
	public GroupbuyEventsRes getOrdersAll(@Valid @RequestParam(name = "event_id") int eventId) {
		return groupbuyEventsService.getOrdersAll(eventId);
	}
}
