package com.ilovepc.project_home.web.sticker.service;

import com.ilovepc.project_home.repository.PraiseStickerMapper;
import com.ilovepc.project_home.web.sticker.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PraiseStickerService {
    private final PraiseStickerMapper praiseStickerMapper;

    public void createBoard(BoardRequest boardRequest){
        //TODO 사용자 받아서 userNo 하드코딩 한거 대체하기
        this.createBoardCommon(boardRequest.buildParam(5L));
    }

    public void stampSticker(StampStickerRequest stampStickerRequest){
        //TODO 사용자 받아서 userNo 하드코딩 한거 대체하기
        this.stampStickerCommon(stampStickerRequest.buildParam(6L));
    }

    public List<BoardResult> getBoardSticker(){
        List<BoardResult> inProgress = praiseStickerMapper.getBoards("IN_PROGRESS");
        log.error("progress:{}",inProgress);
        return inProgress;
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
