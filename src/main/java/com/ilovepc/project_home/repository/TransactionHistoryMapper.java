package com.ilovepc.project_home.repository;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import com.ilovepc.project_home.web.accountbook.vo.AccountCategoryParam;
import com.ilovepc.project_home.web.accountbook.vo.AccountCategoryResult;
import com.ilovepc.project_home.web.accountbook.vo.RecurrenceClassificationSummaryResult;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryParam;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryResult;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistorySearchParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@HomeMaster
public interface TransactionHistoryMapper {
    int insertTransactionHistories(@Param("transactionHistories") List<TransactionHistoryParam> transactionHistories);

    List<TransactionHistoryResult> selectTransactionHistories(
            @Param("searchParam") TransactionHistorySearchParam searchParam
    );
    int countTransactionHistories(@Param("searchParam") TransactionHistorySearchParam searchParam);

    int upsertAccountCategories(@Param("categories") List<AccountCategoryParam> categories);

    List<AccountCategoryResult> selectAccountCategories(@Param("categories") List<AccountCategoryParam> categories);

    //카테고리 뽑아오기(전체)
    List<AccountCategoryResult> selectMakeMemoCategories();

    //토스모임카드 예시용 10개 뽑아오기
    List<TransactionHistoryResult> selectExample10();

    /**
     * 같은 반복 패턴 키를 가진 과거 거래 예시를 조회합니다.
     * MANUAL 분류를 우선으로 정렬해서 자동 메모 추천의 강한 근거로 사용합니다.
     */
    List<TransactionHistoryResult> selectClassificationEvidenceByPatternKey(
            @Param("recurrencePatternKey") String recurrencePatternKey,
            @Param("limit") int limit
    );

    /**
     * 엄격한 반복 패턴 키와 메모 중심 fallback 키를 함께 사용해서 과거 거래 예시를 조회합니다.
     */
    List<TransactionHistoryResult> selectClassificationEvidenceByPatternKeys(
            @Param("recurrencePatternKey") String recurrencePatternKey,
            @Param("recurrenceFallbackKey") String recurrenceFallbackKey,
            @Param("limit") int limit
    );

    /**
     * 같은 반복 패턴 키 안에서 카테고리/주체/반복유형 조합별 누적 근거 개수를 조회합니다.
     */
    List<RecurrenceClassificationSummaryResult> selectClassificationSummaryByPatternKey(
            @Param("recurrencePatternKey") String recurrencePatternKey
    );

    /**
     * 엄격한 반복 패턴 키와 메모 중심 fallback 키를 함께 사용해서 분류 요약을 조회합니다.
     */
    List<RecurrenceClassificationSummaryResult> selectClassificationSummaryByPatternKeys(
            @Param("recurrencePatternKey") String recurrencePatternKey,
            @Param("recurrenceFallbackKey") String recurrenceFallbackKey
    );
}
