package com.ilovepc.project_home.web.accountbook.business;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketBzCompanyLookupService {
    private static final String INDUSTRY_CLASSIFICATION_LABEL = "\uC0B0\uC5C5\uBD84\uB958";
    private static final String SIC_NAME_SELECTOR = ".mbz-sic-name";
    private static final String SIC_LIST_SELECTOR = "[id^=mbz-sic-list-]";
    private static final int INDUSTRY_SECTION_MAX_LENGTH = 300;
    private static final int BZ_TYPE_MAX_LENGTH = 120;
    private static final List<String> INDUSTRY_SECTION_STOP_LABELS = List.of(
            "\uAE30\uC5C5\uD615\uD0DC", "\uAE30\uC5C5\uADDC\uBAA8", "\uC124\uB9BD\uC77C",
            "\uB300\uD45C\uC790", "\uC0AC\uC5C5\uC790\uBC88\uD638", "\uBC95\uC778\uB4F1\uB85D\uBC88\uD638",
            "\uC8FC\uC18C", "\uC804\uD654\uBC88\uD638", "\uD648\uD398\uC774\uC9C0",
            "\uC8FC\uC694\uC81C\uD488", "\uC7AC\uBB34", "\uC778\uD5C8\uAC00", "\uC720\uC0AC",
            "\uBC30\uC9C0", "\uC81C\uD488\uBCF4\uACE0\uC11C", "\uC8FC\uC694\uC18C\uAC1C"
    );

    private final MarketBzCompanyLookupClient marketBzCompanyLookupClient;

    public MarketBzCompanyLookupResult lookup(String businessNumber) {
        String normalizedBusinessNumber = normalizeBusinessNumber(businessNumber);
        MarketBzCompanyLookupClient.MarketBzHtmlResponse response =
                marketBzCompanyLookupClient.fetchCompanyDetail(normalizedBusinessNumber);

        String html = response.html() == null ? "" : response.html();
        String bzType = parseBzTypeFromHTML(html);
        String industrySectionText = extractIndustrySectionText(html);

        return MarketBzCompanyLookupResult.builder()
                .businessNumber(normalizedBusinessNumber)
                .sourceUrl(response.sourceUrl())
                .httpStatus(response.httpStatus())
                .contentLength(html.length())
                .containsIndustryClassification(StringUtils.hasText(bzType) || StringUtils.hasText(industrySectionText))
                .bzType(bzType)
                .industrySectionText(industrySectionText)
                .build();
    }

    private String normalizeBusinessNumber(String businessNumber) {
        if (!StringUtils.hasText(businessNumber)) {
            throw new IllegalArgumentException("Business number is required.");
        }

        String normalized = businessNumber.replaceAll("\\D", "");
        if (normalized.length() != 10) {
            throw new IllegalArgumentException("Business number must contain exactly 10 digits.");
        }
        return normalized;
    }

    public String parseBzTypeFromHTML(String html) {
        if (!StringUtils.hasText(html)) {
            return "";
        }

        Document document = Jsoup.parse(html);
        String sicValue = parseBzTypeFromSicElements(document);
        if (StringUtils.hasText(sicValue)) {
            return sicValue;
        }

        String structuredValue = parseBzTypeFromElements(document);
        if (StringUtils.hasText(structuredValue)) {
            return structuredValue;
        }

        return parseBzTypeFromText(document.text());
    }

    private String parseBzTypeFromSicElements(Document document) {
        for (Element sicNameElement : document.select(SIC_NAME_SELECTOR)) {
            String value = sanitizeBzType(sicNameElement.text());
            if (StringUtils.hasText(value)) {
                return value;
            }
        }

        for (Element sicListElement : document.select(SIC_LIST_SELECTOR)) {
            Element sicNameElement = sicListElement.selectFirst(SIC_NAME_SELECTOR);
            if (sicNameElement != null) {
                String value = sanitizeBzType(sicNameElement.text());
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }

            String value = sanitizeBzType(sicListElement.text());
            if (StringUtils.hasText(value)) {
                return value;
            }
        }

        return "";
    }

    private String parseBzTypeFromElements(Document document) {
        for (Element labelElement : document.getAllElements()) {
            String ownText = normalizeText(labelElement.ownText());
            if (!ownText.contains(INDUSTRY_CLASSIFICATION_LABEL)) {
                continue;
            }

            String value = parseValueNearLabelElement(labelElement);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private String parseValueNearLabelElement(Element labelElement) {
        String ownTextValue = removeIndustryLabel(labelElement.ownText());
        if (StringUtils.hasText(ownTextValue)) {
            return sanitizeBzType(ownTextValue);
        }

        Element nextElement = labelElement.nextElementSibling();
        if (nextElement != null && StringUtils.hasText(nextElement.text())) {
            return sanitizeBzType(nextElement.text());
        }

        Element parent = labelElement.parent();
        if (parent == null) {
            return "";
        }

        String parentTextValue = removeIndustryLabel(parent.text());
        if (StringUtils.hasText(parentTextValue) && !parentTextValue.equals(normalizeText(parent.text()))) {
            return sanitizeBzType(parentTextValue);
        }

        Element parentNextElement = parent.nextElementSibling();
        if (parentNextElement != null && StringUtils.hasText(parentNextElement.text())) {
            return sanitizeBzType(parentNextElement.text());
        }

        return "";
    }

    private String parseBzTypeFromText(String text) {
        String normalizedText = normalizeText(text);
        int labelIndex = normalizedText.indexOf(INDUSTRY_CLASSIFICATION_LABEL);
        if (labelIndex < 0) {
            return "";
        }

        String afterLabel = normalizedText.substring(labelIndex + INDUSTRY_CLASSIFICATION_LABEL.length())
                .replaceFirst("^[\\s:\\uFF1A\\-]+", "");
        int endIndex = findIndustryValueEndIndex(afterLabel);
        if (endIndex >= 0) {
            afterLabel = afterLabel.substring(0, endIndex);
        }
        return sanitizeBzType(afterLabel);
    }

    private int findIndustryValueEndIndex(String text) {
        int endIndex = Math.min(text.length(), BZ_TYPE_MAX_LENGTH);
        for (String stopLabel : INDUSTRY_SECTION_STOP_LABELS) {
            int stopIndex = text.indexOf(stopLabel);
            if (stopIndex >= 0) {
                endIndex = Math.min(endIndex, stopIndex);
            }
        }
        return endIndex;
    }

    private String extractIndustrySectionText(String html) {
        Document document = Jsoup.parse(html);
        String text = document.text();
        int startIndex = text.indexOf(INDUSTRY_CLASSIFICATION_LABEL);
        if (startIndex < 0) {
            return "";
        }

        int endIndex = Math.min(text.length(), startIndex + INDUSTRY_SECTION_MAX_LENGTH);
        return text.substring(startIndex, endIndex)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String removeIndustryLabel(String text) {
        return normalizeText(text)
                .replaceFirst("^.*?" + INDUSTRY_CLASSIFICATION_LABEL + "\\s*[:\\uFF1A\\-]?", "")
                .trim();
    }

    private String sanitizeBzType(String value) {
        String sanitized = normalizeText(value)
                .replaceFirst("^[\\s:\\uFF1A\\-]+", "")
                .trim();

        int stopIndex = findIndustryValueEndIndex(sanitized);
        if (stopIndex >= 0) {
            sanitized = sanitized.substring(0, stopIndex).trim();
        }

        if (sanitized.length() > BZ_TYPE_MAX_LENGTH) {
            sanitized = sanitized.substring(0, BZ_TYPE_MAX_LENGTH).trim();
        }
        return sanitized;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }
}
