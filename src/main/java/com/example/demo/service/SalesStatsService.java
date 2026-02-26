package com.example.demo.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.demo.constants.SalesStatsType;
import com.example.demo.dao.StoresSearchDao;
import com.example.demo.entity.SalesStats;
import com.example.demo.projection.SalesLeaderboardProjection;
import com.example.demo.repository.SalesStatsRepository;
import com.example.demo.response.SalesStatsRes;

import jakarta.transaction.Transactional;

@Service
public class SalesStatsService {
	@Autowired
    private SalesStatsRepository salesStatsRepository;
	
	@Autowired
    private StoresSearchDao storesSearchDao;

	@Transactional
    public SalesStatsRes addSalesVolume(Integer storeId, Integer menuId, int quantity) {
        try {
        	
            if (!storesSearchDao.existsById(storeId)) {
                return new SalesStatsRes(404, "審核失敗：商店 ID " + storeId + " 不存在喵！");
            }

            if (storesSearchDao.getMenuByMenuId(menuId) == null) {
                return new SalesStatsRes(404, "審核失敗：商品 ID " + menuId + " 不存在喵！");
            }
            LocalDate today = LocalDate.now();

            // 今日 (DAILY) - 基準日：今天
            updateStatsLogic(storeId, menuId, SalesStatsType.DAILY, today, quantity);

            // 本週 (WEEKLY) - 基準日：本週週一
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            updateStatsLogic(storeId, menuId, SalesStatsType.WEEKLY, startOfWeek, quantity);

            // 本月 (MONTHLY) - 基準日：本月 1 號
            LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
            updateStatsLogic(storeId, menuId, SalesStatsType.MONTHLY, startOfMonth, quantity);

            // 年度 (YEAR) - 基準日：本年 1 月 1 號
            LocalDate startOfYear = today.with(TemporalAdjusters.firstDayOfYear());
            updateStatsLogic(storeId, menuId, SalesStatsType.YEAR, startOfYear, quantity);

            // 總累計 (ALL) - 基準日：null
            updateStatsLogic(storeId, menuId, SalesStatsType.ALL, null, quantity);

            return new SalesStatsRes(200, "全週期銷量同步更新成功喵！");
        } catch (Exception e) {
            return new SalesStatsRes(500, "同步更新失敗喵...: " + e.getMessage());
        }
    }

    private void updateStatsLogic(Integer storeId, Integer menuId, SalesStatsType type, LocalDate date, int quantity) {
    	salesStatsRepository.upsertSalesVolume(storeId, menuId, type.name(), date, quantity);
    }
    
    public SalesStatsRes getTop10SalesBystore(Integer storeId, SalesStatsType type) {
        try {
            // 將 Enum 轉為 String 以符合 nativeQuery 參數
//            String typeStr = (type != null) ? type.name() : SalesStatsType.ALL.name();
            SalesStatsType targetType = (type != null) ? type : SalesStatsType.ALL;
            LocalDate targetDate = calculateTargetDate(targetType);
            
            List<SalesLeaderboardProjection> details = salesStatsRepository.findTop10WithDetails(storeId, targetType.name(), targetDate);
            if (CollectionUtils.isEmpty(details)) {
            	return new SalesStatsRes(404,"目前該店沒有銷售紀錄喵!快去買喵!");
            }
            SalesStatsRes res = new SalesStatsRes(200, "獲取排行成功喵！");
            res.setSalesDetailList(details); 
            return res;
        } catch (Exception e) {
            return new SalesStatsRes(500, "失敗喵...：" + e.getMessage());
        }
    }
    
    public SalesStatsRes getTop10(SalesStatsType type) {
        try {
//            String typeStr = (type != null) ? type.name() : SalesStatsType.ALL.name();
            SalesStatsType targetType = (type != null) ? type : SalesStatsType.ALL;
            LocalDate targetDate = calculateTargetDate(targetType);
            List<SalesLeaderboardProjection> list = salesStatsRepository.findGlobalTop10(targetType.name(), targetDate);
            if (CollectionUtils.isEmpty(list)) {
            	return new SalesStatsRes(404,"任何店都沒有銷售紀錄喵!快去買喵!");
            }
            SalesStatsRes res = new SalesStatsRes(200, "獲取全平台排行成功喵！");
            res.setSalesDetailList(list);
            return res;
        } catch (Exception e) {
            return new SalesStatsRes(500, "獲取全平台排行失敗喵：" + e.getMessage());
        }
        }
    
    
    private LocalDate calculateTargetDate(SalesStatsType type) {
        LocalDate today = LocalDate.now();
        switch (type) {
            case DAILY:   return today;
            case WEEKLY:  return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY: return today.with(TemporalAdjusters.firstDayOfMonth());
            case YEAR:    return today.with(TemporalAdjusters.firstDayOfYear());
            case ALL:     return null;
            default:      return null;
        }
}
}
