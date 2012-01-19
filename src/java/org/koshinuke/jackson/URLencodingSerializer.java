package org.koshinuke.jackson;

import java.io.IOException;
import java.net.URLEncoder;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 * @author taichi
 */
public class URLencodingSerializer extends JsonSerializer<String> {

	@Override
	public void serialize(String value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeString(URLEncoder.encode(value, "UTF-8"));
	}
}
