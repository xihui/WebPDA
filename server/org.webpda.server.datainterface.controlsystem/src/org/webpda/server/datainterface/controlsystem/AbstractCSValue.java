package org.webpda.server.datainterface.controlsystem;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

import org.epics.util.time.Timestamp;
import org.epics.vtype.AlarmSeverity;
import org.webpda.server.core.JsonUtil;
import org.webpda.server.core.LoggerUtil;
import org.webpda.server.datainterface.IValue;

import com.fasterxml.jackson.core.JsonGenerator;

/**Abstract value that represents live value in a control system.
 * @author Xihui Chen, Kay Kasemir
 *
 */
public abstract class AbstractCSValue implements IValue{
	
	public final static String TIME = "t"; //$NON-NLS-1$
	public final static String SECOND = "s"; //$NON-NLS-1$
	public final static String NANOSECOND = "ns"; //$NON-NLS-1$
	public final static String SEVERITY="sev"; //$NON-NLS-1$
	public final static String ALARM_NAME="an";	//$NON-NLS-1$
	public final static String VALUE = "v"; //$NON-NLS-1$

	
	private final Timestamp time;
	private final AlarmSeverity severity;
	private final String alarmName;	
	
	public AbstractCSValue(Timestamp time, AlarmSeverity severity,
			String alarmName) {
		this.time = time;
		this.severity = severity;
		this.alarmName = alarmName;
	}

	/** Get the time stamp.
     *  @return The time stamp.
     */
    public Timestamp getTime(){
    	return time;
    }

    /** Get the severity info.
     *  @see ISeverity
     *  @see #getStatus()
     *  @return The severity info.
     */
    public AlarmSeverity getSeverity(){
    	return severity;
    }

    /** Get the status text that might describe the severity.
     *  @see #getSeverity()
     *  @return The status string.
     */
    public String getAlarmName(){
    	return alarmName;
    }

    /**Create a {@link JsonGenerator} and write some fields.
     * @return an opened {@link JsonGenerator}. 
     * Its output target is {@link ByteArrayOutputStream}. Subclass is responsible for 
     * closing the generator and output stream.
     */
    protected JsonGenerator createJsonGenerator(){
    	try {
			JsonGenerator jg = JsonUtil.jsonFactory.createGenerator(new ByteArrayOutputStream());
			jg.writeStartObject();
			jg.writeFieldName(TIME);
			jg.writeStartObject();
			jg.writeNumberField(SECOND, time.getSec());
			jg.writeNumberField(NANOSECOND, time.getNanoSec());
			jg.writeEndObject();
			if(severity !=null && severity != AlarmSeverity.NONE){
				jg.writeStringField(SEVERITY, severity.name());
				jg.writeStringField(ALARM_NAME, alarmName);
			}			
			return jg;
		} catch (Exception e) {
			LoggerUtil.getLogger().log(Level.SEVERE, "Failed to create JsonGenerator", e);
			return null;
		}
    }
    
    @Override
    public String toString() {
    	return toJson();
    }
    
  
}
