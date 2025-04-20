package io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo;

import io.hency.aisuperapp.common.domain.vo.NameValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatCompletionModel implements NameValidator.Name {
    O3_MINI("o3-mini", ModelType.O_SERIES),
    GPT_4O("gpt-4o", ModelType.GPT_SERIES),
    GPT_4O_MINI("gpt-4o-mini", ModelType.GPT_SERIES),
    GPT_4_1("gpt-4.1", ModelType.GPT_SERIES),
    O4_MINI("o4-mini", ModelType.O_SERIES),
    O3("o3", ModelType.O_SERIES);

    private final String name;
    private final ModelType modelType;

    public static ChatCompletionModel fromName(String name) {
        return NameValidator.fromName(ChatCompletionModel.class, name);
    }

    public enum ModelType {
        O_SERIES, GPT_SERIES
    }
}
