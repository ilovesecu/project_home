package com.ilovepc.project_home.web.sticker.service;

import com.ilovepc.project_home.config.security.vo.HomeProjectUserDetails;
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

    public BoardResult createBoard(BoardRequest boardRequest, HomeProjectUserDetails homeProjectUserDetails){
        int userNo = homeProjectUserDetails.getUserNo();
        return this.createBoardCommon(boardRequest.buildParam(userNo));
    }

    public StampStickerResult stampSticker(StampStickerRequest stampStickerRequest, HomeProjectUserDetails homeProjectUserDetails){
        int userNo = homeProjectUserDetails.getUserNo();
        StampStickerResult stampStickerResult = this.stampStickerCommon(stampStickerRequest.buildParam(userNo));
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
