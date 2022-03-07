package nl.sbdeveloper.leastslack;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeastSlack {
    private static final Pattern pattern = Pattern.compile("(\\d+)( |\\t+)(\\d+)");

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
                    machines = Integer.parseInt(matcher.group(3));
                    wasFirstLine = true;
                    break;
                } else {
                    int id = Integer.parseInt(matcher.group(1));
                    int duration = Integer.parseInt(matcher.group(3));

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

//        System.out.println("De JobShop bevat " + shop.getJobs().size() + " jobs!");
//        for (Job j : shop.getJobs()) {
//            System.out.println("Job " + j.getId() + " bevat " + j.getTasks().size() + " machines!");
//            System.out.println("En heeft een totale lengte van " + j.calculateTotalDuration());
//        }

        //TODO Tijd erin verwerken. In plaats van altijd op 0 zetten, checken wat het verschil tussen de huidige tijd en eindtijd is, en die opslaan als verschil.
        //TODO Begin en eindtijd opslaan in de job

        long start = System.currentTimeMillis();

        int timeSince = 0;

        while (!shop.isAllJobsDone()) {
            System.out.println("------START------");
            shop.calculateSlack();
            for (Job j : shop.getJobsSorted()) {
                System.out.println("Job " + j.getId() + " (totale duration " + j.calculateTotalDuration() + "):");
                for (Task t : j.getTasksSortedByMachine(true)) {
                    System.out.println("Task " + t.getMachineID() + " heeft een duration van " + t.getDuration() + " en een slack van " + t.getSlack() + ".");
                    System.out.println("De time left is " + t.getTimeLeft());
                }
            }

            List<Task> tasks = new ArrayList<>(); //De taken die in deze loop worden uitgevoerd.

            for (int machineID = lowestMachine; machineID <= machines; machineID++) {
                Map<Job, Task> map = shop.getTasksSortedBySlack(machineID);
                Optional<Map.Entry<Job, Task>> toRunOpt = map.entrySet().stream().min(Comparator.comparing(t -> t.getValue().getSlack())); //Haalt de taak met de kleinste slack op.
                if (toRunOpt.isEmpty()) continue;

                Map.Entry<Job, Task> toRun = toRunOpt.get(); //De job en bijbehorende taak met de kleinste slack op deze machine.

                Job runningJob = toRun.getKey();

                int previousMachine = machineID - 1;
                if (previousMachine >= lowestMachine) {
                    Task task = runningJob.getTask(previousMachine);
                    if (task != null) continue; //The previous job is not done yet.
                }

                Task runningTask = toRun.getValue();

                tasks.add(runningTask);

                runningTask.setRunning(true);

                System.out.println("We beginnen job " + runningJob.getId() + " en daarbinnen task " + runningTask.getMachineID() + "!");

                runningTask.setTimeLeft(0);

                System.out.println("En die is afgelopen! Volgende taak bepalen...");
            }

            Optional<Task> biggestTask = tasks.stream().max(Comparator.comparing(Task::getDuration));
            if (biggestTask.isPresent()) {
                timeSince += biggestTask.get().getDuration();
            }

            tasks.forEach(t -> t.setRunning(false));

            System.out.println("------END------");

            System.out.println("Time: " + timeSince);
        }

        long end = System.currentTimeMillis();

        System.out.println("Done it in " + (end - start) + "ms!");

        fileScanner.close();
    }
}
