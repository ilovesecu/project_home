package com.ilovepc.project_home.web.accountbook.parser;

import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryParam;
import com.ilovepc.project_home.web.accountbook.vo.TransactionParseError;
import com.ilovepc.project_home.web.accountbook.vo.TransactionSourceType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface TransactionHistoryFileParser {
    boolean supports(TransactionSourceType sourceType, String fileName);

    List<TransactionHistoryParam> parse(
            MultipartFile file,
            String originalFileName,
            List<TransactionParseError> errors,
            AtomicInteger failedCount
    ) throws IOException;
}
