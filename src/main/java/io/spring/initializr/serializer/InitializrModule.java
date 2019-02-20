package io.spring.initializr.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import io.spring.initializr.generator.version.VersionProperty;

/**
 *	版本序列化
 * @author lizhiwei
 * 2019-02-19 15:47:32
 */
public class InitializrModule extends SimpleModule {

	private static final long serialVersionUID = 1L;

	public InitializrModule() {
		super("initializr");
		addSerializer(new VersionPropertySerializer());
	}

	private static class VersionPropertySerializer extends StdSerializer<VersionProperty> {

		private static final long serialVersionUID = 1L;

		VersionPropertySerializer() {
			super(VersionProperty.class);
		}

		@Override
		public void serialize(VersionProperty value, JsonGenerator gen,
				SerializerProvider provider) throws IOException {
			gen.writeString(value.toStandardFormat());
		}

	}

}
