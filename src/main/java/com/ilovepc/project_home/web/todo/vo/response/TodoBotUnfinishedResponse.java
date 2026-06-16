package com.ilovepc.project_home.web.todo.vo.response;

import com.ilovepc.project_home.web.todo.vo.TodoKeywordResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TodoBotUnfinishedResponse {
    private int totalCnt;   //미완료된 갯수
    private List<TodoKeywordResult> unfinishedTodos; //미완료된 투두 리스트 --> 해당 VO는 MATTER_BOT 에도 복사해둘 예정입니닷.
}
