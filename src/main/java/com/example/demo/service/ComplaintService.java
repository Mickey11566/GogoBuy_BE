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
import com.example.demo.response.GetComplaintListRes;
import com.example.demo.vo.ComplaintVo;

@Service
public class ComplaintService {

	@Autowired
	private ComplaintDao complaintDao;
	@Autowired
	private UserDao userDao;

	public BasicRes addComplaint(ComplaintReq req) {
		List<Integer> allEventId = complaintDao.getAllEventsId();
		if (!allEventId.contains(req.getEventId())) {
			return new BasicRes(ResMessage.EVENTS_NOT_FOUND.getCode(), ResMessage.EVENTS_NOT_FOUND.getMessage());
		}
		complaintDao.addComplaint(req.getComplaintUuid(), req.getRespondentUuid(), req.getReason(), req.getEventId());
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	public ComplaintRes getComplaint(int id) {
		List<Integer> allComplaintId = complaintDao.getAllComplaintId();
		if (!allComplaintId.contains(id)) {
			return new ComplaintRes(ResMessage.COMPLAINT_ID_NOT_FOUND.getCode(),
					ResMessage.COMPLAINT_ID_NOT_FOUND.getMessage());
		}
		Complaint complaintData = complaintDao.getComplaint(id);
		ComplaintVo vo = convertToVo(complaintData);
		return new ComplaintRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), vo);
	}

	public GetComplaintListRes getAllComplaints() {
		List<Complaint> complaints = complaintDao.getAllComplaints();
		List<ComplaintVo> voList = complaints.stream().map(this::convertToVo).toList();
		return new GetComplaintListRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), voList);
	}

	private ComplaintVo convertToVo(Complaint complaint) {
		ComplaintVo vo = new ComplaintVo();
		vo.setId(complaint.getId());

		User complainant = userDao.getUserById(complaint.getComplaintUuid());
		vo.setComplaintUserName(
				complainant != null ? complainant.getNickname() : "未知用戶 (" + complaint.getComplaintUuid() + ")");

		User respondent = userDao.getUserById(complaint.getRespondentUuid());
		vo.setRespondentUserName(
				respondent != null ? respondent.getNickname() : "未知用戶 (" + complaint.getRespondentUuid() + ")");

		vo.setReason(complaint.getReason());
		vo.setEventId(complaint.getEventId());
		vo.setCompleted(complaint.isCompleted());
		return vo;
	}

	public BasicRes finishComplaint(int id) {
		Complaint complaint = complaintDao.findById(id).orElse(null);
		if (complaint == null) {
			return new BasicRes(ResMessage.COMPLAINT_ID_NOT_FOUND.getCode(),
					ResMessage.COMPLAINT_ID_NOT_FOUND.getMessage());
		}

		complaint.setCompleted(!complaint.isCompleted());
		complaintDao.save(complaint);
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}
}
