package com.ilovepc.project_home.web.sticker.controller;

import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.config.security.vo.HomeProjectUserDetails;
import com.ilovepc.project_home.web.sticker.service.PraiseStickerService;
import com.ilovepc.project_home.web.sticker.vo.BoardRequest;
import com.ilovepc.project_home.web.sticker.vo.BoardResult;
import com.ilovepc.project_home.web.sticker.vo.StampStickerRequest;
import com.ilovepc.project_home.web.sticker.vo.StampStickerResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ApiResponse<BoardResult> createBoard(@Valid @RequestBody BoardRequest boardRequest, BindingResult result,
                                                @AuthenticationPrincipal HomeProjectUserDetails homeProjectUserDetails){
        BoardResult board = praiseStickerService.createBoard(boardRequest, homeProjectUserDetails);
        return ApiResponse.success(board);
    }

    @PostMapping("/sticker")
    public ApiResponse<StampStickerResult> stampSticker(@Valid @RequestBody StampStickerRequest stampStickerRequest, BindingResult result,
                                                        @AuthenticationPrincipal HomeProjectUserDetails homeProjectUserDetails){
        log.error("이 사람이 요청함:",homeProjectUserDetails.getUsername());
        StampStickerResult stampStickerResult = praiseStickerService.stampSticker(stampStickerRequest, homeProjectUserDetails);
        return ApiResponse.success(stampStickerResult);
    }

    @GetMapping("/boardSticker")
    public ApiResponse<?> getBoardSticker(@AuthenticationPrincipal HomeProjectUserDetails homeProjectUserDetails){
        List<BoardResult> boardSticker = praiseStickerService.getBoardSticker();
        log.error("이 사람이 요청함 boardSticker:{}",homeProjectUserDetails.getUsername());
        return ApiResponse.success(boardSticker);
    }
}
