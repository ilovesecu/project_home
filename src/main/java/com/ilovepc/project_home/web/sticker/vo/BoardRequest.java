package com.ilovepc.project_home.web.sticker.vo;

import lombok.Data;

@Data
public class BoardRequest {
    private String title;          //'보드판 제목'
    private String goal;           // '칭찬 목표'
    private int totalSlots;     // '전체 칸 수'
    private String rewardItem;     //'보상 상품 내용'

    public BoardParam buildParam(Long userNo){
        return BoardParam.builder()
                .userNo(userNo)
                .title(title)
                .goal(goal)
                .totalSlots(totalSlots)
                .rewardItem(rewardItem)
                .build();
    }
}
