package com.ilovepc.project_home.web.todo.vo.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TodoMatterResponse {
    private String text;          // 봇이 말할 내용
    private String response_type; // "in_channel" (공개) 또는 "ephemeral" (나만 보기)
    private String username;      // 봇 이름 오버라이드 (선택)
    private String icon_url;      // 봇 아이콘 오버라이드 (선택)
}
