package com.github.kokorin.jaffree.process;

/**
 * @Author Speiger
 */
public class ProcessListener {

    /**
     * Callback for when the Process is started.
     * @param process the started process
     */
    public void onProcessStart(Process process);
    /**
     * Callback for when the Process is stopped for whatever reason.
     * @param process the stopped process
     */
    public void onProcessStop(Process process);
}