package com.ilovepc.project_home.web.todo.service;

import com.ilovepc.project_home.repository.ProjectMasterMapper;
import com.ilovepc.project_home.web.todo.vo.*;
import com.ilovepc.project_home.web.todo.vo.react.TodoInsertResultInfo;
import com.ilovepc.project_home.web.todo.vo.react.TodoKeywordInsResultInfo;
import com.ilovepc.project_home.web.todo.vo.response.TodoBotUnfinishedResponse;
import com.ilovepc.project_home.web.todo.vo.response.TodoMatterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TodoService {
    private final ProjectMasterMapper projectMasterMapper;

    public TodoInsertResultInfo createTodoReact(TodoMatterRequest todoMatterRequest){
        String text = todoMatterRequest.getText();
        String userId = todoMatterRequest.getUserId();
        return this.createTodo(text, userId);
    }

    public int deleteTask(int taskId){
        return this.deleteTaskCommon(taskId);
    }

    public int toggleTask(int taskId){
        return this.toggleTaskCommon(taskId);
    }

    public TodoKeywordInsResultInfo createKeyword(String keyword, String userId){
        return this.createKeywordCommon(keyword, userId);
    }

    public int deleteKeyword(int keywordId){
        this.deleteKeywordCommon(keywordId);
        return 1;
    }

    public TodoBotUnfinishedResponse getUnfinishedTodoListAll(){
        List<TodoKeywordResult> unfinishedTodos = projectMasterMapper.getUnfinishedTodos();
        if(unfinishedTodos == null || unfinishedTodos.isEmpty()){ unfinishedTodos = new ArrayList<>(); }
        log.error("unfinishedTodos : {}",unfinishedTodos);
        return TodoBotUnfinishedResponse.builder()
                .totalCnt(unfinishedTodos.size())
                .unfinishedTodos(unfinishedTodos)
                .build();
    }

    public TodoMatterResponse createTodosMatter(TodoMatterRequest todoMatterRequest){
        TodoMatterResponse todoMatterResponse = TodoMatterResponse.builder()
                .text("\uD83D\uDE00 기본응답 입니다. " + todoMatterRequest.getText())
                .response_type("in_channel") // 모두에게 보일지("in_channel"), 나에게만 보일지("ephemeral")
                .build();

        try{
            String text = todoMatterRequest.getText();
            String userId = todoMatterRequest.getUserId();
            TodoInsertResultInfo todoInsertResultInfo = this.createTodo(text, userId);
            switch (todoInsertResultInfo.getResult()){
                case -1 -> todoMatterResponse.setText("❌ 할 일이 추가되지 않았습니다. (키워드가 비었음.)");
                case -2 -> todoMatterResponse.setText("❌ 할 일이 추가되지 않았습니다. (할 일이 비었음.)");
                case 0 -> todoMatterResponse.setText("❌ 할 일이 추가되지 않았습니다. (오류 발생)");
                default -> todoMatterResponse.setText("✅ 할 일이 추가 되었습니다! "+todoInsertResultInfo.getTodoAddTaskParams().stream().map(TodoAddTaskParam::getContent).collect(Collectors.joining(",")));
            }

            return todoMatterResponse;
        }catch (Exception e){
            log.error("[createTodos] TodoAddRequest:{} error 발생", todoMatterRequest,e);
            todoMatterResponse.setText("❌ 할 일이 추가되지 않았습니다. (에러 발생:"+e.getMessage()+")");
            return todoMatterResponse;
        }
    }

    private TodoKeywordInsResultInfo createKeywordCommon(String keyword, String userId){
        //1. 키워드 있는지 확인
        TodoAddKeywordResult keywordResult = projectMasterMapper.findKeyword(keyword);
        //2. 키워드 없을 시 새로 생성
        if(keywordResult == null){
            TodoAddKeywordParam todoAddKeywordParam = TodoAddKeywordParam.builder()
                    .mmUserId(userId)
                    .name(keyword)
                    .build();
            int insResult = projectMasterMapper.insertKeyword(todoAddKeywordParam); // 실행 후 keyword.getId() 사용 가능
            keywordResult = new TodoAddKeywordResult();
            keywordResult.setId(todoAddKeywordParam.getId());
            keywordResult.setName(keyword);
            keywordResult.setIsDeleted(0);
            keywordResult.setCreatedAt(LocalDateTime.now().toString());
            keywordResult.setMmUserId(userId);
            return new TodoKeywordInsResultInfo(insResult, keywordResult);
        }
        return new TodoKeywordInsResultInfo(-1, keywordResult);
    }

    private TodoInsertResultInfo createTodo(String text, String userId){
        String keyword = getKeyword(text);
        List<String> tasks = getTasks(text);
        if(!StringUtils.hasText(keyword)){
            return new TodoInsertResultInfo(-1, null);
        }
        if(tasks.isEmpty()){
            return new TodoInsertResultInfo(-2, null);
        }
        TodoKeywordInsResultInfo todoKeywordInsResultInfo = this.createKeywordCommon(keyword, userId);
        Long keywordId = todoKeywordInsResultInfo.getTodoAddKeywordResult().getId();

        //2. 확보된 keyword.getId()를 통해 할 일 task 생성
        List<TodoAddTaskParam> taskParams = new ArrayList<>();
        for(String content : tasks){
            taskParams.add(TodoAddTaskParam.builder()
                    .keywordId(keywordId)
                    .content(content)
                    .status(0)
                    .build());
        }

        if (!taskParams.isEmpty()) {
            //1 : 새로운 행이 INSERT 된 경우
            //2 : UPDATE (충돌)
            //0 : 변경사항 없는 경우
            int insertSum = projectMasterMapper.insertTasksBulk(taskParams);
            if(insertSum != taskParams.size()){
                log.error("TODO INSERT 중 에러 발생");
            }
            return new TodoInsertResultInfo(insertSum, taskParams);
        }
        return new TodoInsertResultInfo(0, taskParams);
    }

    private int toggleTaskCommon(int taskId){
        int updateCnt = projectMasterMapper.toggleTask(taskId);
        if(updateCnt > 0){
            return taskId;
        }
        return -1;
    }

    private int deleteTaskCommon(int taskId){
        int deleteCnt = projectMasterMapper.deleteTask(taskId);
        if(deleteCnt > 0){
            return taskId;
        }
        return -1;
    }

    private int deleteKeywordCommon(int keywordId){
        if(keywordId > 0){
            projectMasterMapper.deleteKeyword(keywordId);
            return 1;
        }
        return 0;
    }

    public String getListKeywordAndTask(TodoMatterRequest todoMatterRequest){
        List<String> keywords = getKeywordMulti(todoMatterRequest.getText());

        List<TodoKeywordResult> todoKeywordResults = projectMasterMapper.selectTasksByKeywords(keywords);
        String todoListMessage = createTodoListMessage(todoKeywordResults);
        return todoListMessage;
    }

    //React로 전송할 모든 Todo List
    public List<TodoKeywordResult> getTodoListAll(){
        return projectMasterMapper.selectTaskAll();
    }

    private String createTodoListMessage(List<TodoKeywordResult> results){
        if(results == null || results.isEmpty()){
            return "조회된 할 일이 없습니다. 텅 비었네요! 📭";
        }
        StringBuilder sb = new StringBuilder();
        // 전체 제목
        sb.append("### 📋 검색된 할 일 목록\n");
        sb.append("---\n"); // 구분선

        for(TodoKeywordResult keywordResult : results){
            // 1. 키워드 제목 (Bold 처리 및 이모지)
            sb.append("#### 🏷️ **").append(keywordResult.getKeyword()).append("**");
            sb.append(" (").append(keywordResult.getKeywordCreated()).append(")\n");

            List<TodoTaskResult> tasks = keywordResult.getTasks();
            if(tasks != null && !tasks.isEmpty()){
                // 2. 할 일 목록 (체크박스 스타일)
                for (TodoTaskResult task : tasks) {
                    // Mattermost는 "- [ ]" 를 체크박스로 렌더링하지는 않지만 리스트처럼 show.
                    // 식별을 위해 ID를 괄호에 작게 넣어두면 나중에 삭제/완료할 때 사용.
                    sb.append("- ")
                            .append(task.getContent())
                            .append(" `[ID: ").append(task.getTaskId()).append("]`") // ID를 코드 블럭으로 표시
                            .append("\n");
                }
            }
        }

        return sb.toString();
    }

    public String getKeyword(String text) {
        // 1. 유효성 검사 (null이나 빈 문자열 체크)
        if (!StringUtils.hasText(text)) {
            return null;
        }
        // 2. 앞뒤 공백 제거 (사용자 오타 방지)
        String trimmedText = text.trim();
        // 3. 첫 번째 공백의 위치 찾기
        int spaceIndex = trimmedText.indexOf(" ");
        // 4. 공백이 없으면? (예: "/kd 다이소" 처럼 뒤에 내용 없이 키워드만 왔을 경우)
        if (spaceIndex == -1) {
            return trimmedText; // 전체가 곧 키워드
        }
        // 5. 첫 공백 앞까지만 잘라서 반환
        return trimmedText.substring(0, spaceIndex);
    }

    public List<String> getKeywordMulti(String message) {
        // 메시지가 비어있거나 null인 경우 빈 리스트 반환
        if (message == null || message.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 공백(스페이스, 탭 등)을 기준으로 문자열 분리
        // "\\s+"는 하나 이상의 공백을 의미
        String[] parts = message.trim().split("\\s+");

        // 2. 명령어(/kl)만 있고 키워드가 없는 경우 빈 리스트 반환
        if (parts.length == 0) {
            return Collections.emptyList();
        }

        // 3. 명령어(index 0)를 제외하고 나머지 단어들을 수집
        return new ArrayList<>(Arrays.asList(parts));
    }

    public List<String> getTasks(String text) {
        // 1. 유효성 검사
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }

        String trimmedText = text.trim();
        int spaceIndex = trimmedText.indexOf(" ");

        // 2. 공백이 없으면 키워드만 있는 경우 (할 일 없음)
        if (spaceIndex == -1) {
            return Collections.emptyList();
        }

        // 3. 키워드 뒷부분 내용만 잘라내기
        // 예: "냄비 구매, 할 일 개발, 하하잉"
        String content = trimmedText.substring(spaceIndex + 1);

        // 4. 쉼표(,) 기준으로 자르고 정리하기 (Stream API 사용)
        return Arrays.stream(content.split(",")) // 1) 쉼표로 분리
                .map(String::trim)              // 2) 각 할 일의 앞뒤 공백 제거 (" 할 일 " -> "할 일")
                .filter(StringUtils::hasText)   // 3) 빈 값 제거 (예: "a,,b" 했을 때 중간 공백 무시)
                .collect(Collectors.toList());  // 4) 리스트로 변환
    }
}
