package com.example.demo.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.User;
import com.example.demo.entity.UserInfo;

@Repository
public interface UserDao extends JpaRepository<User, String> {

	// 新增用戶
	@Transactional
	@Modifying
	@Query(value = "insert into user(id, email, password, nickname, phone, status) "//
			+ " values (?1, ?2, ?3, ?4, ?5, ?6)", nativeQuery = true)
	public int addUser(String id, String email, String password, String nickname, String phone, String status);

	// 透過 Email 查詢用戶
	@Query(value = "Select * from user where email = ?", nativeQuery = true)
	public User getUserByEmail(String email);

	// 透過 ID 查詢用戶
	@Query(value = "Select * from user where id = ?", nativeQuery = true)
	public User getUserById(@Param("id") String id);

	// 查詢用戶
	@Query(value = "Select * from user", nativeQuery = true)
	public List<UserInfo> getAllUser();

	// 更改使用者個人資訊(暱稱、大頭貼、統編)
	@Modifying
	@Transactional
	@Query(value = "update user SET nickname = ?1, avatar_url = ?2, carrier = ?3 WHERE id = ?4", nativeQuery = true)
	public int updateProfile(String nickname, String avatarUrl, //
			String carrier, String id);

	// 更改密碼
	@Transactional
	@Modifying
	@Query(value = "update user SET password = ?2 where id = ?1", nativeQuery = true)
	public int userPassword(String id, String password);

	// 串接電話
	@Transactional
	@Modifying
	@Query(value = "update user SET phone = ?2 where id = ?1", nativeQuery = true)
	public int userPhone(String id, String phone);

	// OTP驗證碼至對應id的email
	@Transactional
	@Modifying
	@Query(value = "update user SET otp_code = ?2, otp_expiry=?3 where id = ?1", nativeQuery = true)
	public int sendOtpById(String id, String otpCode, LocalDateTime otpExpiry);

	// 發送OTP驗證碼至email
	@Transactional
	@Modifying
	@Query(value = "update user SET otp_code = ?2, otp_expiry=?3 where email = ?1", nativeQuery = true)
	public int sendOtpByEmail(String email, String otpCode, LocalDateTime otpExpiry);

	// 修改信箱
	@Transactional
	@Modifying
	@Query(value = "update user SET email= ?2, otp_code = NULL, otp_expiry= NULL where id = ?1", nativeQuery = true)
	public int updateEmail(String id, String email);

	// 檢查是否有相同Email存在
	@Query(value = "SELECT EXISTS(SELECT 1 FROM user WHERE email = ?)", nativeQuery = true)
	public int existsByEmail(String email);

	// 刪除殘餘的OTP碼欄位資料
	@Modifying
	@Query(value = "UPDATE user set otp_code = NULL, otp_expiry = NULL WHERE otp_expiry < ?", nativeQuery = true)
	int clearExpiredOtp(@Param("now") LocalDateTime now);

	// 新增Google用戶
	@Transactional
	@Modifying
	@Query(value = "insert into user(id, email, password, nickname, phone, avatar_url, provider, status) "//
			+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)", nativeQuery = true)
	public int addGoogleUser(String id, String email, String password, String nickname, String phone, String avatarUrl,
			String provider, String status);

	// 更改用戶狀態
	@Transactional
	@Modifying
	@Query(value = "UPDATE user SET status = ?2 where id = ?1", nativeQuery = true)
	public int updateStatus(String id, String status);

	//更新最愛店家	
	@Transactional
	@Modifying
	@Query(value = "update user set favorite_store = ?2 where id = ?1", nativeQuery = true)
	public int updateFavoriteStores(@Param("userId")String userId, @Param("storesArray") String storesArray);
	
	//	查詢最愛店家
	@Query(value = "Select favorite_store from user where id = ?", nativeQuery = true)
	public String getFavoriteStoresById(@Param("id") String id);
}
