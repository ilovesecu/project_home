package com.ilovepc.project_home.repository;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import com.ilovepc.project_home.web.dhlottery.vo.param.LottoResultParam;
import com.ilovepc.project_home.web.dhlottery.vo.result.LottoDrawVOResult;
import org.apache.ibatis.annotations.Select;

@HomeMaster
public interface DhLotteryMasterMapper {

    @Select("CALL home_project.lottoDrawResultIns(" +
            // 1. 기본 정보
            "#{ltEpsd}, #{ltRflYmd}, #{gmSqNo}, " +
            // 2. 당첨 번호 (1~6, 보너스)
            "#{tm1WnNo}, #{tm2WnNo}, #{tm3WnNo}, #{tm4WnNo}, #{tm5WnNo}, #{tm6WnNo}, #{bnsWnNo}, " +
            // 3. 1등 당첨 정보
            "#{rnk1WnNope}, #{rnk1WnAmt}, #{rnk1SumWnAmt}, " +
            // 4. 2등 당첨 정보
            "#{rnk2WnNope}, #{rnk2WnAmt}, #{rnk2SumWnAmt}, " +
            // 5. 3등 당첨 정보
            "#{rnk3WnNope}, #{rnk3WnAmt}, #{rnk3SumWnAmt}, " +
            // 6. 4등 당첨 정보
            "#{rnk4WnNope}, #{rnk4WnAmt}, #{rnk4SumWnAmt}, " +
            // 7. 5등 당첨 정보
            "#{rnk5WnNope}, #{rnk5WnAmt}, #{rnk5SumWnAmt}, " +
            // 8. 전체 통계
            "#{sumWnNope}, #{rlvtEpsdSumNtslAmt}, #{wholEpsdSumNtslAmt}, " +
            // 9. 당첨 유형별 통계
            "#{winType0}, #{winType1}, #{winType2}, #{winType3}" +
            ")")
    void lottoDrawResultIns(LottoResultParam lottoResultParam);


    LottoDrawVOResult selectLottoDrawResult(int ltEpsd);
}
