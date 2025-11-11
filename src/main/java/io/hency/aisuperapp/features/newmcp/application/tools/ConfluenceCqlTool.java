package io.hency.aisuperapp.features.newmcp.application.tools;

import io.hency.aisuperapp.features.newmcp.application.service.ConfluenceCqlGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * Confluence CQL 생성 도구
 * Spring AI MCP의 @Tool 어노테이션을 사용하여 자동으로 MCP 도구로 등록됨
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfluenceCqlTool {

    private final ConfluenceCqlGenerator cqlGenerator;

    /**
     * 검색 쿼리와 스페이스 필터를 받아 Confluence CQL을 생성합니다
     *
     * @param query 검색 쿼리 (단순 텍스트 또는 CQL)
     * @param spacesFilter 스페이스 필터 (쉼표로 구분된 스페이스 키 목록, optional)
     * @return 완성된 CQL 쿼리
     */
    @Tool(description = "Generate Confluence CQL query from simple text query and optional space filter")
    public String generateCql(String query, String spacesFilter) {
        log.info("Generating CQL for query: {}, spacesFilter: {}", query, spacesFilter);
        String cql = cqlGenerator.generateFullCql(query, spacesFilter);
        log.info("Generated CQL: {}", cql);
        return cql;
    }

    /**
     * 기본 CQL 생성이 실패했을 때 사용하는 대체 CQL을 생성합니다
     *
     * @param query 검색 쿼리
     * @return text 연산자를 사용한 CQL
     */
    @Tool(description = "Generate fallback Confluence CQL query using text operator")
    public String generateFallbackCql(String query) {
        log.info("Generating fallback CQL for query: {}", query);
        String cql = cqlGenerator.generateFallbackCql(query);
        log.info("Generated fallback CQL: {}", cql);
        return cql;
    }

    /**
     * 스페이스 필터만 적용한 CQL을 생성합니다
     *
     * @param spacesFilter 쉼표로 구분된 스페이스 키 목록
     * @return 스페이스 필터 CQL
     */
    @Tool(description = "Generate Confluence CQL query with only space filter")
    public String generateSpaceFilterCql(String spacesFilter) {
        log.info("Generating space filter CQL for: {}", spacesFilter);
        String cql = cqlGenerator.applySpacesFilter("", spacesFilter);
        log.info("Generated space filter CQL: {}", cql);
        return cql;
    }
}