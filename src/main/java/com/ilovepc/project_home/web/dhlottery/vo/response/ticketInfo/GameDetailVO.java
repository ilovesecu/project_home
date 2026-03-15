package com.ilovepc.project_home.web.dhlottery.vo.response.ticketInfo;

import lombok.Data;

import java.util.List;

@Data
public class GameDetailVO {
    private List<Integer> num; // 선택 번호 6개
    private int rank;
    private long amt;
    private String idx; // A, B, C...
    private int type;   // 3: 자동/수동 등 구분값
}
