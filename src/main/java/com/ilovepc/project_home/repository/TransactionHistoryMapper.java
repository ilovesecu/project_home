package com.ilovepc.project_home.repository;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@HomeMaster
public interface TransactionHistoryMapper {
    int insertTransactionHistories(@Param("transactionHistories") List<TransactionHistoryParam> transactionHistories);
}
