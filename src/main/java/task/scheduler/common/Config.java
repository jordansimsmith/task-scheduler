package task.scheduler.common;

import java.io.File;

/**
 * Config is a singleton object used to store configuration options from the command line arguments.
 */
public class Config {
    // singleton instance field
    private static Config instance;

    // configuration options
    private File inputFile;
    private File outputFile;
    private int numberOfCores;
    private int numberOfThreads;
    private boolean visualise;

    // private constructor
    private Config() {
    }

    /**
     * Instance method to get the instance of the singleton. Provides global access and lazy instantiation.
     * @return the instance of the singleton object.
     */
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }

        return instance;
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public int getNumberOfCores() {
        return numberOfCores;
    }

    public void setNumberOfCores(int numberOfCores) {
        this.numberOfCores = numberOfCores;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public boolean isVisualise() {
        return visualise;
    }

    public void setVisualise(boolean visualise) {
        this.visualise = visualise;
    }
}
