package com.koverse;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.zookeeper.server.ZooTrace;


public class ApplicationServletContextListener implements
		ServletContextListener {

	private Connector connector;
	private Instance instance;
	
	public void contextDestroyed(ServletContextEvent event) {
		
		try {
			System.out.println("Closing Instance");
			instance.close();
			System.out.println("Instance closed");
		}
		catch (AccumuloException e) {
			e.printStackTrace();
		}
		
		System.out.println("Context Destroyed");

	}

	public void contextInitialized(ServletContextEvent event) {
				 
		instance = new ZooKeeperInstance("kv_vagrant", "vagrant");
		try {
			connector = instance.getConnector("root", "vagrant".getBytes());
		} catch (AccumuloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AccumuloSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Context Initialized");

	}
}
