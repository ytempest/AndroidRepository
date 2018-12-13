package com.ytempest.studentmanage.model;

import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class ManageClassListResult extends CommonResult {

    private List<DataBean> data;

    public ManageClassListResult() {
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : null
         * classId : 1511001
         * departmentId : null
         * majorId : null
         * grade : 2015
         * name : 2015级软件工程专业1班
         * createTime : null
         * departmentName : ?????
         * majorName : null
         */

        private Object id;
        private int classId;
        private Object departmentId;
        private Object majorId;
        private int grade;
        private String name;
        private Object createTime;
        private String departmentName;
        private Object majorName;

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            this.id = id;
        }

        public int getClassId() {
            return classId;
        }

        public void setClassId(int classId) {
            this.classId = classId;
        }

        public Object getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Object departmentId) {
            this.departmentId = departmentId;
        }

        public Object getMajorId() {
            return majorId;
        }

        public void setMajorId(Object majorId) {
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

        public Object getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Object createTime) {
            this.createTime = createTime;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public Object getMajorName() {
            return majorName;
        }

        public void setMajorName(Object majorName) {
            this.majorName = majorName;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "id=" + id +
                    ", classId=" + classId +
                    ", departmentId=" + departmentId +
                    ", majorId=" + majorId +
                    ", grade=" + grade +
                    ", name='" + name + '\'' +
                    ", createTime=" + createTime +
                    ", departmentName='" + departmentName + '\'' +
                    ", majorName=" + majorName +
                    '}';
        }
    }
}
