package task.scheduler;

import java.io.*;

public class FileWriter implements Closeable{

    private OutputStream outputStream;

    public FileWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

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

                    for(Tuple<INode, Integer> parentNode : node.getParents()) {
                        bufferedWriter.write(String.format("\t%s -> %s\t[Weight=%d];", parentNode.x.getLabel(), node.getLabel(), parentNode.y));
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
