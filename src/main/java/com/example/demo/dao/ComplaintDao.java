package com.example.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Complaint;
import com.example.demo.entity.GroupbuyEvents;

import jakarta.transaction.Transactional;

@Repository
public interface ComplaintDao extends JpaRepository<Complaint, Integer> {
	// 新增申訴
	@Modifying
	@Transactional
	@Query(value = "insert into complaint (complaint_uuid, respondent_uuid, reason, event_id)" //
			+ " values (?1, ?2, ?3, ?4)", nativeQuery = true)
	public void addComplaint(String complaintUuid, String respondentUuid, String reason, int eventId);

	// 讀取申訴
	@Query(value = "select * from complaint where id = ?", nativeQuery = true)
	public Complaint getComplaint(int id);

	@Query(value = "select * from complaint", nativeQuery = true)
	public List<Complaint> allComplaint();

	// 申訴解決
	@Modifying
	@Transactional
	@Query(value = "update complaint set is_completed = ?2 where id = ?1", nativeQuery = true)
	public void finishComplaint(int id, int x);

	@Query(value = "select id from complaint", nativeQuery = true)
	public List<Integer> getAllComplaintId();

	@Query(value = "select is_completed from complaint where id = ?1", nativeQuery = true)
	public Integer checkFinishOrNot(int id);

	@Query(value = "select * from complaint", nativeQuery = true)
	public List<Complaint> getAllComplaints();

	@Query(value = "select id from groupbuy_events", nativeQuery = true)
	public List<Integer> getAllEventsId();
}