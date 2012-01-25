package org.koshinuke.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.eclipse.jgit.lib.ObjectId;

/**
 * @author taichi
 */
public class ObjectIdSerializer extends JsonSerializer<ObjectId> {

	@Override
	public void serialize(ObjectId value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		if (value == null) {
			jgen.writeString(ObjectId.zeroId().name());
		} else {
			jgen.writeString(value.name());
		}
	}

}
