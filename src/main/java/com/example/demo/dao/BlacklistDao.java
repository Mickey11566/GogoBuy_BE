package com.example.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Blacklist;

@Repository
public interface BlacklistDao extends JpaRepository<Blacklist, Integer> {

	boolean existsByUserIdAndBlockedUserId(String userId, String blockedUserId);

	@Query("SELECT b.blockedUserId FROM Blacklist b WHERE b.userId = ?1")
	List<String> findBlockedUserIdByUserId(String userId);

}
