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

        shop.getJobs().forEach(Job::calculateEarliestStart);

        shop.getJobs().stream().max(Comparator.comparing(Job::calculateTotalDuration))
                .ifPresent(job -> shop.getJobs().forEach(j -> j.calculateLatestStart(job.calculateTotalDuration())));

        shop.calculateSlack();

        for (Job j : shop.getJobs()) {
            System.out.println("Job " + j.getId() + " heeft een total duration van " + j.calculateTotalDuration() + " en een slack van " + j.getSlack() + ":");

            for (Task t : j.getTasks()) {
                System.out.println("Task " + t.getMachineID() + " heeft een LS van " + t.getLatestStart() + " en een ES van " + t.getEarliestStart());
            }
        }

        //TODO Fix dat hij de earliest start update als een taak begint die langer duurt dan de earliest start van een taak op de machine
        //TODO In het huidige voorbeeld start Job 1 op Machine 0 (ES van 40, tijd 40) en heeft Job 2 op Machine 0 een ES van 60. Machine 0 is bezet tot 80, wat betekent dat die van Machine 0 moet updaten naar 80.

        int time = 0;
        while (!shop.isAllJobsDone()) {
            System.out.println("------START------");

            List<Job> conflictedJobs = new ArrayList<>();
            int smallestTaskDuration = Integer.MAX_VALUE;

            Map<Integer, Integer> foundTaskDurations = new HashMap<>(); //Key = machine ID, value = duration
            for (Map.Entry<Job, Task> pair : shop.getTasksWithEarliestTimeNow(time)) {
                if (foundTaskDurations.containsKey(pair.getValue().getMachineID())) { //Er is al een task met een kleinere slack gestart, er was dus een conflict op dit tijdstip!
                    pair.getValue().setEarliestStart(foundTaskDurations.get(pair.getValue().getMachineID()));
                    conflictedJobs.add(pair.getKey());
                    continue;
                }

                System.out.println("Job " + pair.getKey().getId() + " wordt uitgevoerd op machine " + pair.getValue().getMachineID() + ".");

                pair.getValue().setDone(true);

                pair.getKey().setBeginTime(time);

                foundTaskDurations.put(pair.getValue().getMachineID(), pair.getValue().getDuration());

                if (pair.getValue().getDuration() < smallestTaskDuration) smallestTaskDuration = pair.getValue().getDuration();
            }

            for (Job job : conflictedJobs) {
                job.calculateEarliestStart();
            }

            if (!conflictedJobs.isEmpty()) {
                shop.calculateSlack();

                for (Job j : shop.getJobs()) {
                    System.out.println("Job " + j.getId() + " heeft een total duration van " + j.calculateTotalDuration() + " en een slack van " + j.getSlack() + ":");

                    for (Task t : j.getTasks()) {
                        System.out.println("Task " + t.getMachineID() + " heeft een LS van " + t.getLatestStart() + " en een ES van " + t.getEarliestStart());
                    }
                }
            }

            if (smallestTaskDuration == Integer.MAX_VALUE) smallestTaskDuration = 1; //Als er op dit tijdstip geen taken draaien, tellen we er 1 bij op tot we weer wat vinden.
            time += smallestTaskDuration;

            for (Job j : shop.getJobs()) {
                if (j.getEndTime() == 0 && j.hasAllTasksDone()) j.setEndTime(time);
            }

            System.out.println("Time: " + time);
        }

        for (Job j : shop.getJobs()) {
            System.out.println("Job " + j.getId() + " begon op " + j.getBeginTime() + " en eindigde op " + j.getEndTime() + ".");
        }
    }
}
