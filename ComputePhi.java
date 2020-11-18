import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute phi (the Golden Ratio) using a Fibonacci-like series based on
 * an initial pair of input values.
 */
public class ComputePhi {
    public static final String X_PRINT_STEPS = "--print_steps";
    public static final String X_COMPARE_VALUES = "--compare_values";
    public static final String X_ALL_DIGITS = "--all_digits";
    public static final int ACCURACY_UNSET = 0;
    public static final int ACCURACY_EXACT = -1;

    public static void printHelpInfo() {
        System.out.println("usage: java ComputePhi <first-term> <second-term> <iterations> <precision> [ "
                + X_PRINT_STEPS + " " + X_COMPARE_VALUES + " " + X_ALL_DIGITS + " ] ");
        System.out.println();
        System.out.println("Notes:");
        System.out.println("Use --print_steps (-p) to print the approximation for each iteration of the algorithm.");
        System.out.println("Use --compare_values (-c) to compare successive approximations, terminating early when equal.");
        System.out.println("Use --all_digits (-d_ to print all computed digits. (Default prints only accurate digits.)");
        System.out.println();
    }

    /**
     * Computes an approximation of phi according to the input arguments.
     * @param args Expects four required arguments:
     *             <ol>
     *             <li>The first term in the series.</li>
     *             <li>The second term in the series.</li>
     *             <li>The number of iterations to calculate.</li>
     *             <li>The precision to use in calculations.</li>
     *             </ol>
     *             Also accepts the following optional arguments:
     *             <ul>
     *             <li>--print_steps (-p)</li>
     *             <li>--compare_values (-c)</li>
     *             <li></li>
     *             </ul>
     * @return
     */
    public static String computePhi(String... args) {
        long time = System.currentTimeMillis();

        // ------------------------
        // parse required arguments
        // ------------------------
        int requiredArgsLength = 4;
        if (args.length < requiredArgsLength || args[0].equals("help")
                || args[0].equals("--help") || args[0].equals("-h")) {
            if (args.length < requiredArgsLength) {
                System.out.println("Error: missing required arguments.");
                System.out.println();
            }
            printHelpInfo();
            return null;
        }

        // check number formatting ok
        String prevTermStr = args[0];
        String nthTermStr = args[1];
        String iterationsStr = args[2];
        String precisionStr = args[3];

        if (!ComputePi.isPositiveBigInteger(prevTermStr)) {
            System.out.println("1st argument \"" + prevTermStr + "\" is invalid; must be a positive integer.");
            System.out.println();
            printHelpInfo();
            return null;
        }
        if (!ComputePi.isPositiveBigInteger(nthTermStr)) {
            System.out.println("2nd argument \"" + nthTermStr + "\" is invalid; must be a positive integer.");
            System.out.println();
            printHelpInfo();
            return null;
        }
        if (!ComputePi.isPositiveLong(iterationsStr)) {
            if (ComputePi.isPositiveBigInteger(iterationsStr)) {
                System.out.println("3rd argument \"" + iterationsStr + "\" is invalid; must be a positive integer" +
                        " between 1 and " + Long.MAX_VALUE + " (inclusive).");
            } else {
                System.out.println("3rd argument \"" + iterationsStr + "\" is invalid; must be a positive integer.");
            }
            System.out.println();
            printHelpInfo();
            return null;
        }
        if (!ComputePi.isPositiveInteger(precisionStr)) {
            if (ComputePi.isPositiveBigInteger(precisionStr)) {
                System.out.println("4th argument \"" + precisionStr + "\" is invalid; must be a positive integer" +
                        " between 1 and " + Integer.MAX_VALUE + " (inclusive).");
            } else {
                System.out.println("4th argument \"" + precisionStr + "\" is invalid; must be a positive integer.");
            }
            System.out.println();
            printHelpInfo();
            return null;
        }

        BigDecimal prevTerm = new BigDecimal(prevTermStr);
        BigDecimal nthTerm = new BigDecimal(nthTermStr);
        BigDecimal nextTerm;

        // print the first few numbers from this series
        {
            BigDecimal tmp1 = prevTerm;
            BigDecimal tmp2 = nthTerm;
            List<BigDecimal> first8Terms = new ArrayList<>();
            first8Terms.add(tmp1);
            first8Terms.add(tmp2);
            for (int i = 0; i < 6; i++) {
                nextTerm = tmp1.add(tmp2);
                tmp1 = tmp2;
                tmp2 = nextTerm;
                first8Terms.add(nextTerm);
            }
            String first8TermsStr = first8Terms.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", ", "", " ..."));

