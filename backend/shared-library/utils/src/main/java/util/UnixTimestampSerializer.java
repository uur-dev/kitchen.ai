package util;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import java.time.Instant;

public class UnixTimestampSerializer extends StdSerializer<Instant> {

    public UnixTimestampSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        gen.writeNumber(value.getEpochSecond());
    }
}