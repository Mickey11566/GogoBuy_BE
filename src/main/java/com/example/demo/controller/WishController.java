package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.constants.ResMessage;
import com.example.demo.request.WishReq;
import com.example.demo.response.AllWishRes;
import com.example.demo.response.BasicRes;
import com.example.demo.service.WishService;

import jakarta.validation.Valid;

@CrossOrigin
@RestController
public class WishController {
	@Autowired
	private WishService wishService;
	
	@GetMapping("gogobuy/wish/all_wishes")
	public AllWishRes allWish() {
		return wishService.allWish();
	}
	
	@PostMapping("gogobuy/wish/add_wishes")
	public BasicRes create(@Valid @RequestBody WishReq req) throws Exception {
		return wishService.addWish(req);
	}
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public BasicRes handleJsonError(HttpMessageNotReadableException e) {
	    return new BasicRes(ResMessage.WISH_TYPE_ERROR.getCode(), ResMessage.WISH_TYPE_ERROR.getMessage());
	}
	
	@PostMapping("gogobuy/wish/follow_wish")
	public BasicRes follow(@RequestParam("id") int id, @RequestParam("userId") String userId) {
		return wishService.setfollowers(id, userId);
	}
	
	@PostMapping("gogobuy/wish/finish_wish")
	public BasicRes finish(@RequestParam("id") int id, @RequestParam("userId") String userId,  //
			@RequestParam("targetUrl") String targetUrl) throws Exception {
		return wishService.finishWish(id, userId, targetUrl);
	}
	
	@PostMapping("gogobuy/wish/delete_wish")
	public BasicRes delete(@RequestParam("id") int id, @RequestParam("userId") String userId) throws Exception {
		return wishService.delWish(id, userId);
	}
}
