package com.ilovepc.project_home.web.todo.vo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TodoTaskResult {
    private Long taskId;      // t.id
    private String content;   // t.content
    private Integer status;   // t.status
    private String taskCreated; // t.created_at
}
