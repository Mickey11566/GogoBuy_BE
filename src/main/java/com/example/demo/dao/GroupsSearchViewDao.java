package com.example.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.GroupsSearchView;

public interface GroupsSearchViewDao extends JpaRepository<GroupsSearchView, Integer>{
	
	
	// nickname 查詢 eventId，過濾黑名單
		@Query(value = "select * from groups_search_view where host_nickname like %:hostNickname% "
				+ "AND host_id NOT IN (SELECT b.user_id FROM blacklist b WHERE b.blocked_user_id = :currentUserId)", nativeQuery = true)
		public List<GroupsSearchView> getGroupbuyEventByNickname (@Param("hostNickname") String hostNickname, @Param("currentUserId") String currentUserId);

	// eventId 查詢 hostNickname
		@Query(value = "select host_nickname from groups_search_view where event_id =?1 ", nativeQuery = true)
		public List<GroupsSearchView> getNicknameByEventId (int eventId);
		
		// 查詢全部的映射表
	    @Query(value ="select * from groups_search_view ", nativeQuery = true)
	    public List<GroupsSearchView> selectAllView();
	    
		// 找出所有跟我有關的團（我是團長 OR 我有點餐），過濾黑名單
		@Query(value = "SELECT * FROM groups_search_view WHERE (host_id = :userId " +
		               "OR event_id IN (SELECT events_id FROM orders WHERE user_id = :userId AND is_deleted = false)) " +
		               "AND host_id NOT IN (SELECT b.user_id FROM blacklist b WHERE b.blocked_user_id = :userId)", 
		       nativeQuery = true)
		public List<GroupsSearchView> findAllMyRelatedEvents(@Param("userId") String userId);
		
		public GroupsSearchView findById(int eventId);
}
