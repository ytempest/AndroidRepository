package com.ytempest.calculateapp.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculateUtils {

    /**
     * 用于匹配中缀表达式的正则表达式
     */
    private static final String EXPRESSION_REG = "\\+|-|\\*|\\/|\\(|\\)|(\\d+(\\.\\d+)?)";


    /**
     * 根据指定的正则表达式来分割字符串，然后保存为一个列表并return
     *
     * @param regex  正则表达式
     * @param source 要进行分割的字符串
     * @return 存储了正则表达式分割后的所有字符的列表
     */
    public static List<String> splitExpression(String regex, String source) {
        List<String> list = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);

        while (matcher.find()) {
            list.add(matcher.group());
        }

        return list;
    }

    /**
     * 计算满足中缀表达式的字符串的值
     *
     * @param string 满足中缀表达式的字符串，每一个字符之间不能有其他字符，示例：3.3/3+0.9-(1+8)*4
     */
    public static Double calculateInfixExpression(String string) {
        List<String> stringList = splitExpression(EXPRESSION_REG, string);
        ArrayList<String> resultList = new ArrayList<>(stringList.size());

        LinkedList<String> operatorList = new LinkedList<>();

        for (String str : stringList) {
            // 如果是数字
            if (isNumber(str)) {
                resultList.add(str);

            } else {
                // 如果是右括号
                if (isRightBracket(str)) {
                    // 将栈中第一个左括号后面的所有运算符添加到结果列表中
                    String s = null;
                    while (!(s = operatorList.removeFirst()).equals("(")) {
                        resultList.add(s);
                    }

                    // 如果要添加的运算符优先级低于栈顶的运算符，那就遍历栈中左括号之后（如果没有
                    // 左括号则添加所有运算符）的运算符，然后根据优先级添加到结果列表中
                } else if (canAddOperator(operatorList, str)) {
                    // 遍历将左括号之后的运算符，并根据优先级大的添加到结果列表中
                    while (canAddOperator(operatorList, str)) {
                        resultList.add(operatorList.removeFirst());
                    }

                    // 最后将当前的运算符添加到运算符列表中
                    operatorList.addFirst(str);
                } else {
                    // 这里会添加运算符
                    operatorList.addFirst(str);
                }
            }
        }

        while (operatorList.size() != 0) {
            resultList.add(operatorList.removeFirst());
        }
        return calculatePostfixExpression(resultList);
    }

    /**
     * 判断str运算符的优先级是否大于运算符列表栈顶运算符
     */
    private static boolean canAddOperator(LinkedList<String> operatorList, String str) {
        return operatorList.size() != 0 && !isLeftBracket(operatorList.getFirst()) && !isLeftBracket(str)
                && judgeOperatorPriority(operatorList.getFirst(), str);
    }

    private static boolean isRightBracket(String str) {
        return str.equals(")");
    }

    private static boolean isLeftBracket(String str) {
        return str.equals("(");
    }

    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断src栈顶运算符的优先级是否大于dest新增运算符，如果大于则返回true。如果是同级运算符则返回true
     */
    private static boolean judgeOperatorPriority(String src, String dest) {

        switch (src) {
            case "*":
            case "/":
                return true;

            case "-":
            case "+":
                return dest.equals("-") || dest.equals("+");
        }

        return false;
    }

    /**
     * 计算满足后缀表达式的字符列表，存储的顺序必须满足后缀表达式
     *
     * @param list 满足后缀表达式的顺序的列表
     * @return 后缀表达式的结果
     */
    public static Double calculatePostfixExpression(List<String> list) {
        int size = list.size();
        LinkedList<String> operatorList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            String str = list.get(i);
            // 如果该字符是数字
            if (isNumber(str)) {
                operatorList.addFirst(str);

                // 如果该字符是操作符
            } else {
                // 获取栈顶元素，也就是计算的右操作数
                String y = operatorList.removeFirst();
                // 相当于获取栈的第二个元素，也就是计算的左操作数
                String x = operatorList.removeFirst();
                // 根据运算符的类型对x y进行计算
                Double result = calculateNumber(x, y, str);
                operatorList.addFirst(String.valueOf(result));
            }
        }
        return Double.valueOf(operatorList.getFirst());
    }

    /**
     * 根据运算符计算两个数
     *
     * @param numStr1  左操作数
     * @param numStr2  右操作数
     * @param operator 运算符
     * @return 计算结果
     */
    private static Double calculateNumber(String numStr1, String numStr2, String operator) {
        Double num1 = Double.valueOf(numStr1);
        Double num2 = Double.valueOf(numStr2);
        switch (operator) {
            case "+":
                return num1 + num2;

            case "-":
                return num1 - num2;

            case "*":
                return num1 * num2;

            case "/":
                return num1 / num2;
        }
        return num1 + num2;
    }
}
