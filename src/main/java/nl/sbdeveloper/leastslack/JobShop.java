package nl.sbdeveloper.leastslack;

import java.util.*;
import java.util.stream.Collectors;

public class JobShop {
    private final List<Job> jobs = new ArrayList<>();

    public List<Job> getJobs() {
        return jobs;
    }

    /**
     * Check of alle jobs zijn afgehandeld.
     *
     * @return true/false
     */
    public boolean isAllJobsDone() {
        return jobs.stream().noneMatch(j -> j.getTasks().stream().anyMatch(t -> t.getTimeLeft() > 0));
    }

    /**
     * Check of alle jobs zijn afgehandeld.
     *
     * @return true/false
     */
    public boolean isAllJobsDoneOnMachine(int machineID) {
        for (Job j : jobs) {
            for (Task t : j.getTasks()) {
                if (t.getMachineID() == machineID && t.getTimeLeft() != t.getDuration() && t.getTimeLeft() > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Haal de jobs op, gesorteerd op lengte (lang naar kort).
     *
     * @return Een lijst met de jobs.
     */
    public List<Job> getJobsSorted() {
        return jobs.stream().sorted(Comparator.comparing(Job::calculateTotalDuration).reversed()).toList();
    }

    /**
     * Haal per job de taak op die hoort bij deze specifieke machine.
     *
     * @param machineID De ID van de machine om op te zoeken.
     * @return Een map met als key de Job en als value de Task die hoort bij de machine.
     */
    public List<Map.Entry<Job, Task>> getTasksSortedBySlack(int machineID) {
        Map<Job, Task> map = new HashMap<>();
        for (Job j : jobs) {
            //if (j.hasRunningTask()) continue;
            Task foundTask = j.getTask(machineID);
            if (foundTask == null) continue;
            map.put(j, foundTask);
        }
        return map.entrySet().stream().sorted(Comparator.comparing(e -> e.getValue().getSlack())).collect(Collectors.toList());
    }

    public Task getRunningTask(int machineID) {
        for (Job j : jobs) {
            for (Task t : j.getTasks()) {
                if (t.getMachineID() == machineID && t.getDuration() != t.getTimeLeft() && t.getTimeLeft() > 0) return t;
            }
        }
        return null;
    }

    /**
     * Bereken de slack voor alle jobs (en taken) binnen de shop.
     */
    public void calculateSlack() {
        int highestDuration = -1;
        for (Job j : getJobsSorted()) {
            if (highestDuration == -1) {
                highestDuration = j.calculateTotalDuration();
                j.getTasks().forEach(t -> t.setSlack(0));
            } else {
                j.calculateSlack(highestDuration);
            }
        }
    }
}
