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
		List<Wishes> wishesData=wishDao.allWish();
		List<WishVo> returnData=new ArrayList<>();
		for(Wishes w:wishesData) {
			WishVo vo=new WishVo();
			vo.setId(w.getId());
			vo.setUser_id(w.getUser_id());
			vo.setTitle(w.getTitle());
			vo.setType(w.getType());
			vo.setBuildDate(w.getBuildDate());
			vo.setLocation(w.getLocation());
			vo.setFinished(w.isFinished());
			if(!w.isAnonymous()) {
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
			wishDao.addWish(req.getUserId(), req.getTitle(), req.isAnonymous(), null, false, req.getType().name(), req.getLocation(), false);
			times=times-1;
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
		Object[] data=wishData.get(0);
		String wishUser=data[0].toString();
		String followersStr=(data[1] != null) ? data[1].toString() : null;
		boolean deleted=((Number)data[2]).intValue()==1;
		boolean finished=((Number)data[3]).intValue()==1;
		if (deleted) {
	        return new BasicRes(ResMessage.WISH_ID_NOT_FOUND.getCode(), ResMessage.WISH_ID_NOT_FOUND.getMessage());
	    }
		if (finished) {
			return new BasicRes(ResMessage.WISH_IS_FINISHED.getCode(), ResMessage.WISH_IS_FINISHED.getMessage());
		}
		//許願者不可跟願
		if(userId.equals(wishUser)) {
			return new BasicRes(ResMessage.WISH_USER_CAN_NOT_FOLLOW.getCode(), ResMessage.WISH_USER_CAN_NOT_FOLLOW.getMessage());
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
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}
	
	@Transactional(rollbackOn = Exception.class)
	public BasicRes finishWish(int id, String userId, String targetUrl) throws Exception{
		final String userIdPattern="^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
		if(!userId.matches(userIdPattern)) {
			return new BasicRes(ResMessage.USER_NOT_FOUND.getCode(), ResMessage.USER_NOT_FOUND.getMessage());
		}
		// 判斷願望存在
		List<Object[]> wishData=wishDao.getfollowers(id);
		if (wishData.isEmpty()) {
	        return new BasicRes(ResMessage.WISH_ID_NOT_FOUND.getCode(), ResMessage.WISH_ID_NOT_FOUND.getMessage());
	    }
		Object[] data=wishData.get(0);
		String wishUser=data[0].toString();
		List<String> wishersList = (data[1] != null)  //通知用
			    ? new ArrayList<>(Arrays.asList(((String) data[1]).split(","))) 
			    : new ArrayList<>();
		boolean deleted=((Number)data[2]).intValue()==1;
		boolean finished=((Number)data[3]).intValue()==1;
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
		} catch(Exception e){
			throw e;
		}
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}
	
	@Transactional(rollbackOn = Exception.class)
	public BasicRes delWish(int id, String userId) throws Exception {
		// 判斷願望存在
		List<Object[]> wishData=wishDao.getfollowers(id);
		if (wishData.isEmpty()) {
	        return new BasicRes(ResMessage.WISH_ID_NOT_FOUND.getCode(), ResMessage.WISH_ID_NOT_FOUND.getMessage());
	    }
		Object[] data=wishData.get(0);
		String wishUser=data[0].toString();
		List<String> followersList = (data[1] != null) 
			    ? new ArrayList<>(Arrays.asList(((String) data[1]).split(","))) 
			    : new ArrayList<>();
		boolean deleted=((Number)data[2]).intValue()==1;
		boolean finished=((Number)data[3]).intValue()==1;
		if (deleted) {
	        return new BasicRes(ResMessage.WISH_ID_NOT_FOUND.getCode(), ResMessage.WISH_ID_NOT_FOUND.getMessage());
	    }
		if (finished) {
			return new BasicRes(ResMessage.WISH_IS_COMPLETE.getCode(), ResMessage.WISH_IS_COMPLETE.getMessage());
		}
		
		try {
			int result=wishDao.delWish(id, userId);
			if(result<=0) {
				return new BasicRes(ResMessage.WISH_DELETE_ERROR.getCode(), ResMessage.WISH_DELETE_ERROR.getMessage());
			}
			String wishTitle=wishDao.getWishTitle(id);
			if(followersList.size()>0) {
				NotifiCategoryEnum wish=NotifiCategoryEnum.WISH;
				NotifiMes msg = new NotifiMes();
				msg.setCategory(wish);
				msg.setTitle("願望被刪除!!");
				msg.setContent("願望的主人把"+wishTitle+"願望刪掉了，快去許願池找找相似的願望吧!");
				msg.setUserId(userId);
				msg.setEventId(id);
				notifiMsgRepository.save(msg);  // 新增訊息
				wishDao.addRecipientsBatch(msg.getId(), followersList);
			}
		}catch(Exception e) {
			throw e;
		}

		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	public void wishTimesReset() {
		wishDao.wishTimesReset(0, 499, 3);
		wishDao.wishTimesReset(500, 999, 5);
	}
	
//	失效發送通知不刪除
	@Transactional(rollbackOn = Exception.class)
	public BasicRes wishOverThreeMonth() throws Exception{
		// 只收到超過3個月未刪除且未完成的
		List<Wishes> wishesData=wishDao.checkOverTime();
		if(wishesData.size()<=0) {
			return null;
		}

		try {
			NotifiCategoryEnum wish=NotifiCategoryEnum.WISH;
			for(Wishes w:wishesData) {
				// 新增許願者訊息
				NotifiMes userMsg = new NotifiMes();
				userMsg.setCategory(wish);
				userMsg.setTitle("願望已超過3個月嘍!!");
				userMsg.setTargetUrl("/user/wishes?tab=mine&filter=expired&wishId="+w.getId());
				userMsg.setEventId(w.getId());
				userMsg.setUserId(w.getUser_id());
				userMsg.setContent("這個願望已超過3個月，願望未成功開團，請重新許願");
				notifiMsgRepository.save(userMsg);
				wishDao.addRecipientsBatch(userMsg.getId(),Arrays.asList(w.getUser_id()));
				// 新增跟願者訊息
				if(w.getFollowers()!=null) {
					List<String> wishersList=new ArrayList<>();
					wishersList.addAll(Arrays.asList(w.getFollowers().split(",")));
					NotifiMes follwersMsg = new NotifiMes();
					follwersMsg.setCategory(wish);
					follwersMsg.setTitle("願望已超過3個月嘍!!");
					follwersMsg.setTargetUrl("/user/wishes?tab=followed&filter=expired&wishId="+w.getId());
					follwersMsg.setEventId(w.getId());
					follwersMsg.setContent("這個願望已過期，願望未成功開團，快去許願池找找有沒有相似的願望吧!");
					notifiMsgRepository.save(follwersMsg);
					wishDao.addRecipientsBatch(follwersMsg.getId(), wishersList);
				}
			}
			return new BasicRes(ResMessage.SUCCESS.getCode(), //
					ResMessage.SUCCESS.getMessage());
		} catch (Exception e) {
			throw e;
		}
	}
}
