package org.webpda.server.datainterface.controlsystem;

import org.epics.vtype.VNumber;
import org.epics.vtype.VType;

public class VTypeConverter {

	private static ValueWithMeta VNumberToCSNumber(VNumber v) {
		ValueWithMeta result = new ValueWithMeta();
		result.value = new CSNumber(v.getTimestamp(), v.getValue(),
				v.getAlarmSeverity(), v.getAlarmName());
		result.meta = new CSNumericMetaData(v.getLowerDisplayLimit(),
				v.getUpperDisplayLimit(), v.getLowerWarningLimit(),
				v.getUpperWarningLimit(), v.getLowerAlarmLimit(),
				v.getUpperAlarmLimit(), v.getFormat()
						.getMaximumFractionDigits(), v.getUnits());
		return result;
	}
	
	public static ValueWithMeta toValueWithMeta(VType v){
		if(v instanceof VNumber)
			return VNumberToCSNumber((VNumber) v);
		return null;
	}
	

}
