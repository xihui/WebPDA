package org.webpda.server.datainterface.controlsystem;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

import org.webpda.server.core.Constants;
import org.webpda.server.core.JsonUtil;
import org.webpda.server.core.LoggerUtil;
import org.webpda.server.datainterface.IMetaData;

import com.fasterxml.jackson.core.JsonGenerator;

/**Meta data for Control system numeric value. (Originally copied from Utility PV).
 * @author Xihui Chen, Kay Kasemir
 *
 */
public class CSNumericMetaData implements IMetaData {
	
		public final static String DISPLAY_LOW = "dl"; //$NON-NLS-1$
		public final static String DISPLAY_HIGH = "dh"; //$NON-NLS-1$
		public final static String WARN_LOW = "wl"; //$NON-NLS-1$w
		public final static String WARN_HIGH = "wh"; //$NON-NLS-1$
		public final static String ALARM_LOW = "al"; //$NON-NLS-1$
		public final static String ALARM_HIGH = "ah"; //$NON-NLS-1$
		public final static String PRECISION = "prec"; //$NON-NLS-1$
		public final static String UNITS = "units"; //$NON-NLS-1$
		
	    private final double disp_low;
		private final double disp_high;
	    private final double warn_low;
	    private final double warn_high;
	    private final double alarm_low;
		private final double alarm_high;
		private final int prec;
		private final String units;
		
		

		/** Constructor for meta data from pieces. */
		public CSNumericMetaData(double disp_low, double disp_high,
	                    double warn_low, double warn_high,
	                    double alarm_low, double alarm_high,
	                    int prec, String units)
		{
	        this.disp_low = disp_low;
			this.disp_high = disp_high;
	        this.warn_low = warn_low;
	        this.warn_high = warn_high;
	        this.alarm_low = alarm_low;
			this.alarm_high = alarm_high;
			this.prec = prec;
			this.units = units;
		}


	    public double getDisplayLow()
	    {   return disp_low;    }

	    public double getDisplayHigh()
	    {   return disp_high;   }


	    public double getWarnLow()
	    {   return warn_low;    }


	    public double getWarnHigh()
	    {   return warn_high;   }


	    public double getAlarmLow()
	    {   return alarm_low;   }


	    public double getAlarmHigh()
	    {   return alarm_high;  }


		public int getPrecision()
		{	return prec;	}


		public String getUnits()
		{	return units;	}

	    @Override
	    public int hashCode()
	    {
		    final int prime = 31;
		    int result = 1;
		    long temp;
		    temp = Double.doubleToLongBits(alarm_high);
		    result = prime * result + (int) (temp ^ (temp >>> 32));
		    temp = Double.doubleToLongBits(alarm_low);
		    result = prime * result + (int) (temp ^ (temp >>> 32));
		    temp = Double.doubleToLongBits(disp_high);
		    result = prime * result + (int) (temp ^ (temp >>> 32));
		    temp = Double.doubleToLongBits(disp_low);
		    result = prime * result + (int) (temp ^ (temp >>> 32));
		    result = prime * result + prec;
		    result = prime * result + ((units == null) ? 0 : units.hashCode());
		    temp = Double.doubleToLongBits(warn_high);
		    result = prime * result + (int) (temp ^ (temp >>> 32));
		    temp = Double.doubleToLongBits(warn_low);
		    result = prime * result + (int) (temp ^ (temp >>> 32));
		    return result;
	    }

	    /** @return <code>true</code> if given meta data equals this */
	    @Override
	    public boolean equals(final Object obj)
	    {
	        if (obj == this)
	            return true;
	        if (! (obj instanceof CSNumericMetaData))
	            return false;
	        final CSNumericMetaData other = (CSNumericMetaData) obj;
	        // Compare all the elements, w/ proper handling of double NaN/Inf.
	        return Double.doubleToLongBits(other.getDisplayLow())
	                == Double.doubleToLongBits(disp_low) &&
	               Double.doubleToLongBits(other.getDisplayHigh())
	                == Double.doubleToLongBits(disp_high) &&
	               Double.doubleToLongBits(other.getWarnLow())
	                == Double.doubleToLongBits(warn_low) &&
	               Double.doubleToLongBits(other.getWarnHigh())
	                == Double.doubleToLongBits(warn_high) &&
	               Double.doubleToLongBits(other.getAlarmHigh())
	                == Double.doubleToLongBits(alarm_high) &&
	               Double.doubleToLongBits(other.getAlarmLow())
	                == Double.doubleToLongBits(alarm_low) &&
	               other.getPrecision() == prec &&
	               other.getUnits().equals(units);
	    }

	    /** {@inheritDoc} */
	    @Override
		@SuppressWarnings("nls")
	    public String toString()
		{
			return "NumericMetaData:\n"
	        + "    units      :" + units + "\n"
	        + "    prec       :" + prec + "\n"
	        + "    disp_low   :" + disp_low + "\n"
			+ "    disp_high  :" + disp_high + "\n"
	        + "    alarm_low  :" + alarm_low + "\n"
	        + "    warn_low   :" + warn_low + "\n"
	        + "    warn_high  :" + warn_high + "\n"
			+ "    alarm_high :" + alarm_high + "\n";
		}


		@Override
		public String toJson() {
			try {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				JsonGenerator jg = JsonUtil.jsonFactory.createGenerator(outputStream);
				jg.writeStartObject();
				jg.writeNumberField(DISPLAY_LOW, disp_low);
				jg.writeNumberField(DISPLAY_HIGH, disp_high);
				jg.writeNumberField(WARN_LOW, warn_low);
				jg.writeNumberField(WARN_HIGH, warn_high);
				jg.writeNumberField(ALARM_LOW, alarm_low);
				jg.writeNumberField(ALARM_HIGH, alarm_high);
				jg.writeNumberField(PRECISION, prec);
				jg.writeStringField(UNITS, units);		
				jg.writeEndObject();
				jg.close();
				String s = outputStream.toString(Constants.CHARSET);
				outputStream.close();
				return s;
			} catch (Exception e) {
				LoggerUtil.getLogger().log(Level.SEVERE, "Failed to create json.", e);
			}
			
			return null;
		}
}
