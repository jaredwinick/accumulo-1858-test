package org.apache.accumulo.core.util;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.apache.zookeeper.ZooKeeper;

public class ClientThreads {
	
	/**
	 * kills all threads created by internal Accumulo singleton resources. After
	 * this method is called, no accumulo client will work in the current
	 * classloader.
	 */
	public static void shutdownNow() {
		shutdownThriftTransportPoolThreads();
		shutdownZooKeepers();
		waitForZooKeeperClientThreads();
	}
	
	/**
	 * Forcefully shutdown the Closer Daemon of the ThriftTransportPool. If the Instance.close()
	 * functionality gets backed out, then likely the TTP.close() will not exist either as it came 
	 * in the same commit (https://github.com/apache/accumulo/commit/04f81b50d65b1d359cef2d38d5a6793dd6b4065f)
	 * Additionally, this forceful method will allow users of current Accumulo versions to use these ClientThreads
	 * methods in their own applications if need be without modification to Accumulo code.
	 *
	 */
	private static void shutdownThriftTransportPoolThreads() {
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		System.out.println("Current Classloader:" + currentClassLoader);
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread thread : threadSet) {
			System.out.println(String.format("[%s] [%s]", thread.getName(), thread.getClass().getName()));
			printThreadClassloaders(thread);

			if (threadHasNameAndCurrentClassLoader(thread, "Thrift Connection Pool Checker")) {
				thread.stop();
				
				while (thread.isAlive()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Checks to see if the thread has the given name and has the same ClassLoader
	 * as the current thread
	 * @param thread
	 * @param threadName
	 * @return 
	 */
	private static boolean threadHasNameAndCurrentClassLoader(final Thread thread, final String threadName) {
		
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		if (thread.getName().equals(threadName) && thread.getContextClassLoader().equals(currentClassLoader)) {
			return true;
		}
		return false;
	}
	
	private static boolean threadClassStartsWithNameAndCurrentClassLoader(final Thread thread, final String threadName) {
		
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		if (thread.getClass().getName().startsWith(threadName) && thread.getContextClassLoader().equals(currentClassLoader)) {
			return true;
		}
		return false;
	}
	
	private static void printThreadClassloaders(Thread thread) {
		ClassLoader currentClassLoader = thread.getContextClassLoader();
		while (currentClassLoader != null) {
			System.out.println("Classloader:" + currentClassLoader.toString());
			currentClassLoader = currentClassLoader.getParent();
		}
	}
	
	/**
	 * Wait for ZooKeeper threads to die
	 */
	private static void waitForZooKeeperClientThreads() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread thread : threadSet) {
			if (threadClassStartsWithNameAndCurrentClassLoader(thread, "org.apache.zookeeper.ClientCnxn")) {

				while (thread.isAlive()) {
					System.out.println("thread " + thread.getName() + " is still alive in state: "  + thread.getState().toString());
					try {
						Thread.sleep(100); 
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				System.out.println("thread " + thread.getName() + " is now dead");
			}
		}
	}
	
	/**
	 * Gracefully shutdown the ZooKeepers that the ZooSession holds a reference to
	 */
	private static void shutdownZooKeepers() {
		
		try {
			Class zooSessionClass;
			
			// get the package private ZooSession - refactored to fate package in 1.5
			zooSessionClass = Class.forName("org.apache.accumulo.core.zookeeper.ZooSession");
			Field sessionsField = zooSessionClass.getDeclaredField("sessions");
			sessionsField.setAccessible(true);
			Map<?,?> sessions = (Map<?,?>) sessionsField.get(null);
			
			// Iterate over each ZooSessionInfo and get the ZooKeeper and close it
			for (Object zsi : sessions.values()) {
				Field zooKeeperField = zsi.getClass().getDeclaredField("zooKeeper");
				zooKeeperField.setAccessible(true);
				ZooKeeper zooKeeper = (ZooKeeper) zooKeeperField.get(zsi);
				zooKeeper.close();
			}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
