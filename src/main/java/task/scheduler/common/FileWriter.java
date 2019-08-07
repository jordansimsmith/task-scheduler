package task.scheduler.common;

import task.scheduler.common.Config;
import task.scheduler.common.Tuple;
import task.scheduler.graph.IGraph;
import task.scheduler.graph.INode;
import task.scheduler.schedule.ISchedule;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Map;

/**
 * A FileWriter object writes an IGraph and ISchedule to a dot file.
 */
public class FileWriter implements Closeable {

    private OutputStream outputStream;

    public FileWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Writes the given graph with the given solution schedule to a file in dot file format.
     * The name of the output file is taken from the global config settings.
     * @param graph
     * @param schedule
     */
    public void writeScheduledGraphToFile(IGraph graph, ISchedule schedule) {
        String outputGraphName = getOutputGraphName();

        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))){
            bufferedWriter.write(String.format("digraph \"%s\" {", outputGraphName));
            bufferedWriter.newLine();

            for(INode node: graph.getNodes()) {
                Tuple<Integer, Integer> nodeSchedule = schedule.getNodeSchedule(node);

                bufferedWriter.write(String.format("\t%s\t[Weight=%d Start=%d Processor=%d];", node.getLabel(), node.getProcessingCost(), nodeSchedule.x, nodeSchedule.y));
                bufferedWriter.newLine();

                if (!node.getParents().isEmpty()) {

                    for(Map.Entry<INode, Integer> parentNode : node.getParents().entrySet()) {
                        bufferedWriter.write(String.format("\t%s -> %s\t[Weight=%d];", parentNode.getKey().getLabel(), node.getLabel(), parentNode.getValue()));
                        bufferedWriter.newLine();
                    }
                }
            }

            bufferedWriter.write("}");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the output graph name which is the output file name without the file extension
     */
    private String getOutputGraphName() {
        return Config.getInstance().getOutputFile().getName().replaceFirst("[.][^.]+$", "");
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
