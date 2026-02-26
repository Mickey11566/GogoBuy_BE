package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.dto.LinePayDTO;
import com.example.demo.request.LinePayReq;
import com.example.demo.request.LinePayReq.PackageDto;
import com.example.demo.request.LinePayReq.ProductDto;
import com.example.demo.request.LinePayReq.RedirectUrls;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LinePayService {
    
    @Value("${linepay.channel.secret}")
    private String channelSecret;        // 查詢後給個 商家密鑰，用於加密簽章(絕不能外洩)

    @Value("${linepay.channel.id}")
    private String channelId;   			// 註冊時的商家ID

    private final WebClient webClient;          		// Spring 的異步 HTTP 客戶端，負責噴 API
    																	// 異步 : 不用原地等結果，先去忙別的事
    
    private final ObjectMapper objectMapper;

    // 建構子注入 : 這裡用了 Spring 推薦的注入方式，確保 Service 啟動時 WebClient 和 ObjectMapper 已經準備好。
    public LinePayService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://sandbox-api-pay.line.me").build();
        this.objectMapper = objectMapper;
    }

    //預約支付
    public LinePayDTO reservePayment(int amount, String orderName, String orderId) throws Exception {
      
    	//組裝資料：它把前端傳來的 amount (金額) 和 orderName (品名) 包裝成 LINE Pay 指定的 LinePayReq 格式。
        ProductDto product = new ProductDto(orderName, 1, amount);
        /* "PKG_" + UUID.randomUUID() : (包裹唯一編號)，為什麼這樣寫：LINE Pay 要求每個 packageId 在該次請求中必須是唯一的。
       * 用 UUID.randomUUID() 隨機產生一串亂碼，確保不會重複。*/
        PackageDto pkg = new PackageDto("PKG_" + UUID.randomUUID(), amount, orderName, List.of(product));
        // List.of 它的意思就是：「快速建立一個包含 product 物件的 ArrayList 集合」。
        
        // 注意：localhost 的 confirmUrl 只能由你的瀏覽器觸發，LINE 伺服器連不到它
       // 跳轉網址 (RedirectUrls)：這告訴 LINE Pay 付完錢後要導向哪裡。
        RedirectUrls urls = new RedirectUrls(
        		"http://localhost:8080/api/payments/linepay/confirm?amount=" + amount, 
        	    "http://localhost:8080/api/payments/linepay/cancel"
        );

        LinePayReq request = new LinePayReq(
        		// 這裡用 System.currentTimeMillis() 確保每次測試的訂單編號都不重複。
            amount, "TWD", "ORDER_" + System.currentTimeMillis(), List.of(pkg), urls
        );

        return this.sendRequest(request);
    }

    //把打包好的資料傳送到 LinePay 
    public LinePayDTO sendRequest(LinePayReq requestBody) throws Exception {
        String uri = "/v3/payments/request";
        String bodyJson = objectMapper.writeValueAsString(requestBody);
        
        // 這裡會去呼叫下面的 createHeaders
        HttpHeaders headers = createHeaders(uri, bodyJson);

        return webClient.post()
                .uri(uri)
                .headers(h -> h.addAll(headers))
                .bodyValue(bodyJson)
                .retrieve()
                .bodyToMono(LinePayDTO.class)
                .block(); 
    }

    // 給LinePay的通關證件
    public HttpHeaders createHeaders(String uri, String bodyJson) throws Exception {
    	// 產生一個隨機號碼
        String nonce = UUID.randomUUID().toString();
        // 生成無法仿冒的防偽印章公式：Secret + URI + Body + Nonce
        String signature = encrypt(channelSecret, channelSecret + uri + bodyJson + nonce);

        // 打包進 headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-LINE-ChannelId", channelId);
        headers.set("X-LINE-Authorization-Nonce", nonce);
        headers.set("X-LINE-Authorization", signature);
        return headers;
    }

    private String encrypt(String key, String data) throws Exception {
    	/* HMAC-SHA256 加密運算 : 它是一種「單向」加密，「把你的密碼和資料丟進去，攪碎成一串誰都看不懂、也無法反推回來的亂碼。」 
    	* 這樣就算別人在網路上截獲這串亂碼，也沒辦法知道你的原始密碼。
    	* */
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        // 設定 key
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        // 會輸出一串二進位的「數位指紋」（byte 陣列）。這串東西目前還是電腦看的格式，人類看不懂。
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        // 輸出結果是二進位，這行代碼把它轉成我們在網路上常見的 Base64 字串。
        // Base64 : 將「二進位資料」（電腦看的 0 與 1）轉換成純文字。
        return Base64.getEncoder().encodeToString(hash);
    }
    
    // 授權支付
    public LinePayDTO confirmPayment(String transactionId , int amount) throws Exception {
    	// 找對的URL
        String uri = "/v3/payments/" + transactionId + "/confirm";
        
        // 組裝 Confirm 所需的 Body (確認金額)
        Map<String, Object> bodyMap = Map.of(
            "amount", amount,
            "currency", "TWD"
        );
        String bodyJson = objectMapper.writeValueAsString(bodyMap);
        
        HttpHeaders headers = createHeaders(uri, bodyJson);

        //蓋章並發送 (蓋上防偽印章（Header），然後把這封「請款信」寄出去)。
        return webClient.post()
                .uri(uri)
                .headers(h -> h.addAll(headers))
                .bodyValue(bodyJson)
                .retrieve()
                .bodyToMono(LinePayDTO.class)
                .block();
    }
    
}