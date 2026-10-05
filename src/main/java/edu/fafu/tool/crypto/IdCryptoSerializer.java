package edu.fafu.tool.crypto;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import edu.fafu.config.SpringContextHolder;
import edu.fafu.config.SystemConfig;

import tools.jackson.core.JacksonException;

public class IdCryptoSerializer extends ValueSerializer<Integer> {
    @Override
    public void serialize(Integer value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(PathCryptoUtil.encrypt(SpringContextHolder.getBean(SystemConfig.class), String.valueOf(value)));
    }
}