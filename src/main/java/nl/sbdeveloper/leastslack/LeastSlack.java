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

        List<Machine> machineList = new ArrayList<>();
        for (int machineID = lowestMachine; machineID < machines; machineID++) {
            machineList.add(new Machine(machineID));
        }

        int time = 0;
        while (!shop.isAllJobsDone()) {
            System.out.println("------START------");

            if (machineList.stream().anyMatch(m -> !m.isBusy())) {
                System.out.println("------CALCULATING SLACK------");
                shop.calculateSlack();
                for (Job j : shop.getJobsSorted()) {
                    System.out.println("Job " + j.getId() + " (totale duration " + j.calculateTotalDuration() + "):");
                    for (Task t : j.getTasksSortedByMachine(true)) {
                        System.out.println("Voor machine " + t.getMachineID() + " is een duration van " + t.getDuration() + ", een slack van " + t.getSlack() + " en een time left van " + t.getTimeLeft() + ".");
                    }
                }

                List<Task> tasks = new ArrayList<>(); //De taken die in deze loop worden uitgevoerd.

                for (Machine m : machineList) {
                    if (m.isBusy()) continue;

                    List<Map.Entry<Job, Task>> map = shop.getTasksSortedBySlack(m.getId());
                    if (map.isEmpty()) continue;

                    for (Map.Entry<Job, Task> toRun : map) {
                        int previousMachine = m.getId() - 1;
                        if (previousMachine >= lowestMachine) {
                            Task task = toRun.getKey().getTask(previousMachine);
                            if (task != null) continue; //The previous job is not done yet.
                        }

                        tasks.add(toRun.getValue());

                        toRun.getValue().setRunning(true);

                        toRun.getKey().setBeginTime(time);

                        m.setCurrentTask(toRun.getValue());
                        m.setFirstRun();

                        System.out.println("Job " + toRun.getKey().getId() + " wordt op machine " + toRun.getValue().getMachineID() + " uitgevoerd!");
                        break;
                    }
                }

                Optional<Task> smallestTask = tasks.stream().min(Comparator.comparing(Task::getDuration));
                if (smallestTask.isPresent()) {
                    int smallestDuration = smallestTask.get().getDuration();

                    for (Task t : tasks) {
                        t.setTimeLeft(t.getTimeLeft() - smallestDuration);
                        System.out.println("Machine " + t.getMachineID() + " is nog " + t.getTimeLeft() + " bezig.");
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

            for (Machine m : machineList) {
                if (!m.isBusy()) continue;

                m.updateTime();

                System.out.println("Machine " + m.getId() + " is nog " + m.getTimeLeft() + " bezig.");
            }
        }

        for (Job j : shop.getJobs()) {
            System.out.println("Job " + j.getId() + " begon op " + j.getBeginTime() + " en eindigde op " + j.getEndTime() + ".");
        }
    }
}
