package org.koshinuke.jackson;

import java.io.IOException;
import java.net.URLDecoder;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

/**
 * @author taichi
 */
public class URLdecodingDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		return URLDecoder.decode(jp.getText(), "UTF-8");
	}
}
