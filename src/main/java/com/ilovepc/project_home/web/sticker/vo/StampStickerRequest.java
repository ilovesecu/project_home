package com.ilovepc.project_home.web.sticker.vo;

import lombok.Data;

@Data
public class StampStickerRequest {
    private Long boardId;                   //연결된 보드판 ID
    private int slotId;                    //슬롯 ID (1~N)
    private String stickerUrl;                //스티커 이미지 경로

    public StampStickerParam buildParam(int userNo){
        return StampStickerParam.builder()
                .userNo(userNo)
                .boardId(boardId)
                .slotId(slotId)
                .stickerUrl(stickerUrl)
                .build();
    }
}
