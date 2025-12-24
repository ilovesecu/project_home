package com.ilovepc.project_home.web.todo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoAddKeywordParam {
    private Long id;
    private String mmUserId;  // DB: mm_user_id
    private String name;      // DB: name
    private Integer isDeleted;// DB: is_deleted (기본값 0)
    private String createdAt; // DB: created_at
}
