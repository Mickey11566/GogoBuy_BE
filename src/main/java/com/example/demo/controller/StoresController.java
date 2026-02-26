package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.request.StoresReq;
import com.example.demo.request.StoresReq.CreateStoreGroup;
import com.example.demo.request.StoresReq.SearchStoreGroup;
import com.example.demo.response.BasicRes;
import com.example.demo.response.StoresRes;
import com.example.demo.service.StoreService;

import jakarta.validation.Valid;

@CrossOrigin
@RestController
public class StoresController {

	@Autowired
	private StoreService storeService;

	@PostMapping("gogobuy/store/create")
	public BasicRes create(@Validated(CreateStoreGroup.class) @RequestBody StoresReq req) {
	    try {
	        return storeService.create(req);
	    } catch (Exception e) {
	        // 捕捉 Service 丟出的 throw new Exception("...")
	        // 回傳自定義的錯誤代碼（例如 400）與 Exception 的 Message
	        return new BasicRes(400, e.getMessage());
	    }
	}
	
	@PostMapping("gogobuy/store/update")
	public BasicRes update(@RequestParam(name = "id") int storeId, @Valid @RequestBody StoresReq req) {
		try {
			return storeService.update(storeId, req);
		} catch (Exception e) {
			// 捕捉 Service 層拋出的驗證失敗或 SQL 錯誤
			return new BasicRes(400, e.getMessage());
		}
	}
	
//	軟刪
	@PostMapping("gogobuy/store/delete")
	public BasicRes delete(@RequestParam(name = "id") int storeId) {
		try {
			return storeService.deleteStore(storeId);
		} catch (Exception e) {
			return new BasicRes(400, "刪除失敗: " + e.getMessage());
		}
	}
	
//	全刪
	@PostMapping("gogobuy/store/fulldelete")
	public BasicRes deleteFull(@RequestParam(name = "id") int storeId) {
		try {
			return storeService.deleteFullStore(storeId);
		} catch (Exception e) {
			return new BasicRes(400, "刪除失敗: " + e.getMessage());
		}
	}
	
//	名字模糊搜尋店家
	@GetMapping("gogobuy/store/searchName")
    public BasicRes searchStores(@RequestParam(name = "name",required = false) String name) {
        return storeService.getStoresByName(name);
    }
	
//	取得指定店家資訊(開團)
	@GetMapping("gogobuy/store/searchId")
    public BasicRes getStoreById(@RequestParam(name = "id") int id) {
        return storeService.getStoreById(id);
    }
	
	
//	取得全部店家資訊
	@GetMapping("gogobuy/store/all")
    public BasicRes getAllStore() {
        return storeService.getAllStores();
    }
	
//	AI掃圖掃填店家
	@PostMapping("gogobuy/store/menuScan")
	public ResponseEntity<?> scanMenu(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("請上傳圖片檔案");
        }

        try {
            //  呼叫 Service 進行 AI 辨識，得到 StoresReq 物件
            StoresReq recognizedData = storeService.aiAnalyzeMenu(file.getBytes());
            
            //  直接回傳給前端，狀態碼 200
            return ResponseEntity.ok(recognizedData);
            
        } catch (Exception e) {
            // 若 AI 辨識失敗或格式不對，回傳 500 與錯誤訊息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("AI 辨識失敗：" + e.getMessage());
        }
    }
	
	
	//附近店家	
	// 附近店家搜尋 (整合定位與地址)
	@GetMapping("gogobuy/store/searchNearby")
	public StoresRes getNearby(
	        @RequestParam(name = "lat", required = false) Double lat,  // 改為 Double 支援 null
	        @RequestParam(name = "lng", required = false) Double lng,  // 改為 Double 支援 null
	        @RequestParam(name = "address", required = false) String address, // 新增地址參數
	        @RequestParam(name = "radius", required = false, defaultValue = "5.0") double radius) {

	    // 設定硬性上下限：最小 0 公里，最大 20 公里 (超出則設為 20)
	    if (radius < 0) radius = 0;
	    if (radius > 20.0) radius = 20.0;

	    // 呼叫 Service
	    return storeService.getNearbyStores(lat, lng, address, radius);
	}
	
	//範圍內營業中店家
	@PostMapping("gogobuy/store/getOperatingStores")
    public StoresRes getOperatingStores(@Validated(SearchStoreGroup.class) @RequestBody StoresReq req) {
        return storeService.getOperatingStores(req);
    }
}