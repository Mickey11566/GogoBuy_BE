package com.example.demo.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.constants.ResMessage;
import com.example.demo.constants.UserStatusEnum;
import com.example.demo.dao.StoresSearchDao;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.UserInfoDto;
import com.example.demo.dto.UserPasswordDto;
import com.example.demo.entity.User;
import com.example.demo.request.ResetPasswordReq;
import com.example.demo.request.UserAddReq;
import com.example.demo.request.UserLoginReq;
import com.example.demo.response.BasicRes;
import com.example.demo.response.GetUserInfoListRes;
import com.example.demo.response.GetUserInfoRes;
import com.example.demo.response.LoginRes;

import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private StoresSearchDao storesSrerchDao;
	
	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private ImageService imageService;

	@Autowired
	private JwtService jwtService;

	/**
	 * Redis發送修改email驗證碼功能 暫時用不到
	 * 
	 * @Autowired private StringRedisTemplate redisTemplate;
	 */

	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	/*
	 * 會員註冊
	 */
	@Transactional(rollbackOn = Exception.class)
	public BasicRes addUser(UserAddReq req) throws Exception {
		String uniqueID = UUID.randomUUID().toString();
		String email = req.getEmail();
		String password = req.getPassword();
		String name = req.getNickname();
		String phone = req.getPhone();
		String status = UserStatusEnum.PENDING_ACTIVE.name();
		int res = userDao.addUser(uniqueID, email, encoder.encode(password), name, phone, status);
		if (res == 1) {
			// 發送開通驗證信
			sendActivationEmail(email, uniqueID);

			return new BasicRes(ResMessage.SUCCESS.getCode(), //
					"註冊成功，請至電子信箱點擊驗證連結開通帳戶。");
		} else {

			return new BasicRes(ResMessage.REGISTRATION_ERROR.getCode(), //
					ResMessage.REGISTRATION_ERROR.getMessage());
		}
	}

	// 發送會員開通驗證信 (公開方法供 OAuth2 使用)
	public void sendActivationEmail(String email, String userId) {
		// 生成 JWT Token
		String token = jwtService.createActivationToken(userId);

		// 生成開通連結 (以前端 Angular 跑在 4200 port)
		String activationUrl = "http://localhost:4200/active-account?token=" + token + "\n\n連結將於1小時後失效。";

		try {
			// 執行發送郵件
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom("GogobuyAdmin@gmail.com");
			message.setTo(email);
			message.setSubject("[GoGoBuy] 帳號開通驗證");
			message.setText("您好：\n\n請點選以下網址開通：" + activationUrl);
			mailSender.send(message);

			// 測試用
			System.out.println("驗證信已發送至: " + email + ", 連結: " + activationUrl);

		} catch (Exception e) {
			// 僅印出錯誤，不拋出避免中斷 (視需求調整)
			e.printStackTrace();
		}
	}

	// 驗證時啟用帳戶
	public boolean activateUser(String token) {
		// 從 JWT 提取 UUID
		String userId = jwtService.parseActivationToken(token);

		if (userId != null && !userId.isEmpty()) {
			// 直接去資料庫更新該 UUID 的狀態
			int rows = userDao.updateStatus(userId, "ACTIVE");
			return rows > 0;
		}
		return false;
	}

	// 自主停用
	public BasicRes selfSuspend(String userId) {
		User user = userDao.getUserById(userId);
		if ("active".equals(user.getStatus())) {
			userDao.updateStatus(userId, "self_suspended");
		}
		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

	// 管理員停權
	public BasicRes adminBan(String id) {
		userDao.updateStatus(id, "banned");
		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

	/*
	 * 登入
	 */
	public LoginRes login(UserLoginReq req) {
		String email = req.getEmail();
		String password = req.getPassword();

		User user = userDao.getUserByEmail(email);

		if (user == null) {
			return new LoginRes(ResMessage.USER_NOT_FOUND.getCode(), //
					ResMessage.USER_NOT_FOUND.getMessage());
		}
		// 比對密碼:
		// 比對輸入的密碼與資料庫中加密過的密碼是否相同
		if (!encoder.matches(password, user.getPassword())) {
			return new LoginRes(ResMessage.PASSWORD_ERROR.getCode(), //
					ResMessage.PASSWORD_ERROR.getMessage());
		}

		// 核心狀態檢查
		switch (user.getStatus()) {
			case "pending_active":
				return new LoginRes(ResMessage.PENDING_ACTIVE.getCode(), ResMessage.PENDING_ACTIVE.getMessage());
			case "banned":
				return new LoginRes(ResMessage.BANNED.getCode(), ResMessage.BANNED.getMessage());
			case "self_suspended":
				return new LoginRes(ResMessage.SELF_SUSPENDED.getCode(), ResMessage.SELF_SUSPENDED.getMessage());
			case "active":
				return new LoginRes(ResMessage.SUCCESS.getCode(), //
						ResMessage.SUCCESS.getMessage(), user.getId());
			default:
				return new LoginRes(500, "未知狀態");
		}
	}

	public GetUserInfoListRes getAllUser() {
		return new GetUserInfoListRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(),
				userDao.getAllUser());
	}

	public GetUserInfoRes getUser(String id) {
		User user = userDao.getUserById(id);
		if (user == null) {
			return new GetUserInfoRes(ResMessage.USER_NOT_FOUND.getCode(), //
					ResMessage.USER_NOT_FOUND.getMessage());
		}
		List<Integer>favoriteStores = getFavoriteStores(id);
		
		return new GetUserInfoRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage(), user.getId(), //
				user.getNickname(), user.getEmail(), //
				user.getPhone(), user.getAvatarUrl(), //
				user.getRole(), user.getCarrier(), user.getExp(), //
				user.getTimesRemaining(), user.getProvider(),
				favoriteStores);
	}

	/*
	 * 更新暱稱、大頭貼或載具
	 */
	@Transactional(rollbackOn = Exception.class)
	public BasicRes updateInfo(UserInfoDto dto, String id) {
		User user = userDao.getUserById(id);
		if (user == null) {
			return new BasicRes(ResMessage.USER_NOT_FOUND.getCode(), //
					ResMessage.USER_NOT_FOUND.getMessage());
		}

		String finalnickname = StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : user.getNickname();

		String finalavatarUrl = (dto.getAvatarUrl() != null) ? dto.getAvatarUrl() : user.getAvatarUrl();

		String finalcarrier = (dto.getCarrier() != null) ? dto.getCarrier() : user.getCarrier();

		userDao.updateProfile(finalnickname, finalavatarUrl, finalcarrier, id);
		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

	/*
	 * 於會員中心更改密碼
	 */
	@Transactional(rollbackOn = Exception.class)
	public BasicRes updatePassword(String id, UserPasswordDto dto) {
		User user = userDao.getUserById(id);
		// 檢查帳戶是否存在
		if (user == null) {
			return new BasicRes(ResMessage.USER_NOT_FOUND.getCode(), //
					ResMessage.USER_NOT_FOUND.getMessage());
		}

		// 比對密碼:
		// 比對輸入的密碼與資料庫中加密過的密碼是否相同
		if (!encoder.matches(dto.getOldPassword(), user.getPassword())) {
			return new BasicRes(ResMessage.PASSWORD_ERROR.getCode(), //
					ResMessage.PASSWORD_ERROR.getMessage());
		}
		if (dto.getOldPassword().equals(dto.getNewPassword())) {
			return new BasicRes(ResMessage.SAME_PASSWORD_ERROR.getCode(), //
					ResMessage.SAME_PASSWORD_ERROR.getMessage());
		}

		// 加密新密碼
		String encodePassword = encoder.encode(dto.getNewPassword());
		userDao.userPassword(id, encodePassword);
		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

	/*
	 * 串接電話
	 */
	@Transactional(rollbackOn = Exception.class)
	public BasicRes userPhone(String phone, String id) {
		User user = userDao.getUserById(id);
		if (user == null) {
			return new BasicRes(ResMessage.USER_NOT_FOUND.getCode(), //
					ResMessage.USER_NOT_FOUND.getMessage());
		}
		if (phone == null) {
			return new BasicRes(ResMessage.PHONE_ERROR.getCode(), //
					ResMessage.PHONE_ERROR.getMessage());
		}
		userDao.userPhone(id, phone);
		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

	/*
	 * 透過ID發送OTP code 至 Email
	 */
	@Transactional
	public BasicRes sendOtpById(String email, String id) {
		// 1. 生成 6 位數驗證碼
		String otpCode = String.format("%06d", new Random().nextInt(1000000));

		// 2. 找到使用者並更新 OTP 資訊
		userDao.sendOtpById(id, otpCode, LocalDateTime.now().plusMinutes(10));

		SimpleMailMessage message = new SimpleMailMessage();

		// 從 properties 讀取，或直接寫死發信帳號
		message.setFrom("GogobuyAdmin@gmail.com");
		message.setTo(email);
		message.setSubject("[GoGoBuy] 修改信箱驗證碼");
		message.setText("您好：\n\n您的驗證碼為：" + otpCode + "\n驗證碼將於 10 分鐘後失效，請盡速完成操作。");

		try {
			System.out.println("OTP碼:" + otpCode);
			// 執行發送
			mailSender.send(message);
			return new BasicRes(ResMessage.EMAIL_SUCCESS.getCode(), //
					ResMessage.EMAIL_SUCCESS.getMessage());
		} catch (Exception e) {
			return new BasicRes(ResMessage.EMAIL_ERROR.getCode(), //
					ResMessage.EMAIL_ERROR.getMessage());
		}
	}

	/*
	 * 透過 email 確認是否有該用戶 並發送OTP
	 */
	@Transactional
	public BasicRes sendOtpByEmail(String email) {
		// 1. 生成 6 位數驗證碼
		String otpCode = String.format("%06d", new Random().nextInt(1000000));

		User user = userDao.getUserByEmail(email);

		if (user == null) {
			return new BasicRes(ResMessage.USER_NOT_FOUND.getCode(), //
					ResMessage.USER_NOT_FOUND.getMessage());
		}

		// 2. 找到使用者並更新 OTP 資訊
		userDao.sendOtpByEmail(user.getEmail(), otpCode, LocalDateTime.now().plusMinutes(10));

		SimpleMailMessage message = new SimpleMailMessage();

		// 從 properties 讀取，或直接寫死發信帳號
		message.setFrom("GogobuyAdmin@gmail.com");
		message.setTo(user.getEmail());
		message.setSubject("[GoGoBuy] 修改密碼驗證碼");
		message.setText("您好：\n\n您的驗證碼為：" + otpCode + "\n驗證碼將於 10 分鐘後失效，請盡速完成操作。");

		try {
			System.out.println("OTP碼:" + otpCode);
			// 執行發送
			mailSender.send(message);
			return new BasicRes(ResMessage.EMAIL_SUCCESS.getCode(), //
					ResMessage.EMAIL_SUCCESS.getMessage());
		} catch (Exception e) {
			return new BasicRes(ResMessage.EMAIL_ERROR.getCode(), //
					ResMessage.EMAIL_ERROR.getMessage());
		}
	}

	/*
	 * 驗證OTP碼並更新Email
	 */
	@Transactional
	public BasicRes verifyAndUpdateEmail(String newEmail, String inputCode, String id) {
		User user = userDao.getUserById(id);

		// 1. 檢查驗證碼是否為空
		if (user.getOtpCode() == null || user.getOtpExpiry() == null) {
			return new BasicRes(ResMessage.OTP_EMPTY.getCode(), //
					ResMessage.OTP_EMPTY.getMessage());
		}

		// 2. 檢查是否過期
		if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
			return new BasicRes(ResMessage.OTP_EXPIRED.getCode(), //
					ResMessage.OTP_EXPIRED.getMessage());
		}

		// 3. 比對驗證碼
		if (!user.getOtpCode().equals(inputCode)) {
			return new BasicRes(ResMessage.OTP_ERROR.getCode(), //
					ResMessage.OTP_ERROR.getMessage());
		}

		// 檢查新信箱是否已被他人佔用
		if (userDao.existsByEmail(newEmail) > 0) {
			return new BasicRes(ResMessage.EMAIL_EXITS.getCode(), ResMessage.EMAIL_EXITS.getMessage());
		}

		// 4. 驗證通過，執行更新並清空 OTP 資訊
		userDao.updateEmail(id, newEmail);

		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

	/*
	 * 重設密碼 (忘記密碼)
	 */
	@Transactional(rollbackOn = Exception.class)
	public BasicRes resetPassword(ResetPasswordReq req) {
		User user = userDao.getUserByEmail(req.getEmail());
		// 檢查帳戶是否存在
		if (user == null) {
			return new BasicRes(ResMessage.USER_NOT_FOUND.getCode(), //
					ResMessage.USER_NOT_FOUND.getMessage());
		}

		// 1. 檢查驗證碼是否為空
		if (user.getOtpCode() == null || user.getOtpExpiry() == null) {
			return new BasicRes(ResMessage.OTP_EMPTY.getCode(), //
					ResMessage.OTP_EMPTY.getMessage());
		}

		// 2. 檢查驗證碼是否過期
		if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
			return new BasicRes(ResMessage.OTP_EXPIRED.getCode(), //
					ResMessage.OTP_EXPIRED.getMessage());
		}

		// 3. 比對驗證碼
		if (!user.getOtpCode().equals(req.getOtpCode())) {
			return new BasicRes(ResMessage.OTP_ERROR.getCode(), //
					ResMessage.OTP_ERROR.getMessage());
		}

		// 加密新密碼
		String encodePassword = encoder.encode(req.getNewPassword());
		userDao.userPassword(user.getId(), encodePassword);

		// 清空 OTP 防止重複使用
		user.setOtpCode(null);
		user.setOtpExpiry(null);
		user.setPassword(encodePassword);
		userDao.save(user);

		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

	// 每小時進行一次清理
	@Scheduled(cron = "0 0 * * * ?")
	@Transactional
	/*
	 * 每隔一小時清理資料庫的OTP驗證碼
	 */
	public void cleanupExpiredOtp() {
		System.out.println("開始清理過期驗證碼...");

		// 找到所有已過期且還有 code 的使用者，並清空欄位
		int updatedRows = userDao.clearExpiredOtp(LocalDateTime.now());

		System.out.println("清理完成，共影響了 " + updatedRows + " 筆資料。");
	}

	/*
	 * Redis功能區塊 暫時用不到 // 驗證並更新 Email
	 * 
	 * @Transactional(rollbackOn = Exception.class) public BasicRes
	 * verifyAndUpdateEmail(UserAccountDto dto, String id) { User user =
	 * userDao.getUserById(id); String redisKey = "OTP:EMAIL_CHANGE:" + id; String
	 * storedCode = redisTemplate.opsForValue().get(redisKey);
	 * 
	 * // 1. 檢查驗證碼是否存在 if (storedCode == null) { return new
	 * BasicRes(ResMessage.OTP_EXPIRED.getCode(), //
	 * ResMessage.OTP_EXPIRED.getMessage()); }
	 * 
	 * // 2. 比對驗證碼 if (!storedCode.equals(dto.getOtpCode())) { return new
	 * BasicRes(ResMessage.OTP_ERROR.getCode(), //
	 * ResMessage.OTP_ERROR.getMessage()); }
	 * 
	 * // 3. 檢查 Email 是否重複 if (user.getEmail() == (dto.getNewEmail())) { return new
	 * BasicRes(ResMessage.EMAIL_EXITS.getCode(), //
	 * ResMessage.EMAIL_EXITS.getMessage()); }
	 * 
	 * // 4. 更新 user.setEmail(dto.getNewEmail()); userDao.save(user);
	 * 
	 * // 5. 更新成功，刪除驗證碼 redisTemplate.delete(redisKey);
	 * 
	 * return new BasicRes(ResMessage.SUCCESS.getCode(), //
	 * ResMessage.SUCCESS.getMessage()); }
	 */

	@Transactional
	public BasicRes changeAvatar(String id, MultipartFile file) {
		try {
			// 檢查用戶是否存在
			User user = userDao.getUserById(id);
			if (user == null) {
				return new BasicRes(ResMessage.USER_NOT_FOUND.getCode(), ResMessage.USER_NOT_FOUND.getMessage());
			}

			// ImageService 進行 Cloudinary 上傳
			String newAvatarUrl = imageService.uploadImage(file, "avatars");

			// 更新資料庫
			userDao.updateProfile(user.getNickname(), newAvatarUrl, user.getCarrier(), id);

			return new BasicRes(ResMessage.SUCCESS.getCode(), "圖片上傳成功");

		} catch (IOException e) {
			// 借400用
			return new BasicRes(ResMessage.REGISTRATION_ERROR.getCode(), "圖片上傳失敗");
		}
	}

	// 信箱開通驗證
	public boolean verifyEmail(String email) {
		User user = userDao.getUserByEmail(email);
		if (user != null && user.getStatus() == UserStatusEnum.PENDING_ACTIVE.getStatus()) {

			return true;
		}
		return false;
	}

	// 重新發送開通驗證信
	public BasicRes resendActivationEmail(String email) {
		User user = userDao.getUserByEmail(email);
		if (user == null) {
			return new BasicRes(ResMessage.USER_NOT_FOUND.getCode(), ResMessage.USER_NOT_FOUND.getMessage());
		}

		// 檢查狀態是否為待開通
		if (!UserStatusEnum.PENDING_ACTIVE.name().equalsIgnoreCase(user.getStatus())) {
			return new BasicRes(ResMessage.VERIFICATION_ERROR.getCode(), "帳號已開通或狀態異常，無需重新發送。");
		}

		// 重新發送驗證信
		sendActivationEmail(email, user.getId());

		return new BasicRes(ResMessage.SUCCESS.getCode(), "驗證信已重新發送，請檢查您的信箱。");
	}

	// 管理員恢復帳號
	public BasicRes activeUserAdmin(String id) {
		User user = userDao.getUserById(id);
		if (user == null) {
			return new BasicRes(ResMessage.USER_NOT_FOUND.getCode(), ResMessage.USER_NOT_FOUND.getMessage());
		}

		userDao.updateStatus(id, "active");

		return new BasicRes(ResMessage.SUCCESS.getCode(), "帳號已成功恢復為活躍狀態。");
	}
	
//	private void favoriteStoresCheck(int storesId) throws Exception{
//		if (storesSrerchDao.getStoreById(storesId)!=null) {
//			throw new Exception("查無此店家喵");
//		}
//		return;
//	}
	
	//更新最愛店家	
	@Transactional(rollbackOn = Exception.class)
	public BasicRes updateFavoriteStores(String id, List<Integer> storesIdList) {
		List<Integer> newList = (storesIdList == null) ? new ArrayList<>()//
				: storesIdList.stream().distinct().collect(Collectors.toList());//去重
		List<Integer> finalToSave;
//		List<Integer> validStores= newlist.isEmpty() ? new ArrayList<>() : storesSrerchDao.exsitStores(newlist);
		//	單數字>> 沒有>>新增 有>>刪除
		if (newList.size()==1) {
			Integer sId = newList.get(0);//sId為物件而非引索
			List<Integer> oldList = new ArrayList<>(getFavoriteStores(id));
			List<Integer> validTarget = storesSrerchDao.exsitStores(Collections.singletonList(sId));
			if (!validTarget.isEmpty()) {
	            if (oldList.contains(sId)) {
	            	oldList.remove(sId); // 存在則刪除（取消收藏）
	            } else {
	            	oldList.add(sId);    // 不存在則新增（加入收藏）
	            }
	        }
	        finalToSave = oldList;
		}
		//陣列>>直接蓋
		else {
			finalToSave = newList.isEmpty() ? new ArrayList<>() 
                    : storesSrerchDao.exsitStores(newList);
		}

		//		Collections.sort(validStores);
		String storesString = finalToSave.stream()
				.sorted()//排序
                .map(String::valueOf)
                .collect(Collectors.joining(",","[","]"));
		try {
			int done = userDao.updateFavoriteStores(id, storesString);
			if (done>0) {
				return new BasicRes(200,"成功更新最愛店家喵");
			}
			else {
				return new BasicRes(404,"使用者不存在喵");
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("資料更新失敗喵", e);
		}
}

	//查詢最愛店家
	public List<Integer> getFavoriteStores(String id){
//		List<Integer> favoriteStoresList = new ArrayList<>();
		String listStr = "";
		try {
			listStr = userDao.getFavoriteStoresById(id);			
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("好像沒有這個使用者喵!", e);
		}
		if (StringUtils.hasText(listStr) && !listStr.equals("[]")) {
			return Arrays.stream(listStr.replace("[", "").replace("]", "").split(","))
			.map(String::trim)		//去空格
			.filter(s -> !s.isEmpty())  //去空字串
			.map(Integer::parseInt)  //轉數字
			.collect(Collectors.toCollection(ArrayList::new));	//	變成List
		}
		return new ArrayList<>();
	}
}
