package com.ilovepc.project_home.web.dhlottery.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * 동행복권 마이페이지 API 전체 응답 객체
 * JSON의 최상위 (Root) 껍데기
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // JSON에 있는데 VO에 없는 필드는 무시 (에러 방지)
public class LotteryGameHistoryResponse {

    private String resultCode;    // 결과 코드 (예: 성공시 null 또는 "0000")
    private String resultMessage; // 결과 메시지
    private HistoryData data;     // 실제 데이터 블록

    /**
     * JSON의 "data" 객체 부분
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoryData {
        private int total;              // 총 검색된 데이터(티켓) 갯수
        private List<HistoryItem> list; // 구매 내역 리스트
    }

    /**
     * JSON의 "list" 배열 안에 들어가는 개별 복권 내역 객체
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoryItem {

        // 날짜 관련
        private String eltOrdrDt;       // 구매일자 (예: "2026-02-23")
        private String epsdRflDt;       // 추첨일자 (예: "2026-02-26")
        private String wngsGiveExpryYmd;// 지급만료일자 (예: "20270226")

        // 복권 정보
        private String ltGdsCd;         // 복권 코드 (예: "LP72"=연금복권, "LO40"=로또)
        private String ltGdsNm;         // 복권 이름 (예: "연금복권720+", "로또6/45")
        private int ltEpsd;             // 회차 숫자 (예: 304, 1213)
        private String ltEpsdView;      // 회차 표시용 문자열 (예: "304")

        // 구매 내역
        private String ntslOrdrNo;      // 주문번호 (예: "202602235075801")
        private int prchsQty;           // 구매 수량 (예: 1, 4)
        private String gmInfo;          // 게임 정보 (연금복권은 조/번호 "1:434203", 로또는 암호화된 티켓 번호열)
        private String gmType;          // 게임 타입 (자동/수동 여부 등 - 현재 null)
        private int ltPblcnSn;          // 발행 일련번호 (주문 내 순번 1, 2, 3...)

        // 당첨 결과
        private String ltWnResult;      // 추첨 결과 상태 (예: "미추첨", "당첨", "낙첨")
        private Long ltWnAmt;           // 당첨 금액 (미추첨/낙첨 시 null)
        private Long wngsTxam;          // 세금 (당첨 시)
        private Long wngsGiveAmt;       // 실 지급액 (당첨 시)
        private Integer wnRnk;          // 당첨 등수 (당첨 시 1, 2, 3...)
        private String wngsGiveYn;      // 당첨금 지급 여부 (예: "Y", "N")

        // 시스템 기타
        private int rowId;              // 목록 내 행 번호 (순번)
        private String fltr;            // 내부 필터/상태 코드 (예: "|S|")
        private String lramSmamTypeCd;  // 소액/고액 타입 코드 (추정)
    }
}
