package com.ytempest.studentmanage.model;

import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class ManageTeacherListResult extends CommonResult {

    private List<DataBean> data;

    public ManageTeacherListResult() {
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
         * teacherId : 10000
         * password : null
         * name : 朱老师
         * sex : null
         * departmentId : null
         * officeAddress : null
         * identity : 教授
         * politicalStatus : null
         * nationality : null
         * enrollmentDate : null
         * phone : null
         * address : null
         * state : null
         * image : /teacher/teacher.png
         * createTime : null
         */

        private Object id;
        private int teacherId;
        private Object password;
        private String name;
        private Object sex;
        private Object departmentId;
        private Object officeAddress;
        private String identity;
        private Object politicalStatus;
        private Object nationality;
        private Object enrollmentDate;
        private Object phone;
        private Object address;
        private Object state;
        private String image;
        private Object createTime;

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            this.id = id;
        }

        public int getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(int teacherId) {
            this.teacherId = teacherId;
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

        public Object getOfficeAddress() {
            return officeAddress;
        }

        public void setOfficeAddress(Object officeAddress) {
            this.officeAddress = officeAddress;
        }

        public String getIdentity() {
            return identity;
        }

        public void setIdentity(String identity) {
            this.identity = identity;
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

        public Object getPhone() {
            return phone;
        }

        public void setPhone(Object phone) {
            this.phone = phone;
        }

        public Object getAddress() {
            return address;
        }

        public void setAddress(Object address) {
            this.address = address;
        }

        public Object getState() {
            return state;
        }

        public void setState(Object state) {
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
