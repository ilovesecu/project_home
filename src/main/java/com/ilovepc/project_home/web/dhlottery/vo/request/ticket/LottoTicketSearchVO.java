package com.ilovepc.project_home.web.dhlottery.vo.request.ticket;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class LottoTicketSearchVO {
    //ntslOrdrNo=2026022300554412078&srchStrDt=20260301&srchEndDt=20260328&barcd=628558293666620275202908649155&_=1773398164904
    private String ntslOrdrNo="";
    private String barcd = "";
    private String srchStrDt;
    private String srchEndDt;
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
        setTimeStamp(System.currentTimeMillis());
    }
}
