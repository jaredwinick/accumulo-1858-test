package com.koverse;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.ZooKeeperInstance;


public class ApplicationServletContextListener implements
		ServletContextListener {

	private Connector connector;
	private Instance instance;
	
	public void contextDestroyed(ServletContextEvent event) {
		
		//try {
			System.out.println("Closing Instance");
			//instance.close();
			System.out.println("Instance closed...waiting");
			
			/*
			 * Currently it appears we need to wait for the ZK threads (particularly the ClientCnxn SendThread)
			 *  to shutdown before letting the container clean up the application/drop loaded classes. If we do 
			 *  not sleep, the container will clean up the application and when the ZK ClientCnxn SendThread finally
			 *  stops, we end up seeing a
			 *  java.lang.NoClassDefFoundError: org/apache/zookeeper/server/ZooTrace
			 *  as the ZK code is trying to use ZooTrace which has already been unloaded by the container. The 
			 *  ZooKeeper.close() method isn't blocking and it doesn't appear there is any way to know when the
			 *  internal threads have actually exited.
			 *  
			 *  This is the same issue a user appears to have with Tomcat
			 *  http://mail-archives.apache.org/mod_mbox/tomcat-users/201306.mbox/%3CBAY174-W2088813852FC18913D5FC8A89B0@phx.gbl%3E
			 *  also cross posted at
			 *  http://mail-archives.apache.org/mod_mbox/zookeeper-user/201306.mbox/%3CBAY174-W21494DF40247669DA7B719A89B0@phx.gbl%3E
			 */
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		//}
		//catch (AccumuloException e) {
		//	e.printStackTrace();
		//}
		
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
