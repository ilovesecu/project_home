package com.ilovepc.project_home.web.todo.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TodoKeywordResult {
    private Long keywordId;     // k.id
    private String mmUserId;    // k.mm_user_id
    private String keyword;     // k.name
    private Integer isDeleted;  // k.is_deleted
    private String keywordCreated; // k.created_at

    // 여기가 핵심: 1:N 관계를 담을 리스트
    private List<TodoTaskResult> tasks;
}
