import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Objects;

/**
 * {@link ComputePi} contains various algorithms for approximating the digits of pi,
 * utilizing Java's {@link BigDecimal} datatype for high-precision calculations.
 * <p>
 * Requires a minimum of Java 14 for use of switch expressions.
 */
public class ComputePi {
    public static final String X_PRINT_STEPS           = "--print_steps";
    public static final String X_COMPARE_VALUES        = "--compare_values";
    public static final String X_ALL_DIGITS            = "--all_digits";
    public static final String X_ESTIMATE_MEMORY_USAGE = "--estimate_memory_usage";

    public static final String GREGORY_LEIBNIZ = "1";
    public static final String NILAKANTHA      = "2";
    public static final String NEWTON          = "3";
    public static final String VIETE           = "4";
    public static final String WALLIS          = "5";
    public static final String CHUDNOVSKY      = "6";
    public static final String BRENT_SALAMIN   = "7";

    public static final String GREGORY_LEIBNIZ_NAME = "GREGORY_LEIBNIZ";
    public static final String NILAKANTHA_NAME      = "NILAKANTHA";
    public static final String NEWTON_NAME          = "NEWTON";
    public static final String VIETE_NAME           = "VIETE";
    public static final String WALLIS_NAME          = "WALLIS";
    public static final String CHUDNOVSKY_NAME      = "CHUDNOVSKY";
    public static final String BRENT_SALAMIN_NAME   = "BRENT_SALAMIN";
    public static final String GAUSS_LEGENDRE_NAME  = "GAUSS_LEGENDRE";

    /**
     * Get the algorithm name from the input string.
     * @param algorithm The String to get the algorithm name for.
     * @return the name, if the input String is a number that maps to an algorithm name;
     *         otherwise just returns the input string.
     */
    public static String getAlgorithmName(String algorithm) {
        return switch (algorithm) {
            case GREGORY_LEIBNIZ -> GREGORY_LEIBNIZ_NAME;
            case NILAKANTHA -> NILAKANTHA_NAME;
            case NEWTON -> NEWTON_NAME;
            case VIETE -> VIETE_NAME;
            case WALLIS -> WALLIS_NAME;
            case CHUDNOVSKY -> CHUDNOVSKY_NAME;
            case BRENT_SALAMIN -> BRENT_SALAMIN_NAME;
            default -> algorithm;
        };
    }

    /**
     * Convenience class to store some boolean flags.
     */
    private static class Flags {
        final boolean printSteps;
        final boolean compareValues;
        final boolean allDigits;
        final boolean estimateMemoryUsage;
        private Flags(
                boolean printSteps,
                boolean compareValues,
                boolean allDigits,
                boolean estimateMemoryUsage) {
            this.printSteps = printSteps;
            this.compareValues = compareValues;
            this.allDigits = allDigits;
            this.estimateMemoryUsage = estimateMemoryUsage;
        }
    }

    /**
     * Standard recursive implementation of factorial for BigDecimals.
     * @param n The number to compute the factorial for.
     * @return The factorial of n.
     */
    public static BigDecimal factorial(BigDecimal n) {
        if (n.compareTo(BigDecimal.ONE) <= 0) {
            return BigDecimal.ONE;
        }
        return n.multiply(factorial(n.subtract(BigDecimal.ONE)));
    }

