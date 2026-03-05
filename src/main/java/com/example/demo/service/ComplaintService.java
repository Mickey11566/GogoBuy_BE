package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.constants.ResMessage;
import com.example.demo.dao.ComplaintDao;
import com.example.demo.dao.UserDao;
import com.example.demo.entity.Complaint;
import com.example.demo.entity.User;
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
		complaintDao.addComplaint(req.getComplaintUuid(), req.getRespondentUuid(), req.getReason(), req.getEventId());
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}
	
	public ComplaintRes getComplaint(int id) {
		List<Integer> allComplaintId=complaintDao.getAllComplaintId();
		if (!allComplaintId.contains(id)) {
		    return new ComplaintRes(ResMessage.COMPLAINT_ID_NOT_FOUND.getCode(), ResMessage.COMPLAINT_ID_NOT_FOUND.getMessage());
		}
		Complaint complaintData= complaintDao.getComplaint(id);
		ComplaintVo vo= new ComplaintVo();
		vo.setComplaintUserName(userDao.getUserById(complaintData.getComplaintUuid()).getNickname());
		vo.setRespondentUserName(userDao.getUserById(complaintData.getRespondentUuid()).getNickname());
		vo.setReason(complaintData.getReason());
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
}
