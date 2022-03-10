package nl.sbdeveloper.leastslack;

public class Task {
    private final int machineID;
    private final int duration;
    private int earliestStart = 0;
    private int latestStart = 0;
    private boolean done = false;

    public Task(int machineID, int duration) {
        this.machineID = machineID;
        this.duration = duration;
    }

    public int getMachineID() {
        return machineID;
    }

    public int getDuration() {
        return duration;
    }

    public int getEarliestStart() {
        return earliestStart;
    }

    public void setEarliestStart(int earliestStart) {
        this.earliestStart = earliestStart;
    }

    public int getLatestStart() {
        return latestStart;
    }

    public void setLatestStart(int latestStart) {
        this.latestStart = latestStart;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
