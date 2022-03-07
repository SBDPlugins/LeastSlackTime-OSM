package nl.sbdeveloper.leastslack;

import java.util.Objects;

public class Task {
    private final int machineID;
    private final int duration;
    private int timeLeft;
    private int slack;
    private boolean running;

    public Task(int machineID, int duration) {
        this.machineID = machineID;
        this.duration = duration;
        this.timeLeft = duration;
    }

    public int getMachineID() {
        return machineID;
    }

    public int getDuration() {
        return duration;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public int getSlack() {
        return slack;
    }

    public void setSlack(int slack) {
        this.slack = slack;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return machineID == task.machineID && duration == task.duration && timeLeft == task.timeLeft && slack == task.slack && running == task.running;
    }

    @Override
    public int hashCode() {
        return Objects.hash(machineID, duration, timeLeft, slack, running);
    }
}
