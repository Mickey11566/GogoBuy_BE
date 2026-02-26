package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.constants.ResMessage;
import com.example.demo.dto.UserAccountDto;
import com.example.demo.dto.UserInfoDto;
import com.example.demo.dto.UserPasswordDto;
import com.example.demo.request.ResetPasswordReq;
import com.example.demo.request.UserAddReq;
import com.example.demo.request.UserLoginReq;
import com.example.demo.response.BasicRes;
import com.example.demo.response.GetUserInfoListRes;
import com.example.demo.response.LoginRes;
import com.example.demo.service.GoogleOAuth2Service;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@CrossOrigin
@RestController
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private GoogleOAuth2Service googleOAuth2Service;

	// http://localhost:8080/oauth2/authorization/google 授權網址
	// 拿Google資料
	@GetMapping("gogobuy/user/oauth")
	public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User principal) {
		return googleOAuth2Service.loginGoogle(principal);
	}

	/*
	 * 註冊
	 */
	@PostMapping("gogobuy/user/registration")
	public BasicRes create(@Valid @RequestBody UserAddReq req) throws Exception {
		return userService.addUser(req);
	}

	/*
	 * 登入
	 */
	@PostMapping("gogobuy/user/login")
	public BasicRes login(@Valid @RequestBody UserLoginReq req, HttpSession session) throws Exception {
		BasicRes res = userService.login(req);

		if (res.getCode() == 200 && res instanceof LoginRes) {
			LoginRes loginRes = (LoginRes) res;
			// 存入 UUID (給攔截器檢查狀態用)
			session.setAttribute("currentUserId", loginRes.getId());

			// setting session's attribute when login success
			session.setAttribute("account", req.getEmail());
		}
		return res;
	}

	/*
	 * 取得用戶資料
	 */
	@GetMapping("gogobuy/user/get-user")
	public BasicRes getUser(@RequestParam("id") String id) {
		return userService.getUser(id);
	}

	@GetMapping("gogobuy/user/get-all-user")
	public GetUserInfoListRes getAllUser() {
		return userService.getAllUser();
	}

	/*
	 * 修改大頭貼、暱稱、載具
	 */
	@PatchMapping("gogobuy/user/change-profile")
	public BasicRes updateInfo(@RequestBody UserInfoDto dto, @RequestParam("id") String id) {
		return userService.updateInfo(dto, id);
	}

	/*
	 * 會員中心修改密碼
	 */
	@PostMapping("gogobuy/user/change-password")
	public BasicRes changePassword(@RequestBody UserPasswordDto dto, @RequestParam("id") String id) {
		return userService.updatePassword(id, dto);
	}

	/*
	 * 忘記密碼重設密碼
	 */
	@PutMapping("gogobuy/user/reset-password")
	public BasicRes resetPassword(@RequestBody ResetPasswordReq req) {
		return userService.resetPassword(req);
	}

	/*
	 * 串接電話
	 */
	@PostMapping("gogobuy/user/connect-phone")
	public BasicRes userPhone(@RequestBody String phone, @RequestParam("id") String id) {
		return userService.userPhone(phone, id);
	}

	/*
	 * 登出
	 */
	@PostMapping("gogobuy/user/logout")
	public BasicRes logout(HttpSession session) {
		// 讓session 失效
		// 一個 session 的有效時間為 30 分
		// 在有效時間內做任何操作，都會再延長30分
		session.invalidate();
		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

	public record SendOtpRequest(String email) {
	}

	/*
	 * 根據Id發送OTP驗證碼
	 */
	@PostMapping("gogobuy/user/send-otp")
	public BasicRes sendOtpWithoutRedis(@RequestBody SendOtpRequest req, @RequestParam("id") String id) {
		return userService.sendOtpById(req.email(), id);
	}

	/*
	 * 驗證OTP碼並更改email
	 */
	@PutMapping("gogobuy/user/email-verify")
	public BasicRes verifyAndUpdateEmail(@RequestBody UserAccountDto dto, @RequestParam("id") String id) {
		return userService.verifyAndUpdateEmail(dto.getNewEmail(), dto.getOtpCode(), id);
	}

	/*
	 * 根據email發送OTP驗證碼
	 */
	@PostMapping("gogobuy/user/send-otp-email")
	public BasicRes sendOtpByEmail(@RequestBody String email) {
		return userService.sendOtpByEmail(email);
	}

	/*
	 * 點擊認證驗證信
	 */
	@GetMapping("gogobuy/user/active-account")
	public BasicRes activeAccount(@RequestParam("token") String token) {
		// 調用 service 檢查 JWT 並更新狀態
		boolean success = userService.activateUser(token);

		if (success) {
			return new BasicRes(ResMessage.SUCCESS.getCode(), "帳號開通成功");
		} else {
			return new BasicRes(ResMessage.VERIFICATION_ERROR.getCode(), ResMessage.VERIFICATION_ERROR.getMessage());
		}
	}

	/*
	 * 重新發送開通驗證信
	 */
	@GetMapping("gogobuy/user/resend-active-account")
	public BasicRes resendActiveAccount(@RequestParam("email") String email) {
		return userService.resendActivationEmail(email);
	}

	/*
	 * 用戶點擊「停用帳戶」
	 */
	@PostMapping("gogobuy/user/suspend")
	public BasicRes suspend(@RequestParam("id") String userId) {
		return userService.selfSuspend(userId);

	}

	/*
	 * 停權用戶 API
	 */
	@PostMapping("gogobuy/ban-user")
	public BasicRes banUser(@RequestParam("id") String id) {
		return userService.adminBan(id);

	}

	/*
	 * 恢復用戶帳號 API
	 */
	@PostMapping("gogobuy/active-user")
	public BasicRes activeUser(@RequestParam("id") String id) {
		return userService.activeUserAdmin(id);
	}
	
	/*
	 * 更新最愛店家
	 *(傳最終結果陣列就好 不論是新增修改刪除)
	 */
	@PostMapping("gogobuy/updateFavoriteStore")
	public BasicRes updateFavoriteStore(@RequestParam("id") String id,@RequestParam(value = "storesList",required = false) List<Integer> storesList) {
		return userService.updateFavoriteStores(id, storesList);
	}
	
//	/*
//	 *查詢最愛店家 
//	 */
//	@GetMapping("gogobuy/getFavoriteStore")
//	public FavoriteRes getFavoriteStore(@RequestParam("id") String id) {
//	return userService.getFavoriteStores(id);
//	}
}
