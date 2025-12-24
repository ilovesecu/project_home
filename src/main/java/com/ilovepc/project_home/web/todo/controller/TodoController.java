package com.ilovepc.project_home.web.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.web.holiday.vo.HolidayTodayRequest;
import com.ilovepc.project_home.web.todo.service.TodoService;
import com.ilovepc.project_home.web.todo.vo.TodoAddRequest;
import com.ilovepc.project_home.web.todo.vo.TodoAddResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
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
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public TodoAddResponse todoAdd(@RequestParam Map<String,String> paramMap){
        //application/x-www-form-urlencoded 데이터를 받을 때는 CamelCase로 자동변환 시켜주지않음. (변수명이 딱 일치해야함)
        //그래서 일단 Map으로 받고, ObjectMapper로 변환하기로함.
        TodoAddRequest todoAddRequest = om.convertValue(paramMap, TodoAddRequest.class);
        log.info("TODO ADD COMMAND EXEC : {}", todoAddRequest);

        TodoAddResponse todos = todoService.createTodos(todoAddRequest);

        // 4. 응답 (메타모스트 형식)
        return todos;
    }

}
