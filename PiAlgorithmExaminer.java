import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * Compare the approximations, runtimes, and memory usage of pi iteration-by-iteration of
 * various algorithms implemented by {@code ComputePi}, and print the results to a table.
 * <p>
 * Requires a minimum of Java 14 for use of switch expressions.
 */
public class PiAlgorithmExaminer {
    static final String X_SKIP_TESTS = "--skip_tests";
    static final String X_PRINT_TABLE = "--print_table";

    /**
     * Prints the CLI usage info.
     */
    public static void printHelpInfo() {
        System.out.println("Usage: java PiAlgorithmExaminer <algorithms> <iterations> <precision> [options]");
        System.out.println("Runs a battery of tests, stores results in output.txt, and then analyzes the results.");
        System.out.println();
        System.out.println("1st argument details:");
        System.out.println("Pass a comma-delimited list of numbers (or algorithm names) from the following:");
        System.out.println("1 - Gregory-Leibniz series");
        System.out.println("2 - Nilakantha series");
        System.out.println("3 - Newton method");
        System.out.println("4 - Viete formula");
        System.out.println("5 - Wallis product");
        System.out.println("6 - Chudnovsky algorithm");
        System.out.println("7 - Brent-Salamin formula (Gauss-Legendre algorithm)");
        System.out.println();
        System.out.println("2nd argument details:");
        System.out.println("<number> - The number of iterations to run.");
        System.out.println();
        System.out.println("3rd argument details:");
        System.out.println("<number> - The precision to use in calculations.");
        System.out.println();
        System.out.println("Optional arguments:");
        System.out.println(X_SKIP_TESTS + " - (-s) Skip running the tests, only analyze the output.txt file.");
        System.out.println("            " + "   Assumes output.txt matches {iterations} and {precision}.");
        System.out.println("            " + "   (Default behavior always runs the tests and overrites output.txt.)");
        System.out.println(X_PRINT_TABLE + " - (-p) Print the analysis formatted in a table.");
        System.out.println("             " + "   (Default behavior prints rows in a key:value format");
        System.out.println();
    }

    /**
     * Run the tests of the specified algorithms with the input parameters.
     * @param args Expects 3 required arguments:
     *             <ol>
     *             <li>A comma-separated list of algorithms to compare,
     *             either numbers or algorithm names.</li>
     *             <li>The number of iterations to run.</li>
     *             <li>The precision to use in calculations.</li>
     *             </ol>
     *             Also accepts the following optional arguments:
     *             <ul>
     *             <li>--skip_tests (-s)</li>
     *             <li>--print_table (-p)</li>
     *             </ul>
     *             (See {@link #printHelpInfo} implementation for more info
     *             on what the optional arguments do.)
     * @throws IOException
     */
    public static void testAlgorithms(String[] args) throws IOException {
        // ------------------------
        // parse required arguments
        // ------------------------
        int requiredArgsLength = 3;
        if (args.length < requiredArgsLength || args[0].equals("help")
                || args[0].equals("--help") || args[0].equals("-h")) {
            if (args.length < requiredArgsLength) {
                System.out.println("Error: missing required arguments.");
                System.out.println();
            }
            printHelpInfo();
            return;
        }

        // convert algorithms to name values
        String[] algorithms = args[0].split(",");
        for (int i = 0; i < algorithms.length; i++) {
            String algorithm = algorithms[i];
            algorithms[i] = ComputePi.getAlgorithmName(algorithm);
        }

        String iterationsStr = args[1];
        String precisionStr = args[2];

        if (!ComputePi.isPositiveInteger(iterationsStr)) {
            if (ComputePi.isPositiveBigInteger(iterationsStr)) {
                System.out.println("2nd argument \"" + iterationsStr + "\" is invalid; must be a positive integer" +
                        " between 1 and " + Integer.MAX_VALUE + " (inclusive).");
            } else {
                System.out.println("2nd argument \"" + iterationsStr + "\" is invalid; must be a positive integer.");
            }
            System.out.println();
            printHelpInfo();
            return;
        }
        if (!ComputePi.isPositiveInteger(precisionStr)) {
            if (ComputePi.isPositiveBigInteger(precisionStr)) {
                System.out.println("3rd argument \"" + precisionStr + "\" is invalid; must be a positive integer" +
                        " between 1 and " + Integer.MAX_VALUE + " (inclusive).");
            } else {
                System.out.println("3rd argument \"" + precisionStr + "\" is invalid; must be a positive integer.");
            }
            System.out.println();
            printHelpInfo();
            return;
        }
        int iterations = Integer.parseInt(iterationsStr);
        int precision = Integer.parseInt(precisionStr);

        // ------------------------
        // parse optional arguments
        // ------------------------
        boolean runTests = true;
        boolean printTable = false;

        String unknownArg = null;

        int ri = 0;
        int pi = 0;
        for (int i = requiredArgsLength; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals(X_SKIP_TESTS)) {
                runTests = false;
                ri++;
            } else if (arg.equals(X_PRINT_TABLE)) {
                printTable = true;
                pi++;
            } else if (arg.matches("-[sp]{1,2}")) {
                String[] options = arg.substring(1).split("");
                for (String option : options) {
                    if (option.equals("s")) {
                        runTests = false;
                        ri++;
                    } else if (option.equals("p")) {
                        printTable = true;
                        pi++;
                    }
                }
            } else {
                unknownArg = arg;
                break;
            }
        }

