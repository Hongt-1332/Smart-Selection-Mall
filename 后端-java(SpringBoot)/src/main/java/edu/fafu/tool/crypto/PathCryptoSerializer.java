package edu.fafu.tool.crypto;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import edu.fafu.config.SpringContextHolder;
import edu.fafu.config.SystemConfig;

import tools.jackson.core.JacksonException;

public class PathCryptoSerializer extends ValueSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        if (value != null && value.startsWith("`") && value.endsWith("`") && value.length() > 1) {
            value = value.substring(1, value.length() - 1);
        }
        String result = PathCryptoUtil.encrypt(SpringContextHolder.getBean(SystemConfig.class), value);
        System.err.println("[PathCryptoSerializer] value='" + value + "' result='" + result + "'");
        gen.writeString(result);
    }
}