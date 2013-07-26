package org.webpda.server.datainterface.controlsystem;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

import org.epics.util.time.Timestamp;
import org.epics.vtype.AlarmSeverity;
import org.webpda.server.core.Constants;
import org.webpda.server.core.LoggerUtil;

import com.fasterxml.jackson.core.JsonGenerator;

public class CSNumber extends AbstractCSValue {

	private Number value;

	public CSNumber(Timestamp time, Number value, AlarmSeverity severity,
			String alarmName) {
		super(time, severity, alarmName);
		this.value = value;
	}

	public Number getValue() {
		return value;
	}

	@Override
	public String toJson() {
		try {
			JsonGenerator jg = createJsonGenerator();
			jg.writeNumberField(VALUE, value.doubleValue());
			jg.writeEndObject();
			jg.close();
			ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) jg
					.getOutputTarget();
			String s = byteArrayOutputStream.toString(Constants.CHARSET);
			byteArrayOutputStream.close();
			return s;
		} catch (Exception e) {
			LoggerUtil.getLogger().log(Level.SEVERE,
					"Failed to create JSON string.", e);
		}
		return "";
	}
	
	@Override
	public String toString() {
		return toJson();
	}

}
