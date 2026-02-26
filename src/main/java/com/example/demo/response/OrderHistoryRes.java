package com.example.demo.response;

import java.util.List;
import com.example.demo.dto.OrderHistoryDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

// 若希望資料轉JSON時排除全部值為null的屬性，可在對應類別名稱前加上
// 該annotation也可針對個別屬性設定，把@JsonInclude放在屬性名稱前即可
@JsonInclude(Include.NON_NULL)
public class OrderHistoryRes extends BasicRes {

    private List<OrderHistoryDTO> orderHistoryList;

    public OrderHistoryRes() {
        super();
    }

    public OrderHistoryRes(int code, String message) {
        super(code, message);
    }

    public OrderHistoryRes(int code, String message, List<OrderHistoryDTO> orderHistoryList) {
        super(code, message);
        this.orderHistoryList = orderHistoryList;
    }

    public List<OrderHistoryDTO> getOrderHistoryList() {
        return orderHistoryList;
    }

    public void setOrderHistoryList(List<OrderHistoryDTO> orderHistoryList) {
        this.orderHistoryList = orderHistoryList;
    }
}
