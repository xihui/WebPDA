package org.webpda.server.datainterface;

import java.util.concurrent.Executor;

import org.webpda.server.core.ConfigurePropertyConstants;

public class PVFactory extends AbstractPVFactory {

	private static PVFactory instance;
	private AbstractPVFactory internalFactoy;

	private PVFactory() throws Exception {
		String pvFactoryName = System
				.getProperty(ConfigurePropertyConstants.PV_FACTORY_PROPERTY);
		Class<?> clazz = Class.forName(pvFactoryName);
		if (clazz != null) {
			Object newInstance = clazz.newInstance();
			if (newInstance instanceof AbstractPVFactory)
				internalFactoy = (AbstractPVFactory) newInstance;
			else
				throw new Exception("Failed to create pv factory: "
						+ pvFactoryName);
		} else
			throw new Exception("PV factory class not found: " + pvFactoryName);

	}

	public synchronized static PVFactory getInstance() throws Exception {
		if (instance == null)
			instance = new PVFactory();
		return instance;
	}

	@Override
	public IPV createPV(String name, boolean readOnly,
			long minUpdatePeriodInMs, boolean bufferAllValues,
			Executor notificationThread, ExceptionHandler exceptionHandler)
			throws Exception {
		return internalFactoy.createPV(name, readOnly, minUpdatePeriodInMs,
				bufferAllValues, notificationThread, exceptionHandler);

	}

}
