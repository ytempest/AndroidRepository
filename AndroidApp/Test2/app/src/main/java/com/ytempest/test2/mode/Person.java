package com.ytempest.test2.mode;

/**
 * create by ytempest at 2017-11-20
 * Description:
 */
public class Person {
    private int no;
    private String name;
    private String job;

    public Person() {
    }

    public Person(int no, String name, String job) {
        this.no = no;
        this.name = name;
        this.job = job;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }
}
