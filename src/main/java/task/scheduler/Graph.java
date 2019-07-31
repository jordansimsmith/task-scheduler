package task.scheduler;

import task.scheduler.exception.DotFormatException;

import java.io.*;
import java.util.HashMap;

/**
 * Encapsulates the loading and storage of a graph from a Dot File
 * TODO: Needs to handle things like DotFormat comments, etc
 * TODO: Can nodes have multiple char names?
 */
class Graph {
    private HashMap<Character, GraphTask> tasks;

    Graph(File inputFile) throws IOException, DotFormatException {
        this.loadGraphFromDotFile(inputFile);
    }

    private void loadGraphFromDotFile(File inputFile) throws IOException, DotFormatException {
        tasks = new HashMap<>();

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        String line = reader.readLine();
        while (line != null) {
            this.readDotFileLine(line);
            line = reader.readLine();
        }

        for (GraphTask task : tasks.values()) {
            System.out.println(task);
        }
    }

    private void readDotFileLine(String line) throws DotFormatException {
        line = line.replaceAll("\\s", "");

        if (line.startsWith("digraph")) {
            return;
        }

        System.out.println(line);

        // Find tasks
        if (line.matches("^[a-z]\\[(.*)Weight=\\d+(.*)")) {
            tasks.put(line.substring(0, 1).toCharArray()[0], new GraphTask(line.substring(0, 1).toCharArray()[0],
                    Integer.parseInt(line.replaceAll("[^0-9]", ""))));
        }

        // Find dependencies
        if (line.matches("^[a-z]->[a-z]\\[(.*)Weight=\\d+(.*)")) {
            if (!tasks.containsKey(line.substring(3, 4).toCharArray()[0])) {
                throw new DotFormatException("File format error. Couldn't find dependent node at " + line);
            }

            tasks.get(line.substring(3, 4).toCharArray()[0]).setDependency(line.substring(0, 1).toCharArray()[0],
                    Integer.parseInt(line.replaceAll("[^0-9]", "")));
        }
    }
}
