package com.ilovepc.project_home.web.accountbook.controller;

import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.web.accountbook.llm.OnnuriMemoClassificationRequest;
import com.ilovepc.project_home.web.accountbook.llm.OnnuriMemoClassificationResult;
import com.ilovepc.project_home.web.accountbook.llm.OnnuriMemoClassificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/account-book/onnuri")
public class OnnuriMemoClassificationController {
    private final OnnuriMemoClassificationService onnuriMemoClassificationService;

    @PostMapping("/memo-classification")
    public ApiResponse<OnnuriMemoClassificationResult> classifyMemo(
            @RequestBody OnnuriMemoClassificationRequest request
    ) {
        log.info("ONNURI MEMO CLASSIFICATION COMMAND EXEC : merchantName={}", request.merchantName());
        return ApiResponse.success(onnuriMemoClassificationService.classify(request));
    }
}
