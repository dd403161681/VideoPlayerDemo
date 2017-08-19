package io.vec.demo.logger;

import java.util.Map;

public class PerformanceLogger extends Thread {

	private Runtime mRuntime;

	public PerformanceLogger() {
		try {
			mRuntime = Runtime.getRuntime();
			LocalLogger.LOGGER.info("******Memory logger started ******");
		} catch (Exception e) {
			LocalLogger.LOGGER.error("Error while PerformanceLogger ",e);
		}

	}

	public void run() {
		currentThread().setName("Performance Logger");
		while (true) {
			LogMemory();
			try {
				sleep(60000);
			} catch (InterruptedException e) {
				LocalLogger.LOGGER.error("Unable to sleep thread ",e);
			}
		}
	}

	private void LogMemory() {
		try {
			long totalmemory = mRuntime.totalMemory() / 1048576L;
			long freeMemory = mRuntime.freeMemory();
			long memoryUsed = (mRuntime.totalMemory() - freeMemory) / 1048576L;

			LocalLogger.LOGGER.info("Total Memory :" + totalmemory + " MB");
			LocalLogger.LOGGER.info("Free Memory :" + freeMemory / 1048576L
					+ " MB");
			LocalLogger.LOGGER.info("Used Memory :" + memoryUsed + " MB");
			long memoryPerc = (memoryUsed * 100) / totalmemory;
			LocalLogger.LOGGER.info("Percentage of Used Memory :" + memoryPerc
					+ " %");

			if (memoryPerc >= 75) {
				FreeMemory();
			} else {
				LocalLogger.LOGGER.info("No need to release memory. Only "
						+ memoryPerc + "% memory used.");
			}

			LogThreads();

		} catch (Exception e) {
			LocalLogger.LOGGER.info("Unable to get memory details ",e);
		}
	}

	private void FreeMemory() {
		try {
			System.gc();
			LocalLogger.LOGGER.info("Memory Released");
		} catch (Exception e) {
			LocalLogger.LOGGER.error("Error while deallocating memory : ", e);
		}
	}

	private void LogThreads() {
		try {
			// Calculate Current threads in the system
			Map<Thread, StackTraceElement[]> myMap = Thread.getAllStackTraces();
			LocalLogger.LOGGER.debug("Threads in the system : " + myMap);

			LocalLogger.LOGGER.debug("Thread Values() : " + myMap.values());
			LocalLogger.LOGGER.debug("Threads Size() : " + myMap.size());
			LocalLogger.LOGGER
					.debug("Threads toString() : " + myMap.toString());
			LocalLogger.LOGGER.debug("Threads activeCount(): "
					+ Thread.activeCount());
		} catch (Exception e) {
			LocalLogger.LOGGER.error("Unable to get thread details ", e);
		}
	}
}
