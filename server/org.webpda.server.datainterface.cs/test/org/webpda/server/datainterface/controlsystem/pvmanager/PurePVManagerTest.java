package org.webpda.server.datainterface.controlsystem.pvmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.epics.pvmanager.CompositeDataSource;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVReader;
import org.epics.pvmanager.PVReaderEvent;
import org.epics.pvmanager.PVReaderListener;
import org.epics.pvmanager.jca.JCADataSource;
import org.epics.pvmanager.loc.LocalDataSource;
import org.epics.pvmanager.sim.SimulationDataSource;
import org.epics.pvmanager.sys.SystemDataSource;
import org.junit.Before;
import org.junit.Test;

import static org.epics.util.time.TimeDuration.*;
import static org.epics.pvmanager.ExpressionLanguage.*;

public class PurePVManagerTest {

	private static final ExecutorService pvThread = Executors
			.newSingleThreadExecutor();

	@Before
	public void setup() {

		final CompositeDataSource sources = new CompositeDataSource();
		sources.putDataSource("sim", new SimulationDataSource());
		sources.putDataSource("loc", new LocalDataSource());
		sources.putDataSource("ca", new JCADataSource());
		sources.putDataSource("sys", new SystemDataSource());
		sources.setDefaultDataSource("ca");
		PVManager.setDefaultDataSource(sources);
	}

	@Test
	public void test() throws InterruptedException {
		List<PVReader<Object>> hodler = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			PVReader<Object> pvReader = PVManager.read(channel("sim://sine("+i+",1000,100,1)"))
					.notifyOn(pvThread)
					.readListener(new PVReaderListener<Object>() {
						@Override
						public void pvChanged(PVReaderEvent<Object> event) {
							// Do something with each value
							Object newValue = event.getPvReader().getValue();
						}
					}).maxRate(ofMillis(1000));
			hodler.add(pvReader);
		}
		int i=0;
		while (i<1000) {
			Thread.sleep(1000);
			i++;
		}
		for(PVReader<Object> pvReader:hodler){
			pvReader.close();
		}

	}

}
