package org.webpda.server.datainterface.controlsystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.epics.util.array.CollectionNumbers;
import org.epics.util.array.ListDouble;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import org.epics.vtype.VEnum;
import org.epics.vtype.VFloat;
import org.epics.vtype.VNumber;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VShort;
import org.epics.vtype.VString;
import org.epics.vtype.VType;
import org.epics.vtype.ValueUtil;
import org.webpda.server.core.Constants;
import org.webpda.server.core.JsonUtil;
import org.webpda.server.core.LoggerUtil;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

public class VTypeJsonHelper {
	
	private static final String TYPE = "type";
	public final static String TIME = "t"; //$NON-NLS-1$
	public final static String SECOND = "s"; //$NON-NLS-1$
	public final static String NANOSECOND = "ns"; //$NON-NLS-1$
	public final static String SEVERITY="sev"; //$NON-NLS-1$
	public final static String ALARM_NAME="an";	//$NON-NLS-1$
	public final static String VALUE = "v"; //$NON-NLS-1$
	
	public final static String DISPLAY_LOW = "dl"; //$NON-NLS-1$
	public final static String DISPLAY_HIGH = "dh"; //$NON-NLS-1$
	public final static String WARN_LOW = "wl"; //$NON-NLS-1$w
	public final static String WARN_HIGH = "wh"; //$NON-NLS-1$
	public final static String ALARM_LOW = "al"; //$NON-NLS-1$
	public final static String ALARM_HIGH = "ah"; //$NON-NLS-1$
	public final static String PRECISION = "prec"; //$NON-NLS-1$
	public final static String UNITS = "units"; //$NON-NLS-1$

