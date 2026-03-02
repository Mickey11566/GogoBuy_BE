package com.example.demo.service;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

@Service
public class QRCodeService {

	

    public BufferedImage generateGroupBuyQRCode(int eventId) throws Exception {
    	// 基礎網址(前端網址)
        String turnUrl = "http://localhost:4200/groupbuy-event/group-follow/";
        // 自動組合網址
        String fullUrl = turnUrl + eventId;
        
        // 呼叫原本寫好的產生邏輯
        return generateQRCode(fullUrl); 
    }
	
	/**
     * 將網址字串轉換為 BufferedImage (圖片物件)
     * @param url 目標網址
     * @return 產出的 QR Code 圖片
     * @throws WriterException 當編碼發生錯誤時
     */
    public BufferedImage generateQRCode(String url) throws WriterException {
        // 1. QRCodeWriter 是 ZXing 庫的大腦是產生矩陣的核心
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        // 2. 設定參數 (Hints)
        /* 用HashMap是因為很多選填的內容，如果你什麼都不設，它會用預設值。*/
        Map<EncodeHintType, Object> hints = new HashMap<>();
        // CHARACTER_SET : 設定編碼，防止網址有特殊字元時出錯
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // ERROR_CORRECTION : 這是最重要的參數。QR Code 內建了「冗餘資料」。
        // 等級 H 代表即使 QR Code 有 30% 的面積被遮擋或損壞，手機依然能算回原始網址。這對於需要「快速掃描」的網址非常重要。
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 設定邊框寬度 (預設為 4，設為 1 可以讓圖片看起來更緊湊)
        hints.put(EncodeHintType.MARGIN, 1);

        // 3. 建立位元矩陣 (BitMatrix)
        // 參數分別為：內容, 格式, 寬度, 高度, 設定(網址文字、格式類型（QR_CODE）、目標尺寸)
        // 目前是一個 0 與 1 的二維陣列。在記憶體中，它就像一張棋盤，標記著哪裡該是黑色（1），哪裡該是白色（0）。
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 300, 300, hints);

        // 4. 使用 JavaSE 擴充庫的工具將矩陣渲染為圖片(BufferedImage)
        // 這樣後續 Controller 才能方便地轉換成串流傳給瀏覽器
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
