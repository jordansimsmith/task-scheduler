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

        Configuration conf;
        try {
            conf = processArguments(args);
        } catch (IllegalArgumentException e)    {
            printHelp();
            System.out.println(e.getMessage());
            return;
        }

        if (conf == null)  {
            return;
        }

        System.out.println("Processing "+conf.inputFile+" on "+conf.numberThreads+" cores to find a schedule for "+conf.numberCores+" processors.");
        System.out.println((conf.visualise) ? "The results will be visualised" : "The results will not be visualised");
        System.out.println("Output will be saved as "+conf.outputFile);
    }

    /**
     * Processes commandline arguments into a Configuration object, fills defaults if not specified.
     * @throws IllegalArgumentException if arguments incorrect, with error message
     */
    private static Configuration processArguments(String[] args) throws IllegalArgumentException
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
            throw new IllegalArgumentException("Number of processors to schedule on was not a valid integer.");
        }

        conf.numberThreads = 1;
        conf.visualise = false;
        conf.outputFile = conf.inputFile.replace(".dot", "") + "-output.dot";

        for (int i = 2; i < args.length; i++)   {
            switch (args[i])    {
                case "-p":
                    if (args.length < i + 2)    {
                        throw new IllegalArgumentException("Argument -p must be followed by a valid integer.");
                    }

                    try {
                        conf.numberThreads = Integer.parseInt(args[i + 1]);
                        i++;
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Argument -p must be followed by a valid integer.");
                    }
                    break;
                case "-v":
                    conf.visualise = true;
                    break;
                case "-o":
                    if (args.length < i + 2)    {
                        throw new IllegalArgumentException("Argument -o must be followed by a filename.");
                    }

                    conf.outputFile = args[i + 1];
                    i++;
                    break;
                default:
                    throw new IllegalArgumentException("Argument "+args[i]+" was not recognised as a valid command.");
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
