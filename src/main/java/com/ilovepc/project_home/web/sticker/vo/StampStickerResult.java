package com.ilovepc.project_home.web.sticker.vo;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StampStickerResult {
    private Long id;                         //스티커 고유ID
    private Long boardId;                   //연결된 보드판 ID
    private Long userNo;
    private int slotId;                    //슬롯 ID (1~N)
    private String stickerUrl;                //스티커 이미지 경로
    private String stampedAt;                 //스티커 찍은 일자
}
