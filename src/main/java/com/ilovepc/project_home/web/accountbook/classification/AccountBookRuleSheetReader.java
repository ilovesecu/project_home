package com.ilovepc.project_home.web.accountbook.classification;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class AccountBookRuleSheetReader {
    //업로드된 엑셀 안의 규칙 시트를 읽습니다.
    //A열은 수입 카테고리, B열은 지출/기타 카테고리, C열은 고정여부, D열은 대분류로 읽고, 예시 컬럼과 고정 시트의 메모 예시도 태그 근거로 참고합니다.
    private static final String RULE_SHEET_NAME = "규칙";
    private static final String FIXED_SHEET_NAME = "고정";
    private static final Pattern SINGLE_TAG_PATTERN = Pattern.compile("^\\[([^\\]]+)]$");
    private static final Pattern TAG_BLOCK_PATTERN = Pattern.compile("((?:\\[[^\\]]+])+)");
    private static final Pattern TAG_PATTERN = Pattern.compile("\\[([^\\]]+)]");

    public AccountBookMemoRules read(MultipartFile file, String originalFileName, ListBackedWarnings warnings) {
        String extension = getExtension(originalFileName);
        if (!"xlsx".equals(extension) && !"xls".equals(extension)) {
            return AccountBookMemoRules.defaults();
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheet(RULE_SHEET_NAME);
            if (sheet == null) {
                return AccountBookMemoRules.defaults();
            }
            return readRules(workbook, sheet);
        } catch (Exception e) {
            warnings.add(null, "규칙 시트를 읽지 못해 기본 메모 규칙을 사용합니다. reason=" + e.getMessage());
            log.warn("Failed to read account-book rule sheet. fileName={}", originalFileName, e);
            return AccountBookMemoRules.defaults();
        }
    }

    private AccountBookMemoRules readRules(Workbook workbook, Sheet sheet) {
        DataFormatter formatter = new DataFormatter(Locale.KOREA);
        Set<String> incomeCategoryTags = new LinkedHashSet<>();
        Set<String> nonIncomeCategoryTags = new LinkedHashSet<>();
        Set<String> recurrenceTags = new LinkedHashSet<>();
        Set<String> mainTags = new LinkedHashSet<>();

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            addTag(incomeCategoryTags, formatter.formatCellValue(row.getCell(0)));
            addTag(nonIncomeCategoryTags, formatter.formatCellValue(row.getCell(1)));
            addTag(recurrenceTags, formatter.formatCellValue(row.getCell(2)));
            addTag(mainTags, formatter.formatCellValue(row.getCell(3)));
            collectMemoExample(formatter.formatCellValue(row.getCell(6)), incomeCategoryTags, nonIncomeCategoryTags, mainTags);
            collectMemoExample(formatter.formatCellValue(row.getCell(7)), incomeCategoryTags, nonIncomeCategoryTags, mainTags);
        }

        collectFixedSheetExamples(workbook, formatter, incomeCategoryTags, nonIncomeCategoryTags, mainTags);

        return AccountBookMemoRules.of(mainTags, incomeCategoryTags, nonIncomeCategoryTags, recurrenceTags);
    }

    private void addTag(Set<String> tags, String value) {
        String tag = extractSingleTag(value);
        if (tag != null) {
            tags.add(tag);
        }
    }

    private String extractSingleTag(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        Matcher matcher = SINGLE_TAG_PATTERN.matcher(value.trim());
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group(1).trim();
    }

    private void collectFixedSheetExamples(
            Workbook workbook,
            DataFormatter formatter,
            Set<String> incomeCategoryTags,
            Set<String> nonIncomeCategoryTags,
            Set<String> mainTags
    ) {
        Sheet fixedSheet = workbook.getSheet(FIXED_SHEET_NAME);
        if (fixedSheet == null) {
            return;
        }

        for (int rowIndex = 0; rowIndex <= fixedSheet.getLastRowNum(); rowIndex++) {
            Row row = fixedSheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                collectMemoExample(
                        formatter.formatCellValue(row.getCell(cellIndex)),
                        incomeCategoryTags,
                        nonIncomeCategoryTags,
                        mainTags
                );
            }
        }
    }

    private void collectMemoExample(
            String value,
            Set<String> incomeCategoryTags,
            Set<String> nonIncomeCategoryTags,
            Set<String> mainTags
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        Matcher blockMatcher = TAG_BLOCK_PATTERN.matcher(value);
        if (!blockMatcher.find()) {
            return;
        }

        List<String> tags = extractTags(blockMatcher.group(1));
        if (tags.size() < 2) {
            return;
        }

        String mainTag = tags.get(0);
        if (!isKnownMainTag(mainTag)) {
            return;
        }
        mainTags.add(mainTag);

        int categoryIndex = 1;
        if (tags.size() > categoryIndex && isRecurrenceTag(tags.get(categoryIndex))) {
            categoryIndex++;
        }
        if (tags.size() <= categoryIndex) {
            return;
        }

        if ("수입".equals(mainTag)) {
            incomeCategoryTags.add(tags.get(categoryIndex));
            return;
        }
        nonIncomeCategoryTags.add(tags.get(categoryIndex));
    }

    private List<String> extractTags(String tagText) {
        return TAG_PATTERN.matcher(tagText)
                .results()
                .map(matchResult -> matchResult.group(1).trim())
                .toList();
    }

    private boolean isKnownMainTag(String tag) {
        return "수입".equals(tag)
                || "지출".equals(tag)
                || "저축".equals(tag)
                || "투자".equals(tag)
                || "이체".equals(tag)
                || "기타".equals(tag);
    }

    private boolean isRecurrenceTag(String tag) {
        return "고정".equals(tag) || "변동".equals(tag) || "일회성".equals(tag);
    }

    private String getExtension(String originalFileName) {
        if (!StringUtils.hasText(originalFileName) || !originalFileName.contains(".")) {
            return "";
        }
        return originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    public interface ListBackedWarnings {
        void add(Integer rowNumber, String message);
    }
}
