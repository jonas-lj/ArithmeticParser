package dk.jonaslindstrom.arithmeticparser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This class parses arithmetic expressions into reverse polish notation which makes them easy to
 * evaluate. It supports basic arithmetic (+, -, *, /, ^) and parenthesis. It also supports user
 * defined functions and variables to be assigned during evaluation.
 *
 * @param <NumberT>
 */
public class Parser<NumberT> {

  private NumberParser<NumberT> parser;
  private List<String> operators;

  private boolean isNumeric(String s) {
    try {
      parser.parse(s);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  /**
   * Return the default parser.
   * 
   * @return
   */
  public static Parser<Double> getDefault() {
    return new Parser<Double>(Arrays.asList("+", "-", "*", "/", "^"), Double::parseDouble);
  }

  /**
   * Create a new operator recognizing the given operators and using the given parser to convert
   * numbers into the desired type. Operators should be ordered with the least precedent first.
   * 
   * @param operators
   * @param parser
   */
  public Parser(List<String> operators, NumberParser<NumberT> parser) {
    this.operators = operators;
    this.parser = parser;
  }

  /**
   * Parse the given expression given lists of names of user defined variables and functions.
   * 
   * @param infix
   * @param variables
   * @param functions
   * @return
   * @throws ParseException
   */
  public List<Token> parse(String infix, List<String> variables, List<String> functions)
      throws ParseException {

    LinkedList<Token> operaratorStack = new LinkedList<>();
    LinkedList<Token> output = new LinkedList<>();

    List<String> tokens = new ArrayList<>();
    tokens.addAll(operators);
    tokens.addAll(variables);
    tokens.addAll(functions);
    tokens.addAll(Arrays.asList("(", ")", ","));
    infix = spacify(infix, tokens);

    boolean implicitMultiplicationPossible = false;
    
    for (String token : infix.split("\\s")) {
      if (operators.contains(token)) {

        // operator
        while (!operaratorStack.isEmpty() && operators
            .indexOf(operaratorStack.peek().getRepresentation()) >= operators.indexOf(token)) {
          output.add(operaratorStack.pop());
        }
        operaratorStack.push(new Token(token, Token.Type.OPERATOR));

        implicitMultiplicationPossible = false;
        
      } else if (token.equals("(")) {

        // left parenthesis
        if (implicitMultiplicationPossible) {
          operaratorStack.add(new Token("*", Token.Type.OPERATOR));
        }
        
        operaratorStack.push(new Token("(", Token.Type.LEFT_PARANTHESIS));
        implicitMultiplicationPossible = false;

      } else if (token.equals(")")) {
        
        // right parenthesis
        while (!(operaratorStack.peek().getType() == Token.Type.LEFT_PARANTHESIS)) {
          output.add(operaratorStack.pop());
        }
        operaratorStack.pop();
        implicitMultiplicationPossible = true;

      } else if (variables.contains(token)) {

        // variable
        output.add(new Token(token, Token.Type.VARIABLE));
        if (implicitMultiplicationPossible) {
          output.add(new Token("*", Token.Type.OPERATOR));
        }
        implicitMultiplicationPossible = true;

      } else if (functions.contains(token)) {

        // function
        operaratorStack.push(new Token(token, Token.Type.FUNCTION));
        implicitMultiplicationPossible = false;

      } else if (token.equals(",")) {

        // comma -- treat like right and left parenthesis
        while (!(operaratorStack.peek().getType() == Token.Type.LEFT_PARANTHESIS)) {
          output.add(operaratorStack.pop());
        }
        operaratorStack.pop();
        operaratorStack.push(new Token(token, Token.Type.LEFT_PARANTHESIS));
        implicitMultiplicationPossible = false;

      } else if (isNumeric(token)) {

        // number
        output.add(new Token(token, Token.Type.NUMBER));

        if (implicitMultiplicationPossible) {
          output.add(new Token("*", Token.Type.OPERATOR));
        }
        implicitMultiplicationPossible = true;

      } else {
        throw new ParseException("Unknown token: " + token, infix.indexOf(token));
      }
    }

    while (!operaratorStack.isEmpty()) {
      output.add(operaratorStack.pop());
    }

    return output;
  }

  /**
   * Make spaces on both sides of each given token and that there are exactly one such space.
   * 
   * @param expression
   * @param tokens
   * @return
   */
  private static String spacify(String expression, List<String> tokens) {

    for (String token : tokens) {
      expression = expression.replace(token, " " + token + " ");
    }

    while (expression.contains("  ")) {
      expression = expression.replace("  ", " ");
    }

    expression = expression.trim();

    return expression;
  }


}
