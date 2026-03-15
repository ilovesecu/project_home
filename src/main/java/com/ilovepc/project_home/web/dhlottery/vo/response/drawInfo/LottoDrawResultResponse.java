package com.ilovepc.project_home.web.dhlottery.vo.response.drawInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LottoDrawResultResponse {

    private String resultCode;    // 결과 코드
    private String resultMessage; // 결과 메시지
    private DrawData data;        // 실제 데이터 블록

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DrawData {
        private List<DrawItem> list; // 당첨 정보 리스트
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DrawItem {

        // 🗓️ [회차 및 일자 정보]
        private Integer gmSqNo;       // 게임 일련번호 (예: 5133)
        private Integer ltEpsd;       // 로또 회차 (예: 1215)
        private String ltRflYmd;      // 추첨일자 (예: "20260314")
        private String excelRnk;      // 엑셀 등수 표시 (예: "1등")

        // 🎱 [당첨 번호]
        private Integer tm1WnNo;      // 1번째 번호 (예: 13)
        private Integer tm2WnNo;      // 2번째 번호 (예: 15)
        private Integer tm3WnNo;      // 3번째 번호 (예: 19)
        private Integer tm4WnNo;      // 4번째 번호 (예: 21)
        private Integer tm5WnNo;      // 5번째 번호 (예: 44)
        private Integer tm6WnNo;      // 6번째 번호 (예: 45)
        private Integer bnsWnNo;      // 보너스 번호 (예: 39)

        // 🥇 [1등 당첨 정보]
        private Integer rnk1WnNope;   // 1등 당첨자 수 (예: 16명)
        private Long rnk1WnAmt;       // 1등 1인당 당첨금액 (예: 1,998,542,133원)
        private Long rnk1SumWnAmt;    // 1등 총 당첨금액 (예: 31,976,674,128원) *주의: int 초과하므로 Long 필수

        // 🥈 [2등 당첨 정보]
        private Integer rnk2WnNope;   // 2등 당첨자 수 (예: 76명)
        private Long rnk2WnAmt;       // 2등 1인당 당첨금액 (예: 70,124,286원)
        private Long rnk2SumWnAmt;    // 2등 총 당첨금액 (예: 5,329,445,736원)

        // 🥉 [3등 당첨 정보]
        private Integer rnk3WnNope;   // 3등 당첨자 수 (예: 3,120명)
        private Long rnk3WnAmt;       // 3등 1인당 당첨금액 (예: 1,708,156원)
        private Long rnk3SumWnAmt;    // 3등 총 당첨금액 (예: 5,329,446,720원)

        // 🏅 [4등 당첨 정보]
        private Integer rnk4WnNope;   // 4등 당첨자 수 (예: 153,024명)
        private Long rnk4WnAmt;       // 4등 1인당 당첨금액 (예: 50,000원)
        private Long rnk4SumWnAmt;    // 4등 총 당첨금액 (예: 7,651,200,000원)

        // 🏅 [5등 당첨 정보]
        private Integer rnk5WnNope;   // 5등 당첨자 수 (예: 2,640,357명)
        private Long rnk5WnAmt;       // 5등 1인당 당첨금액 (예: 5,000원)
        private Long rnk5SumWnAmt;    // 5등 총 당첨금액 (예: 13,201,785,000원)

        // 📊 [전체 통계 및 게임 타입]
        private Integer sumWnNope;           // 총 당첨자 수 (1~5등 합산)
        private Long rlvtEpsdSumNtslAmt;     // 해당 회차 총 판매금액 (예: 63,488,551,584원)
        private Long wholEpsdSumNtslAmt;     // 누적/전체 판매 관련 금액

        // 1등 당첨 배출 방식 (자동/수동/반자동 등) 통계로 추정됨
        private Integer winType0;     // (추정)
        private Integer winType1;     // (추정) 자동
        private Integer winType2;     // (추정) 수동
        private Integer winType3;     // (추정) 반자동

        public List<Integer> getWinningNumbers(){
            return Arrays.asList(tm1WnNo, tm2WnNo, tm3WnNo, tm4WnNo, tm5WnNo, tm6WnNo);
        }

        public List<Integer> getWinningAllNumbers(){
            return Arrays.asList(tm1WnNo, tm2WnNo, tm3WnNo, tm4WnNo, tm5WnNo, tm6WnNo, bnsWnNo);
        }
    }
}