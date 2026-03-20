package com.ilovepc.project_home.web.dhlottery.vo.param;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
public class LottoResultParam {
    // 🗓️ [기본 정보]
    private Integer ltEpsd;       // 로또 회차 (예: 1215)
    private String ltRflYmd;      // 추첨일자 (예: "20260314")
    private Integer gmSqNo;       // 게임 일련번호

    // 🎱 [당첨 번호]
    private Integer tm1WnNo;      // 1번째 당첨 번호
    private Integer tm2WnNo;      // 2번째 당첨 번호
    private Integer tm3WnNo;      // 3번째 당첨 번호
    private Integer tm4WnNo;      // 4번째 당첨 번호
    private Integer tm5WnNo;      // 5번째 당첨 번호
    private Integer tm6WnNo;      // 6번째 당첨 번호
    private Integer bnsWnNo;      // 보너스 번호

    // 🥇 [1등 당첨 정보]
    private Integer rnk1WnNope;   // 1등 당첨자 수
    private Long rnk1WnAmt;       // 1등 1인당 당첨금액
    private Long rnk1SumWnAmt;    // 1등 총 당첨금액

    // 🥈 [2등 당첨 정보]
    private Integer rnk2WnNope;   // 2등 당첨자 수
    private Long rnk2WnAmt;       // 2등 1인당 당첨금액
    private Long rnk2SumWnAmt;    // 2등 총 당첨금액

    // 🥉 [3등 당첨 정보]
    private Integer rnk3WnNope;   // 3등 당첨자 수
    private Long rnk3WnAmt;       // 3등 1인당 당첨금액
    private Long rnk3SumWnAmt;    // 3등 총 당첨금액

    // 🏅 [4등 당첨 정보]
    private Integer rnk4WnNope;   // 4등 당첨자 수
    private Long rnk4WnAmt;       // 4등 1인당 당첨금액
    private Long rnk4SumWnAmt;    // 4등 총 당첨금액

    // 🏅 [5등 당첨 정보]
    private Integer rnk5WnNope;   // 5등 당첨자 수
    private Long rnk5WnAmt;       // 5등 1인당 당첨금액
    private Long rnk5SumWnAmt;    // 5등 총 당첨금액

    // 📊 [전체 통계 및 게임 타입]
    private Integer sumWnNope;           // 총 당첨자 수 (1~5등 합산)
    private Long rlvtEpsdSumNtslAmt;     // 해당 회차 총 판매금액
    private Long wholEpsdSumNtslAmt;     // 누적/전체 판매 관련 금액

    private Integer winType0;     // 당첨유형 0 (기타)
    private Integer winType1;     // 당첨유형 1 (자동)
    private Integer winType2;     // 당첨유형 2 (수동)
    private Integer winType3;     // 당첨유형 3 (반자동)
}
