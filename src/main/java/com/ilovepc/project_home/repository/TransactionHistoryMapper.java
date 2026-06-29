package com.ilovepc.project_home.repository;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import com.ilovepc.project_home.web.accountbook.vo.AccountCategoryParam;
import com.ilovepc.project_home.web.accountbook.vo.AccountCategoryResult;
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
}
