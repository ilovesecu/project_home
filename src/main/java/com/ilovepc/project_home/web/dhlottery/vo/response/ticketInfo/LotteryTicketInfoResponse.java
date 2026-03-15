package com.ilovepc.project_home.web.dhlottery.vo.response.ticketInfo;

import lombok.Data;

@Data
public class LotteryTicketInfoResponse<T> {
    private String resultCode;    // 결과 코드 (예: 성공시 null 또는 "0000")
    private String resultMessage; // 결과 메시지
    private DataWrapper<T> data;

    @Data
    public static class DataWrapper<T> {
        private T ticket; // 실제 데이터 객체
        private boolean success;
        private String message;
    }
}
