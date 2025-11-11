package io.hency.aisuperapp.features.newmcp.application.service;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * CQL(Confluence Query Language) 생성기
 * 단순 텍스트 쿼리를 Confluence CQL로 변환하는 유틸리티 클래스
 * {
 * "query": "mcp atlassian",
 * "spaces_filter": "my first space"
 * }
 * 위와 같이 input 이 들어올 수 있음
 */
@Setter
@Service
public class ConfluenceCqlGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ConfluenceCqlGenerator.class);

    // CQL 연산자 목록 - 이 연산자들이 포함되어 있으면 이미 CQL 쿼리로 간주
    private static final List<String> CQL_OPERATORS = Arrays.asList(
            "=", "~", ">", "<", " AND ", " OR ", "currentUser()"
    );

    // CQL 식별자에 따옴표가 필요한지 확인하는 정규식
    private static final Pattern NEEDS_QUOTES_PATTERN = Pattern.compile("[\\s~]|^\\d+$|[,\"]");

    /**
     * -- SETTER --
     *  설정에서 가져온 기본 스페이스 필터 설정
     *
     * @param defaultSpacesFilter 기본 스페이스 필터
     */
    // 설정에서 가져올 기본 스페이스 필터 (설정 기능 추가 시 사용)
    private String defaultSpacesFilter;

    public ConfluenceCqlGenerator() {
        // 기본 생성자
    }

    public ConfluenceCqlGenerator(String defaultSpacesFilter) {
        this.defaultSpacesFilter = defaultSpacesFilter;
    }

    /**
     * 입력받은 쿼리를 CQL로 변환
     * - 단순 텍스트면 CQL로 변환
     * - 이미 CQL 형식이면 그대로 반환
     *
     * @param query 검색 쿼리 (단순 텍스트 또는 CQL)
     * @return 변환된 CQL 쿼리
     */
    public String generateCql(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }

        // 쿼리가 이미 CQL 형식인지 확인
        if (containsAnyCqlOperator(query)) {
            // 이미 siteSearch 또는 text 연산자가 포함되어 있는지 확인
            if (query.contains("siteSearch ~") || query.contains("text ~")) {
                logger.info("Query already contains search operator, using as-is: {}", query);
                return query;
            } else {
                // CQL 형식이지만 검색 연산자가 없는 경우, 전체 쿼리를 siteSearch로 감싸기
                String cqlQuery = String.format("siteSearch ~ \"%s\"", escapeQuotes(query));
                logger.info("Converted CQL without search operator to siteSearch: {}", cqlQuery);
                return cqlQuery;
            }
        }

        // 단순 텍스트 쿼리를 CQL로 변환
        String cqlQuery = String.format("siteSearch ~ \"%s\"", escapeQuotes(query));
        logger.info("Converted simple text query to CQL using siteSearch: {}", cqlQuery);
        return cqlQuery;
    }

    /**
     * 대체 CQL 생성 (siteSearch가 실패했을 때 사용)
     *
     * @param query 원본 검색 쿼리
     * @return text 연산자를 사용한 CQL
     */
    public String generateFallbackCql(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }
        String cqlQuery = String.format("text ~ \"%s\"", escapeQuotes(query));
        logger.info("Generated fallback CQL using text operator: {}", cqlQuery);
        return cqlQuery;
    }

    /**
     * 스페이스 필터를 CQL에 추가
     * 전달된 필터가 없으면 기본 설정 필터를 사용 (설정 기능 추가 시)
     *
     * @param cql         기존 CQL 쿼리
     * @param spacesFilter 쉼표로 구분된 스페이스 키 목록
     * @return 스페이스 필터가 적용된 CQL 쿼리
     */
    public String applySpacesFilter(String cql, String spacesFilter) {
        // 전달된 필터 또는 기본 설정 필터 사용
        String filterToUse = (spacesFilter != null && !spacesFilter.trim().isEmpty()) ? spacesFilter : defaultSpacesFilter;
        if (filterToUse == null || filterToUse.trim().isEmpty()) {
            return cql;
        }

        // 스페이스 필터 쿼리 부분 구성
        String[] spaces = filterToUse.split(",");
        String spaceQuery = Arrays.stream(spaces)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(space -> "space = " + quoteCqlIdentifierIfNeeded(space))
                .collect(Collectors.joining(" OR "));

        // 기존 쿼리에 스페이스 필터 조건 추가
        if (!spaceQuery.isEmpty()) {
            if (cql != null && !cql.trim().isEmpty()) {
                if (!cql.contains("space = ")) { // 이미 공간 필터가 있는지 확인
                    String result = "(" + cql + ") AND (" + spaceQuery + ")";
                    logger.info("Applied spaces filter to query: {}", result);
                    return result;
                }
                return cql;
            } else {
                logger.info("Using only spaces filter as query: {}", spaceQuery);
                return spaceQuery;
            }
        }
        return cql;
    }

    /**
     * 주어진 쿼리에 CQL 연산자가 포함되어 있는지 확인
     *
     * @param query 확인할 쿼리
     * @return CQL 연산자가 포함되어 있으면 true, 아니면 false
     */
    private boolean containsAnyCqlOperator(String query) {
        return CQL_OPERATORS.stream().anyMatch(query::contains);
    }

    /**
     * CQL 쿼리에서 따옴표 이스케이프 처리
     *
     * @param value 이스케이프할 값
     * @return 이스케이프된 값
     */
    private String escapeQuotes(String value) {
        return value.replace("\"", "\\\"");
    }

    /**
     * CQL 식별자에 따옴표가 필요한지 확인하고 필요한 경우 추가
     * Python의 quote_cql_identifier_if_needed 함수와 유사한 기능
     *
     * @param identifier CQL 식별자
     * @return 필요한 경우 따옴표로 감싼 식별자
     */
    private String quoteCqlIdentifierIfNeeded(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return "\"\"";
        }
        // 개인 공간 키(~), 공백 포함, 숫자만으로 구성, 쉼표나 따옴표 포함 시 따옴표 필요
        if (NEEDS_QUOTES_PATTERN.matcher(identifier).find()) {
            return "\"" + escapeQuotes(identifier) + "\"";
        }
        return identifier;
    }

    /**
     * 쿼리와 스페이스 필터를 받아 완전한 CQL을 생성하는 단일 메서드
     * Claude에서 받은 JSON 형식의 매개변수를 처리하는 용도로 사용
     *
     * @param query 검색 쿼리 (단순 텍스트 또는 CQL)
     * @param spacesFilter 스페이스 필터 (쉼표로 구분된 스페이스 키 목록)
     * @return 완성된 CQL 쿼리
     */
    public String generateFullCql(String query, String spacesFilter) {
        // 1. 쿼리를 CQL로 변환
        String cql = generateCql(query);
        // 2. 스페이스 필터 적용
        return applySpacesFilter(cql, spacesFilter);
    }

    /**
     * 샘플 사용법을 보여주는 메인 메서드
     */
    public static void main(String[] args) {
        ConfluenceCqlGenerator generator = new ConfluenceCqlGenerator();
        // 설정에서 기본 필터 설정 예시
        generator.setDefaultSpacesFilter("DEFAULT,SPACE");

        // 단순 텍스트 쿼리 변환 예시
        String simpleQuery = "project documentation";
        String cql = generator.generateCql(simpleQuery);
        System.out.println("Simple query: " + simpleQuery);
        System.out.println("Generated CQL: " + cql);
        System.out.println("Fallback CQL: " + generator.generateFallbackCql(simpleQuery));
        System.out.println();

        // 이미 CQL인 경우 예시
        String existingCql = "type=page AND space=DEV";
        String result = generator.generateCql(existingCql);
        System.out.println("Existing CQL: " + existingCql);
        System.out.println("Result: " + result);
        System.out.println();

        // 직접 스페이스 필터 적용 예시
        String filteredCql = generator.applySpacesFilter(cql, "DEV,TEAM,~personal");
        System.out.println("With explicit space filter: " + filteredCql);

        // 설정 기반 스페이스 필터 적용 예시
        String defaultFilteredCql = generator.applySpacesFilter(cql, null);
        System.out.println("With default space filter: " + defaultFilteredCql);

        // 공간 이름 따옴표 처리 예시
        String complexSpaces = "DEV,Marketing Team,~john.doe,123";
        String complexFilteredCql = generator.applySpacesFilter(cql, complexSpaces);
        System.out.println("With complex space names: " + complexFilteredCql);

        // Claude에서 받은 매개변수로 CQL 생성
        String query = "mcp atlassian OR ai";
        String spacesFilter = "MY FIRST SPACE,OVERVIEW";
        // 단일 메서드 호출로 완성된 CQL 얻기
        String fullCql = generator.generateFullCql(query, spacesFilter);
        System.out.println("Generated CQL: " + fullCql);
    }
}