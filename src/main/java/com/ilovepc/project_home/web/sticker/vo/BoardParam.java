package com.ilovepc.project_home.web.sticker.vo;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import lombok.Builder;

@Builder
@HomeMaster
public class BoardParam {
    private Long   id;    //생성된 key 전달받기용임!
    private Long   userNo;         // '사용자 번호'
    private String title;          //'보드판 제목'
    private String goal;           // '칭찬 목표'
    private int    totalSlots;     // '전체 칸 수'
    private String rewardItem;     //'보상 상품 내용'
}
