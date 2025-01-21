import java.util.*;

import static java.lang.Double.NaN;
import static java.lang.Math.pow;


/*
 *   A calculator for rather simple arithmetic expressions
 *
 *   This is not the program, it's a class declaration (with methods) in it's
 *   own file (which must be named Calculator.java)
 *
 *   NOTE:
 *   - No negative numbers implemented
 */
public class Calculator {

    // Here are the only allowed instance variables!
    // Error messages (more on static later)
    final static String MISSING_OPERAND = "Missing or bad operand";
    final static String DIV_BY_ZERO = "Division with 0";
    final static String MISSING_OPERATOR = "Missing operator or parenthesis";
    final static String OP_NOT_FOUND = "Operator not found";

    // Definition of operators
    final static String OPERATORS = "+-*/^";

    // Method used in REPL
    double eval(String expr) {
        if (expr.length() == 0) {
            return NaN;
        }
        List<String> tokens = tokenize(expr);
        List<String> postfix = infix2Postfix(tokens);
        return evalPostfix(postfix);
    }

    // ------  Evaluate RPN expression -------------------

    double evalPostfix(List<String> postfix) {
        Stack<Double> stack = new Stack<>();

        for (String token : postfix) {
            if (isOperand(token)) {
                stack.push(Double.parseDouble(token));
            }
            else if (isOperator(token)) {
                if (stack.size() < 2)
                    throw new IllegalArgumentException(MISSING_OPERAND); // saknar operand
                double d1 = stack.pop();
                double d2 = stack.pop();
                double result = applyOperator(token, d1, d2);
                stack.push(result);
            }
        }
        if (stack.size() > 1) {
            throw new IllegalArgumentException(MISSING_OPERATOR); // operator kräver två operander, operand saknas
        }
        return stack.pop();
    }

    double applyOperator(String op, double d1, double d2) {
        switch (op) {
            case "+":
                return d1 + d2;
            case "-":
                return d2 - d1;
            case "*":
                return d1 * d2;
            case "/":
                if (d1 == 0) {
                    throw new IllegalArgumentException(DIV_BY_ZERO);
                }
                return d2 / d1;
            case "^":
                return pow(d2, d1);
        }
        throw new RuntimeException(OP_NOT_FOUND);
    }

    // ------- Infix 2 Postfix ------------------------

    List<String> infix2Postfix(List<String> infix) {
        List<String> postfix = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        for (String token : infix) {
            if (isOperand(token)) {
                postfix.add(token);
            }
            else if (isOperator(token)) {
                while (!stack.isEmpty() && isOperator(stack.peek()) &&
                        (getPrecedence(stack.peek()) > (getPrecedence(token)) ||
                                (getPrecedence(stack.peek()) == getPrecedence(token)) &&
                                        getAssociativity(token) == Assoc.LEFT)) {
                    postfix.add(stack.pop());

                    // 3 ^ 4 ^ 5
                    // 3 4 5 ^ ^
                }
                stack.push(token);
            }
            else if (token.equals("(")) {
                stack.push(token);
            }
            else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    postfix.add(stack.pop());
                }
                if (!stack.isEmpty() && stack.peek().equals("(")) {
                    stack.pop();
                } else {
                    throw new IllegalArgumentException(MISSING_OPERATOR); // saknar vänsterparentes
                }
            }
        }
        while (!stack.isEmpty()) {
            if (stack.peek().equals("(")) {
                throw new IllegalArgumentException(MISSING_OPERATOR); // saknar högerparentes
            }
            postfix.add(stack.pop());
        }
        return postfix;
    }

    public boolean isOperand(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isOperator(String token) {
        return OPERATORS.contains(token);
    }

    int getPrecedence(String op) {
        if ("+-".contains(op)) {
            return 2;
        } else if ("*/".contains(op)) {
            return 3;
        } else if ("^".contains(op)) {
            return 4;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    Assoc getAssociativity(String op) {
        if ("+-*/".contains(op)) {
            return Assoc.LEFT;
        } else if ("^".contains(op)) {
            return Assoc.RIGHT;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    enum Assoc {
        LEFT,
        RIGHT
    }

    // ---------- Tokenize -----------------------

    // List String (not char) because numbers (with many chars)
    List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentNumber = new StringBuilder();

        for(char c : expr.toCharArray()) {
            if (Character.isDigit(c) || c == '.') {
                currentNumber.append(c);
            }
            else {
                if (currentNumber.length() > 0) {
                    tokens.add(currentNumber.toString());
                    currentNumber.setLength(0);
                }
                if (c != ' ') {
                    tokens.add(String.valueOf(c));
                }
            }
        }
        if (currentNumber.length() > 0) {
            tokens.add(currentNumber.toString());
        }
        return tokens;
    }
}
