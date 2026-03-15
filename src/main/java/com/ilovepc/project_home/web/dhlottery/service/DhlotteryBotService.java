package com.ilovepc.project_home.web.dhlottery.service;

import com.ilovepc.project_home.web.dhlottery.component.DhlotteryCookieStore;
import com.ilovepc.project_home.web.dhlottery.component.DhlotteryHttpFactory;
import com.ilovepc.project_home.web.dhlottery.vo.response.search.LotteryGameHistoryResponse;
import com.ilovepc.project_home.web.dhlottery.vo.request.search.LottoLedgerSearchVO;
import com.ilovepc.project_home.web.dhlottery.vo.request.ticket.LottoTicketSearchVO;
import com.ilovepc.project_home.web.dhlottery.vo.response.ticketInfo.LotteryTicketInfoResponse;
import com.ilovepc.project_home.web.dhlottery.vo.response.ticketInfo.TicketVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@Slf4j
public class DhlotteryBotService {
    private final RestTemplate restTemplate;
    private final DhlotteryHttpFactory httpFactory;
    private final DhlotteryCookieStore cookieStore;
    private final DhlotteryLoginService loginService;

    public DhlotteryBotService(DhlotteryHttpFactory httpFactory,
                               DhlotteryCookieStore cookieStore,
                               DhlotteryLoginService loginService) {
        this.restTemplate = new RestTemplate();
        this.httpFactory = httpFactory;
        this.cookieStore = cookieStore;
        this.loginService = loginService;
    }

    private List<String> getUserCookieOrLogin(String userId, String password) {
        List<String> userCookies = cookieStore.getCookies(userId);
        //쿠키가 아예 없으면 바로 로그인
        if (userCookies == null || userCookies.isEmpty()) {
            log.info("[{}] 최초 접속, 로그인을 시도합니다.", userId);
            userCookies = loginService.loginAndGetCookie(userId, password);
            if (userCookies == null || userCookies.isEmpty()) {
                log.error("로그인에 실패하였습니다. userID:{}", userId);
                return null;
            }
        }
        return userCookies;
    }


    //티켓 정보 가져오기 (내가 찍은 번호 같은거)
    public LotteryTicketInfoResponse<TicketVO> getTicketInfo(String userId, String password, LottoTicketSearchVO lottoTicketSearchVO) {
        try {
            //String targetUrl = "https://www.dhlottery.co.kr/mypage/lotto645TicketDetail.do?ntslOrdrNo=2026022300554412078&srchStrDt=20260218&srchEndDt=20260225&barcd=628558293666620275202908649155&_=1771999762545";
            final String API_URL = "https://www.dhlottery.co.kr/mypage/lotto645TicketDetail.do";

            if (!StringUtils.hasText(lottoTicketSearchVO.getBarcd())) {
                //예외처리 바코드없으면 안됨.
                return null;
            }
            if (!StringUtils.hasText(lottoTicketSearchVO.getNtslOrdrNo())) {
                //예외처리 해당 값 없으면 안됨.
                return null;
            }
            List<String> userCookies = getUserCookieOrLogin(userId, password);
            if (userCookies == null || userCookies.isEmpty()) {
                log.error("[getTicketInfo] 로그인 실패! userId:{}", userId);
                return null;
            }
            String uriString = UriComponentsBuilder.fromUriString(API_URL)
                    .queryParam("ntslOrdrNo", lottoTicketSearchVO.getNtslOrdrNo())
                    .queryParam("barcd", lottoTicketSearchVO.getBarcd())
                    .queryParam("srchStrDt", lottoTicketSearchVO.getSrchStrDt())
                    .queryParam("srchEndDt", lottoTicketSearchVO.getSrchEndDt())
                    .queryParam("_", lottoTicketSearchVO.getTimeStamp())
                    .build(true)//이미 인코딩 했을 때 True
                    .toUriString();

            LotteryTicketInfoResponse<TicketVO> lotteryTicketInfoResponse = sendApiGet(uriString, userCookies, new ParameterizedTypeReference<LotteryTicketInfoResponse<TicketVO>>() {
            });
            return lotteryTicketInfoResponse;
        } catch (Exception e) {
            log.error("[getTicketInfo] Exception:", e);
            return null;
        }
    }

    //동행복권 추첨결과 가져오기
    public LotteryGameHistoryResponse getLedger(String userId, String password, LottoLedgerSearchVO searchVO) {
        try {
            final String API_URL = "https://www.dhlottery.co.kr/mypage/selectMyLotteryledger.do";
            String uriString = UriComponentsBuilder.fromUriString(API_URL)
                    .queryParam("srchStrDt", searchVO.getSrchStrDt())
                    .queryParam("srchEndDt", searchVO.getSrchEndDt())
                    .queryParam("sort", searchVO.getSort())
                    .queryParam("ltGdsCd", searchVO.getLtGdsCd())
                    .queryParam("winResult", searchVO.getWinResult())
                    .queryParam("lramSmam", searchVO.getLramSmam())
                    .queryParam("pageNum", searchVO.getPageNum())
                    .queryParam("recordCountPerPage", searchVO.getRecordCountPerPage())
                    .queryParam("_", searchVO.getTimeStamp())
                    .build(true) //이미 인코딩했다면 url (Encoding키를 사용했을 떄)
                    .toUriString();
            //String example = "https://www.dhlottery.co.kr/mypage/selectMyLotteryledger.do?srchStrDt=20260215&srchEndDt=20260223&sort=&ltGdsCd=&winResult=&lramSmam=&pageNum=1&recordCountPerPage=10&_=1771694446306";

        /*List<String> userCookies = cookieStore.getCookies(userId);
        //쿠키가 아예 없으면 바로 로그인
        if(userCookies == null || userCookies.isEmpty()){
            log.info("[{}] 최초 접속, 로그인을 시도합니다.", userId);
            userCookies = loginService.loginAndGetCookie(userId,password);
            if(userCookies == null || userCookies.isEmpty()){
                log.error("로그인에 실패하였습니다. userID:{}", userId);
                return null;
            }
        }*/
            List<String> userCookies = getUserCookieOrLogin(userId, password);

            return sendApiGet(uriString, userCookies, new ParameterizedTypeReference<LotteryGameHistoryResponse>() {
            });
        } catch (Exception e) {
            log.error("[getLedger] Exception:", e);
            return null;
        }
    }

    public <T> T sendApiGet(String uriString, List<String> userCookies, ParameterizedTypeReference<T> responseType) {
        HttpEntity<String> entity = httpFactory.createEntityWithCookie(userCookies);
        // API 요청 (쿠키가 섞이지 않고 안전하게 전송됨)
        ResponseEntity<T> response = restTemplate.exchange(
                uriString,
                HttpMethod.GET,
                entity,
                responseType
        );
        return response.getBody();
    }
}
