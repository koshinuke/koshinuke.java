package org.koshinuke.jackson;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;
import org.eclipse.jgit.lib.ObjectId;

/**
 * @author taichi
 */
public class KoshinukeModule extends SimpleModule {

	public KoshinukeModule() {
		super(KoshinukeModule.class.getSimpleName(), Version.unknownVersion());
		this.addDeserializer(ObjectId.class, new ObjectIdDeserializer());
		this.addSerializer(ObjectId.class, new ObjectIdSerializer());
	}
}
