package com.ytempest.studentmanage.model;

/**
 * @author ytempest
 *         Description：
 */
public class DepartmentInfoResult extends CommonResult {

    /**
     * data : {"id":1,"departmentId":11,"name":"音乐学院","introductions":"音乐","createTime":1543902527000}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 1
         * departmentId : 11
         * name : 音乐学院
         * introductions : 音乐
         * createTime : 1543902527000
         */

        private int id;
        private int departmentId;
        private String name;
        private String introductions;
        private long createTime;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(int departmentId) {
            this.departmentId = departmentId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIntroductions() {
            return introductions;
        }

        public void setIntroductions(String introductions) {
            this.introductions = introductions;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }
    }
}
