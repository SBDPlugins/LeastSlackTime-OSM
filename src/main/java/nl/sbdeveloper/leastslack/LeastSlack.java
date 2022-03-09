package nl.sbdeveloper.leastslack;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeastSlack {
    private static final Pattern pattern = Pattern.compile("(\\d+)[ \\t]+(\\d+)");

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Geef een bestandsnaam mee!");
            System.exit(0);
            return;
        }

        URL resource = LeastSlack.class.getClassLoader().getResource(args[0]);
        if (resource == null) {
            System.out.println("De meegegeven bestandsnaam bestaat niet!");
            System.exit(0);
            return;
        }

        Scanner fileScanner = new Scanner(resource.openStream());

        JobShop shop = new JobShop();

        int jobs = 0;
        int machines = 0;
        int lowestMachine = Integer.MAX_VALUE;

        boolean wasFirstLine;
        int currentJob = 0;

        Matcher matcher;
        while (fileScanner.hasNextLine()) {
            matcher = pattern.matcher(fileScanner.nextLine());

            wasFirstLine = false;

            if (jobs != 0 && machines != 0) {
                Job job = new Job(currentJob);
                shop.getJobs().add(job);
            }

            while (matcher.find()) {
                if (jobs == 0 || machines == 0) {
                    jobs = Integer.parseInt(matcher.group(1));
                    machines = Integer.parseInt(matcher.group(2));
                    wasFirstLine = true;
                    break;
                } else {
                    int id = Integer.parseInt(matcher.group(1));
                    int duration = Integer.parseInt(matcher.group(2));

                    if (id < lowestMachine) lowestMachine = id;

                    Task task = new Task(id, duration);
                    int finalCurrentJob = currentJob;
                    Optional<Job> jobOpt = shop.getJobs().stream().filter(j -> j.getId() == finalCurrentJob).findFirst();
                    if (jobOpt.isEmpty()) break;
                    jobOpt.get().getTasks().add(task);
                }
            }

            if (!wasFirstLine) currentJob++;
        }

        fileScanner.close();

        //////////////////////////////////

        Map<Integer, Integer> machineBusyUntil = new HashMap<>(); //key = machine ID, value = busy end time

        //Fill with default values, all the machines -> 0
        for (int machineID = lowestMachine; machineID < machines; machineID++) {
            machineBusyUntil.put(machineID, 0);
        }

        int time = 0;
        while (!shop.isAllJobsDone()) {
            System.out.println("------START------");

            List<Integer> machinesDone = new ArrayList<>();
            List<Integer> machinesNotDone = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : machineBusyUntil.entrySet()) {
                if (time < entry.getValue()) {
                    machinesNotDone.add(entry.getKey());
                    continue; //Machine is still busy!!!
                }
                if (!shop.isAllJobsDoneOnMachine(entry.getKey())) {
                    machinesNotDone.add(entry.getKey());
                    continue; //Machine is still busy (double check)!!!
                }

                machinesDone.add(entry.getKey());
            }

            if (machinesDone.size() > 0) {
                System.out.println("------CALCULATING SLACK------");
                shop.calculateSlack();
                for (Job j : shop.getJobsSorted()) {
                    System.out.println("Job " + j.getId() + " (totale duration " + j.calculateTotalDuration() + "):");
                    for (Task t : j.getTasksSortedByMachine(true)) {
                        System.out.println("Voor machine " + t.getMachineID() + " is een duration van " + t.getDuration() + " en een slack van " + t.getSlack() + ".");
                    }
                }

                List<Task> tasks = new ArrayList<>(); //De taken die in deze loop worden uitgevoerd.

                for (int machineID : machinesDone) {
                    List<Map.Entry<Job, Task>> map = shop.getTasksSortedBySlack(machineID);
                    if (map.isEmpty()) continue;

                    for (Map.Entry<Job, Task> toRun : map) {
                        int previousMachine = machineID - 1;
                        if (previousMachine >= lowestMachine) {
                            Task task = toRun.getKey().getTask(previousMachine);
                            if (task != null) continue; //The previous job is not done yet.
                        }

                        tasks.add(toRun.getValue());

                        toRun.getValue().setRunning(true);

                        toRun.getKey().setBeginTime(time);

                        System.out.println("Job " + toRun.getKey().getId() + " wordt op machine " + toRun.getValue().getMachineID() + " uitgevoerd!");
                        break;
                    }
                }

                Optional<Task> smallestTask = tasks.stream().min(Comparator.comparing(Task::getDuration));
                if (smallestTask.isPresent()) {
                    int smallestDuration = smallestTask.get().getDuration();

                    for (Task t : tasks) {
                        t.setTimeLeft(t.getTimeLeft() - smallestDuration);
                        System.out.println("Machine is nog " + t.getTimeLeft() + " bezig.");
                    }

                    time += smallestDuration;
                }

                tasks.forEach(t -> t.setRunning(false));

                System.out.println("------END------");

                System.out.println("Time: " + time);

                for (Job j : shop.getJobs()) {
                    if (j.getEndTime() == 0 && j.hasAllTasksDone()) j.setEndTime(time);
                }
            }

            for (int machineID : machinesNotDone) {
                Task t = shop.getRunningTask(machineID);
                if (t == null) continue;

                t.setTimeLeft(t.getTimeLeft() - 1);

                System.out.println("Machine is nog " + t.getTimeLeft() + " bezig.");
            }
        }

        for (Job j : shop.getJobs()) {
            System.out.println("Job " + j.getId() + " begon op " + j.getBeginTime() + " en eindigde op " + j.getEndTime() + ".");
        }
    }
}
