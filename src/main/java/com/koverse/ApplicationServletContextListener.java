package com.koverse;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.util.ClientThreads;


public class ApplicationServletContextListener implements
		ServletContextListener {

	private Connector connector;
	private Instance instance;
	
	public void contextDestroyed(ServletContextEvent event) {
		
		ClientThreads.shutdownNow();
		System.out.println("Context Destroyed");

	}

	public void contextInitialized(ServletContextEvent event) {
				 
		
		instance = new ZooKeeperInstance("kv_vagrant", "vagrant");
		try {
			connector = instance.getConnector("root", "vagrant".getBytes());
		} catch (AccumuloException e) {
			e.printStackTrace();
		} catch (AccumuloSecurityException e) {
			e.printStackTrace();
		}
		
		System.out.println("Context Initialized");

	}
}
