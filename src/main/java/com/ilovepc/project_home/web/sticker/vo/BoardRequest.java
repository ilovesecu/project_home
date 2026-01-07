package com.ilovepc.project_home.web.sticker.vo;

import lombok.Data;

@Data
public class BoardRequest {
    private Long id;             // '보드판 고유 ID'
    private String title;          //'보드판 제목'
    private String goal;           // '칭찬 목표'
    private int totalSlots;     // '전체 칸 수'
    private String rewardItem;     //'보상 상품 내용'

    public BoardParam buildParam(Long userNo){
        return BoardParam.builder()
                .id(id)
                .userNo(userNo)
                .title(title)
                .goal(goal)
                .totalSlots(totalSlots)
                .rewardItem(rewardItem)
                .build();
    }
}
