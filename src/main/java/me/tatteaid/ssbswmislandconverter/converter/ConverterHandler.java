package me.tatteaid.ssbswmislandconverter.converter;

public class ConverterHandler {

    private volatile boolean taskRunning;
    private volatile boolean taskStopped;

    public ConverterHandler() {
        this.taskRunning = false;
        this.taskStopped = false;
    }

    public boolean isTaskRunning() {
        return taskRunning;
    }

    public void setTaskRunning(boolean taskRunning) {
        this.taskRunning = taskRunning;
    }

    public boolean isTaskStopped() {
        return taskStopped;
    }

    public void setTaskStopped(boolean taskStopped) {
        this.taskStopped = taskStopped;
    }
}