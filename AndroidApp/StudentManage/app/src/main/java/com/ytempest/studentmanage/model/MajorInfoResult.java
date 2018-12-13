package com.ytempest.studentmanage.model;

/**
 * @author ytempest
 *         Description：
 */
public class MajorInfoResult extends CommonResult {

    /**
     * data : {"id":1,"majorId":11001,"departmentId":11,"name":"软件工程","introductions":"软件工程专业","createTime":1543902576000,"departmentName":"计算机学院"}
     */

    private DataBean data;

    public MajorInfoResult() {
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 1
         * majorId : 11001
         * departmentId : 11
         * name : 软件工程
         * introductions : 软件工程专业
         * createTime : 1543902576000
         * departmentName : 计算机学院
         */

        private int id;
        private int majorId;
        private int departmentId;
        private String name;
        private String introductions;
        private long createTime;
        private String departmentName;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getMajorId() {
            return majorId;
        }

        public void setMajorId(int majorId) {
            this.majorId = majorId;
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

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }
    }
}
