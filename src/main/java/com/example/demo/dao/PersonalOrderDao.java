package com.example.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.PersonalOrder;

@Repository
public interface PersonalOrderDao extends JpaRepository<PersonalOrder, Integer> {

	// 新增結單
	@Transactional
	@Modifying
	@Query(value = "insert into personal_order(events_id, user_id, total_weight, person_fee, total_sum, payment_status, payment_time)values(?1, ?2, ?3, ?4, ?5, ?6, CURRENT_TIMESTAMP)", nativeQuery = true)
	public int addPersonalOrder(int eventsId, String userId, double totalWeight, int personFee, int totalSum,
			String paymentStatus);

	// 更新
	@Transactional
	@Modifying
	@Query(value = "update personal_order set " + "events_id = ?1, " + "user_id = ?2, " + "total_weight = ?3, "
			+ "person_fee = ?4, " + "total_sum = ?5, " + "payment_status = ?6, "
			+ "payment_time = case when ?6 = 'PAID' or ?6 = 'CONFIRMED' then CURRENT_TIMESTAMP else payment_time END "
			+ "where id = ?7", nativeQuery = true)
	public int updatePersonalOrder(int eventsId, String userId, Double totalWeight, int personFee, int totalSum,
			String paymentStatus, int id);

	// 查詢相同 userId 跟 eventsId 在 personal_order (限制回傳一筆以免 duplicates 報錯)
	@Query(value = "select * from personal_order where events_id = ?1 and user_id = ?2 limit 1", nativeQuery = true)
	public PersonalOrder findByEventsIdAndUserId(int eventsId, String userId);

	// 取得該團所有人的結算單 (排除已刪除訂單的用戶)
	@Query(value = "SELECT p.* FROM personal_order p " +
			"WHERE p.events_id = ?1 " +
			"AND EXISTS (SELECT 1 FROM orders o " +
			"            WHERE o.user_id = p.user_id " +
			"            AND o.events_id = p.events_id " +
			"            AND o.is_deleted = false)", nativeQuery = true)
	public List<PersonalOrder> findUserIdByEventsId(int eventsId);

	// 統計該團未付款人數 (排除已收訖或已付款，且必須有有效訂單)
	@Query(value = "SELECT COUNT(*) FROM personal_order p " +
			"WHERE p.events_id = ?1 " +
			"AND p.payment_status NOT IN ('PAID', 'CONFIRMED') " +
			"AND EXISTS (SELECT 1 FROM orders o " +
			"            WHERE o.user_id = p.user_id " +
			"            AND o.events_id = p.events_id " +
			"            AND o.is_deleted = false)", nativeQuery = true)
	public int countUnpaidByEventsId(int eventId);

	// 查詢 eventId 跟 userId 的結單
	@Query(value = "select * from personal_order where events_id = ?1 and user_id = ?2", nativeQuery = true)
	public PersonalOrder findByEventsId(int eventId, String userId);

	// (已付款過) 更新 PaymentStatus 和 PaymentTime
	@Transactional
	@Modifying
	@Query(value = " update personal_order set payment_status ='PAID' , payment_time = now() where events_id = ?1 and user_id = ?2", nativeQuery = true)
	public int updatePaymentStatusAndPaymentTime(int eventId, String userId);

	// 刪除特定結算單
	@Transactional
	@Modifying
	@Query(value = "DELETE FROM personal_order WHERE events_id = ?1 AND user_id = ?2", nativeQuery = true)
	public int deleteByEventsIdAndUserId(int eventId, String userId);
}
