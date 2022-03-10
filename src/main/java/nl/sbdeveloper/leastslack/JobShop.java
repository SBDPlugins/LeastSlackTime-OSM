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
        return jobs.stream().noneMatch(j -> j.getTasks().stream().anyMatch(t -> !t.isDone()));
    }

    public List<Map.Entry<Job, Task>> getTasksWithEarliestTimeNow(int currentTime) {
        Map<Job, Task> map = new HashMap<>();
        for (Job j : jobs) {
            for (Task t : j.getTasks()) {
                if (t.isDone()) continue; //Deze taak is klaar!

                if (t.getEarliestStart() == currentTime) map.put(j, t);
            }
        }

        return map.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getSlack())).collect(Collectors.toList());
    }

//    /**
//     * Haal per job de taak op die hoort bij deze specifieke machine.
//     *
//     * @param machineID De ID van de machine om op te zoeken.
//     * @return Een map met als key de Job en als value de Task die hoort bij de machine.
//     */
//    public List<Map.Entry<Job, Task>> getTasksSortedBySlack(int machineID) {
//        Map<Job, Task> map = new HashMap<>();
//        for (Job j : jobs) {
//            //if (j.hasRunningTask()) continue;
//            Task foundTask = j.getTask(machineID);
//            if (foundTask == null) continue;
//            map.put(j, foundTask);
//        }
//        return map.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getSlack())).collect(Collectors.toList());
//    }

    /**
     * Bereken de slack voor alle jobs (en taken) binnen de shop.
     */
    public void calculateSlack() {
        jobs.forEach(j -> j.getTasks().stream().max(Comparator.comparing(Task::getMachineID)).ifPresent(task -> j.setSlack(task.getLatestStart() - task.getEarliestStart())));
    }
}
