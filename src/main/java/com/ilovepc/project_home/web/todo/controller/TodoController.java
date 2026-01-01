package com.ilovepc.project_home.web.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.web.todo.service.TodoService;
import com.ilovepc.project_home.web.todo.vo.TodoKeywordResult;
import com.ilovepc.project_home.web.todo.vo.TodoMatterRequest;
import com.ilovepc.project_home.web.todo.vo.TodoMatterResponse;
import com.ilovepc.project_home.web.todo.vo.react.TodoInsertResultInfo;
import com.ilovepc.project_home.web.todo.vo.react.TodoKeywordInsResultInfo;
import com.ilovepc.project_home.web.todo.vo.react.TodoKeywordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/todo")
public class TodoController {
    private final TodoService todoService;
    private final ObjectMapper om;

    @PostMapping(
        value = "/add",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResponse<TodoInsertResultInfo> todoAdd(@RequestBody TodoMatterRequest todoMatterRequest){
        log.info("TODO ADD COMMAND EXEC : {}", todoMatterRequest);
        TodoInsertResultInfo todoReact = todoService.createTodoReact(todoMatterRequest);
        return ApiResponse.success(todoReact);
    }

    @PostMapping(
            value = "/add",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public TodoMatterResponse todoAdd(@RequestParam Map<String,String> paramMap){
        //application/x-www-form-urlencoded 데이터를 받을 때는 CamelCase로 자동변환 시켜주지않음. (변수명이 딱 일치해야함)
        //그래서 일단 Map으로 받고, ObjectMapper로 변환하기로함.
        TodoMatterRequest todoMatterRequest = om.convertValue(paramMap, TodoMatterRequest.class);
        log.info("TODO ADD COMMAND EXEC : {}", todoMatterRequest);
        TodoMatterResponse todos = todoService.createTodosMatter(todoMatterRequest);

        // 4. 응답 (메타모스트 형식)
        return todos;
    }

    @PostMapping(
            value = "/list",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public TodoMatterResponse todoList(@RequestParam Map<String,Object>paramMap){
        //application/x-www-form-urlencoded 데이터를 받을 때는 CamelCase로 자동변환 시켜주지않음. (변수명이 딱 일치해야함)
        //그래서 일단 Map으로 받고, ObjectMapper로 변환하기로함.
        TodoMatterRequest todoMatterRequest = om.convertValue(paramMap, TodoMatterRequest.class);
        log.info("TODO ADD COMMAND EXEC : {}", todoMatterRequest);

        String listKeywordAndTask = todoService.getListKeywordAndTask(todoMatterRequest);
        return TodoMatterResponse.builder()
                .response_type("in_channel") //or "ephemeral" (나에게만 보이기)
                .text(listKeywordAndTask)
                .build();
    }

    //REACT 용
    @GetMapping("/list/all")
    public List<TodoKeywordResult> todoListAll(){
        return todoService.getTodoListAll();
    }

    //REACT 용
    @DeleteMapping(value = "/delete/task/{taskId}")
    public ApiResponse<Integer> deleteTask(@PathVariable("taskId")int taskId){
        int deleteResult = todoService.deleteTask(taskId);
        return ApiResponse.success(deleteResult);
    }
    //REACT 용
    @PatchMapping(value = "/toggle/task/{taskId}")
    public ApiResponse<Integer> toggleTask(@PathVariable("taskId")int taskId){
        int toggleResult = todoService.toggleTask(taskId);
        return ApiResponse.success(toggleResult);
    }

    //REACT 용
    @PostMapping(value = "/add/keyword")
    public ApiResponse<TodoKeywordInsResultInfo> addKeyword(@RequestBody TodoKeywordRequest todoKeywordRequest){
        TodoKeywordInsResultInfo keyword = todoService.createKeyword(todoKeywordRequest.getKeyword(), todoKeywordRequest.getMmUserId());
        return ApiResponse.success(keyword);
    }

    @DeleteMapping(value="/keyword/{keywordId}")
    public ApiResponse<Void> deleteKeyword(@PathVariable("keywordId")int keywordId){
        todoService.deleteKeyword(keywordId);
        return ApiResponse.success();
    }
}
