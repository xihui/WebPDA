package org.webpda.server.datainterface.controlsystem.pvmanager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.webpda.server.core.ConfigurePropertyConstants;
import org.webpda.server.core.datainterface.IPV;
import org.webpda.server.core.datainterface.IPVListener;
import org.webpda.server.core.datainterface.PVFactory;
import org.webpda.server.datainterface.cs.pvmanager.PVManagerPVFactory;
public class PureSimplePVPVManagerTest {
	
	private PVFactory factory;

	@Before
	public void setup() throws Exception{


    	System.setProperty(ConfigurePropertyConstants.PV_FACTORY_CLASS, 
    			"org.webpda.server.datainterface.cs.pvmanager.PVManagerPVFactory");
        factory = PVFactory.getInstance();
	}

	@Test
	public void test() throws Exception {
		List<IPV> holder = new ArrayList<>();
		for(int i=0; i<1000;i++){
		 LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
	        parameters.put(PVManagerPVFactory.READ_ONLY,true);
	        parameters.put(PVManagerPVFactory.UPDATE_PERIOD, 1);
	        parameters.put(PVManagerPVFactory.BUFFER_ALL_VALUES, false);
	        final IPV pv = factory.createPV("sim://sine("+i+",1000,100,1)", parameters);
	        pv.start();
	        pv.addListener(new IPVListener.Stub(){
	        	@Override
	        	public void valueChanged(IPV pv) {
	        		Object value = pv.getValue();
	        	}
	        });
	        holder.add(pv);
		}
		int i=0;
	     while(i<1000){
	 		Thread.sleep(1000);
	 		i++;
	 	}
	 	for(IPV pv:holder){
	 		pv.stop();
	 	}
	}

}
