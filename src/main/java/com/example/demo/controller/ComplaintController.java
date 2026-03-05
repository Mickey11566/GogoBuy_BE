package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.request.ComplaintReq;
import com.example.demo.response.BasicRes;
import com.example.demo.response.ComplaintRes;
import com.example.demo.service.ComplaintService;

import jakarta.validation.Valid;

@CrossOrigin
@RestController
public class ComplaintController {
	@Autowired
	private ComplaintService complaintService;
	
	@PostMapping("gogobuy/complaint/add_conplaint")
	public BasicRes addComplaint(@Valid @RequestBody ComplaintReq req) {
		return complaintService.addComplaint(req);
	}
	
	@GetMapping("gogobuy/complaint/get_complaint")
	public ComplaintRes getComplaint(@RequestParam("id") int id) {
		return complaintService.getComplaint(id);
	}
	
	@PostMapping("gogobuy/complaint/set_state")
	public BasicRes finishComplaint(@RequestParam("id") int id) {
		return complaintService.finishComplaint(id);
	}
}
