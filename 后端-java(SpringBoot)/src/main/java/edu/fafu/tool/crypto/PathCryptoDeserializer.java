package edu.fafu.tool.crypto;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import edu.fafu.config.SpringContextHolder;
import edu.fafu.config.SystemConfig;

import tools.jackson.core.JacksonException;

public class PathCryptoDeserializer extends ValueDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String value = p.getValueAsString();
        if (value != null && value.startsWith("`") && value.endsWith("`") && value.length() > 1) {
            value = value.substring(1, value.length() - 1);
        }
        return PathCryptoUtil.decrypt(SpringContextHolder.getBean(SystemConfig.class), value);
    }
}