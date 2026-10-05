package edu.fafu.tool.crypto;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.core.JacksonException;
import lombok.Data;

public class PathCryptoSerializerTest {

    public static class SimpleSerializer extends ValueSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
            gen.writeString(value);
        }
    }

    @Data
    public static class TestDto {
        @JsonSerialize(using = SimpleSerializer.class)
        private String path;

        private String normalPath;
    }

    @Test
    public void testSerializeWithCustomSerializer() throws Exception {
        TestDto dto = new TestDto();
        dto.setPath("https://example.com/test.png");
        dto.setNormalPath("https://example.com/normal.png");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dto);
        System.err.println("JSON output: " + json);
        if (json.contains("`")) {
            System.err.println("BACKTICK DETECTED in output!");
        } else {
            System.err.println("No backtick - serializer works correctly");
        }
    }
}