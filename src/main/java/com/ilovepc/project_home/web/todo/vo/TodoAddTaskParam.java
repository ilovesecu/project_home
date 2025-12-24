package com.ilovepc.project_home.web.todo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoAddTaskParam {
    private Long id;
    private Long keywordId;   // DB: keyword_id
    private String content;   // DB: content
    private Integer status;   // DB: status (기본값 0)
    private String createdAt; // DB: created_at
}
