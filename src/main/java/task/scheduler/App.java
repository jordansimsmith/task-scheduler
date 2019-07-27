package task.scheduler;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "SOFTENG306 Scheduler - Group 14" );

        Configuration conf = processArguments(args);
        if (conf == null)   {
            return;
        }

        System.out.println("Processing "+conf.inputFile+" on "+conf.numberThreads+" cores to find a schedule for "+conf.numberCores+" processors.");
        System.out.println((conf.visualise) ? "The results will be visualised" : "The results will not be visualised");
        System.out.println("Output will be saved as "+conf.outputFile);
    }

    /**
     * Processes commandline arguments into a Configuration object, fills defaults if not specified.
     * Prints help, descriptive error and returns null if invalid commands given.
     */
    private static Configuration processArguments(String[] args)
    {
        if (args.length < 2)
        {
            printHelp();
            return null;
        }

        Configuration conf = new Configuration();
        conf.inputFile = args[0];

        try {
            conf.numberCores = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            printHelp();
            System.out.println("Number of processors to schedule on was not a valid integer.");
            return null;
        }

        conf.numberThreads = 1;
        conf.visualise = false;
        conf.outputFile = conf.inputFile.replace(".dot", "") + "-output.dot";

        for (int i = 2; i < args.length; i++)   {
            switch (args[i])    {
                case "-p":
                    if (args.length < i + 2)    {
                        printHelp();
                        System.out.println("Argument -p must be followed by a valid integer.");
                        return null;
                    }

                    try {
                        conf.numberThreads = Integer.parseInt(args[i + 1]);
                        i++;
                    } catch (NumberFormatException e) {
                        printHelp();
                        System.out.println("Argument -p must be followed by a valid integer.");
                        return null;
                    }
                    break;
                case "-v":
                    conf.visualise = true;
                    break;
                case "-o":
                    if (args.length < i + 2)    {
                        printHelp();
                        System.out.println("Argument -o must be followed by a filename.");
                        return null;
                    }

                    conf.outputFile = args[i + 1];
                    i++;
                    break;
                default:
                    printHelp();
                    System.out.println("Argument "+args[i]+" was not recognised as a valid command.");
                    return null;
            }
        }

        return conf;
    }

    /**
     * Prints help menu.
     * Help menu taken from project specification (project1-description.pdf)
     */
    private static void printHelp()
    {
        System.out.println("java −jar scheduler.jar INPUT.dot P [OPTION]\r\n" +
                "INPUT.dot a task graph with integer weights in dot format\r\n" +
                "P number of processors to schedule the INPUT graph on\r\n");

        System.out.println("Optional: \r\n" +
                "−p N use N cores for execution in parallel (default is sequential )\r\n" +
                "−v visualise the search\r\n" +
                "−o OUTPUT.dot output file is named OUTPUT.dot (default is INPUT−output.dot)\r\n");
    }
}
