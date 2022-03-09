package nl.sbdeveloper.leastslack;

public class Machine {
    private final int id;
    private Task currentTask;
    private boolean firstRun = false;

    public Machine(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isBusy() {
        return currentTask != null && currentTask.getDuration() != currentTask.getTimeLeft() && currentTask.getTimeLeft() > 0;
    }

    public int getTimeLeft() {
        return currentTask != null ? currentTask.getTimeLeft() : 0;
    }

    public void updateTime() {
        if (firstRun) { //De 1e keer niet updaten, hij is net gestart!
            firstRun = false;
            return;
        }

        currentTask.setTimeLeft(currentTask.getTimeLeft() - 1);
    }

    public void setFirstRun() {
        this.firstRun = true;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }
}
