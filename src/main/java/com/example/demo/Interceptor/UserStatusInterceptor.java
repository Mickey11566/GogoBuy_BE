package com.example.demo.Interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.demo.dao.UserDao;
import com.example.demo.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class UserStatusInterceptor implements HandlerInterceptor {

	@Autowired
	private UserDao userDao;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		try {
			// 1. 從 Session 取得當前用戶 ID
			HttpSession session = request.getSession(false);
			if (session == null || session.getAttribute("currentUserId") == null) {
				// 未登入，放行 (由 Controller 或其他機制處理驗證)
				return true;
			}

			String userId = (String) session.getAttribute("currentUserId");

			if (userDao == null) {
				System.err.println("CRITICAL: userDao is null in Interceptor!");
				throw new RuntimeException("userDao is null - Autowiring failed");
			}

			// 2. 查詢此用戶最新狀態
			User user = userDao.getUserById(userId);
			if (user == null) {
				// 用戶不存在 (可能被刪除)，強制登出
				session.invalidate();
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
				return false;
			}

			String status = user.getStatus();

			// 3. 檢查狀態
			// BANNED: 停權 -> 403
			if ("banned".equalsIgnoreCase(status)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.setContentType("application/json;charset=UTF-8");
				response.getWriter().write("{\"code\": 403, \"message\": \"Your account has been banned.\"}");
				session.invalidate(); // 強制登出
				return false;
			}

			// PENDING_ACTIVE: 未開通 -> 403
			if ("pending_active".equalsIgnoreCase(status)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.setContentType("application/json;charset=UTF-8");
				response.getWriter().write("{\"code\": 403, \"message\": \"Account not activated.\"}");
				return false;
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace(); // Log to console
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{\"code\": 500, \"message\": \"Interceptor Error: " + e.getMessage() + "\"}");
			return false;
		}
	}
}
