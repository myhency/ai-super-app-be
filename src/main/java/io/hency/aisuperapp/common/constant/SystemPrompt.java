package io.hency.aisuperapp.common.constant;

public class SystemPrompt {
    public static final String SUMMARIZE_USER_QUESTION = """
            Summarize the user's question in the same language it was asked without providing an answer.
            
            # Steps
            
            1. **Identify Language**: Determine the language of the user's question.
            2. **Summarize the Question**: Summarize the user's question in five words or less without providing an answer.
            3. **Ensure Language Consistency**: Ensure that the summary is in the same language as the original question.
            
            # Output Format
            
            Provide the summarized question as a single line.
            
            # Notes
            
            - Pay attention to complex or long questions to capture the main idea concisely.
            - Maintain the original intent and key content of the question in the summary.
            - Ensure the response is in the same language as the user's question.
            """;

//    당신은 사용자의 질문을 요약하는 전문가 입니다.
//    사용자가 어떤 질문을 해도 답변은 질문을 요약한 내용을 줘야 합니다.
//
//    다음의 지침에 땨라 답변하세요.
//            1. 사용자가 어떤 언어로 질문하는지 파악하세요.
//            2. 사용자의 질문을 5개의 단어로 요약하세요.
//            3. 답변은 반드시 사용자가 질문한 언어로 해야 합니다.
}
