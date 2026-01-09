package com.ilovepc.project_home.web.sticker.service;

import com.ilovepc.project_home.repository.PraiseStickerMapper;
import com.ilovepc.project_home.web.sticker.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PraiseStickerService {
    private final PraiseStickerMapper praiseStickerMapper;

    public BoardResult createBoard(BoardRequest boardRequest){
        //TODO 사용자 받아서 userNo 하드코딩 한거 대체하기
        return this.createBoardCommon(boardRequest.buildParam(5L));
    }

    public StampStickerResult stampSticker(StampStickerRequest stampStickerRequest){
        //TODO 사용자 받아서 userNo 하드코딩 한거 대체하기
        StampStickerResult stampStickerResult = this.stampStickerCommon(stampStickerRequest.buildParam(6L));
        return stampStickerResult;
    }

    public List<BoardResult> getBoardSticker(){
        List<BoardResult> inProgress = praiseStickerMapper.getBoards("IN_PROGRESS");
        return inProgress;
    }

    //보드판 생성
    private BoardResult createBoardCommon(BoardParam boardParam){
        BoardResult board = praiseStickerMapper.createBoard(boardParam);
        if(board.getPlacedStickers() == null || board.getPlacedStickers().isEmpty()){
            board.setPlacedStickers(new ArrayList<>());
        }
        return board;
    }

    //스티커 찍기
    private StampStickerResult stampStickerCommon(StampStickerParam param){
        return praiseStickerMapper.stampSticker(param);
    }
}
