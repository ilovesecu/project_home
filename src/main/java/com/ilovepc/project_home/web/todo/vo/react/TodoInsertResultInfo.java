package com.ilovepc.project_home.web.todo.vo.react;

import com.ilovepc.project_home.web.todo.vo.TodoAddTaskParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TodoInsertResultInfo {
    private int result;
    private List<TodoAddTaskParam> todoAddTaskParams;
}