            System.out.println("Calulating Phi (the Golden Ratio) using the following Fibonacci-like series:");
            System.out.println(first8TermsStr);
            System.out.println();
        }

        long iterations = Long.parseLong(iterationsStr);
        int precision = Integer.parseInt(precisionStr);

        // calculate optional flags
        boolean printSteps = false;
        boolean compareValues = false;
        boolean allDigits = false;

        String unknownArg = null;

        int pi = 0;
        int ci = 0;
        int ai = 0;
        for (int i = requiredArgsLength; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals(X_PRINT_STEPS)) {
                printSteps = true;
            } else if (arg.equals(X_COMPARE_VALUES)) {
                compareValues = true;
            } else if (arg.equals(X_ALL_DIGITS)) {
                allDigits = true;
            } else if (arg.matches("-[pca]{1,3}")) {
                String[] options = arg.substring(1).split("");
                for (String option : options) {
                    if (option.equals("p")) {
                        printSteps = true;
                        pi++;
                    } else if (option.equals("c")) {
                        compareValues = true;
                        ci++;
                    } else if (option.equals("a")) {
                        allDigits = true;
                        ai++;
                    }
                }
            } else {
                unknownArg = arg;
                break;
            }
        }

        boolean duplicateArgs = pi > 1 || ci > 1 || ai > 1;
        if (duplicateArgs || unknownArg != null) {
            if (duplicateArgs) {
                System.out.println("Duplicate arguments found. Please only pass each argument once.");
                System.out.println();
            }
            else if (unknownArg != null) {
                System.out.println("Unknown argument \"" + unknownArg + "\".");
                System.out.println();
            }
            printHelpInfo();
            return null;
        }

        int nFormatLength = args[2].length();
        String nFormatStr = "%" + nFormatLength + "s: ";

        MathContext context = new MathContext(precision, RoundingMode.HALF_UP);
        BigDecimal approximation = null;
        BigDecimal prevApproximation = null;
        int accurateDigits = ACCURACY_UNSET;

        long n;
        for (n = 1; n <= iterations; n++) {
            if (compareValues || n == iterations) {
                prevApproximation = approximation;
            }
            if (compareValues || printSteps || n >= iterations - 1) {
                approximation = nthTerm.divide(prevTerm, context);
            }
            if (printSteps) {
                System.out.format(nFormatStr, n);
                System.out.println(approximation);
            }
            if (compareValues && approximation.equals(prevApproximation)) {
                accurateDigits = ACCURACY_EXACT;
                break;
            } else if (n != iterations) {
                nextTerm = prevTerm.add(nthTerm);
                prevTerm = nthTerm;
                nthTerm = nextTerm;
            }
        }

        if (printSteps) {
            System.out.println();
        }

        time = System.currentTimeMillis() - time;
        System.out.println("Iterations: " + n);
        System.out.println("Elapsed time: " + time / 1000.0 + " seconds");

        String approxStr = approximation.toString();
        if (accurateDigits == ACCURACY_EXACT || approximation.equals(prevApproximation)) {
            accurateDigits = approxStr.length() - 1;
        } else {
            String prevApproxStr = prevApproximation.toString();
            int i = 0;
            while (i < approxStr.length() && i < prevApproxStr.length()) {
                char a = approxStr.charAt(i);
                char b = prevApproxStr.charAt(i);
                if (a == b) {
                    i++;
                } else {
                    break;
                }
            }
            accurateDigits = i - 1;
            int substringLength = i;
            if (!allDigits && substringLength < approxStr.length()) {
                approxStr = approxStr.substring(0, substringLength);
            }
        }

        System.out.println();
        System.out.println("Approximation accurate to " + accurateDigits + " digits:"); // remove the '.' character
        System.out.println(approxStr);

        // print a marker for the last accurate digit
        if (allDigits) {
            for (int i = 0; i < accurateDigits; i++) {
                 System.out.print(' ');
            }
            System.out.println("^ (last accurate digit)");
        }

        return approxStr;
    }

    /**
     * See {@link #computePhi(String...)} for details.
     */
    public static void main(String[] args) {
//        computePhi("2", "1", "10", "400", "--print_steps", "--compare_values", "--all_digits");
//        computePhi("2", "1", "10", "400", "--print_steps", "--compare_values");

//        computePhi("23 9001   10  20 --compare_values --all_digits".split("\\s+"));
//        computePhi("23 9001 1000 100 --compare_values --all_digits --print_steps".split("\\s+"));
//        computePhi("23 9001  100   5 --compare_values --all_digits".split("\\s+"));

//        String result = computePhi("1 1 40 15 --print_steps".split("\\s+"));
//        String result = computePhi("2 1 40 15 --print_steps".split("\\s+"));
//        String result = computePhi("1 1 20 15".split("\\s+"));

         String result = computePhi(args);
    }
}
