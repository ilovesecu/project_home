package com.ilovepc.project_home.web.accountbook.controller;

import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.web.accountbook.service.TransactionHistoryQueryService;
import com.ilovepc.project_home.web.accountbook.service.TransactionHistoryUploadService;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistorySearchResponse;
import com.ilovepc.project_home.web.accountbook.vo.TransactionUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/account-book/transactions")
public class TransactionHistoryController {
    private final TransactionHistoryUploadService transactionHistoryUploadService;
    private final TransactionHistoryQueryService transactionHistoryQueryService;

    @GetMapping
    public ApiResponse<TransactionHistorySearchResponse> getTransactionHistory(
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ){
        //transactionHistoryQueryService.transactionAmount(startDate, endDate);
        return null;
    }


    @GetMapping("/list")
    public ApiResponse<TransactionHistorySearchResponse> list(
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "limit", required = false, defaultValue = "200") int limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset
    ) {
        log.info(
                "TRANSACTION HISTORY LIST COMMAND EXEC : startDate={}, endDate={}, month={}, limit={}, offset={}",
                startDate,
                endDate,
                month,
                limit,
                offset
        );
        TransactionHistorySearchResponse response = transactionHistoryQueryService.search(
                startDate,
                endDate,
                month,
                limit,
                offset
        );
        return ApiResponse.success(response);
    }

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
