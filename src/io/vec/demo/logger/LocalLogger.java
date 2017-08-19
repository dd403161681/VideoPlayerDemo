package io.vec.demo.logger;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.mindpipe.android.logging.log4j.LogConfigurator;
import android.app.Application;
import android.os.Environment;

public class LocalLogger extends Application {
	public static final Logger LOGGER;

	static {
		LogConfigurator logConfigurator = new LogConfigurator();
		logConfigurator.setFileName(Environment.getExternalStorageDirectory()
				+ File.separator + "XM" + File.separator + "logs"
				+ File.separator + "mediaCodec.log");
		logConfigurator.setRootLevel(Level.INFO);
		logConfigurator.setLevel("org.apache", Level.ERROR);
		// logConfigurator.setFilePattern("%d %-5p [%F:%L] %m%n");
		logConfigurator
				.setFilePattern("%-5p %d{ISO8601} [%t] [%F:%L] %x - %m%n");
		logConfigurator.setMaxFileSize(1024 * 1024 * 5);
		logConfigurator.setImmediateFlush(true);
		logConfigurator.configure();
		LOGGER = Logger.getLogger(LocalLogger.class);
	}
}
