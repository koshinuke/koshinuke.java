package org.koshinuke.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.util.StringUtils;

/**
 * @author taichi
 */
public class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {

	@Override
	public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String value = jp.getText();
		if (StringUtils.isEmptyOrNull(value) == false
				&& value.length() == Constants.OBJECT_ID_STRING_LENGTH) {
			return ObjectId.fromString(value);
		}
		return ObjectId.zeroId();
	}

	@Override
	public ObjectId getNullValue() {
		return ObjectId.zeroId();
	}
}