        // ---------------
        // error reporting
        // ---------------
        boolean duplicateArgs = ri > 1 || pi > 1;
        if (duplicateArgs || unknownArg != null) {
            if (duplicateArgs) {
                System.out.println("Error: duplicate arguments found. Please only pass each argument once.");
                System.out.println();
            }
            if (unknownArg != null) {
                System.out.println("Error: unknown argument \"" + unknownArg + "\".");
                System.out.println();
            }
            printHelpInfo();
            return;
        }

        // -------------------
        // run algorithm tests
        // -------------------
        String print = ComputePi.X_PRINT_STEPS;
        String compare = ComputePi.X_COMPARE_VALUES;
        String estimate = ComputePi.X_ESTIMATE_MEMORY_USAGE;

        String fileName = "output.txt";
        Charset encoding = Charset.defaultCharset();
        FileReader fileReader;
        BufferedReader bufferedReader;

        if (runTests) {
            // redirect standard output to write to a file
            FileOutputStream outputStream = new FileOutputStream(fileName, false);
            PrintStream printStream = new PrintStream(outputStream);
            PrintStream out = System.out;

            // setup reader to read memory usage
            fileReader = new FileReader(fileName, encoding);
            bufferedReader = new BufferedReader(fileReader);

            // run pi algorithms
            System.out.println("Testing algorithms:");
            System.out.println();

            for (String algorithm : algorithms) {
                // print algorithm options
                String fmtStr = "%-15s %s %s %s %s %s";
                System.out.format(fmtStr, algorithm, iterationsStr, precisionStr, print, compare, estimate);
                System.setOut(printStream);

                // run algorithm
                long time = System.currentTimeMillis();
                ComputePi.computePi(algorithm, iterationsStr, precisionStr, print, compare, estimate);
                time = System.currentTimeMillis() - time;

                System.setOut(out);
                System.out.format(" %10.3f seconds", time / 1000.0);

                // read memory usage from output file
                printStream.flush();
                String line = "";
                while (!line.matches("Memory usage: .*")) {
                    line = bufferedReader.readLine();
                }
                String[] split = line.split("Memory usage: ");
                String memoryUsage = split[1];
                System.out.format(" %13s\n", memoryUsage);
            }

            // close the input and output streams
            printStream.close();
            bufferedReader.close();
            System.out.println();
        }

        // ---------------------------------
        // collect data from the output file
        // ---------------------------------
        fileReader = new FileReader(fileName, encoding);
        bufferedReader = new BufferedReader(fileReader);

        String prefixFormat = "%" + iterationsStr.length() + "s: ";
        String startPrefix = String.format(prefixFormat, "1");

        int columns = algorithms.length;
        int rows = iterations;
        String[][] approximationsTable = new String[columns][rows];
        int[][] accuracyTable = new int[columns][rows];

        // each column holds a different algorithm's data
        for (int column = 0; column < columns; column++) {

            // advance to the first approximation line
            String line;
            do {
                line = bufferedReader.readLine();
                if (line == null) {
                    String algorithm = algorithms[column];
                    System.out.println("Something went wrong, rows missing in " +
                            fileName + " for " + algorithm + " algorithm.");
                    return;
                }
            }
            while (!line.startsWith(startPrefix));

            // read approximations into a table
            String approximation = null;
            for (int row = 0; row < rows; row++) {
                if (line == null) {
                    String algorithm = algorithms[column];
                    System.out.println("Something went wrong, rows terminated early in " +
                            fileName + " for " + algorithm + ".");
                    return;
                }

                // valid line, extract approximation and advance read to next line
                if (line.matches("\\s*\\d+: .*")) {
                    String[] split = line.split("\\s*\\d+: ");
                    approximation = split[1];
                    line = bufferedReader.readLine();
                }
                // otherwise just sets next approximation to previous approximation
                approximationsTable[column][row] = approximation;
            }
        }
        bufferedReader.close();

