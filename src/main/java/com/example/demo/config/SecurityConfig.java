package com.example.demo.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.dao.UserDao;
import com.example.demo.entity.User;
import com.example.demo.service.GoogleOAuth2Service;

// @Configuration 告訴 Spring 這是一個配置類別，程式啟動時要先讀取這裡的設定
@Configuration
@EnableWebSecurity
// 定義誰可以進入你的網站以及如何驗證身分。
public class SecurityConfig {

	@Autowired
	private GoogleOAuth2Service googleOAuth2;

	@Autowired
	private UserDao userDao;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:4200", "https://*.ts.net:*", // 允許所有
																													// Tailscale
																													// 網域與任何連接埠
				"https://gogobuy.netlify.app/", // 你的 Render 前端網址
				frontendUrl // 保留原本從 properties 讀取的設定
		)); // Angular 端口
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true); // 允許攜帶 Cookie (JSESSIONID)

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				/*
				 * 跨來源資源共享 是一種瀏覽器機制，透過特定的 HTTP 標頭， 允許一個網域的網頁程式（如 JavaScript）安全地向不同網域的伺服器請求資源。
				 */
				.csrf(csrf -> csrf.disable()) // 關閉 CSRF，讓前端可以用 POST 傳資料
				.cors(cors -> cors.configurationSource(corsConfigurationSource())) // 開啟 CORS，允許 Angular 跨網域連線

				// 1. 授權區塊
				.authorizeHttpRequests(auth -> auth
						// 繞過免google授權的
						.requestMatchers("/", "/**", "/oauth2/**", "/login/**", "/swagger-ui/**", "/gogobuy/user/oauth",
								"/oauth2/**")
						.permitAll())
				// 2. OAuth2 登入配置區塊
				.oauth2Login(oauth2 -> {
					// 交給 googelOAuth2 處理
					oauth2.userInfoEndpoint(userInfo -> userInfo.userService(googleOAuth2));

					// 登入成功後的處理: 設定 Session 並跳轉
					oauth2.successHandler((request, response, authentication) -> {
						OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
						String email = oauthUser.getAttribute("email");
						User user = userDao.getUserByEmail(email);

						if (user != null) {
							// 設定 currentUserId 以便攔截器檢查狀態
							request.getSession().setAttribute("currentUserId", user.getId());
							request.getSession().setAttribute("account", email);
						}

						// 動態獲取來源。如果前端有傳入 Referer 或特定 Header，可以從那裡抓
						// 或者最簡單的方法：根據目前 Request 的 Host 來判斷要跳轉去哪裡
						String origin = request.getHeader("Referer");
						String redirectBase = (origin != null && !origin.isEmpty()) ? origin.split("/#/")[0]
								: frontendUrl;

						// 如果想要更精確，可以判斷 request.getServerName() 是否包含 .ts.net
						if (request.getServerName().endsWith(".ts.net")) {
							redirectBase = "https://" + request.getServerName() + ":8443"; // 假設前端掛在 8443
						}

						// 確保最後路徑正確 (避免重複的 /)
						if (redirectBase.endsWith("/")) {
							redirectBase = redirectBase.substring(0, redirectBase.length() - 1);
						}

						response.sendRedirect(frontendUrl + "/auth-callback");
					});

					// 登入失敗處理：將錯誤訊息編碼後傳回前端
					oauth2.failureHandler((request, response, exception) -> {
						String errorMessage = "Login failed";

						// 嘗試從 OAuth2AuthenticationException 取得詳細描述
						if (exception instanceof OAuth2AuthenticationException) {
							OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
							if (error != null && error.getDescription() != null) {
								errorMessage = error.getDescription();
							}
						} else if (exception != null && exception.getMessage() != null) {
							// 其他類型的例外
							errorMessage = exception.getMessage();
						}

						System.out.println("OAuth2 Login Failure: " + errorMessage); // Debug logging

						// 特殊處理：如果是「新帳號建立」的例外，導向到驗證提示而非錯誤
						if (errorMessage.contains("Account created")) {
							String encodedMsg = URLEncoder.encode("註冊成功，已發送驗證信至您的信箱", StandardCharsets.UTF_8);
							response.sendRedirect(
									frontendUrl + "/gogobuy/login?verificationSent=true&message=" + encodedMsg);
						} else {
							String encodedMsg = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
							response.sendRedirect(frontendUrl + "/gogobuy/login?errorMsg=" + encodedMsg);
						}
					});
				})

				// 3. 登出區塊
				.logout(logout -> {
					// 登出成功後同樣跳回前端首頁
					logout.logoutSuccessUrl(frontendUrl);
					// 順便清除 Session 和 Cookie，確保登出乾淨
					// JSESSIONID : Java Web 容器在使用者第一次訪問時自動產生的 Cookie
					logout.invalidateHttpSession(true);
					logout.deleteCookies("JSESSIONID");
				});
		return http.build();
	}
}