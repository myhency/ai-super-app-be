package io.hmg.openai.chat.completion.application.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class OSeriesPayload extends ChatCompletionPayload {
    @Setter
    private Integer maxCompletionTokens;
}
