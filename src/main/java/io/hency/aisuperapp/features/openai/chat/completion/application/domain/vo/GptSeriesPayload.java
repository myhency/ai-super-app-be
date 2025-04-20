package io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class GptSeriesPayload extends ChatCompletionPayload {
    @Setter
    private Integer maxTokens;


}
