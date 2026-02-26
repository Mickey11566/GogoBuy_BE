package com.example.demo.request;

import java.util.List;

public record LinePayReq(//
		Integer amount, // 總金額 (必填)
		String currency, // 幣別，台灣請填 "TWD" (必填)
		String orderId, // 你商店內部的訂單編號 (必填)
		List<PackageDto> packages, // 商品包裝清單 (必填)
		RedirectUrls redirectUrls // 付款後導回的網址 (必填)
) {
	// 內部類別 1: 包裝 (LINE Pay 規定至少要有一個 package)
	public record PackageDto(//
			String id, // 包裝 ID (例如：團購編號)
			Integer amount, // 該包裝的總金額
			String name, // 包裝名稱 (會顯示在 LINE Pay 畫面)
			List<ProductDto> products // 詳細商品清單
	) {
	}
	// 內部類別 2: 商品細項
	public record ProductDto(//
			String name, // 商品名稱
			Integer quantity, // 數量
			Integer price // 單價
	) {
	}

	// 內部類別 3: 跳轉網址
	public record RedirectUrls(//
			String confirmUrl, // 用戶付完錢後，瀏覽器要導向你後端的哪裡？
			String cancelUrl // 用戶取消付款後，要導向哪裡？
	) {
	}
}
