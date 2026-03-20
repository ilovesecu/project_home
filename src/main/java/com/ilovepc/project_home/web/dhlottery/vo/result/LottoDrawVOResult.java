package com.ilovepc.project_home.web.dhlottery.vo.result;

import lombok.Data;
import java.util.List;

/**
 * DB에서 조회한 로또 당첨 정보 전체를 담는 VO
 */
@Data
public class LottoDrawVOResult {

    // 🗓️ 1. [lotto_draw 테이블] 기본 정보 및 당첨 번호
    private Integer ltEpsd;       // 로또 회차
    private String ltRflYmd;      // 추첨일자
    private Integer gmSqNo;       // 게임 일련번호

    private Integer tm1WnNo;
    private Integer tm2WnNo;
    private Integer tm3WnNo;
    private Integer tm4WnNo;
    private Integer tm5WnNo;
    private Integer tm6WnNo;
    private Integer bnsWnNo;

    // 📊 2. [lotto_draw_stats 테이블] 1:1 통계 정보 객체
    private LottoStatsVO stats;

    // 🏆 3. [lotto_draw_rank 테이블] 1:N 등수별 당첨 정보 리스트 (1등~5등)
    private List<LottoRankVO> ranks;


    // ==========================================
    // 내부 클래스: 통계 정보 VO
    // ==========================================
    @Data
    public static class LottoStatsVO {
        private Integer sumWnNope;           // 총 당첨자 수
        private Long rlvtEpsdSumNtslAmt;     // 회차 총 판매금액
        private Long wholEpsdSumNtslAmt;     // 누적 판매금액
        private Integer winType0;
        private Integer winType1;
        private Integer winType2;
        private Integer winType3;
    }

    // ==========================================
    // 내부 클래스: 등수별 상세 정보 VO
    // ==========================================
    @Data
    public static class LottoRankVO {
        private Integer rankNo;       // 등수 (1~5)
        private Integer winnerCount;  // 당첨자 수
        private Long winAmt;          // 1인당 당첨금액
        private Long sumWinAmt;       // 해당 등수 총 당첨금액
    }
}
