package com.ilovepc.project_home.web.todo.controller;

import com.ilovepc.project_home.web.holiday.vo.HolidayTodayRequest;
import com.ilovepc.project_home.web.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/todo")
public class TodoController {
    private final TodoService todoService;

    @PostMapping("/add")
    public void todoAdd(){

    }

}
