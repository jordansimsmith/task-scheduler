package task.scheduler;

import java.io.*;

public class FileWriter {
    private static final String DEFAULT_FILE_NAME = "INPUT−output.dot";

    public FileWriter(){
    }

    public void writeScheduledGraphToFile(IGraph graph, ISchedule schedule) {
        File outputFile = Config.getInstance().getOutputFile() != null ? Config.getInstance().getOutputFile() : new File(DEFAULT_FILE_NAME);

        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)))){

            writer.write(String.format("digraph \"%s\" {", outputFile.getName()));
            writer.newLine();

            for(INode node: graph.getNodes()) {
                Tuple<Integer, Integer> nodeSchedule = schedule.getNodeSchedule(node);

                writer.write(String.format("\t%s\t[Weight=%d Start=%d Processor=%d];", node.getLabel(), node.getProcessingCost(), nodeSchedule.x, nodeSchedule.y));
                writer.newLine();

                if (!node.getParents().isEmpty()) {

                    for(Tuple<INode, Integer> parentNode : node.getParents()) {
                        writer.write(String.format("\t%s -> %s\t[Weight=%d]", parentNode.x.getLabel(), node.getLabel(), parentNode.y));
                        writer.newLine();
                    }
                }
            }

            writer.write("}");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
