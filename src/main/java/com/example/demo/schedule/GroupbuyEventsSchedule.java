package com.example.demo.schedule;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.demo.constants.GroupbuyStatusEnum;
import com.example.demo.dao.GroupbuyEventsDao;
import com.example.demo.entity.GroupbuyEvents;
import com.example.demo.service.GroupbuyEventsService;

import jakarta.transaction.Transactional;

@Service

public class GroupbuyEventsSchedule {
	
	@Autowired
    private GroupbuyEventsDao groupbuyEventsDao;
	
	@Autowired
    private GroupbuyEventsService groupbuyEventsService;
	
    // "0 * * * * *" 代表「每分鐘的第一秒」執行一次

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void autoUpdateEventsStatus() {
    	
    	// 查詢狀態 open 且 結單時間結束的
    	List<GroupbuyEvents> expiredEvents = groupbuyEventsDao.findByEndTimeBeforeAndStatus(
    			GroupbuyStatusEnum.OPEN.name(),
                LocalDateTime.now()
            );
    	// 檢查
    	if(CollectionUtils.isEmpty(expiredEvents)) {
    		return;
    	}
    	int successCount = 0;
    	for(GroupbuyEvents events : expiredEvents) {
    		try {
                // 產帳單 + 算運費 + 改狀態 
                // 這裡的 ID 欄位請根據你 Entity 實際名稱修改
                groupbuyEventsService.autoCloseEvent(
                		events.getId(), 
                		events.getHostId());
                successCount++;
            } catch (Exception e) {
                // 如果某一筆失敗，紀錄錯誤但不中斷排程，繼續處理下一筆
                System.err.println("活動 ID " + events.getId() + " 自動結單異常: " + e.getMessage());
            }
        }

        if (successCount > 0) {
            System.out.println("排程執行完畢，成功自動結單並帳單共 " + successCount + " 筆。");
        }
    }
}
