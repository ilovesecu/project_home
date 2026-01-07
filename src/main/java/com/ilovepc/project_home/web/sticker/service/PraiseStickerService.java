package com.ilovepc.project_home.web.sticker.service;

import com.ilovepc.project_home.repository.PraiseStickerMapper;
import com.ilovepc.project_home.web.sticker.vo.BoardParam;
import com.ilovepc.project_home.web.sticker.vo.BoardRequest;
import com.ilovepc.project_home.web.sticker.vo.StampStickerParam;
import com.ilovepc.project_home.web.sticker.vo.StampStickerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PraiseStickerService {
    private final PraiseStickerMapper praiseStickerMapper;

    public void createBoard(BoardRequest boardRequest){
        //TODO 사용자 받아서 userNo 하드코딩 한거 대체하기
        this.createBoardCommon(boardRequest.buildParam(1L));
    }

    public void stampSticker(StampStickerRequest stampStickerRequest){
        //TODO 사용자 받아서 userNo 하드코딩 한거 대체하기
        this.stampStickerCommon(stampStickerRequest.buildParam(1L));
    }


    //보드판 생성
    private int createBoardCommon(BoardParam boardParam){
        return praiseStickerMapper.createBoard(boardParam);
    }

    //스티커 찍기
    private int stampStickerCommon(StampStickerParam param){
        return praiseStickerMapper.stampSticker(param);
    }
}
