package com.ilovepc.project_home.web.todo.service;

import com.ilovepc.project_home.repository.ProjectMasterMapper;
import com.ilovepc.project_home.web.todo.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    public TodoAddResponse createTodos(TodoMatterRequest todoMatterRequest){
        TodoAddResponse todoAddResponse = TodoAddResponse.builder()
                .text("\uD83D\uDE00 기본응답 입니다. " + todoMatterRequest.getText())
                .response_type("in_channel") // 모두에게 보일지("in_channel"), 나에게만 보일지("ephemeral")
                .build();

        try{
            String text = todoMatterRequest.getText();
            String keyword = getKeyword(text);
            List<String> tasks = getTasks(text);
            if(!StringUtils.hasText(keyword)){
                todoAddResponse.setText("❌ 할 일이 추가되지 않았습니다. (키워드가 비었음.)");
                return todoAddResponse;
            }
            if(tasks.isEmpty()){
                todoAddResponse.setText("❌ 할 일이 추가되지 않았습니다. (할 일이 비었음.)");
                return todoAddResponse;
            }
            //1. 키워드 있는지 확인
            TodoAddKeywordResult keywordResult = projectMasterMapper.findKeyword(keyword);

            //1-1. 키워드 없을 시 새로생성
            if(keywordResult == null){
                TodoAddKeywordParam todoAddKeywordParam = TodoAddKeywordParam.builder()
                        .mmUserId(todoMatterRequest.getUserId())
                        .name(keyword)
                        .build();
                projectMasterMapper.insertKeyword(todoAddKeywordParam); // 실행 후 keyword.getId() 사용 가능
                keywordResult = new TodoAddKeywordResult();
                keywordResult.setId(todoAddKeywordParam.getId());
            }
            Long keywordId = keywordResult.getId();

            //2. 확보된 keyword.getId()를 통해 할 일 task 생성
            List<TodoAddTaskParam> taskParams = new ArrayList<>();
            for(String content : tasks){
                taskParams.add(TodoAddTaskParam.builder()
                        .keywordId(keywordId)
                        .content(content)
                        .build());
            }

            if (!taskParams.isEmpty()) {
                projectMasterMapper.insertTasksBulk(taskParams);
                todoAddResponse.setText("✅ 할 일이 추가 되었습니다! "+taskParams.stream().map(TodoAddTaskParam::getContent).collect(Collectors.joining(",")));
            }
            return todoAddResponse;
        }catch (Exception e){
            log.error("[createTodos] TodoAddRequest:{} error 발생", todoMatterRequest,e);
            todoAddResponse.setText("❌ 할 일이 추가되지 않았습니다. (에러 발생:"+e.getMessage()+")");
            return todoAddResponse;
        }
    }

    public void getListKeywordAndTask(TodoMatterRequest todoMatterRequest){
        List<String> keywords = getKeywordMulti(todoMatterRequest.getText());

        List<TodoKeywordResult> todoKeywordResults = projectMasterMapper.selectTasksByKeywords(keywords);

        log.info("aa:{}",todoKeywordResults);
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
