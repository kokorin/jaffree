package com.github.kokorin.jaffree.process;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Speiger
 */
public interface ProcessListener {
	/**
	 * Provides the Process instance that was started
	 * @param process the process
	 */
	void onStart(Process process);
	
	/**
	 * Provides the Process instance that was just completed
	 * @param process the process
	 */
	void onStop(Process process);
	
	/**
	 * Simple tracker wrapper that allows to track all instances being loaded.
	 * 
	 * @param instances Set. Highly Suggest {@link Collections#newSetFromMap} using a {@link ConcurrentHashMap} for multithreading support
	 * @return ProcessListener wrapper
	 */
	static ProcessListener of(Set<Process> instances) {
		return new Impl(instances);
	}
	
	static class Impl implements ProcessListener {
		Set<Process> instances;

		public Impl(Set<Process> instances) {
			this.instances = instances;
		}
		
		@Override
		public void onStart(Process process) {
			instances.add(process);
		}
		
		@Override
		public void onStop(Process process) {
			instances.remove(process);
		}
		
	}
}
