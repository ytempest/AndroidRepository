package com.ytempest.architectureanalysis.retrofit;

/**
 * @author ytempest
 *         Description：
 */
public class UserInfoResult {

    /**
     * code : 0011
     * data : {"age":21,"name":"dy","sex":"男"}
     * msg : 登录成功
     */

    private String code;
    private DataBean data;
    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static class DataBean {
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
            return "DataBean{" +
                    "age=" + age +
                    ", name='" + name + '\'' +
                    ", sex='" + sex + '\'' +
                    '}';
        }
    }
}
