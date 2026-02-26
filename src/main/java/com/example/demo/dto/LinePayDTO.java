package com.example.demo.dto;

/* 用 record 的原因 (要用 class 也可以)
 * 封裝性：LINE Pay V3 的 JSON 結構比較深（巢狀結構），用 record 嵌套可以完美對應其 JSON 格式。
 * 自動轉換：Spring Boot 內建的 Jackson 會自動把這些 record 轉成 LINE Pay 看得懂的 JSON。*/
/* 
 * 選 class 的時機：你正在使用 Lombok。你之後需要繼承。你需要物件的屬性是可以被修改的。
 * 選 record 的時機：你只想簡單、快速地定義一個「純粹用來傳輸資料」的結構。程式碼看起來會非常乾淨。*/
public record LinePayDTO(//
		String returnCode, // 回傳碼 (成功為 "0000")
		String returnMessage, // 回傳訊息
		PaymentInfo info // 交易資訊
) {
	
	public record PaymentInfo(//
			PaymentUrl paymentUrl, Long transactionId // LINE Pay 產生的交易序號 (重要！請款用)
	) {
	}

	public record PaymentUrl(//
			String web, // 這是前端要用來跳轉的網址
			String app // 開啟 LINE App 的連結
	) {
	}
}