package org.webpda.server.datainterface.controlsystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.epics.util.array.CollectionNumbers;
import org.epics.vtype.Alarm;
import org.epics.vtype.Array;
import org.epics.vtype.Display;
import org.epics.vtype.Enum;
import org.epics.vtype.Time;
import org.epics.vtype.VByte;
import org.epics.vtype.VDouble;
import org.epics.vtype.VEnum;
import org.epics.vtype.VEnumArray;
import org.epics.vtype.VFloat;
import org.epics.vtype.VInt;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VShort;
import org.epics.vtype.VString;
import org.epics.vtype.VStringArray;
import org.epics.vtype.VType;
import org.epics.vtype.ValueUtil;
import org.webpda.server.core.Constants;
import org.webpda.server.core.DataUtil;
import org.webpda.server.core.JsonUtil;
import org.webpda.server.core.LoggerUtil;
import org.webpda.server.datainterface.ValueFrame;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

public class VTypeJsonHelper {
	
	
	private static final String LENGTH = "len"; //$NON-NLS-1$
	private static final String TYPE = "type"; //$NON-NLS-1$
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
	private static final String LABELS = "labels";

	public static ValueFrame vTypeToValueFrame(VType v, Object oldValue) {
		if(v==null)
			return null;
		try {
			JsonGenerator jg = JsonUtil.jsonFactory.createGenerator(new ByteArrayOutputStream());
			jg.writeStartObject();
			if(oldValue == null || ValueUtil.typeOf(oldValue) != ValueUtil.typeOf(v)){
				jg.writeStringField(TYPE, ValueUtil.typeOf(v).getSimpleName());
				oldValue = null;
			}
			if(v instanceof Time)
				writeTimeToJson((Time) v, jg);		
			
			writeValueJsonInfo(v, jg);
			
			if(v instanceof Alarm)
				writeAlarmToJson((Alarm) v, oldValue, jg);
			if(v instanceof Display)
				writeDisplayToJson((Display) v, oldValue, jg);
			if(v instanceof Enum)
				writeEnumMetaToJson((Enum) v, oldValue, jg);
			jg.writeEndObject();
			jg.close();
			ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) jg
					.getOutputTarget();
			String string = byteArrayOutputStream.toString(Constants.CHARSET);
			byte[] jsonPart = string.getBytes(Constants.CHARSET);
			
			byte[] valueBinaryPart = getValueBinary(v);
			byteArrayOutputStream.close();
			
			ValueFrame r = new ValueFrame();
			r.addValue(DataUtil.shortToBytes(
					(short) jsonPart.length), jsonPart, valueBinaryPart);
			return r;
		} catch (Exception e) {
			LoggerUtil.getLogger().log(Level.SEVERE, "Failed to create Json String", e);
			return null;
		}		
		
	}
	
	private static void writeValueJsonInfo(VType v, JsonGenerator jg) throws JsonGenerationException, IOException{
		if(v instanceof Array){
			jg.writeNumberField(LENGTH, ((Array)v).getSizes().getInt(0));
		}else if(v instanceof VString){
			jg.writeStringField(VALUE, ((VString)v).getValue());
		}else if(v instanceof VStringArray){
			jg.writeFieldName(VALUE);
			jg.writeStartArray();
			for(String s: ((VStringArray)v).getData())
				jg.writeString(s);
			jg.writeEndArray();			
		}
		return;
	}

	private static byte[] getValueBinary(VType v) throws JsonGenerationException{
		if(v instanceof VDouble){			
			return DataUtil.doubleToBytes(((VDouble)v).getValue());
		}else if(v instanceof VFloat){
			return DataUtil.floatToBytes(((VFloat)v).getValue());
		}else if(v instanceof VShort){
			return DataUtil.shortToBytes(((VShort)v).getValue());
		}else if(v instanceof VByte){
			return DataUtil.byteToBytes(((VByte)v).getValue());
		}else if(v instanceof VInt){
			return DataUtil.intToBytes(((VInt)v).getValue());
		}else if(v instanceof VEnum){
			return DataUtil.intToBytes(((VEnum)v).getIndex());
		}else if(v instanceof VNumberArray){
			Object wrappedArray = CollectionNumbers.wrappedArray(((VNumberArray) v).getData());
			if(wrappedArray instanceof double[])
				return DataUtil.doubleArrayToBytes((double[]) wrappedArray);
			else if(wrappedArray instanceof float[])
				return DataUtil.floatArrayToBytes((float[]) wrappedArray);
			else if(wrappedArray instanceof long[])
				return DataUtil.longArrayToBytes((long[]) wrappedArray);
			else if(wrappedArray instanceof int[])
				return DataUtil.intArrayToBytes((int[]) wrappedArray);
			else if(wrappedArray instanceof short[])
				return DataUtil.shortArrayToBytes((short[]) wrappedArray);
			else if(wrappedArray instanceof byte[])
				return (byte[])wrappedArray;
			else
				throw new JsonGenerationException("The wrapped array is not primary array.");
		}else if(v instanceof VEnumArray){
			Object wrappedArray = CollectionNumbers.wrappedArray(((VEnumArray) v).getIndexes());
			if(wrappedArray instanceof int[])
				return DataUtil.intArrayToBytes((int[]) wrappedArray);
			else
				throw new JsonGenerationException("The wrapped array is not primary array.");
		}
		return new byte[0];
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
	
	private static void writeEnumMetaToJson(Enum e, Object oldValue, JsonGenerator jg)throws JsonGenerationException, IOException{
		if (oldValue == null || !(oldValue instanceof Enum)
				|| !e.getLabels().equals(((Enum) oldValue).getLabels())) {
			jg.writeArrayFieldStart(LABELS);
			for (String s : e.getLabels())
				jg.writeString(s);
			jg.writeEndArray();
		}
	}

}
