package com.example.demo.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.demo.constants.NotifiCategoryEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import com.example.demo.constants.ResMessage;
import com.example.demo.dao.GroupbuyEventsDao;
import com.example.demo.dao.NotifiMesDao;
import com.example.demo.dao.UserDao;
import com.example.demo.dao.UserNotifDao;
import com.example.demo.entity.NotifiMes;
import com.example.demo.entity.User;
import com.example.demo.entity.UserNotif;
import com.example.demo.request.NotifiMesReq;
import com.example.demo.response.BasicRes;
import com.example.demo.response.MessagesRes;
import com.example.demo.vo.UserNotificationVo;

import jakarta.transaction.Transactional;

@Service
public class MessagesService {

	@Autowired
	private NotifiMesDao notifiMesDao;

	@Autowired
	private UserNotifDao userNotifDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private GroupbuyEventsDao groupbuyEventsDao;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private ObjectMapper objectMapper;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	// 檢查通知訊息
	private void checkNotifiMes(NotifiMesReq req) throws Exception {
		// 檢查類別
		try {
			NotifiCategoryEnum category = req.getCategory();
		} catch (IllegalArgumentException e) {
			throw new Exception("不存在的類別喵");
		}
		// 檢查標題
		// if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
		if (!StringUtils.hasText(req.getTitle())) {
			throw new Exception("標題不可為空喵");
		}

		// 檢查訊息內容
		// if (req.getContent() == null || req.getContent().trim().isEmpty()) {
		if (!StringUtils.hasText(req.getContent())) {
			throw new Exception("內容不可為空喵");
		}

		// 檢查URL
		// if (req.getTargetUrl() == null || req.getTargetUrl().trim().isEmpty()) {
		if (!StringUtils.hasText(req.getTargetUrl())) {
			throw new Exception("URL不可為空喵");
		}

		// 檢查時效
		try {

			LocalDate expiredAt = LocalDate.parse(req.getExpiredAt(), FORMATTER);

			LocalDate now = LocalDate.now();

			if (expiredAt != null && expiredAt.isBefore(now)) {
				throw new Exception("時效期限不可早於現在時間喵");
			}
		} catch (DateTimeParseException e) {
			// 格式不正確的處理
			throw new IllegalArgumentException("無效的時間格式喵: " + req.getExpiredAt());
		}

//		檢查使用者id (訊息發布者，選填)
		String user = req.getUserId();
		if (StringUtils.hasText(user)) {
			User checkUser = userDao.getUserById(user);
			if (checkUser == null) {
				System.err.println("Warning: notification sender userId '" + user + "' not found in DB, skipping sender validation.");
				// 不拋出例外，允許通知繼續發送 (sender 不一定需要存在於 user 表)
			}
		}

		// 檢查活動id
		Integer eventId = req.getEventId();

		if (eventId != null) {
			NotifiCategoryEnum category = req.getCategory();
			if (NotifiCategoryEnum.GROUP_BUY.equals(category)) {
				if (groupbuyEventsDao.findById(eventId.intValue()) == null) {
					throw new Exception("團購不存在");
				}

			}
		}
	}

	// 檢查使用者通知
	private void checkUserNotif(List<UserNotificationVo> voList) throws Exception {
		if (voList == null) {
			throw new Exception("沒有收件者");
		} else {
			for (UserNotificationVo vo : voList) {
				String user = vo.getUserId();

				// if (user == null || user.trim().isEmpty()) {
				if (!StringUtils.hasText(user)) {
					throw new Exception("沒有收件者");
				}

				User checkUser = userDao.getUserById(user);

				if (checkUser == null) {
					throw new Exception("收件者id不存在");
				}
			}
		}
	}

