package task.scheduler;

import java.io.*;

public class FileWriter {

    private BufferedWriter bufferedWriter;

    public FileWriter(BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
    }

    public void writeScheduledGraphToFile(IGraph graph, ISchedule schedule, String graphName) {

        try {
            bufferedWriter.write(String.format("digraph \"%s\" {", graphName));
            bufferedWriter.newLine();

            for(INode node: graph.getNodes()) {
                Tuple<Integer, Integer> nodeSchedule = schedule.getNodeSchedule(node);

                bufferedWriter.write(String.format("\t%s\t[Weight=%d Start=%d Processor=%d];", node.getLabel(), node.getProcessingCost(), nodeSchedule.x, nodeSchedule.y));
                bufferedWriter.newLine();

                if (!node.getParents().isEmpty()) {

                    for(Tuple<INode, Integer> parentNode : node.getParents()) {
                        bufferedWriter.write(String.format("\t%s -> %s\t[Weight=%d]", parentNode.x.getLabel(), node.getLabel(), parentNode.y));
                        bufferedWriter.newLine();
                    }
                }
            }

            bufferedWriter.write("}");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
