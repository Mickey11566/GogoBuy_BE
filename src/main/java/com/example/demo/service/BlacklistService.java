package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dao.BlacklistDao;
import com.example.demo.entity.Blacklist;
import com.example.demo.request.BlacklistReq;
import com.example.demo.response.BasicRes;

@Service
public class BlacklistService {

	@Autowired
	private BlacklistDao blacklistDao;

	public BasicRes addBlacklist(BlacklistReq req) {
		if (req.getUserId() == null || req.getBlockedUserId() == null) {
			return new BasicRes(400, "使用者和封鎖對象的 ID 都不能為空");
		}

		boolean exists = blacklistDao.existsByUserIdAndBlockedUserId(req.getUserId(), req.getBlockedUserId());
		if (exists) {
			return new BasicRes(400, "已經在黑名單中，無法重複加入");
		}

		Blacklist blacklist = new Blacklist();
		blacklist.setUserId(req.getUserId());
		blacklist.setBlockedUserId(req.getBlockedUserId());

		blacklistDao.save(blacklist);
		
		return new BasicRes(200, "成功加入黑名單");
	}

}
