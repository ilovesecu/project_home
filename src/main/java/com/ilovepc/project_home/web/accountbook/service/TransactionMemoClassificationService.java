package com.ilovepc.project_home.web.accountbook.service;

import com.ilovepc.project_home.repository.TransactionHistoryMapper;
import com.ilovepc.project_home.web.accountbook.classification.AccountBookMemoParser;
import com.ilovepc.project_home.web.accountbook.classification.AccountBookMemoRules;
import com.ilovepc.project_home.web.accountbook.classification.AccountBookRuleSheetReader;
import com.ilovepc.project_home.web.accountbook.classification.MemoClassificationResult;
import com.ilovepc.project_home.web.accountbook.classification.TransactionMemoClassificationResult;
import com.ilovepc.project_home.web.accountbook.vo.AccountCategoryParam;
import com.ilovepc.project_home.web.accountbook.vo.AccountCategoryResult;
import com.ilovepc.project_home.web.accountbook.vo.ClassificationStatus;
import com.ilovepc.project_home.web.accountbook.vo.FixedStatus;
import com.ilovepc.project_home.web.accountbook.vo.MemoParseStatus;
import com.ilovepc.project_home.web.accountbook.vo.RecurrenceType;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryParam;
import com.ilovepc.project_home.web.accountbook.vo.TransactionParseError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionMemoClassificationService {
    //업로드 파서가 만든 TransactionHistoryParam 목록에 메모 분류 결과를 입히는 서비스입니다.
    //정상 메모면 classificationStatus=MANUAL, 빈 메모나 이상한 태그면 UNCLASSIFIED로 두고 warning에 담습니다.
    //카테고리를 account_category에 upsert한 뒤 categoryId를 거래에 매핑합니다.

    private final AccountBookRuleSheetReader ruleSheetReader;
    private final AccountBookMemoParser memoParser;
    private final TransactionHistoryMapper transactionHistoryMapper;

    public TransactionMemoClassificationResult classify(
            MultipartFile file,
            String originalFileName,
            List<TransactionHistoryParam> transactions,
            List<TransactionParseError> warnings
    ) {
        if (transactions.isEmpty()) {
            return new TransactionMemoClassificationResult(transactions, 0);
        }

        WarningCollector warningCollector = new WarningCollector(warnings);
        AccountBookMemoRules rules = ruleSheetReader.read(file, originalFileName, warningCollector::add); //엑셀의 규칙/고정 시트 읽기
        Set<AccountCategoryParam> categoryParams = collectRuleCategoryParams(rules);
        List<TransactionHistoryParam> classifiedTransactions = new ArrayList<>(transactions.size());

        for (TransactionHistoryParam transaction : transactions) {
            MemoClassificationResult result = memoParser.parse(transaction.getMemo(), rules);   //메모 파싱
            TransactionHistoryParam classifiedTransaction = applyClassification(transaction, result); //기존 param에 메모 관련 정보들 업데이트 해서 param을 다시 만들어준다.
            classifiedTransactions.add(classifiedTransaction);

            if (result.isParsed()) {
                categoryParams.add(new AccountCategoryParam(
                        result.getCashFlowType().name(),
                        result.getCategoryName(),
                        categoryParams.size()
                ));
            } else {
                warningCollector.add(
                        transaction.getSourceRowNumber(),
                        "메모 분류 제외: " + result.getMessage()
                );
            }
        }

        Map<String, AccountCategoryResult> categoryMap = syncAndLoadCategories(categoryParams);
        List<TransactionHistoryParam> categoryMappedTransactions = mapCategoryIds(classifiedTransactions, categoryMap, warningCollector);   //거래내역-카테고리 맵핑

        return new TransactionMemoClassificationResult(categoryMappedTransactions, warningCollector.count());
    }

    private TransactionHistoryParam applyClassification(
            TransactionHistoryParam transaction,
            MemoClassificationResult result
    ) {
        if (!result.isParsed()) {
            return transaction.toBuilder()
                    .recurrenceType(RecurrenceType.NONE.name())
                    .memoParseStatus(result.getMemoParseStatus().name())
                    .classificationStatus(ClassificationStatus.UNCLASSIFIED.name())
                    .fixedStatus(FixedStatus.NONE.name())
                    .isFixed(0)
                    .build();
        }

        boolean fixed = result.getRecurrenceType() == RecurrenceType.FIXED;
        return transaction.toBuilder()
                .cashflowType(result.getCashFlowType().name())
                .recurrenceType(result.getRecurrenceType().name())
                .memoOwner(result.getMemoOwner())
                .memoBody(result.getMemoBody())
                .memoTargetYearMonth(result.getMemoTargetYearMonth())
                .memoParseStatus(MemoParseStatus.PARSED.name())
                .memoCategoryName(result.getCategoryName())
                .classificationStatus(ClassificationStatus.MANUAL.name())
                .fixedStatus(fixed ? FixedStatus.MANUAL.name() : FixedStatus.NONE.name())
                .isFixed(fixed ? 1 : 0)
                .build();
    }

    private Set<AccountCategoryParam> collectRuleCategoryParams(AccountBookMemoRules rules) {
        Set<AccountCategoryParam> categoryParams = new LinkedHashSet<>();
        int sortOrder = 0;
        for (String categoryName : rules.getIncomeCategoryTags()) {
            categoryParams.add(new AccountCategoryParam("INCOME", categoryName, sortOrder++));
        }
        for (String categoryName : rules.getNonIncomeCategoryTags()) {
            categoryParams.add(new AccountCategoryParam("EXPENSE", categoryName, sortOrder++));
        }
        return categoryParams;
    }

    private Map<String, AccountCategoryResult> syncAndLoadCategories(Set<AccountCategoryParam> categoryParams) {
        List<AccountCategoryParam> categories = categoryParams.stream() //CashFlow있고, Name있는 유효한 카테고리만 남긴다. (EXPENSE | 식비, EXPENSE | 보험, INCOME | 급여 등)
                .filter(category -> StringUtils.hasText(category.getCashflowType()))
                .filter(category -> StringUtils.hasText(category.getName()))
                .toList();
        if (categories.isEmpty()) {
            return Map.of();
        }

        transactionHistoryMapper.upsertAccountCategories(categories);   //있으면 수정, 없으면 INSERT 해준다.

        //거래내역에 카테고리ID를 넣기위해 DB에서 카테고리 긁어온다. (upsert 한것 포함해서 조회 하기 위해)
        return transactionHistoryMapper.selectAccountCategories(categories)
                .stream()
                .collect(Collectors.toMap(this::categoryKey, Function.identity(), (left, right) -> left));
    }

    private List<TransactionHistoryParam> mapCategoryIds(
            List<TransactionHistoryParam> transactions,
            Map<String, AccountCategoryResult> categoryMap,
            WarningCollector warningCollector
    ) {
        List<TransactionHistoryParam> mappedTransactions = new ArrayList<>(transactions.size());
        for (TransactionHistoryParam transaction : transactions) {
            if (!StringUtils.hasText(transaction.getMemoCategoryName())
                    || !StringUtils.hasText(transaction.getCashflowType())) {
                mappedTransactions.add(transaction);
                continue;
            }

            AccountCategoryResult category = categoryMap.get(categoryKey(
                    transaction.getCashflowType(),
                    transaction.getMemoCategoryName()
            ));
            if (category == null) {
                warningCollector.add(
                        transaction.getSourceRowNumber(),
                        "카테고리 ID를 찾지 못했습니다. cashflowType="
                                + transaction.getCashflowType()
                                + ", categoryName="
                                + transaction.getMemoCategoryName()
                );
                mappedTransactions.add(transaction);
                continue;
            }

            //거래내역에 카테고리ID 맵핑
            mappedTransactions.add(transaction.toBuilder()
                    .categoryId(category.getId())
                    .build());
        }
        return mappedTransactions;
    }

    private String categoryKey(AccountCategoryResult category) {
        return categoryKey(category.getCashflowType(), category.getName());
    }

    private String categoryKey(String cashflowType, String categoryName) {
        return cashflowType + "|" + categoryName;
    }

    private static class WarningCollector {
        private final List<TransactionParseError> warnings;
        private int count;

        private WarningCollector(List<TransactionParseError> warnings) {
            this.warnings = warnings;
        }

        private void add(Integer rowNumber, String message) {
            count++;
            if (warnings.size() < 50) {
                warnings.add(TransactionParseError.builder()
                        .rowNumber(rowNumber)
                        .message(message)
                        .build());
            }
        }

        private int count() {
            return count;
        }
    }
}
