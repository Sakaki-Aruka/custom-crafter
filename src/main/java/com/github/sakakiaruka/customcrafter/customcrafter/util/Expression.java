/**
 * MIT License
 *
 * Copyright (c) 2022 vkvashistha (vkvashistha@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.sakakiaruka.customcrafter.customcrafter.util;
import java.util.*;
public class Expression {
    private static final List<String> reservedTokens =
            Arrays.asList("(", ")", "==", "&&", "||", "<", ">", "!", "<=", ">=", "!=", "+", "-", "/", "*", "%", "^");
    private static final List<String> binaryOperators =
            Arrays.asList("==", "&&", "||", "<", ">", "!", "<=", ">=", "!=", "+", "-", "/", "*", "%", "^");
    private static final List<String> unaryOperators =
            Arrays.asList("!", "-");

    private Map<String, Object> symbolTable = new HashMap<>();
    Expression left;
    Expression right;
    Expression parent;
    String node;

    public Expression() {

    }
    public Expression(String node, Map<String, Object> symbolTable) {
        if(symbolTable != null) {
            this.symbolTable = symbolTable;
        }
        List<String> tokens = tokenize(node);
        if(tokens.size() == 1) {
            this.node = tokens.get(0);
        } else {
            Stack<Expression> expressionStack = new Stack<>();
            expressionStack.push(this);
            for(String token : tokens) {
                if(token.equals("(")) {
                    Expression exp = new Expression();
                    exp.symbolTable = symbolTable;
                    Expression parentExp = expressionStack.peek();
                    if(parentExp != null) {
                        if(parentExp.left == null) {
                            parentExp.left = exp;
                        } else {
                            parentExp.right = exp;
                        }
                        exp.parent = parentExp;
                    }
                    expressionStack.push(exp);

                } else if(token.equals(")")) {
                    expressionStack.pop();
                } else if(binaryOperators.contains(token)) {
                    expressionStack.peek().node = token;
                } else if(unaryOperators.contains(token)) {
                    expressionStack.peek().node = token;
                } else {
                    if(expressionStack.isEmpty()) {
                        expressionStack.push(new Expression(token, symbolTable));
                    }
                    Expression currentExpression = expressionStack.peek();
                    Expression exp = new Expression(token, symbolTable);
                    if (currentExpression.left == null && !unaryOperators.contains(currentExpression.node)) {
                        currentExpression.left = exp;
                        exp.parent = expressionStack.peek().left;
                    } else  {
                        currentExpression.right = exp;
                    }
                }
            }
        }
    }

    public static EvaluationResult eval(String expression) {
        return expressionBuilder().build(expression).evaluate();
    }

    public static EvaluationResult eval(String expression, Map<String, Object> symbolTable) {
        return expressionBuilder().putAll(symbolTable).build(expression).evaluate();
    }

    public static Builder expressionBuilder() {
        return new Builder();
    }
    private static List<String> tokenize(String expression) {

        List<String> reservedTokens =
                Arrays.asList("(", ")", "==", "&&", "||", "<", ">", "!", "<=", ">=", "!=", "+", "/", "*", "-", "%", "^");
        List<String> operatorSymbols = Arrays.asList("(", ")", "=", "&", "|", "<", ">", "!", "+", "/", "*", "-", "%", "^");
        ArrayList<String> tokens = new ArrayList<String>();
        StringBuilder operand = new StringBuilder();
        for (char ch : expression.toCharArray()) {
            if (ch == ' ') continue;
            String symbol = operand.toString() + ch;
            if (reservedTokens.contains(symbol)) {
                tokens.add(symbol);
                operand = new StringBuilder();
            } else if (operatorSymbols.contains(""+ch) && !operand.isEmpty()) {
                tokens.add(operand.toString());
                operand = new StringBuilder();
                operand.append(ch);
            } else if (reservedTokens.contains(operand.toString()) && !operatorSymbols.contains(""+ch)) {
                tokens.add(operand.toString());
                operand = new StringBuilder();
                operand.append(ch);
            } else {
                operand.append(ch);
            }
        }

        if (!operand.isEmpty()) {
            tokens.add(operand.toString());
        }
        return tokens;
    }

    public EvaluationResult evaluate() {
        if(node == null) {
            return left.evaluate();
        } else if(left == null && right == null) {
            if(reservedTokens.contains(node)) {
                return new EvaluationResult(node);
            } else {
                return new EvaluationResult(getValueOfDefault(symbolTable, node));
            }
        } else if(unaryOperators.contains(node) && left == null) {
            return new EvaluationResult(evaluateUnary(right.evaluate().asString(), node));
        } else if(binaryOperators.contains(node)) {
            return new EvaluationResult(evaluateBinary(Objects.requireNonNull(left).evaluate().asString(), node, right.evaluate().asString()));
        }
        return null;
    }

    private String evaluateBinary(String leftOperand, String operator, String rightOperand) {
        String leftValue = getValueOfDefault(symbolTable, leftOperand);
        String rightValue = getValueOfDefault(symbolTable, rightOperand);
        if (Objects.equals(leftValue, "nil") || Objects.equals(rightValue, "nil")) {
            return "false";
        }

        String regex = "^((?!-0?(\\.0+)?(e|$))-?(0|[1-9]\\d*)?(\\.\\d+)?(?<=\\d)(e-?(0|[1-9]\\d*))?|0x[0-9a-f]+)$";
        boolean areBothOperandNumeric = leftValue.matches(regex) && rightValue.matches(regex);
        switch (operator) {
            case "<": {

                return String.valueOf(areBothOperandNumeric && (Double.parseDouble(leftValue) < Double.parseDouble(rightValue)));
            }


            case "<=":
                return String.valueOf(areBothOperandNumeric && (Double.parseDouble(leftValue) <= Double.parseDouble(rightValue)));


            case "==":
                return String.valueOf(leftValue.equals(rightValue));


            case "!=":
                return String.valueOf(!leftValue.equals(
                        rightValue));


            case ">":
                return String.valueOf(areBothOperandNumeric && (Double.parseDouble(leftValue) > Double.parseDouble(rightValue)));


            case ">=":
                return String.valueOf(areBothOperandNumeric && (Double.parseDouble(leftValue) >= Double.parseDouble(rightValue)));


            case "-":
                return String.valueOf(Double.parseDouble(leftValue) - Double.parseDouble(rightValue));


            case "+": {
                if (areBothOperandNumeric) {
                    return String.valueOf(Double.parseDouble(leftValue) + Double.parseDouble(rightValue));
                } else {
                    return "0";
                }
            }


            case "*":
                if (areBothOperandNumeric) {
                    return String.valueOf(Double.parseDouble(leftValue) * Double.parseDouble(rightValue));
                } else return "0";


            case "/":
                if (areBothOperandNumeric) {
                    return String.valueOf(Double.parseDouble(leftValue) / Double.parseDouble(rightValue));
                } else return "0";

            case "%":
                if (areBothOperandNumeric) {
                    return String.valueOf(Double.parseDouble(leftValue) % Double.parseDouble(rightValue));
                } else return "0";

            case "^":
                if (areBothOperandNumeric) {
                    return String.valueOf(Math.pow(Double.parseDouble(leftValue), Double.parseDouble(rightValue)));
                } else return "0";

            case "&&":
                return String.valueOf(Boolean.parseBoolean(leftValue) && Boolean.parseBoolean(rightValue));

            case "||":
                return String.valueOf(Boolean.parseBoolean(leftValue) || Boolean.parseBoolean(rightValue));

            default:
                return "";
        }
    }

    private String evaluateUnary(String operand, String operator) {
        if (operator.equals("!")) {
            return String.valueOf(!Boolean.parseBoolean(operand));
        } else if (operator.equals("-")) {
            return "-" + operand;
        }
        return null;
    }

    private static String getValueOfDefault(
            Map<String, Object> data,
            String key
    ) {
        if (key.startsWith("$")) {
            if ("$currentTimeStamp".equals(key)) {
                return String.valueOf(System.currentTimeMillis());
            } else {
                return getValueOfDefault(data, key.replace("$", ""));
            }
        }
        if (data.containsKey(key)) {
            return data.get(key).toString();
        } else {
            return key;
        }
    }

    public static class EvaluationResult {
        private final String result;
        public EvaluationResult(String result) {
            this.result = result;
        }

        public int asInt() {
            return (int)Double.parseDouble(result);
        }

        public double asDouble() {
            return Double.parseDouble(result);
        }

        public boolean asBoolean() {
            return Boolean.parseBoolean(result);
        }

        public String asString() {
            return result;
        }

        @Override
        public String toString() {
            return result;
        }
    }

    public static class Builder {
        private final HashMap<String, Object> symbolTable = new HashMap<>();
        public Builder putSymbol(String key, Object value) {
            symbolTable.put(key, value);
            return this;
        }

        public Builder putAll(Map<String, Object> symbolTable) {
            this.symbolTable.putAll(symbolTable);
            return this;
        }

        public Expression build(String expression) {
            return new Expression(expression, symbolTable);
        }
    }
}
