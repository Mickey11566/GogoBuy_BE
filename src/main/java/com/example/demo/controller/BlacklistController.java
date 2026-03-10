package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.request.BlacklistReq;
import com.example.demo.response.BasicRes;
import com.example.demo.service.BlacklistService;

import jakarta.validation.Valid;

@CrossOrigin
@RestController
public class BlacklistController {

	@Autowired
	private BlacklistService blacklistService;

	@PostMapping("gogobuy/blacklist/add")
	public BasicRes addBlacklist(@Valid @RequestBody BlacklistReq req) {
		return blacklistService.addBlacklist(req);
	}
}
