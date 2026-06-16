package com.ilovepc.project_home.web.accountbook.service;

import com.ilovepc.project_home.repository.TransactionHistoryMapper;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryParam;
import com.ilovepc.project_home.web.accountbook.vo.TransactionParseError;
import com.ilovepc.project_home.web.accountbook.vo.TransactionUploadResponse;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionHistoryUploadService {
    private static final int HEADER_ROW_NUMBER = 9;
    private static final int BATCH_SIZE = 5000;
    private static final int MAX_RESPONSE_ERRORS = 50;
    private static final DateTimeFormatter TOSS_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    private final TransactionHistoryMapper transactionHistoryMapper;

    @Transactional
    public TransactionUploadResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 거래내역 파일이 비어 있습니다.");
        }

        String originalFileName = file.getOriginalFilename();
        List<TransactionParseError> errors = new ArrayList<>();
        AtomicInteger failedCount = new AtomicInteger();
        List<TransactionHistoryParam> transactions = parse(file, originalFileName, errors, failedCount);

        int insertedCount = insertBatch(transactions);

        return TransactionUploadResponse.builder()
                .fileName(originalFileName)
                .parsedCount(transactions.size())
                .insertedCount(insertedCount)
                .failedCount(failedCount.get())
                .errors(errors)
                .build();
    }

    private List<TransactionHistoryParam> parse(
            MultipartFile file,
            String originalFileName,
            List<TransactionParseError> errors,
            AtomicInteger failedCount
    ) {
        String extension = getExtension(originalFileName);

        try {
            if ("csv".equals(extension)) {
                return parseCsv(file, originalFileName, errors, failedCount);
            }
            if ("xlsx".equals(extension) || "xls".equals(extension)) {
                return parseExcel(file, originalFileName, errors, failedCount);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("거래내역 파일을 읽는 중 오류가 발생했습니다.", e);
        }

        throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. csv, xls, xlsx 파일만 업로드할 수 있습니다.");
    }

    private List<TransactionHistoryParam> parseCsv(
            MultipartFile file,
            String originalFileName,
            List<TransactionParseError> errors,
            AtomicInteger failedCount
    ) throws IOException {
        List<TransactionHistoryParam> transactions = new ArrayList<>();

        try (
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
                );
                CSVReader csvReader = new CSVReader(bufferedReader)
        ) {
            String[] row;
            int rowNumber = 0;
            while ((row = csvReader.readNext()) != null) {
                rowNumber++;
                if (rowNumber <= HEADER_ROW_NUMBER) {
                    continue;
                }
                parseRow(row, rowNumber, originalFileName, transactions, errors, failedCount);
            }
        } catch (CsvValidationException e) {
            throw new IllegalArgumentException("CSV 파일 형식이 올바르지 않습니다.", e);
        }

        return transactions;
    }

    private List<TransactionHistoryParam> parseExcel(
            MultipartFile file,
            String originalFileName,
            List<TransactionParseError> errors,
            AtomicInteger failedCount
    ) throws IOException {
        List<TransactionHistoryParam> transactions = new ArrayList<>();
        DataFormatter formatter = new DataFormatter(Locale.KOREA);

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0 || workbook.getSheetAt(0) == null) {
                return transactions;
            }

            var sheet = workbook.getSheetAt(0);
            for (int rowIndex = HEADER_ROW_NUMBER; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                String[] values = new String[9];
                if (row != null) {
                    for (int cellIndex = 0; cellIndex < values.length; cellIndex++) {
                        values[cellIndex] = formatter.formatCellValue(row.getCell(cellIndex));
                    }
                }
                parseRow(values, rowIndex + 1, originalFileName, transactions, errors, failedCount);
            }
        }

        return transactions;
    }

    private void parseRow(
            String[] row,
            int rowNumber,
            String originalFileName,
            List<TransactionHistoryParam> transactions,
            List<TransactionParseError> errors,
            AtomicInteger failedCount
    ) {
        try {
            if (isBlankRow(row)) {
                return;
            }
            if (row.length < 8) {
                throw new IllegalArgumentException("필수 컬럼 수가 부족합니다. expected=8, actual=" + row.length);
            }

            TransactionHistoryParam transaction = TransactionHistoryParam.builder()
                    .transactionAt(parseTransactionAt(value(row, 1)))
                    .description(value(row, 2))
                    .transactionType(value(row, 3))
                    .transactionInstitution(value(row, 4))
                    .accountNumber(value(row, 5))
                    .amount(parseAmount(value(row, 6), "거래 금액"))
                    .balanceAfter(parseAmount(value(row, 7), "거래 후 잔액"))
                    .memo(value(row, 8))
                    .sourceFileName(originalFileName)
                    .sourceRowNumber(rowNumber)
                    .build();
            transactions.add(transaction);
        } catch (Exception e) {
            failedCount.incrementAndGet();
            addError(errors, rowNumber, e.getMessage());
            log.warn("거래내역 파싱 실패. fileName={}, rowNumber={}, row={}", originalFileName, rowNumber, row, e);
        }
    }

    private int insertBatch(List<TransactionHistoryParam> transactions) {
        if (transactions.isEmpty()) {
            return 0;
        }

        int insertedCount = 0;
        for (int start = 0; start < transactions.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, transactions.size());
            insertedCount += transactionHistoryMapper.insertTransactionHistories(transactions.subList(start, end));
        }
        return insertedCount;
    }

    private LocalDateTime parseTransactionAt(String value) {
        try {
            return LocalDateTime.parse(value, TOSS_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("거래 일시 형식이 올바르지 않습니다. value=" + value, e);
        }
    }

    private Long parseAmount(String value, String columnName) {
        String normalized = value.replace(",", "").replace("\u2212", "-").trim();
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException(columnName + " 값이 비어 있습니다.");
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(columnName + " 형식이 올바르지 않습니다. value=" + value, e);
        }
    }

    private boolean isBlankRow(String[] row) {
        if (row == null || row.length == 0) {
            return true;
        }
        for (String value : row) {
            if (StringUtils.hasText(value)) {
                return false;
            }
        }
        return true;
    }

    private String value(String[] row, int index) {
        if (row == null || index >= row.length || row[index] == null) {
            return "";
        }
        return row[index].trim();
    }

    private void addError(List<TransactionParseError> errors, int rowNumber, String message) {
        if (errors.size() < MAX_RESPONSE_ERRORS) {
            errors.add(TransactionParseError.builder()
                    .rowNumber(rowNumber)
                    .message(message)
                    .build());
        }
    }

    private String getExtension(String originalFileName) {
        if (!StringUtils.hasText(originalFileName) || !originalFileName.contains(".")) {
            return "";
        }
        return originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
