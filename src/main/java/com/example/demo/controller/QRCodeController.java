package com.example.demo.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.QRCodeService;

@CrossOrigin
@RestController
public class QRCodeController {

	@Autowired
    private QRCodeService qrCodeService;

    // 告訴瀏覽器我回傳的是 PNG 圖片，不是普通文字
    @GetMapping(value = "/event/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    // byte[] : 這是圖片在網路傳輸的最原始格式，相容性最高，前端直接用 <img> 標籤就能看。
    public byte[] getQRCode(@RequestParam("eventId") int eventId) throws Exception {
        
        // 向 Service 拿畫好的圖片
        BufferedImage image = qrCodeService.generateGroupBuyQRCode(eventId);
        
        // 轉換格式：將圖片物件轉成「二進位數據 (byte[])」
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos); // 把圖「壓」進輸出流
            return baos.toByteArray();         // 最終送出這串數據
        }
    }
}
