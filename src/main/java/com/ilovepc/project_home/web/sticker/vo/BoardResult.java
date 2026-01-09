package com.ilovepc.project_home.web.sticker.vo;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class BoardResult {
    private Long    id;             // '보드판 고유 ID'
    //private Long userNo;         // '사용자 번호'
    private String title;          //'보드판 제목'
    private String goal;           // '칭찬 목표'
    private int totalSlots;     // '전체 칸 수'
    private String rewardItem;     //'보상 상품 내용'
    private boolean isRewarded;     //'보상 완료 여부'
    private String status;         //('IN_PROGRESS', 'COMPLETED') '보드 상태'
    private String createdAt;      // '생성 일자'
    private String completedAt;    // '전체 완료 일자'

    private List<StampStickerResult> placedStickers;
}
