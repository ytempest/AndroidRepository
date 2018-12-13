package com.ytempest.studentmanage.model;

/**
 * @author ytempest
 *         Description：
 */
public class ClassInfoResult extends CommonResult {

    /**
     * data : {"id":1,"classId":1511001,"departmentId":11,"majorId":11001,"grade":2015,"name":"2015级软件工程专业1班","createTime":1543902681000,"departmentName":"计算机学院","majorName":"软件工程"}
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
         * classId : 1511001
         * departmentId : 11
         * majorId : 11001
         * grade : 2015
         * name : 2015级软件工程专业1班
         * createTime : 1543902681000
         * departmentName : 计算机学院
         * majorName : 软件工程
         */

        private int id;
        private int classId;
        private int departmentId;
        private int majorId;
        private int grade;
        private String name;
        private long createTime;
        private String departmentName;
        private String majorName;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getClassId() {
            return classId;
        }

        public void setClassId(int classId) {
            this.classId = classId;
        }

        public int getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(int departmentId) {
            this.departmentId = departmentId;
        }

        public int getMajorId() {
            return majorId;
        }

        public void setMajorId(int majorId) {
            this.majorId = majorId;
        }

        public int getGrade() {
            return grade;
        }

        public void setGrade(int grade) {
            this.grade = grade;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public String getMajorName() {
            return majorName;
        }

        public void setMajorName(String majorName) {
            this.majorName = majorName;
        }
    }
}
