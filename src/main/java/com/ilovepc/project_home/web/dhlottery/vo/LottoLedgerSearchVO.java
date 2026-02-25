package com.ilovepc.project_home.web.dhlottery.vo;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/*
https://www.dhlottery.co.kr/mypage/selectMyLotteryledger.do?
srchStrDt=20260216&srchEndDt=20260223&sort=&ltGdsCd=&winResult=&lramSmam=&pageNum=1&recordCountPerPage=10&_=1771834717992
 */


@Data
public class LottoLedgerSearchVO {
    // 조회 시작일 (예: 20260216)
    private String srchStrDt;

    // 조회 종료일 (예: 20260223)
    private String srchEndDt;

    // 정렬 기준
    private String sort;

    // 복권 상품 코드 (Lotto, Pension 등)
    private String ltGdsCd; //LP72-연금복권, LO40-로또

    // 당첨 결과 상태
    private String winResult;

    // 입금/출금 금액 관련 파라미터
    private String lramSmam;

    // 현재 페이지 번호
    private int pageNum;

    // 페이지당 출력 개수
    private int recordCountPerPage;

    // 캐시 방지용 타임스탬프 (언더바 파라미터)
    private long timeStamp; // _=1771834717992

    public void fillDefaultValues(){
        if( (!StringUtils.hasText(srchStrDt)) ||
            (!StringUtils.hasText(srchEndDt))){
            LocalDate nowDate = LocalDate.now();
            LocalDate before7Day = nowDate.minusDays(7);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            String now = formatter.format(nowDate);
            String before7 = formatter.format(before7Day);

            this.setSrchStrDt(before7);
            this.setSrchEndDt(now);
        }
        if(this.recordCountPerPage == 0){
            this.setRecordCountPerPage(10);
        }
        if(this.pageNum == 0){
            this.setPageNum(1);
        }
        setTimeStamp(System.currentTimeMillis());
    }
}
