package com.ilovepc.project_home.web.accountbook.parser;

import com.ilovepc.project_home.web.accountbook.vo.CashFlowType;
import com.ilovepc.project_home.web.accountbook.vo.PaymentMethod;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryParam;
import com.ilovepc.project_home.web.accountbook.vo.TransactionParseError;
import com.ilovepc.project_home.web.accountbook.vo.TransactionSourceType;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
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

@Component
@Slf4j
public class TossBankTransactionHistoryFileParser extends AbstractTransactionHistoryFileParser {
    private static final int HEADER_ROW_NUMBER = 9;
    private static final DateTimeFormatter TOSS_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    @Override
    public boolean supports(TransactionSourceType sourceType, String fileName) {
        return sourceType == TransactionSourceType.TOSS_BANK;
    }

    @Override
    public List<TransactionHistoryParam> parse(
            MultipartFile file,
            String originalFileName,
            List<TransactionParseError> errors,
            AtomicInteger failedCount
    ) throws IOException {
        String extension = getExtension(originalFileName);
        if ("csv".equals(extension)) {
            return parseCsv(file, originalFileName, errors, failedCount);
        }
        if ("xlsx".equals(extension) || "xls".equals(extension)) {
            return parseExcel(file, originalFileName, errors, failedCount);
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
            int offset = value(row, 0).matches("\\d{4}\\.\\d{2}\\.\\d{2} \\d{2}:\\d{2}:\\d{2}") ? 0 : 1;
            if (row.length < offset + 8) {
                throw new IllegalArgumentException("필수 컬럼 수가 부족합니다. expected=" + (offset + 8) + ", actual=" + row.length);
            }
            TransactionHistoryParam transaction = TransactionHistoryParam.builder()
                    .transactionAt(parseTransactionAt(value(row, offset)))
                    .description(value(row, offset + 1))
                    .transactionType(value(row, offset + 2))
                    .transactionInstitution(value(row, offset + 3))
                    .accountNumber(value(row, offset + 4))
                    .amount(parseAmount(value(row, offset + 5), "거래 금액"))
                    .balanceAfter(parseAmount(value(row, offset + 6), "거래 후 잔액"))
                    .memo(value(row, offset + 7))
                    .sourceFileName(originalFileName)
                    .sourceRowNumber(rowNumber)
                    .cashflowType(parseCashFlowType(value(row, offset + 7)).name())
                    .isFixed(0)
                    .sourceType(TransactionSourceType.TOSS_BANK.name())
                    .paymentMethod(PaymentMethod.TOSS_MOIM_CARD.name())
                    .externalKey(createExternalKey(row, offset))
                    .build();
            transactions.add(transaction);
        } catch (Exception e) {
            failedCount.incrementAndGet();
            addError(errors, rowNumber, e.getMessage());
            log.warn("토스뱅크 거래내역 파싱 실패. fileName={}, rowNumber={}, row={}", originalFileName, rowNumber, row, e);
        }
    }

    private LocalDateTime parseTransactionAt(String value) {
        try {
            return LocalDateTime.parse(value, TOSS_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("거래 일시 형식이 올바르지 않습니다. value=" + value, e);
        }
    }

    private CashFlowType parseCashFlowType(String memo) {
        if (!StringUtils.hasText(memo)) {
            return CashFlowType.NONE;
        }
        if (memo.startsWith("[지출]")) {
            return CashFlowType.EXPENSE;
        }
        if (memo.startsWith("[저축]")) {
            return CashFlowType.SAVING;
        }
        if (memo.startsWith("[수입]")) {
            return CashFlowType.INCOME;
        }
        if (memo.startsWith("[투자]")) {
            return CashFlowType.INVESTMENT;
        }
        if (memo.startsWith("[이체]")) {
            return CashFlowType.TRANSFER;
        }
        if (memo.startsWith("[기타]")) {
            return CashFlowType.ETC;
        }
        return CashFlowType.NONE;
    }

    private String createExternalKey(String[] row, int offset) {
        return sha256Key(
                TransactionSourceType.TOSS_BANK.name(),
                value(row, offset),
                value(row, offset + 1),
                value(row, offset + 4),
                value(row, offset + 5),
                value(row, offset + 6),
                value(row, offset + 7)
        );
    }
}
