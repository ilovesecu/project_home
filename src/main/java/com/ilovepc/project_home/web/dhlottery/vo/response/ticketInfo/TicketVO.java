package com.ilovepc.project_home.web.dhlottery.vo.response.ticketInfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TicketVO {
    @JsonProperty("serv_trans_no")
    private String servTransNo;

    @JsonProperty("term_trans_no")
    private String termTransNo;

    @JsonProperty("ticket_amt")
    private int ticketAmt;

    @JsonProperty("shop_code")
    private String shopCode;

    @JsonProperty("game_round")
    private int gameRound;

    @JsonProperty("pay_end_date")
    private String payEndDate;

    @JsonProperty("sale_seqno")
    private String saleSeqno;

    @JsonProperty("svr_ntss_dttm")
    private String svrNtssDttm;

    @JsonProperty("win_total_amt")
    private long winTotalAmt;

    @JsonProperty("win_num")
    private List<Integer> winNum; // 당첨 번호 (보너스 포함 7개)

    @JsonProperty("draw_date")
    private String drawDate;

    @JsonProperty("sale_date")
    private String saleDate;

    private int games;
    private boolean drawed;
    private String barcode;

    @JsonProperty("game_dtl")
    private List<GameDetailVO> gameDtl; // 구매한 게임 리스트

    @JsonProperty("seller_id")
    private String sellerId;
}
