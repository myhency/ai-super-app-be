package io.hmg.openai.chat.completion.application.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonSchemaProperty {
    @JsonProperty("type")
    private Object type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("properties")
    private Map<String, JsonSchemaProperty> properties;

    @JsonProperty("required")
    private List<String> required;

    @JsonProperty("additional_properties")
    private Boolean additionalProperties;

    @JsonProperty("enum")
    private List<String> enumValues;
}