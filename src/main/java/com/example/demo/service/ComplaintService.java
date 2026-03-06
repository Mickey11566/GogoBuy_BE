package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.constants.ResMessage;
import com.example.demo.dao.ComplaintDao;
import com.example.demo.dao.UserDao;
import com.example.demo.entity.Complaint;
import com.example.demo.entity.User;
import com.example.demo.entity.UserInfo;
import com.example.demo.request.ComplaintReq;
import com.example.demo.response.BasicRes;
import com.example.demo.response.ComplaintRes;
import com.example.demo.vo.ComplaintVo;

@Service
public class ComplaintService {
	
	@Autowired
	private ComplaintDao complaintDao;
	@Autowired
	private UserDao userDao;
	
	public BasicRes addComplaint(ComplaintReq req) {
		List<Integer> allEventId=complaintDao.getAllEventsId();
		if(!allEventId.contains(req.getEventId())) {
			return new BasicRes(ResMessage.EVENTS_NOT_FOUND.getCode(), ResMessage.EVENTS_NOT_FOUND.getMessage());
		}
		complaintDao.addComplaint(req.getComplaintUuid(), req.getRespondentUuid(), req.getReason(), req.getEventId());
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}
	
	public ComplaintRes getComplaint(int id) {
		List<Integer> allComplaintId=complaintDao.getAllComplaintId();
		if (!allComplaintId.contains(id)) {
		    return new ComplaintRes(ResMessage.COMPLAINT_ID_NOT_FOUND.getCode(), ResMessage.COMPLAINT_ID_NOT_FOUND.getMessage());
		}
		Complaint complaintData= complaintDao.getComplaint(id);
		List<ComplaintVo> vo= new ArrayList<ComplaintVo>();
		vo.add(new ComplaintVo( //
				userDao.getUserById(complaintData.getComplaintUuid()).getNickname(), //
				userDao.getUserById(complaintData.getRespondentUuid()).getNickname(), //
				complaintData.getReason()) //
			);
		return new ComplaintRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), vo);
	}
	
	public BasicRes finishComplaint(int id) {
		List<Integer> allComplaintId=complaintDao.getAllComplaintId();
		if (!allComplaintId.contains(id)) {
		    return new BasicRes(ResMessage.COMPLAINT_ID_NOT_FOUND.getCode(), ResMessage.COMPLAINT_ID_NOT_FOUND.getMessage());
		}
		Integer completed=complaintDao.checkFinishOrNot(id);
		if(completed==1) {
			complaintDao.finishComplaint(id, 0);		
			return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
		}else {
			complaintDao.finishComplaint(id, 1);		
			return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
		}
	}
	
	public ComplaintRes allComplaint() {
		List<Complaint> allData=complaintDao.allComplaint();
		Set<String> allUuids = new HashSet<>();
		for (Complaint c : allData) {
		    allUuids.add(c.getComplaintUuid());
		    allUuids.add(c.getRespondentUuid());
		}
		List<UserInfo> allUser=userDao.getAllUser();
		Map<String, String> userNickname=new HashMap<>();
		for(UserInfo user:allUser) {
			userNickname.put(user.getId(), user.getNickname());
		}
		List<ComplaintVo> vo= new ArrayList<ComplaintVo>();
		for(Complaint c:allData) {
			String complaintUser=userNickname.get(c.getComplaintUuid());
			String respondentUser=userNickname.get(c.getRespondentUuid());
			vo.add(new ComplaintVo(complaintUser, respondentUser, c.getReason()));
		}
		return new ComplaintRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), vo);
	}
}
