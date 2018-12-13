package com.ytempest.studentmanage.model;

import java.util.List;

/**
 * @author ytempest
 *         Description：课程列表下的每一个课程的所有学生信息
 */
public class StudentListResult extends CommonResult {

    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * student : {"id":null,"studentId":111,"password":null,"name":"simon","sex":null,"departmentId":null,"majorId":null,"grade":null,"classId":null,"politicalStatus":null,"nationality":null,"enrollmentDate":null,"phone":"147258","address":null,"state":null,"image":"/kk/m.jpg","createTime":null}
         */

        private StudentBean student;

        public StudentBean getStudent() {
            return student;
        }

        public void setStudent(StudentBean student) {
            this.student = student;
        }

        public static class StudentBean {
            /**
             * id : null
             * studentId : 111
             * password : null
             * name : simon
             * sex : null
             * departmentId : null
             * majorId : null
             * grade : null
             * classId : null
             * politicalStatus : null
             * nationality : null
             * enrollmentDate : null
             * phone : 147258
             * address : null
             * state : null
             * image : /kk/m.jpg
             * createTime : null
             */

            private Object id;
            private int studentId;
            private Object password;
            private String name;
            private Object sex;
            private Object departmentId;
            private Object majorId;
            private Object grade;
            private Object classId;
            private Object politicalStatus;
            private Object nationality;
            private Object enrollmentDate;
            private String phone;
            private Object address;
            private Integer state;
            private String image;
            private Object createTime;

            public Object getId() {
                return id;
            }

            public void setId(Object id) {
                this.id = id;
            }

            public int getStudentId() {
                return studentId;
            }

            public void setStudentId(int studentId) {
                this.studentId = studentId;
            }

            public Object getPassword() {
                return password;
            }

            public void setPassword(Object password) {
                this.password = password;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Object getSex() {
                return sex;
            }

            public void setSex(Object sex) {
                this.sex = sex;
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

            public Object getGrade() {
                return grade;
            }

            public void setGrade(Object grade) {
                this.grade = grade;
            }

            public Object getClassId() {
                return classId;
            }

            public void setClassId(Object classId) {
                this.classId = classId;
            }

            public Object getPoliticalStatus() {
                return politicalStatus;
            }

            public void setPoliticalStatus(Object politicalStatus) {
                this.politicalStatus = politicalStatus;
            }

            public Object getNationality() {
                return nationality;
            }

            public void setNationality(Object nationality) {
                this.nationality = nationality;
            }

            public Object getEnrollmentDate() {
                return enrollmentDate;
            }

            public void setEnrollmentDate(Object enrollmentDate) {
                this.enrollmentDate = enrollmentDate;
            }

            public String getPhone() {
                return phone;
            }

            public void setPhone(String phone) {
                this.phone = phone;
            }

            public Object getAddress() {
                return address;
            }

            public void setAddress(Object address) {
                this.address = address;
            }

            public Integer getState() {
                return state;
            }

            public void setState(Integer state) {
                this.state = state;
            }

            public String getImage() {
                return image;
            }

            public void setImage(String image) {
                this.image = image;
            }

            public Object getCreateTime() {
                return createTime;
            }

            public void setCreateTime(Object createTime) {
                this.createTime = createTime;
            }
        }
    }
}
