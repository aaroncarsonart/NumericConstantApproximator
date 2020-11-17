# NumericConstantApproximator
Approximate numeric constants such as [Pi][8] and [Phi][9] to arbitrary precision using java's
[BigDecimal][11] class (and analyze the results).

### Compile
```
javac ComputePhi.java ComputePi.java PiAlgorithmExaminer.java
```

## 1. ComputePhi
[ComputePhi](ComputePhi.java) demonstrates algorithmically that no matter which two terms you start 
with, if you generate a series of numbers by adding the two previous terms, the ratio between
consecutive terms converges to [Phi (the Golden Ratio)][9].

#### Usage:
```
java ComputePhi <first-term> <second-term> <iterations> <precision> [options]
```

#### Options:
```
# TODO coming soon
```

#### Examples:
```bash
# the fibonacci sequence
java ComputePhi 1 1 10 10

# the lucas numbers
java ComputePhi 2 1 10 10

# go wild if you want to
java ComputePhi 23 9001 200 1000
```

## 2. ComputePi
[ComputePi](ComputePi.java) demonstrates 7 different algorithms for approximating [Pi][8].
The result is then compared to a precomputed file of one million digits (sourced from
[here][10]) to determine accuracy.

#### Usage:
```
java ComputePi <algorithm> <iterations> <precision> [options]
```
#### Notes:
- `<algorithm>` can be a number, or an algorithm name (case-insensitive):

 | Number | Algorithm Name                    | Reference  |
 | ------ | --------------------------------- | ---------- |
 |      1 | Gregory-Leibniz                   | [\[1\]][1] |
 |      2 | Nilakantha                        | [\[2\]][2] |
 |      3 | Newton                            | [\[3\]][3] |
 |      4 | Viete                             | [\[4\]][4] |
 |      5 | Wallis                            | [\[5\]][5] |
 |      6 | Chudnovsky                        | [\[6\]][6] |
 |      7 | Brent-Salamin (or Gauss-Legendre) | [\[7\]][7] |

#### Options:
```
# TODO coming soon
```

#### Examples:
```bash
java ComputePi 1          1000  10
java ComputePi 2          1000  10 --print_steps
java ComputePi CHUDNOVSKY   10 100 --compare_values --estimate_memory_usage
```

## 3. PiAlgorithmExaminer
[PiAlgorithmExaminer](PiAlgorithmExaminer.java) compares the approximations, runtimes, and memory 
usage of the algorithms implemented in [ComputePi](ComputePi.java) iteration-by-iteration, and
prints the results to a table.

#### Usage:
```
java PiAlgorithmExaminer <algorithms> <iterations> <precision> [options]
```
#### Notes:
- `<algorithms>` is a comma-separated list of algorithm names and numbers (see above for values).

#### Options:
```bash
# TODO coming soon
```

#### Examples:
```bash
java PiAlgorithmExaminer CHUDNOVSKY,BRENT_SALAMIN 1000 20000
java PiAlgorithmExaminer 1,2,3,4,5,6,7 10 2000 --print_table
```

### Example Results:
Here's a comparison of the first 10 iterations of all 7 implemented algorithms.  The values show 
the number of accurate leading digits of pi per iteration.

| ITERATIONS | GREGORY_LEIBNIZ | NILAKANTHA | NEWTON | VIETE | WALLIS | CHUDNOVSKY | BRENT_SALAMIN |
| ---------- | --------------- | ---------- | ------ | ----- | ------ | ---------- | ------------- |
|          1 |               0 |          2 |      1 |     0 |      0 |         14 |             0 |
|          2 |               1 |          2 |      3 |     1 |      0 |         28 |             3 |
|          3 |               0 |          3 |      3 |     2 |      0 |         42 |             8 |
|          4 |               1 |          2 |      4 |     2 |      0 |         56 |            19 |
|          5 |               0 |          3 |      4 |     3 |      1 |         70 |            41 |
|          6 |               1 |          3 |      6 |     4 |      1 |         85 |            84 |
|          7 |               1 |          3 |      6 |     5 |      1 |         99 |           171 |
|          8 |               1 |          4 |      7 |     5 |      1 |        113 |           345 |
|          9 |               1 |          4 |      8 |     5 |      1 |        127 |           694 |
|         10 |               1 |          4 |      9 |     6 |      1 |        142 |          1392 |

[1]:https://en.wikipedia.org/wiki/Leibniz_formula_for_%CF%80
[2]:https://en.wikipedia.org/wiki/Pi#Infinite_series
[3]:https://www.dropbox.com/s/jndc4y12f2zhnt4/Derivation%20of%20Newton's%20Sum.pdf
[4]:https://en.wikipedia.org/wiki/Viète%27s_formula
[5]:https://en.wikipedia.org/wiki/Wallis_product
[6]:https://en.wikipedia.org/wiki/Chudnovsky_algorithm
[7]:https://en.wikipedia.org/wiki/Gauss–Legendre_algorithm
[8]:https://en.wikipedia.org/wiki/Pi
[9]:https://en.wikipedia.org/wiki/Phi
[10]:https://www.angio.net/pi/digits.html
[11]:https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/math/BigDecimal.html
