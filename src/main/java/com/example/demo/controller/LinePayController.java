package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.GroupsSearchViewDao;
import com.example.demo.dao.PersonalOrderDao;
import com.example.demo.dto.LinePayDTO;
import com.example.demo.entity.GroupsSearchView;
import com.example.demo.entity.PersonalOrder;
import com.example.demo.service.LinePayService;

@RestController
@RequestMapping("/api/payments/linepay")
public class LinePayController {

    private final LinePayService linePayService;
    
    
    @Autowired 
    private GroupsSearchViewDao groupsSearchViewDao;
    @Autowired
    private PersonalOrderDao personalOrderDao;

    public LinePayController(LinePayService linePayService) {
        this.linePayService = linePayService;
    }

//    @PostMapping("/request/pay")
//    public ResponseEntity<?> reserve(@RequestParam("eventId") int eventId,
//    													@RequestParam("userId") String userId) {
//        try {
//            //  從資料庫抓取該團資料
//        	GroupsSearchView group = groupsSearchViewDao.findById(eventId);
//            if (group == null) {
//                return ResponseEntity.badRequest().body("找不到"+ userId + "在" + eventId + " 的活動團");
//            }
//            PersonalOrder personal = personalOrderDao.findByEventsId(eventId , userId) ;
//            if(personal == null) {
//            	 return ResponseEntity.badRequest().body("找不到"+ userId + "在" + eventId + " 的結單");
//            }
//
//            // 準備支付資訊
//            int amount = personal.getTotalSum();
//            String productName =group.getStoreName();
//            
//            // 生成唯一的訂單編號 (建議格式：時間戳記 + 活動ID)
//            String myOrderId = "ORDER_" + System.currentTimeMillis() + "_" + eventId;
//
//            // 呼叫 Service 進行 LINE Pay 預約 (傳入金額、品名、訂單號)
//            LinePayDTO result = linePayService.reservePayment(amount, productName, myOrderId);
//            
//            // 判斷 LINE Pay 回傳結果
//            if ("0000".equals(result.returnCode())) {
//            	int upPayment = personalOrderDao.updatePaymentStatusAndPaymentTime(eventId, userId);
//                
//                if (upPayment > 0) {
//                	  // 回傳支付連結給前端 (讓使用者跳轉到 LINE Pay 畫面)
//                    return ResponseEntity.ok(result.info().paymentUrl().web());
//                } else {
//                    return ResponseEntity.ok("付款成功，但找不到對應訂單進行更新。");
//                }
//
//            } else {
//                return ResponseEntity.badRequest().body("LINE Pay 預約失敗：" + result.returnMessage());
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body("系統錯誤：" + e.getMessage());
//        }
//    }
    
    @PostMapping("/request/pay")
    public ResponseEntity<?> reserve(
            @RequestParam("eventId") int eventId,
            @RequestParam(value = "userId", required = false) String userId) {
        try {
            //  從資料庫抓取該團資料
            GroupsSearchView group = groupsSearchViewDao.findById(eventId);
            if (group == null) {
                return ResponseEntity.badRequest().body("找不到" + eventId + " 的活動團");
            }

            int amount = 0;
            String productName = group.getStoreName();

            if (userId != null && !userId.isEmpty()) {
                // 付個人結單金額
                PersonalOrder personal = personalOrderDao.findByEventsId(eventId, userId);
                if (personal == null) {
                    return ResponseEntity.badRequest().body("找不到使用者 " + userId + " 在團購 " + eventId + " 的訂單資料");
                }
                amount = personal.getTotalSum();
                productName += " - 個人訂單";
            } else {
                // 付整團總額
                amount = group.getTotalOrderAmount();
                productName += " - 全團總額";
            }
            
            if (amount <= 0) {
                return ResponseEntity.badRequest().body("支付金額必須大於 0");
            }

            // 生成唯一的訂單編號 (建議格式：時間戳記 + 活動ID)
            String myOrderId = "ORDER_" + System.currentTimeMillis() + "_" + eventId;

            // 呼叫 Service 進行 LINE Pay 預約 (傳入金額、品名、訂單號)
            LinePayDTO result = linePayService.reservePayment(amount, productName, myOrderId);
            
            // 判斷 LINE Pay 回傳結果
            if ("0000".equals(result.returnCode())) {
                return ResponseEntity.ok(result.info().paymentUrl().web());
            }
            return ResponseEntity.badRequest().body("LINE Pay 預約失敗：" + result.returnMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("系統錯誤：" + e.getMessage());
        }
    }
    
 // 注意：LINE Pay 預設是用 GET 跳轉回你的網站
    @GetMapping("/confirm")
    public ResponseEntity<?> confirmPayment(
        @RequestParam("transactionId") String transactionId,
        @RequestParam("amount") int amount
    ) {
        try {
            // 呼叫 Service 去跟 LINE Pay 做最後的確認扣款
            LinePayDTO result = linePayService.confirmPayment(transactionId, amount);
            
            if ("0000".equals(result.returnCode())) {
                return ResponseEntity.ok("付款成功！");
            }
            return ResponseEntity.badRequest().body("確認支付失敗：" + result.returnMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("系統錯誤：" + e.getMessage());
        }
    }
}