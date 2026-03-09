package com.example.demo.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/*關於多併發
 * 現在在locolHost測試只會是http://localhost(http1)
 * 會有連線數6的限制
 * 但如果實裝在http2(https)就可以不用考慮此問題
 * */
@Service
public class NotificationService {

	@Autowired
	private SystemNotificationService systemNotificationService;

	private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	// 1. 用戶訂閱 (建立連線)
	public SseEmitter subscribe(String userId) {
		if (emitters.containsKey(userId)) {
			try {
				// complete() 會觸發舊連線的終止邏輯
				emitters.get(userId).complete();
			} catch (Exception e) {
				// 忽略關閉舊連線的錯誤
			}
			emitters.remove(userId);
		}

		// 設定超時時間，30分鐘: 1800000L
		SseEmitter emitter = new SseEmitter(1_800_000L);

		// 存入 Map
		emitters.put(userId, emitter);

		// 連線結束或超時時，從 Map 中移除
		emitter.onCompletion(() -> {
			System.out.println("[SSE] Connection completed for user: " + userId);
			emitters.remove(userId);
		});
		emitter.onTimeout(() -> {
			System.out.println("[SSE] Connection timeout for user: " + userId);
			emitters.remove(userId);
		});
		emitter.onError((e) -> {
			System.err.println("[SSE] Connection error for user: " + userId + ", error: " + e.getMessage());
			emitters.remove(userId);
		});

		try {
			// 發送一個初始事件，確認連線成功
			emitter.send(SseEmitter.event().name("INIT").data("Connected successfully").reconnectTime(5000)); // 建議客戶端 5
																												// 秒後重連

			System.out.println("[SSE] User subscribed: " + userId + ". Current active connections: " + emitters.size());

			// 檢查>>如果有 發送系統公告訊息
			systemNotificationService.getValidNotice().ifPresent(notice -> {
				try {
					emitter.send(SseEmitter.event().name("SYSTEM_NOTICE").data(notice));
				} catch (IOException e) {
					// 靜默處理
				}
			});
		} catch (IOException e) {
			emitters.remove(userId);
		}
		return emitter;
	}

	// 2. 發送通知給特定用戶
	public void sendNotification(String userId, String message) {
		SseEmitter emitter = emitters.get(userId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event().name("message").data(message));
				System.out.println("[SSE] Notification sent to user: " + userId);
			} catch (IOException e) {
				System.err.println("[SSE] Failed to send notification to user: " + userId + ", removing emitter.");
				emitters.remove(userId);
			}
		} else {
			System.out.println("[SSE] User " + userId + " not online, notification skipped.");
		}
	}

	// 在線廣播
	public void broadcast(String message) {
		System.out.println("[SSE] Broadcasting message to " + emitters.size() + " users.");
		emitters.forEach((userId, emitter) -> {
			try {
				emitter.send(SseEmitter.event().name("SYSTEM_NOTICE").data(message));
			} catch (IOException e) {
				emitters.remove(userId);
			}
		});
	}

	// 3. 心跳機制 (每 25 秒發送一次，防止被 Nginx/Proxy 斷線)
	@Scheduled(fixedRate = 25000)
	public void sendHeartbeat() {
		if (emitters.isEmpty())
			return;

		emitters.forEach((userId, emitter) -> {
			try {
				emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
			} catch (IOException e) {
				emitters.remove(userId);
			}
		});
	}
}
