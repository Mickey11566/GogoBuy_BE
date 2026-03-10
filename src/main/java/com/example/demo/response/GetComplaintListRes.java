package com.example.demo.response;

import java.util.List;
import com.example.demo.vo.ComplaintVo;

public class GetComplaintListRes extends BasicRes {
    private List<ComplaintVo> complaintList;

    public GetComplaintListRes() {
        super();
    }

    public GetComplaintListRes(int code, String message) {
        super(code, message);
    }

    public GetComplaintListRes(int code, String message, List<ComplaintVo> complaintList) {
        super(code, message);
        this.complaintList = complaintList;
    }

    public List<ComplaintVo> getComplaintList() {
        return complaintList;
    }

    public void setComplaintList(List<ComplaintVo> complaintList) {
        this.complaintList = complaintList;
    }
}
