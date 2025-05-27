package com.gandalp.gandalp.openAi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class OpenAIResponseDTO extends HashMap<String, Map<String, UUID>> {
}
