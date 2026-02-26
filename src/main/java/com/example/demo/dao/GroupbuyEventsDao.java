package com.example.demo.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.GroupbuyEvents;
import com.example.demo.entity.GroupsSearchView;
import com.example.demo.entity.Menu;
import com.example.demo.entity.OrdersSearchView;
import com.example.demo.projection.GroupbuyEventsProjection;

@Repository
public interface GroupbuyEventsDao extends JpaRepository<GroupbuyEvents, Integer> {

	// 新增開團
	@Transactional
	@Modifying
	@Query(value = "insert into groupbuy_events(host_id, stores_id, event_name, status, end_time, total_order_amount, "
			+ "shipping_fee, split_type, announcement, type, temp_menu, recommend, recommend_description, limitation)"
			+ "values(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13,?14)", nativeQuery = true)
	public int addEvent(String hostId, int storesId, String event_name, String status, LocalDateTime endTime,
			int totalOrderAmount, int shippingFee, String splitType, String announcement, String type, String temp_menu,
			String recommend, String recommendDescription, int limitation);

	// 檢查開團活動
	@Query(value = "select count(*) from groupbuy_events where host_id = ?1 and stores_id = ?2 and status = 'OPEN' and is_deleted = false", nativeQuery = true)
	public int checkEvnet(String hostId, int storesId);

	// 查所屬團ID
	@Query(value = "select* from groupbuy_events where id = ?1", nativeQuery = true)
	public GroupbuyEvents findById(int id);

	// 更新團
	@Transactional
	@Modifying
	@Query(value = "update groupbuy_events set host_id = ?1, stores_id = ?2, event_name = ?3, status = ?4, "
			+ "end_time = ?5, total_order_amount = ?6, shipping_fee = ?7, "
			+ "split_type = ?8, announcement = ?9, type= ?10, temp_menu = ?11, "
			+ "recommend = ?12, recommend_description = ?13, limitation = ?14 , pick_location = ?15 , pickup_time = ?16 where id = ?17", nativeQuery = true)
	public int updateEvent(String hostId, int storesId, String eventName, String status, LocalDateTime endTime,
			int totalOrderAmount, int shippingFee, String splitType, String announcement, String type, String tempMenu,
			String recommend, String recommendDescription, int limitation, String pickLocation,
			LocalDateTime pickupTime, int id);

	// 更新總金額
	@Transactional
	@Modifying
	@Query(value = "update groupbuy_events set total_order_amount = ?1 where id = ?2 and is_deleted = false ", nativeQuery = true)
	public int updateTotalAmount(int totalOrderAmount, int id);

	// 手動結單更新
	@Transactional
	@Modifying
	@Query(value = "update groupbuy_events set status = ?1 where id = ?2 and host_id = ?3 and is_deleted = false ", nativeQuery = true)
	public int updateStatus(String status, int id, String hostId);

	// 自動比較結單時間並結單
	@Modifying
	@Query(value = "update groupbuy_events set status = ?1 where end_time <= ?2 and status = ?3 and is_deleted = false", nativeQuery = true)
	public int autoUpdateEventsStatus(String targetStatus, LocalDateTime now, String currentStatus);

	// 先刪子表再刪主表(這個是要接API的刪除)
	@Transactional
	@Modifying
	@Query(value = "delete from groupbuy_events where id = ?1 ", nativeQuery = true)
	public int delete(int eventsId);

	// 用 hostId 檢索主表
	@Query(value = "select * from groupbuy_events  where host_id = ?1 and is_deleted = false ", nativeQuery = true)
	public List<GroupbuyEvents> getGroupbuyEventById(String hostId);

	// 用 storesId 查詢菜單
	@Query(value = "select * from menu where stores_id = ?1 ", nativeQuery = true)
	public List<Menu> getMenuByStoresId(int storesId);

	// 查詢全部的開團
	@Query(value = "SELECT e.*, u.nickname AS nickname FROM groupbuy_events e JOIN user u ON e.host_id = u.id", nativeQuery = true)
	public List<GroupbuyEventsProjection> getAll();

	// eventsId 查詢 event
	// @Query(value = "select * from groups_search_view where event_id = ?1 and
	// is_deleted = false", nativeQuery = true)
	// public List<GroupsSearchView> getEventsByEventsId(int id);

	@Query(value = "select * from groups_search_view where event_id = ?1 ", nativeQuery = true)
	public List<GroupsSearchView> getEventsByEventsId(int eventId);

	// @Query(value = "SELECT * FROM groupbuy_events WHERE id = ?1 AND is_deleted =
	// false", nativeQuery = true)
	// public List<GroupbuyEvents> getEventsByEventsId1(int id);

	// 用店家Id找符合的團
	@Query(value = "select * from groupbuy_events where stores_id = ?1 and is_deleted = false ", nativeQuery = true)
	public List<GroupbuyEvents> getGroupbuyEventByStoresId(int storesId);

	// 查詢運費狀態
	@Query(value = "select split_type from groupbuy_events where id = ?1 and is_deleted = false ", nativeQuery = true)
	public String getSplitTypeById(int eventsId);

	// 查詢 時間已到 且 狀態OPEN 的活動
	@Query(value = "select * from groupbuy_events where status = 'OPEN' and end_time <= now() and is_deleted = false ", nativeQuery = true)
	public List<GroupbuyEvents> findByEndTimeBeforeAndStatus(@Param("status") String status,
			@Param("now") LocalDateTime now);

	// 物理刪除
	@Modifying
	@Query(value = "delete from groupbuy_events where id = ?1", nativeQuery = true)
	public int deleteEvent(int id);

	// 查詢全部的映射表
	@Query(value = "select * from groups_search_view ", nativeQuery = true)
	public int selectAll();

	// 查詢orders映射表
	@Query(value = "select * from orders_search_view where event_id = ?1 and is_deleted = false", nativeQuery = true)
	public List<OrdersSearchView> selectOrdersAll(int eventId);

	// 查詢 event 裡面的 storeId
	@Query(value = "select stores_id from groupbuy_events where id = ?1 and is_deleted = false", nativeQuery = true)
	public int selectStoreIdByEventId(int eventId);
}