    /**
     * Standard iterative implementation of factorial for BigDecimals.
     * @param n The number to compute the factorial for.
     * @return The factorial of n.
     */
    public static BigDecimal factorial2(BigDecimal n) {
        if (n.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        BigDecimal result = n;
        while (n.compareTo(BigDecimal.ONE) > 0) {
            n = n.subtract(BigDecimal.ONE);
            result = result.multiply(n);
        }
        return result;
    }

    /**
     * Helper method to test if a String is a positive integer.
     * @param maybeInteger The candidate String.
     * @return True if the string is a positive integer, else false.
     */
    public static boolean isPositiveInteger(String maybeInteger) {
        try {
            int integer = Integer.parseInt(maybeInteger);
            return integer > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Helper method to test if a String is a positive long.
     * @param maybeLong The candidate String.
     * @return True if the string is a positive long, else false.
     */
    public static boolean isPositiveLong(String maybeLong) {
        try {
            long longValue = Long.parseLong(maybeLong);
            return longValue > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Helper method to test if a String represents a positive BigInteger.
     * @param maybeBigInteger The candidate String.
     * @return True if the string represents a positive BigInteger, else false.
     */
    public static boolean isPositiveBigInteger(String maybeBigInteger) {
        try {
            BigInteger bigInteger = new BigInteger(maybeBigInteger);
            return bigInteger.signum() > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Helper method to test if a String represents a positive BigDecimal.
     * @param maybeBigDecimal The candidate String.
     * @return True if the string represents a positive BigDecimal, else false.
     */
    public static boolean isPositiveBigDecimal(String maybeBigDecimal) {
        try {
            BigDecimal bigDecimal = new BigDecimal(maybeBigDecimal);
            return bigDecimal.signum() > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Format the input BigDecimal number to scientific notation,
     * matching the form: 0.00 x 10 ^ 0.
     * @param n The BigDecimal to format.
     * @param scale The number of digits after the decimal.
     * @return A formatted string representation of the input BigDecimal.
     */
    private static String format(BigDecimal n, int scale) {
        NumberFormat formatter = new DecimalFormat("0.0E0");
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        formatter.setMinimumFractionDigits(scale);
        return formatter.format(n).replaceAll("E", " * 10 ^ ");
    }

    /**
     * Estimate the memory used by the input list of BigDecimal values.
     * @param values The list of BigDecimals to estimate.
     * @return The estimated memory usage in bytes.
     */
    private static int estimateMemoryUsage(BigDecimal... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .map(BigDecimal::unscaledValue)
                .map(BigInteger::toByteArray)
                .mapToInt(array -> array.length)
                .sum();
    }

    /**
     * Format the input number of bytes into KB.
     * @param bytes The number of bytes.
     * @return A formatted string representing the number of bytes in KB.
     */
    private static String formatMemoryUsage(int bytes) {
        double kb = bytes / 1024.0;
        return String.format("%.3f KB", kb);
    }

    /**
     * Calculate an approximation of pi using the following infinite series:
     * <pre>pi/4 = 1 - 1/3 + 1/5 - 1/7 + 1/9 ...</pre>
     *
     * See <a href="https://en.wikipedia.org/wiki/Leibniz_formula_for_%CF%80">Wikipedia</a>
     * for more details.
     *
     * @param iterations The number of iterations.
     * @param precision The amount of precision.
     * @param flags The set of boolean flags to alter behavior.
     * @return The approximation of pi.
     */
    public static BigDecimal gregoryLeibnizSeries(BigDecimal iterations, int precision, Flags flags) {
        System.out.println("Calculating an approximation of pi using the Gregory-Leibniz series:");
        System.out.println("pi/4 = 1/1 - 1/3 + 1/5 - 1/7 + 1/9 - 1/11 + 1/13 ...");

        MathContext context = new MathContext(precision, RoundingMode.HALF_UP);

        BigDecimal one = BigDecimal.ONE;
        BigDecimal two = new BigDecimal("2");
        BigDecimal four = new BigDecimal("4");
        BigDecimal five = new BigDecimal("5");
        BigDecimal numerator = one;
        BigDecimal denominator = one;
        BigDecimal prevResult = null;
        BigDecimal result = null;

        int thresholdPrecision = 1;
        boolean negate = true;

        int iterationsLength = iterations.toString().length();
        String format = "%" + iterationsLength + "s: ";

        BigDecimal iterationsPlusTwo = iterations.add(two);
        BigDecimal iteration = null;

        for (BigDecimal n = two; n.compareTo(iterationsPlusTwo) < 0; n = n.add(one)) {
            BigDecimal nextNumerator = one;
            if (negate) {
                nextNumerator = nextNumerator.negate();
            }
            BigDecimal nextDenominator = n.multiply(two).subtract(one);
            nextNumerator = nextNumerator.multiply(denominator);

            numerator = numerator.multiply(nextDenominator);
            denominator = denominator.multiply(nextDenominator);

            numerator = numerator.add(nextNumerator);
            negate = !negate;

            // ------------------------------------------------------------------
            // The following commented block contains the mathematical relation
            // required to know when the next digit is computed.  However I found
            // to get n digits of precision, you need 5 * 10 ^ n iterations.
            // Keeping this commented block for posterity.
            // ------------------------------------------------------------------
//            int nPrecision = new BigDecimal(Integer.MAX_VALUE).precision();
//            BigDecimal oneOver2nPlusOne = one.divide(two.multiply(n).add(one), nPrecision, RoundingMode.HALF_UP);
//            BigDecimal nextLevelPrecision = one.divide(BigDecimal.TEN.pow(precision), nPrecision, RoundingMode.HALF_UP);
//            System.out.println("iterations = " + n);
//            System.out.println("precision  = " + precision);
//            System.out.println("oneOver2iPlusOne   = " + oneOver2nPlusOne);
//            System.out.println("nextLevelPrecision = " + nextLevelPrecision );
//            boolean nextPrecisionFound = oneOver2nPlusOne.compareTo(nextLevelPrecision) <= 0;

            BigDecimal nextThreshold = five.scaleByPowerOfTen(thresholdPrecision);
            boolean nextPrecisionFound = n.compareTo(nextThreshold) >= 0;
            if (flags.compareValues) {
                prevResult = result;
            }
            if (flags.compareValues || flags.printSteps) {
                result = four.multiply(numerator).divide(denominator, context);
            }
            if (flags.printSteps) {
                iteration = n.subtract(one);
                System.out.format(format, iteration);
                System.out.println(result);
            }
            if (!flags.printSteps && nextPrecisionFound) {
                System.out.print("iterations: " + n + ", ");
                result = four.multiply(numerator).divide(denominator, thresholdPrecision - 1, RoundingMode.DOWN);
                System.out.println(" result: " + result);
                thresholdPrecision += 1;
            }
            if (flags.compareValues && result.equals(prevResult)) {
                break;
            }
        }
        if (!flags.printSteps) {
            result = four.multiply(numerator).divide(denominator, context);
        }
        if (flags.estimateMemoryUsage) {
            String numeratorString = format(numerator, 2);
            String denominatorString = format(denominator, 2);
            System.out.println();
            System.out.println("numerator:   " + numeratorString);
            System.out.println("denominator: " + denominatorString);

            int memoryUsageBytes = estimateMemoryUsage(one, two, four, five,
                    numerator, denominator, result, prevResult, iterations,
                    iterationsPlusTwo, iteration);

            String memoryUsageString = formatMemoryUsage(memoryUsageBytes);
            System.out.println("Memory usage: " + memoryUsageString);
        }
        return result;
    }

    /**
     * Calculate an approximation of pi using the following infinite series:
     * <pre>pi = 3 + 4/(2*3*4) - 4/(4*5*6) + 4/(6*7*8) - 4/(8*9*10) ...</pre>
     *
     * See <a href="https://en.wikipedia.org/wiki/Pi#Infinite_series">Wikipedia</a>
     * for more details.
     *
     * @param iterations The number of iterations.
     * @param precision The amount of precision.
     * @param flags The set of boolean flags to alter behavior.
     * @return The approximation of pi.
     */
    public static BigDecimal nilakanthaSeries(BigDecimal iterations, int precision, Flags flags) {
        System.out.println("Calculating an approximation of pi using the Nilakantha series:");
        System.out.println("pi = 3 + 4/(2*3*4) - 4/(4*5*6) + 4/(6*7*8) - 4/(8*9*10) ...");

        MathContext context = new MathContext(precision, RoundingMode.HALF_UP);

        BigDecimal one = BigDecimal.ONE;
        BigDecimal two = new BigDecimal("2");
        BigDecimal four = new BigDecimal("4");
        BigDecimal numerator = new BigDecimal("3");
        BigDecimal denominator = one;
        BigDecimal result = null;
        BigDecimal prevResult = null;
        BigDecimal iteration = null;

        boolean negate = false;

        int iterationsLength = iterations.toString().length();
        String format = "%" + iterationsLength + "s: ";

        for (BigDecimal n = one; n.compareTo(iterations) <= 0; n = n.add(one)) {
            BigDecimal nextNumerator = four;
            if (negate) {
                nextNumerator = nextNumerator.negate();
            }
            BigDecimal next = n.multiply(two);
            BigDecimal nextPlusOne = next.add(one);
            BigDecimal nextPlusTwo = next.add(two);
            BigDecimal nextDenominator = next.multiply(nextPlusOne).multiply(nextPlusTwo);
            nextNumerator = nextNumerator.multiply(denominator);

            numerator = numerator.multiply(nextDenominator);
            denominator = denominator.multiply(nextDenominator);

            numerator = numerator.add(nextNumerator);
            negate = !negate;

            if (flags.compareValues) {
                prevResult = result;
            }
            if (flags.compareValues || flags.printSteps) {
                result = numerator.divide(denominator, context);
            }
            if (flags.printSteps) {
                iteration = n;
                System.out.format(format, iteration);
                System.out.println(result);
            }
            if (flags.compareValues && result.equals(prevResult)) {
                break;
            }
        }
        if (!flags.printSteps) {
            result = numerator.divide(denominator, context);
        }
        if (flags.estimateMemoryUsage) {
            String numeratorString = format(numerator, 2);
            String denominatorString = format(denominator, 2);
            System.out.println();
            System.out.println("numerator:   " + numeratorString);
            System.out.println("denominator: " + denominatorString);

            int memoryUsageBytes = estimateMemoryUsage(one, two, four,
                    numerator, denominator, result, prevResult, iteration);

            String memoryUsageString = formatMemoryUsage(memoryUsageBytes);
            System.out.println("Memory usage: " + memoryUsageString);
        }
        return result;
    }

    /**
     * Calculate an approximation of pi using the infinite series used by Issac Newton.
     * See <a href="https://www.dropbox.com/s/jndc4y12f2zhnt4/Derivation%20of%20Newton's%20Sum.pdf">
     * paper</a> explaining the series and its derivation.
     * <p>
     * See Matt Parker demonstrate using this sum to calculate pi by hand on the
     * <a href="https://www.youtube.com/watch?v=CKl1B8y4qXw">Standup Maths</a>
     * YouTube channel.
     *
     * @param iterations The number of iterations.
     * @param precision The amount of precision.
     * @param flags The set of boolean flags to alter behavior.
     * @return The approximation of pi.
     */
    public static BigDecimal newtonMethod(BigDecimal iterations, int precision, Flags flags) {
        System.out.println("Calculating an approximation of pi using Issac Newton's method:");
        System.out.println("See https://www.dropbox.com/s/jndc4y12f2zhnt4/Derivation%20of%20Newton's%20Sum.pdf" +
                " for more details.");

        MathContext context = new MathContext(precision);

        BigDecimal one = BigDecimal.ONE;
        BigDecimal two = new BigDecimal("2");
        BigDecimal three = new BigDecimal("3");
        BigDecimal four = new BigDecimal("4");
        BigDecimal twentyFour = new BigDecimal("24");
        BigDecimal result = null;
        BigDecimal prevResult = null;

        BigDecimal firstTerm = three.multiply(three.sqrt(context)).divide(four, context);
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal iteration = null;

        int iterationsLength = iterations.toString().length();
        String format = "%" + iterationsLength + "s: ";

        for (BigDecimal n = one; n.compareTo(iterations) <= 0; n = n.add(one)) {
            BigDecimal numerator = factorial(two.multiply(n).subtract(two)).negate();
            BigDecimal t1 = two.pow(4 * n.intValue() - 2);
            BigDecimal t2 = factorial(n.subtract(one)).pow(2);
            BigDecimal t3 = two.multiply(n).subtract(three);
            BigDecimal t4 = two.multiply(n).add(one);
            BigDecimal denominator = t1.multiply(t2).multiply(t3).multiply(t4);

            BigDecimal nextTerm = numerator.divide(denominator, context);
            sum = sum.add(nextTerm);

            if (flags.compareValues) {
                prevResult = result;
            }
            if (flags.compareValues || flags.printSteps) {
                result = firstTerm.add(twentyFour.multiply(sum));
            }
            if (flags.printSteps) {
                iteration = n;
                System.out.format(format, iteration);
                System.out.println(result);
            }
            if (flags.compareValues && result.equals(prevResult)) {
                break;
            }
        }
        if (!flags.printSteps) {
            result = firstTerm.add(twentyFour.multiply(sum));
        }
        if (flags.estimateMemoryUsage) {
            int memoryUsageBytes = estimateMemoryUsage(one, two, three, four,
                    twentyFour, result, prevResult, firstTerm, sum, iteration);

            String memoryUsageString = formatMemoryUsage(memoryUsageBytes);
            System.out.println("Memory usage: " + memoryUsageString);
        }
        return result;
    }

    /**
     * Calculate an approximation of pi using Viète's formula.
     * See <a href="https://en.wikipedia.org/wiki/Viète%27s_formula">Wikipedia</a>
     * for more details.
     *
     * @param iterations The number of iterations.
     * @param precision The amount of precision.
     * @param flags The set of boolean flags to alter behavior.
     * @return The approximation of pi.
     */
    public static BigDecimal vieteFormula(BigDecimal iterations, int precision, Flags flags) {
        System.out.println("Calculating an approximation of pi using Viète's formula:");
        System.out.println("See https://en.wikipedia.org/wiki/Viète%27s_formula for more details.");

        MathContext context = new MathContext(precision, RoundingMode.HALF_UP);

        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal one = BigDecimal.ONE;
        BigDecimal two = new BigDecimal("2");

        BigDecimal a = two.sqrt(context);
//        BigDecimal product = one;
        BigDecimal numerator = one;
        BigDecimal denominator = one;
        BigDecimal result = null;
        BigDecimal prevResult = null;
        BigDecimal iteration = null;

        int iterationsLength = iterations.toString().length();
        String format = "%" + iterationsLength + "s: ";

        for (BigDecimal n = zero; n.compareTo(iterations) < 0; n = n.add(one)) {
//            product = product.multiply(a.divide(two, context));
//          calulating as 2/sqrt(2) to avoid inverse calculation
            numerator = numerator.multiply(two);
            denominator = denominator.multiply(a);
            if(flags.compareValues) {
                prevResult = result;
            }
            if (flags.compareValues || flags.printSteps) {
//                result = one.divide(product, context).multiply(two);
                result = numerator.divide(denominator, context).multiply(two);
            }
            if (flags.printSteps) {
                iteration = n.add(one);
                System.out.format(format, iteration);
                System.out.println(result);
            }
            if (flags.compareValues && result.equals(prevResult)) {
                break;
            }
            a = two.add(a).sqrt(context);
        }
        if (!flags.printSteps) {
//            result = one.divide(product, context).multiply(two);
            result = numerator.divide(denominator, context).multiply(two);
        }
        if (flags.estimateMemoryUsage) {
            int memoryUsageBytes = estimateMemoryUsage(zero, one, two, a,
                    numerator, denominator, result, prevResult, iteration);

            String memoryUsageString = formatMemoryUsage(memoryUsageBytes);
            System.out.println("Memory usage: " + memoryUsageString);
        }
        return result;
    }

    /**
     * Calculate an approximation of pi using the Wallis product.
     * See <a href="https://en.wikipedia.org/wiki/Wallis_product">Wikipedia</a>
     * for more details.
     *
     * @param iterations The number of iterations.
     * @param precision The amount of precision.
     * @param flags The set of boolean flags to alter behavior.
     * @return The approximation of pi.
     */
    public static BigDecimal wallisProduct(BigDecimal iterations, int precision, Flags flags) {
        System.out.println("Calculating an approximation of pi using the Wallis product:");
        System.out.println("See https://en.wikipedia.org/wiki/Wallis_product for more details.");

        MathContext context = new MathContext(precision, RoundingMode.HALF_UP);

        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal one = BigDecimal.ONE;
        BigDecimal two = new BigDecimal("2");
        BigDecimal four = new BigDecimal("4");

        BigDecimal numerator = one;
        BigDecimal denominator = one;
        BigDecimal newNumerator = null;
        BigDecimal newDenominator = null;
        BigDecimal result = null;
        BigDecimal prevResult = null;
        BigDecimal iteration = null;

        int iterationsLength = iterations.toString().length();
        String format = "%" + iterationsLength + "s: ";

        for (BigDecimal n = one; n.compareTo(iterations) <= 0; n = n.add(one)) {
            newNumerator = four.multiply(n.pow(2));
            newDenominator = two.multiply(n).subtract(one).multiply(two.multiply(n).add(one));
            numerator = numerator.multiply(newNumerator);
            denominator = denominator.multiply(newDenominator);
            if (flags.compareValues) {
                prevResult = result;
            }
            if (flags.compareValues || flags.printSteps) {
                result = numerator.divide(denominator, context).multiply(two);
            }
            if (flags.printSteps) {
                iteration = n;
                System.out.format(format, iteration);
                System.out.println(result);
            }
            if (flags.compareValues && result.equals(prevResult)) {
                break;
            }
        }
        if (!flags.printSteps) {
            result = numerator.divide(denominator, context).multiply(two);
        }
        if (flags.estimateMemoryUsage) {
            int memoryUsageBytes = estimateMemoryUsage(zero, one, two,
                    numerator, denominator, newNumerator, newDenominator,
                    result, prevResult, iteration);

            String memoryUsageString = formatMemoryUsage(memoryUsageBytes);
            System.out.println("Memory usage: " + memoryUsageString);
        }
        return result;
    }

    /**
     * Calculate an approximation of pi using the Chudnovsky algorithm.
     * See <a href="https://en.wikipedia.org/wiki/Chudnovsky_algorithm">Wikipedia</a>
     * for more details.
     *
     * @param iterations The number of iterations.
     * @param precision The amount of precision.
     * @param flags The set of boolean flags to alter behavior.
     * @return The approximation of pi.
     */
    public static BigDecimal chudnovskyAlgorithm(BigDecimal iterations, int precision, Flags flags) {
        System.out.println("Calculating an approximation of pi using the Chudnovsky algorithm:");
        System.out.println("See https://en.wikipedia.org/wiki/Chudnovsky_algorithm for more details.");

        MathContext context = new MathContext(precision, RoundingMode.HALF_UP);

        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal one = BigDecimal.ONE;
        BigDecimal twelve = new BigDecimal("12");
        BigDecimal sixteen = new BigDecimal("16");

        BigDecimal c = new BigDecimal("426880").multiply(new BigDecimal("10005").sqrt(context));

        BigDecimal l = new BigDecimal("13591409");
        BigDecimal x = one;
        BigDecimal m = one;
        BigDecimal k = new BigDecimal("6");

        BigDecimal l_nPlusOne = null;
        BigDecimal x_nPlusOne = null;
        BigDecimal m_nPlusOne = null;
        BigDecimal k_nPlusOne = null;

        BigDecimal l_addend = new BigDecimal("545140134");
        BigDecimal x_multiplicand = new BigDecimal("-262537412640768000");

        BigDecimal numerator = null;
        BigDecimal denominator = null;

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal result = null;
        BigDecimal prevResult = null;
        BigDecimal iteration = null;

        int iterationsLength = iterations.toString().length();
        String format = "%" + iterationsLength + "s: ";

        for (BigDecimal n = zero; n.compareTo(iterations) < 0; n = n.add(one)) {
            numerator = m.multiply(l);
            denominator = x;
            sum = sum.add(numerator.divide(denominator, context));
            if (flags.compareValues) {
                prevResult = result;
            }
            if (flags.compareValues || flags.printSteps) {
                result = c.divide(sum, context);
            }
            if (flags.printSteps) {
                iteration = n.add(one);
                System.out.format(format, iteration);
                System.out.println(result);
            }
            if (flags.compareValues && result.equals(prevResult)) {
                break;
            }
            l_nPlusOne = l.add(l_addend);
            x_nPlusOne = x.multiply(x_multiplicand);
            k_nPlusOne = k.add(twelve);

            BigDecimal multNumerator = k.pow(3).subtract(sixteen.multiply(k));
            BigDecimal multDenominator = n.add(one).pow(3);
            m_nPlusOne = m.multiply(multNumerator.divide(multDenominator, context));

            l = l_nPlusOne;
            x = x_nPlusOne;
            k = k_nPlusOne;
            m = m_nPlusOne;
        }
        if (!flags.printSteps) {
            result = c.divide(sum, context);
        }
        if (flags.estimateMemoryUsage) {
            int memoryUsageBytes = estimateMemoryUsage(zero, one, twelve, sixteen, c, l, x, m, k,
                    l_nPlusOne, x_nPlusOne, m_nPlusOne, k_nPlusOne, l_addend, x_multiplicand,
                    numerator, denominator, sum, result, prevResult, iteration);

            String memoryUsageString = formatMemoryUsage(memoryUsageBytes);
            System.out.println("Memory usage: " + memoryUsageString);
        }
        return result;
    }

    /**
     * Calculate an approximation of pi using the Brent-Salamin formula.
     * See <a href="https://en.wikipedia.org/wiki/Gauss–Legendre_algorithm">Wikipedia</a>
     * for more details.
     *
     * @param iterations The number of iterations.
     * @param precision The amount of precision.
     * @param flags The set of boolean flags to alter behavior.
     * @return The approximation of pi.
     */
    public static BigDecimal brentSalaminFormula(BigDecimal iterations, int precision, Flags flags) {
        System.out.print("Calculating an approximation of pi using the Brent-Salamin formula");
        System.out.println(" (or Gauss-Legendre algorithm):");
        System.out.println("See https://en.wikipedia.org/wiki/Gauss–Legendre_algorithm for more details.");

        MathContext context = new MathContext(precision, RoundingMode.HALF_UP);

        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal one = BigDecimal.ONE;
        BigDecimal two = new BigDecimal("2");
        BigDecimal four = new BigDecimal("4");

        BigDecimal a = one;
        BigDecimal b = one.divide(two.sqrt(context), context);
        BigDecimal t = one.divide(four, context);
        BigDecimal p = one;

        BigDecimal a_nPlusOne = null;
        BigDecimal b_nPlusOne = null;
        BigDecimal t_nPlusOne = null;
        BigDecimal p_nPlusOne = null;

        BigDecimal numerator = null;
        BigDecimal denominator = null;
        BigDecimal result = null;
        BigDecimal prevResult = null;
        BigDecimal iteration = null;

        int iterationsLength = iterations.toString().length();
        String format = "%" + iterationsLength + "s: ";

        for (BigDecimal n = zero; n.compareTo(iterations) < 0; n = n.add(one)) {
            numerator = a.add(b).pow(2);
            denominator = four.multiply(t);

            if (flags.compareValues) {
                prevResult = result;
            }
            if (flags.compareValues || flags.printSteps) {
                result = numerator.divide(denominator, context);
            }
            if (flags.printSteps) {
                iteration = n.add(one);
                System.out.format(format, iteration);
                System.out.println(result);
            }
            if (flags.compareValues && result.equals(prevResult)) {
                break;
            }
            a_nPlusOne = a.add(b).divide(two, context);
            b_nPlusOne = a.multiply(b).sqrt(context);
            t_nPlusOne = t.subtract(p.multiply(a.subtract(a_nPlusOne).pow(2)));
            p_nPlusOne = two.multiply(p);
            a = a_nPlusOne;
            b = b_nPlusOne;
            t = t_nPlusOne;
            p = p_nPlusOne;
        }
        if (!flags.printSteps) {
            result = numerator.divide(denominator, context);
        }
        if (flags.estimateMemoryUsage) {
            int memoryUsageBytes = estimateMemoryUsage(zero, one, two, four, a, b, t, p,
                    a_nPlusOne, b_nPlusOne, t_nPlusOne, p_nPlusOne, numerator, denominator,
                    result, prevResult, iteration);

            String memoryUsageString = formatMemoryUsage(memoryUsageBytes);
            System.out.println("Memory usage: " + memoryUsageString);
        }
        return result;
    }

    /**
     * Print the CLI usage info.
     */
    public static void printHelpInfo() {
        System.out.println("usage: java ComputePi {1|2|3|4|5|6|7} <iterations> <precision> [options]");
        System.out.println();
        System.out.println("1st argument details:");
        System.out.println("Pass a number to select from the following algorithms:");
        System.out.println("1 - Gregory-Leibniz series");
        System.out.println("2 - Nilakantha series");
        System.out.println("3 - Newton method");
        System.out.println("4 - Viete formula");
        System.out.println("5 - Wallis product");
        System.out.println("6 - Chudnovsky algorithm");
        System.out.println("7 - Brent-Salamin formula (Gauss-Legendre algorithm)");
        System.out.println("(Or, pass the name of the algorithm, i.e. NEWTON.)");
        System.out.println();
        System.out.println("2nd argument details:");
        System.out.println("<number> - The number of iterations to run.");
        System.out.println();
        System.out.println("3rd argument details:");
        System.out.println("<number> - The precision to use in calculations.");
        System.out.println();
        System.out.println("Optional argument(s) details:");
        System.out.println("The following arguments can be used to change the program behavior:");
        System.out.println(X_ALL_DIGITS + " - (-a) Output the approximation to the fully calculated precision.");
        System.out.println("             " + "  (Default behavior is to print only the accurate digits.)");
        System.out.println(X_PRINT_STEPS + " -  (-p) print the approximation at each iteration of the algorithm.");
        System.out.println(X_COMPARE_VALUES + " - (-c) Compare the current and previous approximations,");
        System.out.println("                " + "   and terminate calculations early if they are equivalent.");
        System.out.println(X_ESTIMATE_MEMORY_USAGE + " - (-e) Print an estimation of memory consumption afterwards.");
        System.out.println();
    }

    /**
     * Compute pi according to the input arguments.
     * @param args Expects 3 required arguments:
     *             <ol>
     *             <li>The algorithm name (or number) to use.</li>
     *             <li>The number of iterations to run.</li>
     *             <li>The precision to use in calculations.</li>
     *             </ol>
     *             Also accepts the following optional arguments:
     *             <ul>
     *             <li>--all_digits (-a)</li>
     *             <li>--print_steps (-p)</li>
     *             <li>--compare_values (-c)</li>
     *             <li>--estimate_memory_usage (-e)</li>
     *             </ul>
     *             (See {@link #printHelpInfo()} implementation for
     *             more info on what the optional arguments do.)
     */
    public static void computePi(String... args) {
        long time = System.currentTimeMillis();

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

        String algorithm = args[0].toUpperCase().replace("-", "_");
        String iterationsStr = args[1];
        String precisionStr = args[2];
        if (!ComputePi.isPositiveBigInteger(iterationsStr)) {
            System.out.println("Error: 2nd argument \"" + iterationsStr + "\" is invalid; must be a positive integer.");
            System.out.println();
            printHelpInfo();
            return;
        }
        if (!ComputePi.isPositiveInteger(precisionStr)) {
            if (ComputePi.isPositiveBigInteger(precisionStr)) {
                System.out.println("Error: 3rd argument \"" + precisionStr + "\" is invalid; must be a positive integer" +
                        " between 1 and " + Integer.MAX_VALUE + " (inclusive).");
            } else {
                System.out.println("Error: 3rd argument \"" + precisionStr + "\" is invalid; must be a positive integer.");
            }
            System.out.println();
            printHelpInfo();
            return;
        }
        BigDecimal iterations = new BigDecimal(iterationsStr);
        int precision = Integer.parseInt(precisionStr);

        // ------------------------
        // parse optional arguments
        // ------------------------
        boolean printSteps = false;
        boolean compareValues = false;
        boolean allDigits = false;
        boolean estimateMemoryUsage = false;

        String unknownArg = null;

        int pi = 0;
        int ci = 0;
        int ai = 0;
        int ei = 0;
        for (int i = requiredArgsLength; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals(X_PRINT_STEPS)) {
                printSteps = true;
                pi++;
            } else if (arg.equals(X_COMPARE_VALUES)) {
                compareValues = true;
                ci++;
            } else if (arg.equals(X_ALL_DIGITS)) {
                allDigits = true;
                ai++;
            } else if (arg.equals(X_ESTIMATE_MEMORY_USAGE)) {
                estimateMemoryUsage = true;
                ei++;
            } else if (arg.matches("-[pcae]{1,4}")) {
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
                    } else if (option.equals("e")) {
                        estimateMemoryUsage = true;
                        ei++;
                    }
                }
            } else {
                unknownArg = arg;
                break;
            }
        }
        boolean duplicateArgs = pi > 1 || ci > 1 || ai > 1 || ei > 1;
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
        Flags flags = new Flags(printSteps, compareValues, allDigits, estimateMemoryUsage);

        // -------------
        // run algorithm
        // -------------
        BigDecimal approximation;
        if (algorithm.equals(GREGORY_LEIBNIZ) || algorithm.equals(GREGORY_LEIBNIZ_NAME)) {
            approximation = gregoryLeibnizSeries(iterations, precision, flags);
        }
        else if (algorithm.equals(NILAKANTHA) || algorithm.equals(NILAKANTHA_NAME)) {
            approximation = nilakanthaSeries(iterations, precision, flags);
        }
        else if (algorithm.equals(NEWTON) || algorithm.equals(NEWTON_NAME)) {
            approximation = newtonMethod(iterations, precision, flags);
        }
        else if (algorithm.equals(VIETE) || algorithm.equals(VIETE_NAME)) {
            approximation = vieteFormula(iterations, precision, flags);
        }
        else if (algorithm.equals(WALLIS) || algorithm.equals(WALLIS_NAME)) {
            approximation = wallisProduct(iterations, precision, flags);
        }
        else if (algorithm.equals(CHUDNOVSKY) || algorithm.equals(CHUDNOVSKY_NAME)) {
            approximation = chudnovskyAlgorithm(iterations, precision, flags);
        }
        else if (algorithm.equals(BRENT_SALAMIN) || algorithm.equals(BRENT_SALAMIN_NAME)
                || algorithm.equals(GAUSS_LEGENDRE_NAME)) {
            approximation = brentSalaminFormula(iterations, precision, flags);
        } else {
            System.out.println("Error: unknown algorithm \"" + algorithm + "\".");
            printHelpInfo();
            return;
        }

        time = System.currentTimeMillis() - time;
        System.out.println("Elapsed time: " + time / 1000.0 + " seconds");

        // ----------------------------
        // compare to precomputed value
        // ----------------------------
        compareDigits(approximation, flags);
    }

    /**
     * Compare the input BigDecimal approximation of pi to the precomputed source file.
     * @param approximation The BigDecimal approximation of pi.
     */
    public static void compareDigits(BigDecimal approximation, Flags flags) {
        try {
            String approximationString = approximation.toString();

            String piCheckFileName = "pi1000000.txt";
            Charset encoding = Charset.defaultCharset();
            FileReader fileReader = new FileReader(piCheckFileName, encoding);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            int accurateDigits;
            int next;
            int i = 0;

            // assuming this code will never be called with a precision > one million
            int limit = approximationString.length();
            while ((next = bufferedReader.read()) != -1 && i < limit) {
                char checkDigit = (char) next;
                char approxDigit = approximationString.charAt(i);
                if (checkDigit == approxDigit) {
                    i++;
                } else {
                    break;
                }
            }
            // subtract  the '.' character
            accurateDigits = Math.max(i - 1, 0);
            System.out.println();
            System.out.println("Approximation accurate to " + accurateDigits + " digits:");
            if (!flags.allDigits) {
                approximationString = approximationString.substring(0, accurateDigits + 1);
            }
            System.out.println(approximationString);

            if (flags.allDigits) {
                // ok to use accurateDigits because the '.' compensates for the - 1
                for (int j = 0; j < accurateDigits; j++) {
                    System.out.print(' ');
                }
                System.out.println("^ (last accurate digit)");
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * See {@link #computePi(String...)} for details.
     */
    public static void main(String[] args) {
//         computePi("BRENT_SALAMIN 10 11 --print_steps --estimate_memory_usage".split("\\s+"));
//         computePi("BRENT_SALAMIN 10 11 --print_steps --estimate_memory_usage -XallDigits".split("\\s+"));

//         computePi("GREGORY_LEIBNIZ 100 2000 --print_steps --estimateMemoryUsage --all_digits".split("\\s+"));
//         computePi("NILAKANTHA      100 2000 --print_steps --estimateMemoryUsage --all_digits".split("\\s+"));
//         computePi("NEWTON          100 2000 --print_steps --estimateMemoryUsage --all_digits".split("\\s+"));
//         computePi("CHUDNOVSKY      100 2000 --print_steps --estimateMemoryUsage --all_digits".split("\\s+"));
//         computePi("BRENT_SALAMIN   100 2000 --print_steps --estimateMemoryUsage --all_digits".split("\\s+"));

//         computePi("CHUDNOVSKY        20 20000 --estimate_memory_usage --all_digits".split("\\s+"));
//         computePi("BRENT_SALAMIN     20 20000 --estimate_memory_usage --all_digits".split("\\s+"));

//        computePi("VIETE  10000 20 --estimate_memory_usage --all_digits".split("\\s+"));
//        computePi("WALLIS 10000 20 --estimate_memory_usage --all_digits".split("\\s+"));

//        computePi("GREGORY_LEIBNIZ 100 10 --print_steps --estimate_memory_usage --all_digits --compare_values".split("\\s+"));
//        computePi("NILAKANTHA      100 10 --print_steps --estimate_memory_usage --all_digits --compare_values".split("\\s+"));
//        computePi("NEWTON          100 10 --print_steps --estimate_memory_usage --all_digits --compare_values".split("\\s+"));
//        computePi("VIETE           100 10 --print_steps --estimate_memory_usage --all_digits --compare_values".split("\\s+"));
//        computePi("WALLIS          100 10 --print_steps --estimate_memory_usage --all_digits --compare_values".split("\\s+"));
//        computePi("CHUDNOVSKY      100 10 --print_steps --estimate_memory_usage --all_digits --compare_values".split("\\s+"));
//        computePi("BRENT_SALAMIN   100 10 --print_steps --estimate_memory_usage --all_digits --compare_values".split("\\s+"));

//        computePi("GREGORY_LEIBNIZ 100 10 -peacd".split("\\s+"));

//        computePi("CHUDNOVSKY      11 2000 --print_steps --compare_values --estimate_memory_usage".split("\\s+"));
//        computePi("BRENT_SALAMIN   11 2000 --print_steps --compare_values --estimate_memory_usage".split("\\s+"));

        computePi(args);
    }
}
