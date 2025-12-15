package com.ilovepc.project_home.web.holiday.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HolidayRestResponse {
    // 최상위 루트 'response'
    private Response response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Header header;
        private Body body;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JsonDeserialize(using = ItemsDeserializer.class) //데이터가 아예 없을 때 null이나 []를 주는게 아니라서 Items가 들어갈 자리에 ""를 줘버리니 에러가 남. (빈 문자열이 오면 null로 처리하라는 것 추가)
        private Items items; // items가 객체로 감싸져 있음
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        // 실제 휴일 데이터 리스트는 'item' 필드에 들어있음
        // 배열([])이 아니라 객체({}) 하나만 와도 리스트로 자동 변환해주는 옵션 (공공API는 1개만 들어오면 리스트가 아니라 ""로 줘버리는것 같다.)
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<Item> item;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        // 날짜 종류 (01: 국경일 등)
        private String dateKind;

        // 휴일 명칭 (예: 어린이날, 광복절)
        private String dateName;

        // 공공기관 휴일 여부 (Y/N)
        private String isHoliday;

        // 날짜 (YYYYMMDD 형식, 숫자나 문자열로 올 수 있음. 계산을 위해 Integer 권장)
        private Integer locdate;

        // 순번
        private Integer seq;
    }

    public static class ItemsDeserializer extends JsonDeserializer<Items> {
        @Override
        public Items deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            if(jsonParser.hasToken(JsonToken.VALUE_STRING) && "".equals(jsonParser.getText())){
                //문자열이면서 && ""라면 null 반환
                Items items = new Items();
                items.setItem(new ArrayList<>());
                return items;
            }
            return deserializationContext.readValue(jsonParser, Items.class); //원래 Items클래스 변환
        }
    }

    public boolean isEmpty(){
        return response.body.items.item.isEmpty();
    }

    public List<Item> getHolidayItem(){
        return getResponse().getBody().getItems().getItem();
    }
}
