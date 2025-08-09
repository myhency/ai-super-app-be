package io.hmg.claude.messages.adapter.in.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.hmg.claude.common.adapter.in.dto.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnthropicRequest extends BaseRequest {

    private String model;

    private List<Object> messages;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private String container;

    @JsonProperty("mcp_servers")
    private List<Object> mcpServers;

    private Object metadata;

    @JsonProperty("service_tier")
    private String serviceTier;

    @JsonProperty("stop_sequences")
    private List<String> stopSequences;

    private boolean stream = false;

    private Object system;

    private Float temperature;

    private Object thinking;

    @JsonProperty("tool_choice")
    private String toolChoice;

    private List<Object> tools;

    @JsonProperty("top_k")
    private int topK;

    @JsonProperty("top_p")
    private Float topP;

}
