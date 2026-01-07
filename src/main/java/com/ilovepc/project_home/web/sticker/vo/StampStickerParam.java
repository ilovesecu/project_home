package com.ilovepc.project_home.web.sticker.vo;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StampStickerParam {
    private Long boardId;                   //연결된 보드판 ID
    private Long userNo;
    private int slotId;                    //슬롯 ID (1~N)
    private String stickerUrl;                //스티커 이미지 경로
}
