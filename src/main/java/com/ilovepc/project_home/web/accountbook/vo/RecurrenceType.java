package com.ilovepc.project_home.web.accountbook.vo;

public enum RecurrenceType {
    FIXED("고정"),
    VARIABLE("변동"),
    ONE_TIME("일회성"),
    NONE("없음");

    private final String tagName;

    RecurrenceType(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

    public static RecurrenceType fromTag(String tag) {
        if ("고정".equals(tag)) {
            return FIXED;
        }
        if ("변동".equals(tag)) {
            return VARIABLE;
        }
        if ("일회성".equals(tag)) {
            return ONE_TIME;
        }
        return NONE;
    }
}
