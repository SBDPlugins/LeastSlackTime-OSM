package nl.sbdeveloper.leastslack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Job {
    private final int id;
    private final List<Task> tasks = new ArrayList<>();
    private boolean hasBeginTimeSet = false;
    private int beginTime;
    private int endTime;

    public Job(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        if (hasBeginTimeSet) return;
        this.beginTime = beginTime;
        hasBeginTimeSet = true;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    /**
     * Haalt een actieve taak op binnen deze job die hoort bij de meegegeven machine.
     *
     * @param machineID De ID van de machine.
     * @return De taak die erbij hoort.
     */
    public Task getTask(int machineID) {
        return tasks.stream().filter(t -> t.getMachineID() == machineID && t.getTimeLeft() > 0).findAny().orElse(null);
    }

    /**
     * Haal de actieve taken op gesorteerd op de machine
     * @param reversed Wil je hem van 1-3 of andersom?
     * @return De lijst van taken.
     */
    public List<Task> getTasksSortedByMachine(boolean reversed) {
        return reversed ? tasks.stream().filter(t -> t.getTimeLeft() > 0).sorted(Comparator.comparing(Task::getMachineID).reversed()).toList() : tasks.stream().sorted(Comparator.comparing(Task::getMachineID)).toList();
    }

    /**
     * Bereken de totale duration van alle actieve taken in deze job.
     * @return De totale duration.
     */
    public int calculateTotalDuration() {
        return tasks.stream().filter(t -> t.getTimeLeft() > 0).map(Task::getDuration).mapToInt(Integer::intValue).sum();
    }

    public boolean hasAllTasksDone() {
        return tasks.stream().allMatch(t -> t.getTimeLeft() <= 0);
    }

    /**
     * Bereken de slack op alle taken in deze job.
     *
     * @param longestDuration De lengte van de langste job binnen de shop.
     */
    public void calculateSlack(int longestDuration) {
        int counter = longestDuration;
        for (Task t : getTasksSortedByMachine(true)) {
            if (t.getTimeLeft() <= 0) continue; //Uitgevoerde taak, negeren.
            counter -= t.getDuration();
            t.setSlack(counter);
        }
    }
}