        // ---------------------------------------------------
        // read precomputed file with one million digits of pi
        // ---------------------------------------------------
        String piCheckFileName = "pi1000000.txt";
        fileReader = new FileReader(piCheckFileName, encoding);
        bufferedReader = new BufferedReader(fileReader);
        String piDigits;
        {
            int charsToRead = precision + 1;
            char[] charBuffer = new char[charsToRead];
            int charsRead = bufferedReader.read(charBuffer, 0, charsToRead);
            piDigits = new String(charBuffer, 0, charsRead);
        }

        // --------------------------------
        // check accuracy of approximations
        // --------------------------------
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                // compensate for '.' char
                int accuracy = -1;
                String approximation = approximationsTable[column][row];
                for (int i = 0; i < piDigits.length() && i < approximation.length(); i++) {
                    char approxDigit = approximation.charAt(i);
                    char checkDigit = piDigits.charAt(i);
                    if (approxDigit == checkDigit) {
                        accuracy++;
                    } else {
                        break;
                    }
                }
                accuracy = Math.max(accuracy, 0);
                accuracyTable[column][row] = accuracy;
            }
        }

        // ------------------------
        // print results to a table
        // ------------------------
        if (printTable) {

            // print table header row
            String itHeaderStr = "ITERATIONS";
            int itFormatLen = Math.max(itHeaderStr.length(), iterationsStr.length());
            String itFormat = "%" + itFormatLen + "s ";
            System.out.format(itFormat, itHeaderStr);

            String[] columnFormats = new String[algorithms.length];
            for (int column = 0; column < columns; column++) {
                String algorithm = algorithms[column];

                int columnFormatLen = Math.max(algorithm.length(), precisionStr.length());
                String columnFormat = "%" + columnFormatLen + "s ";
                columnFormats[column] = columnFormat;

                System.out.format(columnFormat, algorithm);
            }
            System.out.println();

            // print table content rows
            for (int row = 0; row < rows; row++) {
                System.out.format(itFormat, row + 1);
                for (int column = 0; column < columns; column++) {
                    String columnFormat = columnFormats[column];
                    int accurateDigits = accuracyTable[column][row];
                    System.out.format(columnFormat, accurateDigits);
                }
                System.out.println();
            }
        }

        // -----------------------------------
        // print results in a key:value format
        // -----------------------------------
        else {
            String itFormat = "%-" + iterationsStr.length() + "d ";
            String accuracyFormat = "%-" + precisionStr.length() + "d ";

            for (int row = 0; row < rows; row++) {
                System.out.print("ITERATIONS:");
                System.out.format(itFormat, row + 1);
                for (int column = 0; column < columns; column++) {
                    String algorithm = algorithms[column];
                    System.out.print(algorithm);
                    System.out.print(':');

                    int accurateDigits = accuracyTable[column][row];
                    System.out.format(accuracyFormat, accurateDigits);
                }
                System.out.println();
            }
        }
    }

    /**
     * @see {@link #testAlgorithms(String[])} for details.
     */
    public static void main(String[] args) {
        try {
//            testAlgorithms("1,2,3,4,5,6,7 10 100 --print_table".split("\\s+"));
//            testAlgorithms("1,2,3,4,5,6,7 10 100 --skip_tests --print_table".split("\\s+"));

//            testAlgorithms("1,2,3,5,6,7 10 100 -sp".split("\\s+"));
//            testAlgorithms("1,2,3,5,6,7 10 100 -s".split("\\s+"));
//            testAlgorithms("1,2,3,5,6,7 10 100 -p".split("\\s+"));
//            testAlgorithms("1,2,3,5,6,7 10 100".split("\\s+"));

//            testAlgorithms("1,2,3,4,5,6,7 10 2000 -p".split("\\s+"));
//            testAlgorithms("1,2,3,4,5,6,7 10 2000".split("\\s+"));

            testAlgorithms(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
