package com.ilovepc.project_home.web.accountbook.parser;

import com.ilovepc.project_home.web.accountbook.vo.CashFlowType;
import com.ilovepc.project_home.web.accountbook.vo.PaymentMethod;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryParam;
import com.ilovepc.project_home.web.accountbook.vo.TransactionParseError;
import com.ilovepc.project_home.web.accountbook.vo.TransactionSourceType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class OnnuriGiftCardTransactionHistoryFileParser extends AbstractTransactionHistoryFileParser {
    private static final int HEADER_ROW_NUMBER = 9;
    private static final DateTimeFormatter ONNURI_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public boolean supports(TransactionSourceType sourceType, String fileName) {
        return sourceType == TransactionSourceType.ONNURI_GIFT_CARD;
    }

    @Override
    public List<TransactionHistoryParam> parse(
            MultipartFile file,
            String originalFileName,
            List<TransactionParseError> errors,
            AtomicInteger failedCount
    ) throws IOException {
        String extension = getExtension(originalFileName);
        if (!"xlsx".equals(extension) && !"xls".equals(extension)) {
            throw new IllegalArgumentException("온누리상품권 사용내역은 xls 또는 xlsx 파일만 업로드할 수 있습니다.");
        }

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
            if (row.length < 9) {
                throw new IllegalArgumentException("필수 컬럼 수가 부족합니다. expected=9, actual=" + row.length);
            }

            String tradeType = value(row, 6);
            String tradeStatus = value(row, 7);
            Long amount = parseAmount(value(row, 8), "거래금액");
            Long signedAmount = isCancel(tradeStatus, tradeType) ? Math.abs(amount) : -Math.abs(amount);

            TransactionHistoryParam transaction = TransactionHistoryParam.builder()
                    .transactionAt(parseTransactionAt(value(row, 0), value(row, 1))) //거래일자 + 거래시각
                    .description(value(row, 3)) //가맹점 및 상품권명
                    .transactionType(value(row, 2))
                    .transactionInstitution("디지털온누리상품권")
                    .accountNumber(null)   //사업자번호 넣을지 말지 고민
                    .amount(signedAmount)
                    .balanceAfter(null)
                    .memo(createMemo(row))
                    .sourceFileName(originalFileName)
                    .sourceRowNumber(rowNumber)
                    .cashflowType(CashFlowType.EXPENSE.name())
                    .isFixed(0)
                    .sourceType(TransactionSourceType.ONNURI_GIFT_CARD.name())
                    .paymentMethod(PaymentMethod.ONNURI_GIFT_CARD.name())
                    .externalKey(createExternalKey(row))
                    .build();
            transactions.add(transaction);
        } catch (Exception e) {
            failedCount.incrementAndGet();
            addError(errors, rowNumber, e.getMessage());
            log.warn("온누리상품권 거래내역 파싱 실패. fileName={}, rowNumber={}, row={}", originalFileName, rowNumber, row, e);
        }
    }

    private LocalDateTime parseTransactionAt(String date, String time) {
        try {
            return LocalDateTime.parse(date + time, ONNURI_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("거래일자/거래시각 형식이 올바르지 않습니다. value=" + date + " " + time, e);
        }
    }

    private boolean isCancel(String tradeStatus, String tradeType) {
        return tradeStatus.contains("취소") || tradeType.contains("취소");
    }

    private String createMemo(String[] row) {
        return String.format(
                "[지출][변동][식비] @공동 온누리상품권 / %s / %s / %s",
                value(row, 5),
                value(row, 6),
                value(row, 7)
        );
    }

    private String createExternalKey(String[] row) {
        return sha256Key(
                TransactionSourceType.ONNURI_GIFT_CARD.name(),
                value(row, 0),
                value(row, 1),
                value(row, 3),
                value(row, 4),
                value(row, 6),
                value(row, 7),
                value(row, 8)
        );
    }
}
