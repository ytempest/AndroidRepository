package com.ytempest.calculateapp.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ytempest
 *         Description：该类是一个对表达式输入格式进行合理性检测和对表达式进行一些适应性修改的帮助类，
 *         用于协助 CalculatorTextView
 */
public class InputHelper {

    /**
     * 表达式允许的最大长度
     */
    private static final int MAX_EXP_LENGTH = 20;

    private InputHelper() {
    }

    public static boolean isInputCorrect(String ch, String expression) {
        // 判断表达式的长度是否超过了MAX_EXP_LENGTH个字符
        if (expression.length() >= MAX_EXP_LENGTH) {
            return false;
        }

        String lastChar = String.valueOf(expression.charAt(expression.length() - 1));
        switch (ch) {
            // 1、左括号的添加约束
            case "(":
                // 判断最后一个元素是否为数字、")"、"."、四则运算符，则禁止添加 "("运算符
                if (isMatch("\\d+|\\)|\\.", lastChar)) {
                    return false;
                }
                break;

            // 2、右括号的添加约束
            case ")":
                // 如果表达式末尾不是数字或者")"，或者表达式只有一个0的时候，则禁止添加
                if (!isMatch("\\d|\\)", lastChar) || "0".equals(expression)) {
                    return false;
                }
                break;

            // 3、小数点的添加约束
            case ".":
                // 如果表达式最后一个元素是运算符或者小数点，则禁止继续添加小数点
                if (isMatch("\\+|-|×|÷|\\(|\\)|\\.", lastChar)) {
                    return false;
                }
                // 处理出现 1.32.54这种数字情况
                int index = expression.lastIndexOf(".");
                if (index != -1) {
                    String sub = expression.substring(index + 1, expression.length());
                    if (isMatch("^([0-9]{1,})$", sub)) {
                        return false;
                    }
                }
                break;

            // 4、数字0的添加约束
            case "0":
                // 禁止添加到 ")"后面
                if (")".equals(lastChar) || "0".equals(expression)) {
                    return false;
                }
                // 防止出现0000这种情况，即禁止在[+0、-0、×0、÷0]后面再加0
                if (expression.length() > 3 && isMatch("[\\+×\\-÷\\(]{1}0{1}", expression.substring(expression.length() - 2, expression.length()))) {
                    return false;
                }
                break;

            default:

                // 5、1-9数字的添加规则
                // 禁止添加到 ")"后面
                if (isMatch("\\d", ch)) {
                    if (")".equals(lastChar)) {
                        return false;
                    }
                }

                // 6、四则运算符的添加规则
                // 如果表达式最后一个元素是[+,-,×,÷,(],则禁止添加[+,-,×,÷]
                if (isMatch("\\+|-|×|÷", ch)) {
                    if (isMatch("\\(|\\.", lastChar) || ch.equals(lastChar)) {
                        return false;
                    }
                }

                break;
        }

        return true;

    }

    public static boolean isMatch(String reg, String str) {
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(str);
        return m.matches();
    }


    /**
     * 根据要添加的字符和表达式的内容进行适应性修复，如果要添加数字，则如43+27+0后面的0去掉
     *
     * @param expression 需要修复的表达式
     * @param ch         输入的字符
     */
    public static String autoRepairExpression(String expression, String ch) {
        String lastChar = String.valueOf(expression.charAt(expression.length() - 1));
        // 修复一开始输入数字时，显示为02、04等情况
        if ("0".equals(expression) && isMatch("[1-9]", ch)) {
            expression = "";


            // 数字的添加修复
            // 如果在+0后面添加数字，则将0删除，然后添加数字
        } else if (isMatch("\\d", ch)) {
            if (expression.length() >= 2 && isMatch("[\\+×÷\\-\\(]{1}0{1}",
                    expression.substring(expression.length() - 2, expression.length()))) {
                expression = expression.substring(0, expression.length() - 1);
            }


            // 四则运算符的修复
            // 如果在四则运算符后面添加新的运算符，则覆盖之前的运算符
        } else if (isMatch("\\+|-|×|÷", ch) && isMatch("\\+|-|×|÷", lastChar)) {
            expression = expression.substring(0, expression.length() - 1);
        }
        return expression;
    }

}