	public static String VTypeToJson(VType v, Object oldValue) {
		if(v==null)
			return null;
		try {
			JsonGenerator jg = JsonUtil.jsonFactory.createGenerator(new ByteArrayOutputStream());
			jg.writeStartObject();
			if(oldValue == null || ValueUtil.typeOf(oldValue) != ValueUtil.typeOf(v)){
				jg.writeStringField(TYPE, ValueUtil.typeOf(v).getSimpleName());
			}
			if(v instanceof Time)
				writeTimeToJson((Time) v, jg);		
			
			writeValue(v, jg);
			
			if(v instanceof Alarm)
				writeAlarmToJson((Alarm) v, oldValue, jg);
			if(v instanceof Display)
				writeDisplayToJson((Display) v, oldValue, jg);
			jg.writeEndObject();
			jg.close();
			ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) jg
					.getOutputTarget();
			String s = byteArrayOutputStream.toString(Constants.CHARSET);
			byteArrayOutputStream.close();
			return s;
		} catch (Exception e) {
			LoggerUtil.getLogger().log(Level.SEVERE, "Failed to create Json String", e);
			return null;
		}		
		
	}

	private static void writeValue(VType v, JsonGenerator jg)
			throws IOException, JsonGenerationException {
		if(v instanceof VDouble){			
			jg.writeStringField(VALUE, JsonUtil.doubleToBinString(((VDouble)v).getValue()));
		}else if(v instanceof VFloat){
			jg.writeStringField(VALUE, JsonUtil.floatToBinString(((VFloat)v).getValue()));
		}else if(v instanceof VShort){
			jg.writeStringField(VALUE, JsonUtil.shortToBinString(((VShort)v).getValue()));
		}else if(v instanceof VNumber){
			jg.writeStringField(VALUE, JsonUtil.intToBinString(((VNumber)v).getValue().intValue()));
		}else if(v instanceof VEnum){
			jg.writeNumberField(VALUE, ((VEnum)v).getIndex());
		}else if(v instanceof VString){
			jg.writeStringField(VALUE, ((VString)v).getValue());
		}else if(v instanceof VNumberArray){
			jg.writeFieldName(VALUE);
			jg.writeStartObject();
			jg.writeNumberField("len", ((VNumberArray)v).getData().size());
			Object wrappedArray = CollectionNumbers.wrappedArray(((VNumberArray) v).getData());
			if(wrappedArray instanceof double[])
				jg.writeStringField("arr", JsonUtil.doubleArrayToBinString((double[]) wrappedArray));
			else
				throw new JsonGenerationException("The warpped array is not primary array.");
			
		}
		
	}
	
	private static void writeTimeToJson(Time t, JsonGenerator jg) throws JsonGenerationException, IOException{
		jg.writeFieldName(TIME);
		jg.writeStartObject();
		jg.writeNumberField(SECOND, t.getTimestamp().getSec());
		jg.writeNumberField(NANOSECOND, t.getTimestamp().getNanoSec());
		jg.writeEndObject();
	}
	
	private static void writeAlarmToJson(Alarm a, Object oldValue, JsonGenerator jg) throws JsonGenerationException, IOException{
		if(oldValue == null || !(oldValue instanceof Alarm)){
			jg.writeStringField(SEVERITY, a.getAlarmSeverity().name());
			jg.writeStringField(ALARM_NAME, a.getAlarmName());
			return;
		}
		
		if (((Alarm) oldValue).getAlarmSeverity() != a.getAlarmSeverity()) {
			jg.writeStringField(SEVERITY, a.getAlarmSeverity().name());
		}
		if (!((Alarm) oldValue).getAlarmName().equals(a.getAlarmName())) {
			jg.writeStringField(ALARM_NAME, a.getAlarmName());
		}

	}
	
	private static void writeDisplayToJson(Display d, Object oldValue, JsonGenerator jg) throws JsonGenerationException, IOException{
		if(oldValue == null || !(oldValue instanceof Display)){
			jg.writeNumberField(DISPLAY_LOW, d.getLowerDisplayLimit());
			jg.writeNumberField(DISPLAY_HIGH, d.getUpperDisplayLimit());
			jg.writeNumberField(WARN_LOW, d.getLowerWarningLimit());
			jg.writeNumberField(WARN_HIGH, d.getUpperWarningLimit());
			jg.writeNumberField(ALARM_LOW, d.getLowerAlarmLimit());
			jg.writeNumberField(ALARM_HIGH, d.getUpperAlarmLimit());
			jg.writeNumberField(PRECISION, d.getFormat().getMaximumFractionDigits());
			jg.writeStringField(UNITS, d.getUnits());	
			return;
		}
	
		if (((Display) oldValue).getLowerDisplayLimit() != d
				.getLowerDisplayLimit())
			jg.writeNumberField(DISPLAY_LOW, d.getLowerDisplayLimit());
		if (((Display) oldValue).getUpperAlarmLimit() != d.getUpperAlarmLimit())
			jg.writeNumberField(DISPLAY_HIGH, d.getUpperDisplayLimit());
		if (((Display) oldValue).getLowerWarningLimit() != d
				.getLowerWarningLimit())
			jg.writeNumberField(WARN_LOW, d.getLowerWarningLimit());
		if (((Display) oldValue).getUpperWarningLimit() != d
				.getUpperWarningLimit())
			jg.writeNumberField(WARN_HIGH, d.getUpperWarningLimit());
		if (((Display) oldValue).getLowerAlarmLimit() != d.getLowerAlarmLimit())
			jg.writeNumberField(ALARM_LOW, d.getLowerAlarmLimit());
		if (((Display) oldValue).getUpperAlarmLimit() != d.getUpperAlarmLimit())
			jg.writeNumberField(ALARM_HIGH, d.getUpperAlarmLimit());
		if (((Display) oldValue).getFormat().getMaximumFractionDigits() != d
				.getFormat().getMaximumFractionDigits())
			jg.writeNumberField(PRECISION, d.getFormat()
					.getMaximumFractionDigits());
		if (!((Display) oldValue).getUnits().equals(d.getUnits()))
			jg.writeStringField(UNITS, d.getUnits());	
		
		
	}

}
