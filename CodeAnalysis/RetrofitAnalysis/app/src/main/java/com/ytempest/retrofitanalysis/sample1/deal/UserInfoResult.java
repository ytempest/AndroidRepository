package com.ytempest.retrofitanalysis.sample1.deal;

/**
 * @author ytempest
 *         Description：
 */
public class UserInfoResult {

    /**
     * age : 21
     * name : dy
     * sex : 男
     */

    private int age;
    private String name;
    private String sex;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "name=" + name + "," +
                "age=" + age + "," +
                "sex=" + sex;

    }
}