import groovy.util.logging.Log

@Log
class Calculator {
    final static def INCORRECT_EXPRESSION = "Expression is incorrect. Expression: "

    final static def LEFT_ASSOCIATION = 0
    final static def RIGHT_ASSOCIATION = 1

    final static def ADD = "+"
    final static def SUBTRACT = "-"
    final static def MULTIPLY = "*"
    final static def DIVIDE = "/"
    final static def LEFT_BRACKET = "("
    final static def RIGHT_BRACKET = ")"

    final static def EXPRESSION_SPLIT_REGEXP = "((?<=\\*)|(?=\\*)|(?<=/)|(?=/)|(?<=\\+)|(?=\\+)|(?<=-)|(?=-)|(?<=\\()|(?=\\)))"
    final static def CHECK_SYMBOLS_REGEXP = "(?!(?:(([+-/*0-9()])+))\$).*"
    final static def CHECK_DOUBLES_REGEXP = "([0-9]+(\\.|,){1}[0-9]+)(([+-/*0-9()])+)"
    final static def REGEXPS = [CHECK_SYMBOLS_REGEXP, CHECK_DOUBLES_REGEXP]

    final def OPERATORS = [
            (ADD)     : new int[]{0, LEFT_ASSOCIATION},
            (SUBTRACT): new int[]{0, LEFT_ASSOCIATION},
            (MULTIPLY): new int[]{5, LEFT_ASSOCIATION},
            (DIVIDE)  : new int[]{5, LEFT_ASSOCIATION}
    ]

    boolean isOperator(final String item) {
        item in OPERATORS.keySet()
    }

    boolean isAssociative(final String item, final int type) {
        if (!isOperator(item)) {
            throw new IllegalArgumentException("Invalid item: ${item}")
        }

        if (OPERATORS.get(item)[1] == type) {
            return true
        }

        false
    }

    int comparePriority(final String item1, final String item2) {
        if (!isOperator(item1) || !isOperator(item2)) {
            throw new IllegalArgumentException("Invalid items: ${item1} and/or ${item2}")
        }

        OPERATORS.get(item1)[0] - OPERATORS.get(item2)[0]
    }

    String[] convertToReversePolishNotation(final String[] inputItems) {
        final List<String> out = []
        final Stack<String> stack = []

        inputItems.each { String item ->
            if (isOperator(item)) {
                while (!stack.empty() && isOperator(stack.peek())) {
                    if ((isAssociative(item, LEFT_ASSOCIATION) && comparePriority(item, stack.peek()) <= 0) ||
                            (isAssociative(item, RIGHT_ASSOCIATION) && comparePriority(item, stack.peek()) < 0)) {
                        out << stack.pop()
                        continue
                    }
                    break
                }
                stack.push(item)
            } else if (item == LEFT_BRACKET) {
                stack.push(item)
            } else if (item == RIGHT_BRACKET) {
                while (!stack.empty() && stack.peek() != LEFT_BRACKET) {
                    out << stack.pop()
                }
                stack.pop()
            } else {
                out << item
            }
        }

        while (!stack.empty()) {
            out << stack.pop()
        }

        final String[] output = new String[out.size()]

        out.toArray(output)
    }

    double resolveReversePolishNotation(final String[] items) {
        final Stack<String> stack = []

        items.each {
            if (!isOperator(it)) {
                stack.push(it)
            } else {
                final double b = stack.pop().toDouble()
                final double a = stack.pop().toDouble()

                switch (it) {
                    case ADD:
                        stack.push((a + b).toString())
                        break
                    case SUBTRACT:
                        stack.push((a - b).toString())
                        break
                    case MULTIPLY:
                        stack.push((a * b).toString())
                        break
                    case DIVIDE:
                        stack.push((a / b).toString())
                        break
                }
            }
        }

        stack.pop().toDouble()
    }

    void validateExpression(final String expression) {
        if (expression == null || expression.isEmpty()) {
            throw new IllegalArgumentException(INCORRECT_EXPRESSION + expression)
        }

        REGEXPS.each {
            if (expression ==~ it) {
                throw new IllegalArgumentException(INCORRECT_EXPRESSION + expression)
            }
        }
    }

    static String mod(final String s1, final String s2) {
        s1.trim() + " " + s2.trim()
    }

    double calculate(final String expression) {
        validateExpression(expression)

        final def splittedExpression = expression.split(EXPRESSION_SPLIT_REGEXP)
        final def reversePolishNotation = convertToReversePolishNotation(splittedExpression)
        final Double result = resolveReversePolishNotation(reversePolishNotation)

        result
    }
}
