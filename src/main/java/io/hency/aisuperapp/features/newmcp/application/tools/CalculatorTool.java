package io.hency.aisuperapp.features.newmcp.application.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * Spring AI MCP를 사용한 계산기 도구
 * @Tool 어노테이션을 사용하여 자동으로 MCP 도구로 등록됨
 */
@Slf4j
@Service
public class CalculatorTool {

    /**
     * 두 숫자를 더합니다
     *
     * @param a 첫 번째 숫자
     * @param b 두 번째 숫자
     * @return 두 숫자의 합
     */
    @Tool(description = "Add two numbers")
    public double add(double a, double b) {
        double result = a + b;
        log.info("Add: {} + {} = {}", a, b, result);
        return result;
    }

    /**
     * 두 숫자를 곱합니다
     *
     * @param a 첫 번째 숫자
     * @param b 두 번째 숫자
     * @return 두 숫자의 곱
     */
    @Tool(description = "Multiply two numbers")
    public double multiply(double a, double b) {
        double result = a * b;
        log.info("Multiply: {} * {} = {}", a, b, result);
        return result;
    }

    /**
     * 두 숫자를 뺍니다
     *
     * @param a 첫 번째 숫자
     * @param b 두 번째 숫자
     * @return 두 숫자의 차
     */
    @Tool(description = "Subtract two numbers")
    public double subtract(double a, double b) {
        double result = a - b;
        log.info("Subtract: {} - {} = {}", a, b, result);
        return result;
    }

    /**
     * 두 숫자를 나눕니다
     *
     * @param a 첫 번째 숫자
     * @param b 두 번째 숫자
     * @return 두 숫자의 나눗셈 결과
     * @throws IllegalArgumentException 0으로 나누려고 할 때
     */
    @Tool(description = "Divide two numbers")
    public double divide(double a, double b) {
        if (b == 0) {
            log.error("Division by zero attempted: {} / {}", a, b);
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        double result = a / b;
        log.info("Divide: {} / {} = {}", a, b, result);
        return result;
    }
}