	// 建立通知
	@Transactional
	public BasicRes create(NotifiMesReq req) throws Exception {
		checkNotifiMes(req);
		checkUserNotif(req.getUserNotificationVoList());

		NotifiMes mes = new NotifiMes();
		mes.setCategory(req.getCategory());
		mes.setTitle(req.getTitle());
		mes.setContent(req.getContent());
		mes.setTargetUrl(req.getTargetUrl());
		mes.setExpiredAt(LocalDate.parse(req.getExpiredAt(), FORMATTER));
		mes.setUserId(req.getUserId());
		mes.setEventId(req.getEventId());
		mes = notifiMesDao.save(mes);
		// 上行作用取代下行 避免ID撞車問題
		// notifiMesDao.createNotifiMes(req.getCategory().name(), req.getTitle(),
		// req.getContent(), req.getTargetUrl(), //
		// req.getExpiredAt(), req.getUserId(), req.getEventId());
		int nofitId = mes.getId();
		List<UserNotificationVo> UserNotificationVoList = req.getUserNotificationVoList();

		// 準備推播訊息 JSON
		String pushMessage = "";
		try {
			var pushObj = new java.util.HashMap<String, Object>();
			pushObj.put("id", nofitId);
			pushObj.put("category", req.getCategory().name());
			pushObj.put("title", req.getTitle());
			pushObj.put("message", req.getContent());
			pushObj.put("link", req.getTargetUrl());
			pushObj.put("createdAt", java.time.LocalDateTime.now().toString());
			pushMessage = objectMapper.writeValueAsString(pushObj);
		} catch (Exception e) {
			pushMessage = req.getContent();
		}

		System.out.println("Creating notification: " + req.getTitle() + " for " + UserNotificationVoList.size() + " recipients.");
		for (UserNotificationVo vo : UserNotificationVoList) {
			System.out.println("Notifying user: " + vo.getUserId() + (StringUtils.hasText(vo.getEmail()) ? " (with email)" : ""));
			userNotifDao.createUserNotifi(vo.getUserId(), nofitId);
			
			// 1. 站內即時通知 (SSE)
			notificationService.sendNotification(vo.getUserId(), pushMessage);
			
			// 2. Email 通知
			if (StringUtils.hasText(vo.getEmail())) {
				sendPickupEmail(vo.getEmail(), req.getTitle(), req.getContent());
			}
		}

		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

	private void sendPickupEmail(String email, String title, String content) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom("GogobuyAdmin@gmail.com");
			message.setTo(email);
			message.setSubject("[GoGoBuy] " + title);
            message.setText("您好：\n\n" + content + "\n\n詳情請點擊 http://localhost:4200/gogobuy/home 查看 GoGoBuy 官網 。");
			mailSender.send(message);
		} catch (Exception e) {
			// 郵件發送失敗不應中斷整個流程，僅紀錄錯誤
			System.err.println("Failed to send email to " + email + ": " + e.getMessage());
		}
	}

	// 更新通知
	public MessagesRes update(int id, NotifiMesReq req) throws Exception {
		// 呼叫檢查 function
		checkNotifiMes(req);
		checkUserNotif(req.getUserNotificationVoList());
		NotifiMes mes = notifiMesDao.findById(id).orElseThrow(() -> //
		new Exception("找不到該筆訊息 ID: " + id ));

		mes.setCategory(req.getCategory());
		mes.setTitle(req.getTitle());
		mes.setContent(req.getContent());
		mes.setTargetUrl(req.getTargetUrl());
		mes.setExpiredAt(LocalDate.parse(req.getExpiredAt(), FORMATTER));
		mes.setUserId(req.getUserId());
		mes.setEventId(req.getEventId());
		mes = notifiMesDao.save(mes);

		// 先刪
		userNotifDao.deleteByNotifId(id);

		// 後加
		List<UserNotificationVo> voList = req.getUserNotificationVoList();
		for (UserNotificationVo vo : voList) {
			userNotifDao.createUserNotifi(vo.getUserId(), id);
		}

		List<UserNotif> updatedUserNotifList = userNotifDao.findByNotifId(id);

		return new MessagesRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), List.of(mes),
				updatedUserNotifList);
	}

	// 根據使用者id刪除對應通知
	public BasicRes deleteByUser(String userId, int notifId) throws Exception {
		UserNotif userNotif = userNotifDao.findByUserIdAndNotifId(userId, notifId)
				.orElseThrow(() -> new Exception("找不到該使用者的通知紀錄喵"));
		userNotif.setDeleted(true);
		userNotifDao.save(userNotif);

		return new BasicRes(ResMessage.SUCCESS.getCode(), "使用者 " + userId + " 的個人通知已刪除");

	}

	// 刪除通知
	public BasicRes delete(int notifId) throws Exception {
//		檢查
		NotifiMes mes = notifiMesDao.findById(notifId)
				.orElseThrow(() -> new Exception("找不到該筆訊息 ID: " + notifId + "，刪除失敗"));
		String title = mes.getTitle();
//		砍子表
		userNotifDao.deleteByNotifId(notifId);
//		砍主表
		notifiMesDao.deleteById(notifId);
		return new BasicRes(ResMessage.SUCCESS.getCode(), "訊息標題:[" + title + "]已經被砍光了");
	}

	// 根據使用者id搜尋通知
	public MessagesRes searchByUser(String userId) throws Exception {

		// if (userId == null || userId.trim().isEmpty()) {
		if (!StringUtils.hasText(userId)) {
			throw new Exception("使用者id不能是空白");
		}

		User checkUser = userDao.getUserById(userId);

		if (checkUser == null) {
			throw new Exception("使用者id不存在");
		}

		// 獲取該使用者全部訊息細節
		List<UserNotif> userMsgList = userNotifDao.searchByUser(userId);

		if (userMsgList == null || userMsgList.isEmpty()) {
			return new MessagesRes(ResMessage.SUCCESS.getCode(), "該使用者目前沒有訊息", //
					new ArrayList<>(), new ArrayList<>());
		}

		// 取得訊息編號陣列
		// 建立資料流(水線)(類似迴圈)
		List<Integer> notifId = userMsgList.stream()
				// 映射 對流中的每一個 UserNotif 物件，呼叫 getNotifiId() 方法
				.map(UserNotif::getNotifiId)
				// 排除重複 (notif_id) (雖然我感覺不會有)
				.distinct()
				// 收集
				.collect(Collectors.toList());

		List<NotifiMes> msgList = notifiMesDao.searchById(notifId);

		return new MessagesRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), //
				msgList, userMsgList);
	}

}
