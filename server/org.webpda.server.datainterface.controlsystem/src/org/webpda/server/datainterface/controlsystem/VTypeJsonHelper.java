package org.webpda.server.datainterface.controlsystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
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
import org.epics.vtype.VNumber;
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
	
	
	private static final String ARRAY = "arr"; //$NON-NLS-1$
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

	public static ValueFrame VTypeToJson(VType v, Object oldValue) {
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
		}
		return;
	}

	private static byte[] getValueBinary(VType v){
		if(v instanceof VDouble){			
			return DataUtil.doubleToBytes(((VDouble)v).getValue());
		}else if(v instanceof VFloat){
//			jg.writeStringField(VALUE, JsonUtil.floatToBinString(((VFloat)v).getValue()));
		}else if(v instanceof VShort){
//			jg.writeStringField(VALUE, JsonUtil.shortToBinString(((VShort)v).getValue()));
		}else if(v instanceof VByte){
//			jg.writeStringField(VALUE, JsonUtil.byteToBinString(((VByte)v).getValue()));
		}else if(v instanceof VNumber){
//			jg.writeStringField(VALUE, JsonUtil.intToBinString(((VNumber)v).getValue().intValue()));
		}else if(v instanceof VEnum){
//			jg.writeNumberField(VALUE, ((VEnum)v).getIndex());
		}else if(v instanceof VString){
//			jg.writeStringField(VALUE, ((VString)v).getValue());
		}else if(v instanceof VNumberArray){
//			jg.writeFieldName(VALUE);
//			jg.writeStartObject();
//			jg.writeNumberField(LENGTH, ((VNumberArray)v).getData().size());
//			Object wrappedArray = CollectionNumbers.wrappedArray(((VNumberArray) v).getData());
//			if(wrappedArray instanceof double[])
//				jg.writeStringField(ARRAY, JsonUtil.doubleArrayToBinString((double[]) wrappedArray));
//			else if(wrappedArray instanceof float[])
//				jg.writeStringField(ARRAY, JsonUtil.floatArrayToBinString((float[]) wrappedArray));
//			else if(wrappedArray instanceof long[])
//				jg.writeStringField(ARRAY, JsonUtil.longArrayToBinString((long[]) wrappedArray));
//			else if(wrappedArray instanceof int[])
//				jg.writeStringField(ARRAY, JsonUtil.intArrayToBinString((int[]) wrappedArray));
//			else if(wrappedArray instanceof short[])
//				jg.writeStringField(ARRAY, JsonUtil.shortArrayToBinString((short[]) wrappedArray));
//			else if(wrappedArray instanceof byte[])
//				jg.writeStringField(ARRAY, JsonUtil.byteArrayToBinString((byte[]) wrappedArray));			
//			else
//				throw new JsonGenerationException("The wrapped array is not primary array.");
//			jg.writeEndObject();
		}else if(v instanceof VEnumArray){
//			jg.writeFieldName(VALUE);
//			jg.writeStartObject();
//			jg.writeNumberField(LENGTH, ((VEnumArray)v).getIndexes().size());
//			Object wrappedArray = CollectionNumbers.wrappedArray(((VNumberArray) v).getData());
//			if(wrappedArray instanceof int[])
//				jg.writeStringField(ARRAY, JsonUtil.intArrayToBinString((int[]) wrappedArray));
//			else
//				throw new JsonGenerationException("The wrapped array is not primary array.");
//			jg.writeEndObject();
		}else if(v instanceof VStringArray){
//			jg.writeFieldName(VALUE);
//			jg.writeStartArray();
//			for(String s: ((VStringArray)v).getData())
//				jg.writeString(s);
//			jg.writeEndArray();			
		}
		return null;
	}

	
	private static void writeTimeToJson(Time t, JsonGenerator jg) throws JsonGenerationException, IOException{
		jg.writeFieldName(TIME);
		jg.writeStartObject();
		jg.writeStringField(SECOND, JsonUtil.longToBinString(t.getTimestamp().getSec()));
		jg.writeStringField(NANOSECOND, JsonUtil.intToBinString(
				t.getTimestamp().getNanoSec()));
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
