package com.example.demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.example.demo.constants.NotifiCategoryEnum;
import com.example.demo.request.NotifiMesReq;
import com.example.demo.vo.UserNotificationVo;
import java.time.LocalDate;
import java.util.stream.Collectors;
import com.example.demo.entity.User;
import com.example.demo.dao.UserDao;
import com.example.demo.constants.ResMessage;
import com.example.demo.constants.WishTypeEnum;
import com.example.demo.dao.WishDao;
import com.example.demo.entity.NotifiMes;
import com.example.demo.entity.Wishes;
import com.example.demo.repository.NotifiMesRepository;
import com.example.demo.request.WishReq;
import com.example.demo.response.AllWishRes;
import com.example.demo.response.BasicRes;
import com.example.demo.vo.WishVo;

import jakarta.transaction.Transactional;

@Service
public class WishService {

	@Autowired
	private WishDao wishDao;

	@Autowired
	private NotifiMesRepository notifiMsgRepository;

	@Autowired
	private MessagesService messagesService;

	@Autowired
	private UserDao userDao;

	public AllWishRes allWish() {
		List<Wishes> wishesData = wishDao.allWish();
		List<WishVo> returnData = new ArrayList<>();
		for (Wishes w : wishesData) {
			WishVo vo = new WishVo();
			vo.setId(w.getId());
			vo.setUser_id(w.getUser_id());
			vo.setTitle(w.getTitle());
			vo.setType(w.getType());
			vo.setBuildDate(w.getBuildDate());
			vo.setLocation(w.getLocation());
			vo.setFinished(w.isFinished());
			if (!w.isAnonymous()) {
				vo.setNickname(wishDao.getNickname(w.getUser_id()));
			} else {
				vo.setNickname(null);
			}

			// if (w.getFollowers() != null && !w.getFollowers().isBlank()) {
			if (StringUtils.hasText(w.getFollowers())) {
				vo.setFollowers(Arrays.asList(w.getFollowers().split(",")));
			} else {
				vo.setFollowers(Collections.emptyList());
			}
			returnData.add(vo);
		}
		return new AllWishRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), returnData);
	}

	@Transactional(rollbackOn = Exception.class)
	public BasicRes addWish(WishReq req) throws Exception {
		int times = wishDao.getTimes(req.getUserId());

		if (times <= 0) {
			return new BasicRes(ResMessage.OUT_OF_TIMES_REMAINING.getCode(),
					ResMessage.OUT_OF_TIMES_REMAINING.getMessage());
		}

		try {

			if (req.getType() != WishTypeEnum.beverage && req.getType() != WishTypeEnum.restaurant
					&& req.getType() != WishTypeEnum.groceries) {
				return new BasicRes(ResMessage.WISH_TYPE_ERROR.getCode(), ResMessage.WISH_TYPE_ERROR.getMessage());
			}
			wishDao.addWish(req.getUserId(), req.getTitle(), req.isAnonymous(), null, false, req.getType().name(),
					req.getLocation(), false);
			times = times - 1;
			wishDao.setTimes(req.getUserId(), times);
		} catch (Exception e) {
			throw e;
		}
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	public BasicRes setfollowers(int id, String userId) {
		final String userIdPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

		if (!userId.matches(userIdPattern)) {
			return new BasicRes(ResMessage.USER_NOT_FOUND.getCode(), ResMessage.USER_NOT_FOUND.getMessage());
		}

		List<Object[]> wishData = wishDao.getfollowers(id);

		if (wishData.isEmpty()) {
			return new BasicRes(ResMessage.WISH_ID_NOT_FOUND.getCode(), ResMessage.WISH_ID_NOT_FOUND.getMessage());
		}
		Object[] data = wishData.get(0);
		String wishUser = data[0].toString();
		String followersStr = (data[1] != null) ? data[1].toString() : null;
		boolean deleted = ((Number) data[2]).intValue() == 1;
		boolean finished = ((Number) data[3]).intValue() == 1;
		if (deleted) {
			return new BasicRes(ResMessage.WISH_ID_NOT_FOUND.getCode(), ResMessage.WISH_ID_NOT_FOUND.getMessage());
		}
		if (finished) {
			return new BasicRes(ResMessage.WISH_IS_FINISHED.getCode(), ResMessage.WISH_IS_FINISHED.getMessage());
		}
		// 許願者不可跟願
		if (userId.equals(wishUser)) {
			return new BasicRes(ResMessage.WISH_USER_CAN_NOT_FOLLOW.getCode(),
					ResMessage.WISH_USER_CAN_NOT_FOLLOW.getMessage());
		}

		// 許願者不可跟願
		if (userId.equals(wishUser)) {
			return new BasicRes(ResMessage.WISH_USER_CAN_NOT_FOLLOW.getCode(),
					ResMessage.WISH_USER_CAN_NOT_FOLLOW.getMessage());
		}

		// 物件轉陣列
		List<String> followersList = new ArrayList<>();

		// if (followersStr != null && !followersStr.trim().isEmpty()) {
		if (StringUtils.hasText(followersStr)) {

			String[] splitData = followersStr.split(","); // 過濾字串

			for (String s : splitData) {
				// if (s != null && !s.trim().isEmpty()) {
				if (StringUtils.hasText(s)) {
					followersList.add(s.trim());
				}
			}
		}

		// 檢查是否已經跟隨過
		if (followersList.contains(userId)) {
			followersList.remove(userId);
		} else {
			followersList.add(userId);
		}

		// 轉回字串存回 DB
		String saveStr = String.join(",", followersList);
		wishDao.setfollowers(id, saveStr);

		// 如果是「跟隨」動作 (不是取消跟隨)，則通知許願者
		if (followersList.contains(userId)) {
			try {
				User wisher = userDao.getUserById(wishUser);
				User follower = userDao.getUserById(userId);
				if (wisher != null && follower != null) {
					String wishTitle = data[4].toString();
					NotifiMesReq notifyReq = new NotifiMesReq();
					notifyReq.setCategory(NotifiCategoryEnum.WISH);
					notifyReq.setTitle("新跟隨者！！");
					notifyReq.setContent("團員「" + (follower.getNickname() != null ? follower.getNickname() : "匿名")
							+ "」跟隨了您的願望：「" + wishTitle + "」！");
					notifyReq.setTargetUrl("/user/wishes?tab=mine&wishId=" + id);
					notifyReq.setUserId(userId);
					notifyReq.setEventId(id);
					notifyReq.setExpiredAt(LocalDate.now().plusDays(30).toString());

					UserNotificationVo vo = new UserNotificationVo();
					vo.setUserId(wishUser);
					vo.setEmail(wisher.getEmail());
					notifyReq.setUserNotificationVoList(Arrays.asList(vo));

					messagesService.create(notifyReq);
				}
			} catch (Exception e) {
				System.err.println("Failed to send follow notification: " + e.getMessage());
			}
		}

		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	@Transactional(rollbackOn = Exception.class)
	public BasicRes finishWish(int id, String userId, String targetUrl) throws Exception {
		final String userIdPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
		if (!userId.matches(userIdPattern)) {
			return new BasicRes(ResMessage.USER_NOT_FOUND.getCode(), ResMessage.USER_NOT_FOUND.getMessage());
		}
		// 判斷願望存在
		List<Object[]> wishData = wishDao.getfollowers(id);
		if (wishData.isEmpty()) {
			return new BasicRes(ResMessage.WISH_ID_NOT_FOUND.getCode(), ResMessage.WISH_ID_NOT_FOUND.getMessage());
		}
		Object[] data = wishData.get(0);
		String wishUser = data[0].toString();
		List<String> wishersList = (data[1] != null) // 通知用
				? new ArrayList<>(Arrays.asList(((String) data[1]).split(",")))
				: new ArrayList<>();
		boolean deleted = ((Number) data[2]).intValue() == 1;
		boolean finished = ((Number) data[3]).intValue() == 1;
		if (deleted) {
			return new BasicRes(ResMessage.WISH_ID_NOT_FOUND.getCode(), ResMessage.WISH_ID_NOT_FOUND.getMessage());
		}
		if (finished) {
			return new BasicRes(ResMessage.WISH_IS_FINISHED.getCode(), ResMessage.WISH_IS_FINISHED.getMessage());
		}
		// 收集所有需要通知的人 (排除執行操作的本人)
		Set<String> notifyUserIds = new HashSet<>(wishersList);
		if (!wishUser.equals(userId)) {
			notifyUserIds.add(wishUser);
		}
		notifyUserIds.remove(userId);

		try {
			NotifiMesReq notifyReq = new NotifiMesReq();
			notifyReq.setCategory(NotifiCategoryEnum.WISH);
			notifyReq.setTitle("願望開團成功!!");
			notifyReq.setContent("您許願/跟隨的「" + data[4].toString() + "」成功開團了，快去看看吧！");
			notifyReq.setTargetUrl(targetUrl);
			notifyReq.setUserId(userId); // 管理員或開團者 ID
			notifyReq.setEventId(id);
			notifyReq.setExpiredAt(LocalDate.now().plusDays(30).toString());

			notifyReq.setUserNotificationVoList(notifyUserIds.stream().map(uid -> {
				UserNotificationVo vo = new UserNotificationVo();
				vo.setUserId(uid);
				User u = userDao.getUserById(uid);
				if (u != null) {
					vo.setEmail(u.getEmail());
				}
				return vo;
			}).collect(Collectors.toList()));

			messagesService.create(notifyReq);

			wishDao.finishWish(id);
		} catch (Exception e) {
			throw e;
		}
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	@Transactional(rollbackOn = Exception.class)
	public BasicRes delWish(int id, String userId) throws Exception {
		// 判斷願望存在
		List<Object[]> wishData = wishDao.getfollowers(id);
		if (wishData.isEmpty()) {
			return new BasicRes(ResMessage.WISH_ID_NOT_FOUND.getCode(), ResMessage.WISH_ID_NOT_FOUND.getMessage());
		}
		Object[] data = wishData.get(0);
		String wishUser = data[0].toString();
		List<String> followersList = (data[1] != null)
				? new ArrayList<>(Arrays.asList(((String) data[1]).split(",")))
				: new ArrayList<>();
		boolean deleted = ((Number) data[2]).intValue() == 1;
		boolean finished = ((Number) data[3]).intValue() == 1;
		if (deleted) {
			return new BasicRes(ResMessage.WISH_ID_NOT_FOUND.getCode(), ResMessage.WISH_ID_NOT_FOUND.getMessage());
		}
		if (finished) {
			return new BasicRes(ResMessage.WISH_IS_COMPLETE.getCode(), ResMessage.WISH_IS_COMPLETE.getMessage());
		}

		try {
			int result = wishDao.delWish(id, userId);
			if (result <= 0) {
				return new BasicRes(ResMessage.WISH_DELETE_ERROR.getCode(), ResMessage.WISH_DELETE_ERROR.getMessage());
			}
			String wishTitle = wishDao.getWishTitle(id);
			if (!followersList.isEmpty()) {
				NotifiMesReq notifyReq = new NotifiMesReq();
				notifyReq.setCategory(NotifiCategoryEnum.WISH);
				notifyReq.setTitle("願望被刪除！！");
				notifyReq.setContent("願望的主人把「" + wishTitle + "」願望刪掉了，快去許願池找找相似的願望吧！");
				notifyReq.setTargetUrl("/user/wishes");
				notifyReq.setUserId(userId);
				notifyReq.setEventId(id);
				notifyReq.setExpiredAt(LocalDate.now().plusDays(30).toString());

				notifyReq.setUserNotificationVoList(followersList.stream().map(fid -> {
					UserNotificationVo vo = new UserNotificationVo();
					vo.setUserId(fid);
					User u = userDao.getUserById(fid);
					if (u != null) {
						vo.setEmail(u.getEmail());
					}
					return vo;
				}).collect(Collectors.toList()));

				messagesService.create(notifyReq);
			}
		} catch (Exception e) {
			throw e;
		}

		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	public void wishTimesReset() {
		wishDao.wishTimesReset(0, 499, 3);
		wishDao.wishTimesReset(500, 999, 5);
	}

	// 失效發送通知不刪除
	@Transactional(rollbackOn = Exception.class)
	public BasicRes wishOverThreeMonth() throws Exception {
		// 只收到超過3個月未刪除且未完成的
		List<Wishes> wishesData = wishDao.checkOverTime();
		if (wishesData.isEmpty()) {
			return null;
		}

		try {
			for (Wishes w : wishesData) {
				// 1. 通知許願者
				NotifiMesReq wisherReq = new NotifiMesReq();
				wisherReq.setCategory(NotifiCategoryEnum.WISH);
				wisherReq.setTitle("願望已超過3個月嘍！！");
				wisherReq.setContent("您的願望「" + w.getTitle() + "」已超過3個月且未成功開團，請考慮重新許願或修改資訊。");
				wisherReq.setTargetUrl("/user/wishes?tab=mine&filter=expired&wishId=" + w.getId());
				wisherReq.setUserId("SYSTEM"); // 系統自動發送
				wisherReq.setEventId(w.getId());
				wisherReq.setExpiredAt(LocalDate.now().plusDays(30).toString());

				UserNotificationVo wisherVo = new UserNotificationVo();
				wisherVo.setUserId(w.getUser_id());
				User wisher = userDao.getUserById(w.getUser_id());
				if (wisher != null) {
					wisherVo.setEmail(wisher.getEmail());
				}
				wisherReq.setUserNotificationVoList(Arrays.asList(wisherVo));
				messagesService.create(wisherReq);

				// 2. 通知跟願者
				if (StringUtils.hasText(w.getFollowers())) {
					List<String> followersList = Arrays.asList(w.getFollowers().split(","));
					NotifiMesReq followerReq = new NotifiMesReq();
					followerReq.setCategory(NotifiCategoryEnum.WISH);
					followerReq.setTitle("願望已超過3個月嘍！！");
					followerReq.setContent("您跟隨的願望「" + w.getTitle() + "」已過期且未成功開團，快去許願池找找有沒有相似的願望吧！");
					followerReq.setTargetUrl("/user/wishes?tab=followed&filter=expired&wishId=" + w.getId());
					followerReq.setUserId("SYSTEM");
					followerReq.setEventId(w.getId());
					followerReq.setExpiredAt(LocalDate.now().plusDays(30).toString());

					followerReq.setUserNotificationVoList(followersList.stream().map(fid -> {
						UserNotificationVo vo = new UserNotificationVo();
						vo.setUserId(fid);
						User u = userDao.getUserById(fid);
						if (u != null) {
							vo.setEmail(u.getEmail());
						}
						return vo;
					}).collect(Collectors.toList()));

					messagesService.create(followerReq);
				}
			}
			return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
		} catch (Exception e) {
			throw e;
		}
	}

	@Transactional(rollbackOn = Exception.class)
	public void realizeWishesByStoreName(String storeName, int storeId) throws Exception {
		List<Wishes> matches = wishDao.findActiveWishesByTitle(storeName);
		if (matches == null || matches.isEmpty()) {
			return;
		}

		for (Wishes w : matches) {
			// 1. 標記願望已實現
			w.setFinished(true);
			wishDao.save(w);

			// 2. 收集通知對象 (許願者 + 跟願者)
			Set<String> notifyUserIds = new HashSet<>();
			notifyUserIds.add(w.getUser_id());
			if (StringUtils.hasText(w.getFollowers())) {
				notifyUserIds.addAll(Arrays.asList(w.getFollowers().split(",")));
			}

			// 3. 發送通知
			try {
				NotifiMesReq notifyReq = new NotifiMesReq();
				notifyReq.setCategory(NotifiCategoryEnum.WISH);
				notifyReq.setTitle("願望實現了！！");
				notifyReq.setContent("您許願/跟隨的店家「" + storeName + "」已被管理員新增，快去看看吧！");
				notifyReq.setTargetUrl("/stores/store/" + storeId); // 導向新店家頁面
				notifyReq.setUserId("SYSTEM");
				notifyReq.setEventId(w.getId());
				notifyReq.setExpiredAt(LocalDate.now().plusDays(30).toString());

				notifyReq.setUserNotificationVoList(notifyUserIds.stream().map(uid -> {
					UserNotificationVo vo = new UserNotificationVo();
					vo.setUserId(uid);
					User u = userDao.getUserById(uid);
					if (u != null) {
						vo.setEmail(u.getEmail());
					}
					return vo;
				}).collect(Collectors.toList()));

				messagesService.create(notifyReq);
			} catch (Exception e) {
				System.err.println(
						"Failed to send wish realization notification for store " + storeName + ": " + e.getMessage());
			}
		}
	}
}
