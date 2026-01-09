package com.ilovepc.project_home.web.sticker.controller;

import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.web.sticker.service.PraiseStickerService;
import com.ilovepc.project_home.web.sticker.vo.BoardRequest;
import com.ilovepc.project_home.web.sticker.vo.BoardResult;
import com.ilovepc.project_home.web.sticker.vo.StampStickerRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/praise")
public class PraiseStickerController {
    private final PraiseStickerService praiseStickerService;

    @PostMapping("/board")
    public ApiResponse<?> createBoard(@Valid @RequestBody BoardRequest boardRequest, BindingResult result){
        praiseStickerService.createBoard(boardRequest);
        return ApiResponse.success();
    }

    @PostMapping("/sticker")
    public ApiResponse<?> stampSticker(@Valid @RequestBody StampStickerRequest stampStickerRequest, BindingResult result){
        praiseStickerService.stampSticker(stampStickerRequest);
        return ApiResponse.success();
    }

    @GetMapping("/boardSticker")
    public ApiResponse<?> getBoardSticker(){
        List<BoardResult> boardSticker = praiseStickerService.getBoardSticker();
        return ApiResponse.success(boardSticker);
    }
}
