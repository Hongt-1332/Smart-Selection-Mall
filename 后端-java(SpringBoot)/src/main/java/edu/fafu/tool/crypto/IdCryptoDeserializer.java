package edu.fafu.tool.crypto;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import edu.fafu.config.SpringContextHolder;
import edu.fafu.config.SystemConfig;

import tools.jackson.core.JacksonException;

public class IdCryptoDeserializer extends ValueDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String decrypted = PathCryptoUtil.decrypt(SpringContextHolder.getBean(SystemConfig.class), p.getValueAsString());
        if (decrypted == null) {
            return null;
        }
        return Integer.valueOf(decrypted);
    }
}