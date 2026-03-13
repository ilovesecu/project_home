package com.ilovepc.project_home.web.dhlottery.vo;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
public class LottoTicketSearchVO {
    //ntslOrdrNo=2026022300554412078&srchStrDt=20260301&srchEndDt=20260328&barcd=628558293666620275202908649155&_=1773398164904
    private String ntslOrdrNo;
    private String barcd;
    private String srchStrDt;
    private String srchEndDt;
    // 캐시 방지용 타임스탬프 (언더바 파라미터)
    private long timeStamp; // _=1771834717992
}
