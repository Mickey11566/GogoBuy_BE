package com.example.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Wishes;
import com.example.demo.repository.WishRepository;

import jakarta.transaction.Transactional;

@Repository
public interface WishDao extends JpaRepository<Wishes, Integer>, WishRepository {

//	全部願望
	@Query(value = "select * from wishes where is_deleted = false", nativeQuery = true)
	public List<Wishes> allWish();

	// 抓非匿名人類
	@Query(value = "select nickname from user where id = ?", nativeQuery = true)
	public String getNickname(String userId);

//	新增願望
	@Modifying
	@Transactional
	@Query(value = "insert into wishes (user_id, title, is_anonymous, followers, is_deleted, type, location, is_finished)" //
			+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)", nativeQuery = true)
	public void addWish(String userId, String title, boolean anonymous, String followers, boolean deleted, //
			String type, String location, boolean finished);
	
//	查詢剩餘次數
	@Query(value = "select times_remaining from user where id = ?", nativeQuery = true)
	public int getTimes(String id);

	// 更新次數
	@Modifying
	@Transactional
	@Query(value = "update user set times_remaining = ?2 where id = ?1", nativeQuery = true)
	public int setTimes(String id, int times);

	// 每月許願次數重新計算
	@Modifying
	@Transactional
	@Query(value = "update user set times_remaining = ?3 where exp >= ?1 and exp <= ?2", nativeQuery = true)
	public void wishTimesReset(int minExp, int maxExp, int times);
	
//	查詢該願望的follower
	@Query(value = "select user_id, followers, is_deleted, is_finished from wishes where id = ?", nativeQuery = true)
	public List<Object[]> getfollowers(int id);

	// 許願followers更新
	@Modifying
	@Transactional
	@Query(value = "update wishes set followers = ?2 where id = ?1", nativeQuery = true)
	public void setfollowers(int id, String newFollowersStr);

//	個人願望完成
	@Modifying
	@Transactional
	@Query(value = "update wishes set is_finished = true where id = ?1", nativeQuery = true)
	public int finishWish(int id);
	// 查開團id(前端回傳)
	
//	個人願望刪除
	@Modifying
	@Transactional
	@Query(value = "update wishes set is_deleted = true where id = ?1 and user_id = ?2", nativeQuery = true)
	public int delWish(int id, String userId);
	// 查被刪除願望data
	@Query(value = "select title from wishes where id = ?", nativeQuery = true)
	public String getWishTitle(int id);
	
//	超過3個月且未被刪除同時也未被完成
	@Query(value = "select * from wishes where build_date <= DATE_SUB(NOW(), INTERVAL 3 MONTH)"  //
			+ " and is_deleted = false and is_finished = false", nativeQuery = true)
	public List<Wishes> checkOverTime();

//	新增收信人
//	(WishRepositoryImpl實作WishRepository，由WishDao繼承WishRepository)
}
