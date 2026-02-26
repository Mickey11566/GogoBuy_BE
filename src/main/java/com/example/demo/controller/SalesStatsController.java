package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.constants.SalesStatsType;
import com.example.demo.request.SalesStatsReq;
import com.example.demo.response.SalesStatsRes;
import com.example.demo.service.SalesStatsService;

@RestController
@RequestMapping("/gogobuy/salesStats")
public class SalesStatsController {

	@Autowired
    private SalesStatsService salesStatsService;

    /**
     * 模擬手動計入銷量 (測試用)
     */
    @PostMapping("/add")
    public String addSales(@RequestBody SalesStatsReq req) {
        try {
            // 呼叫 Service 執行 DAILY 與 ALL 的更新
            salesStatsService.addSalesVolume(
                req.getStoreId(), 
                req.getMenuId(), 
                req.getQuantity()
            );
            return "銷量累計成功喵！店鋪:" + req.getStoreId() + ", 品項:" + req.getMenuId();
        } catch (Exception e) {
            return "累計失敗喵... 錯誤: " + e.getMessage();
        }
    }
    
    @GetMapping("/top10/{storeId}")
    public SalesStatsRes getTop10ByStore(
            @PathVariable("storeId") Integer storeId, 
            @RequestParam(value = "type", required = false) SalesStatsType type) {
        
        // type預設為 ALL
        return salesStatsService.getTop10SalesBystore(storeId, type);
    }
    
    @GetMapping("/Top10")
    public SalesStatsRes getTop10(@RequestParam(value = "type", required = false) SalesStatsType type) {
        return salesStatsService.getTop10(type);
    }
}
