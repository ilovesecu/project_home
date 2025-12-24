package com.ilovepc.project_home.web.todo.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
// ▼ 이 한 줄이면 user_id -> userId 로 자동 매핑
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TodoAddRequest {
    private String channelId;   //명령어가 실행된 채널 고유 ID, channel_id -> etto6py87tgntfhzcgntfj1jmy
    private String channelName; //채널 이름, channel_name -> 4q1u8wsgojn47m5m7ozxohxwsr__dny87ykrjbgxfeq3wrtcopndky
    private String command;     //실행된 명령어 : /tadev, /ta /kl 등
    private String responseUrl; //3초 이상 걸리는 작업 시, 나중에 응답을 보낼 때 쓰는 임시 URL - response_url -> http://192.168.0.3:8065/hooks/commands/btr6k66xdirstc19a3uyjnduph
    private String teamDomain;  //팀 도메인   (team_domain -> home)
    private String teamId;      //팀 고유 ID   (team_id -> 4aqmyzhcz3y4jfut63jdstfc4a)
    private String text;       // ★ 명령어 뒤 내용: 사용자가 입력한 실제 텍스트 - "다이소 냄비 구매"가 여기 들어옴
    private String token;      // 보안 검증용 토큰 - ★ 요청이 내 메타모스트 서버에서 온 것인지 확인할 때 사용 (설정값과 비교)
    private String triggerId; //대화형 모달(Dialog)을 띄울 때 사용하는 ID
    private String userId;    // 사용자 식별용
    private String userName;    //사용자 이름 (hehe 등)
}
