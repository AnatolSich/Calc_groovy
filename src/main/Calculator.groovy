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

    boolean isOperator(final String token) {
        token in OPERATORS.keySet()
    }

    boolean isAssociative(final String token, final int type) {
        if (!isOperator(token)) {
            throw new IllegalArgumentException("Invalid token: ${token}")
        }

        if (OPERATORS.get(token)[1] == type) {
            return true
        }

        false
    }

    int comparePrecedence(final String token1, final String token2) {
        if (!isOperator(token1) || !isOperator(token2)) {
            throw new IllegalArgumentException("Invalid tokens: ${token1} and/or ${token2}")
        }

        OPERATORS.get(token1)[0] - OPERATORS.get(token2)[0]
    }

    String[] convertToRPN(final String[] inputTokens) {
        final List<String> out = []
        final Stack<String> stack = []

        inputTokens.each { String token ->
            if (isOperator(token)) {
                while (!stack.empty() && isOperator(stack.peek())) {
                    if ((isAssociative(token, LEFT_ASSOCIATION) && comparePrecedence(token, stack.peek()) <= 0) ||
                            (isAssociative(token, RIGHT_ASSOCIATION) && comparePrecedence(token, stack.peek()) < 0)) {
                        out << stack.pop()
                        continue
                    }
                    break
                }
                stack.push(token)
            } else if (token == LEFT_BRACKET) {
                stack.push(token)
            } else if (token == RIGHT_BRACKET) {
                while (!stack.empty() && stack.peek() != LEFT_BRACKET) {
                    out << stack.pop()
                }
                stack.pop()
            } else {
                out << token
            }
        }

        while (!stack.empty()) {
            out << stack.pop()
        }

        final String[] output = new String[out.size()]

        out.toArray(output)
    }

    double resolveRPN(final String[] tokens) {
        final Stack<String> stack = []

        tokens.each {
            if (!isOperator(it)) {
                stack.push(it)
            } else {
                final BigInteger bi2 = stack.pop().toBigInteger()
                final BigInteger bi1 = stack.pop().toBigInteger()

                switch (it) {
                    case ADD:
                        stack.push((bi1 + bi2).toString())
                        break
                    case SUBTRACT:
                        stack.push((bi1 - bi2).toString())
                        break
                    case MULTIPLY:
                        stack.push((bi1 * bi2).toString())
                        break
                    case DIVIDE:
                        stack.push((bi1 / bi2).toString())
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
        final def reversePolishNotation = convertToRPN(splittedExpression)
        final Double result = resolveRPN(reversePolishNotation)

        result
    }
}
