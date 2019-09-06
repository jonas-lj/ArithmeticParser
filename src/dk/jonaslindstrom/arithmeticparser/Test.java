package dk.jonaslindstrom.arithmeticparser;

import java.text.ParseException;
import java.util.Collections;
import java.util.Map;

public class Test {

  private static Double func(int x, int y) {
    return func((double) x, (double) y);
  }

  private static Double func(Double x, Double y) {
    return Math.exp(x) + y * 7;
  }

  public static void main(String[] arguments) throws EvaluationException, ParseException {

    String e = "(3+func(7-3*2, 2y)) * 2^3";

    Map<String, Double> variables = Collections.singletonMap("y", 3.0);
    Map<String, MultiOperator<Double>> functions =
        Collections.singletonMap("func", new MultiOperator<Double>(Test::func));

    Double value = Evaluator.evaluate(e, variables, functions);
    System.out.println(value);
    
    int y = 3;
    double expected = (3 + func(7 - 3 * 2, 2 * y)) * Math.pow(2, 3);
    System.out.println(expected);
  }

}
