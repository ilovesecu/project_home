package com.ilovepc.project_home.web.accountbook.controller;

import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.web.accountbook.service.TransactionHistoryUploadService;
import com.ilovepc.project_home.web.accountbook.vo.TransactionUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/account-book/transactions")
public class TransactionHistoryController {
    private final TransactionHistoryUploadService transactionHistoryUploadService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<TransactionUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        log.info("TRANSACTION HISTORY UPLOAD COMMAND EXEC : originalFileName={}", file.getOriginalFilename());
        TransactionUploadResponse response = transactionHistoryUploadService.upload(file);
        return ApiResponse.success(response);
    }
}
