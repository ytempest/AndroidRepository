package com.ytempest.studentmanage.model;

/**
 * @author ytempest
 *         Description：
 */
public class TeacherInfoResult extends CommonResult {

    /**
     * data : {"id":2,"teacherId":10001,"password":"456","name":"马老师","sex":"female","departmentId":12,"officeAddress":"数学院202","identity":"讲师","politicalStatus":"共产党员","nationality":"汉族","enrollmentDate":1544637130000,"phone":"16060606060","address":"教师公寓1栋","state":1,"image":"/lklk/oko/lll.jpg","createTime":1543893809000,"departmentName":"数学与统计学院"}
     */

    private DataBean data;

    public TeacherInfoResult() {
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 2
         * teacherId : 10001
         * password : 456
         * name : 马老师
         * sex : female
         * departmentId : 12
         * officeAddress : 数学院202
         * identity : 讲师
         * politicalStatus : 共产党员
         * nationality : 汉族
         * enrollmentDate : 1544637130000
         * phone : 16060606060
         * address : 教师公寓1栋
         * state : 1
         * image : /lklk/oko/lll.jpg
         * createTime : 1543893809000
         * departmentName : 数学与统计学院
         */

        private int id;
        private int teacherId;
        private String password;
        private String name;
        private String sex;
        private int departmentId;
        private String officeAddress;
        private String identity;
        private String politicalStatus;
        private String nationality;
        private long enrollmentDate;
        private String phone;
        private String address;
        private int state;
        private String image;
        private long createTime;
        private String departmentName;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(int teacherId) {
            this.teacherId = teacherId;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
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

        public int getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(int departmentId) {
            this.departmentId = departmentId;
        }

        public String getOfficeAddress() {
            return officeAddress;
        }

        public void setOfficeAddress(String officeAddress) {
            this.officeAddress = officeAddress;
        }

        public String getIdentity() {
            return identity;
        }

        public void setIdentity(String identity) {
            this.identity = identity;
        }

        public String getPoliticalStatus() {
            return politicalStatus;
        }

        public void setPoliticalStatus(String politicalStatus) {
            this.politicalStatus = politicalStatus;
        }

        public String getNationality() {
            return nationality;
        }

        public void setNationality(String nationality) {
            this.nationality = nationality;
        }

        public long getEnrollmentDate() {
            return enrollmentDate;
        }

        public void setEnrollmentDate(long enrollmentDate) {
            this.enrollmentDate = enrollmentDate;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
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

        @Override
        public String toString() {
            return "DataBean{" +
                    "id=" + id +
                    ", teacherId=" + teacherId +
                    ", password='" + password + '\'' +
                    ", name='" + name + '\'' +
                    ", sex='" + sex + '\'' +
                    ", departmentId=" + departmentId +
                    ", officeAddress='" + officeAddress + '\'' +
                    ", identity='" + identity + '\'' +
                    ", politicalStatus='" + politicalStatus + '\'' +
                    ", nationality='" + nationality + '\'' +
                    ", enrollmentDate=" + enrollmentDate +
                    ", phone='" + phone + '\'' +
                    ", address='" + address + '\'' +
                    ", state=" + state +
                    ", image='" + image + '\'' +
                    ", createTime=" + createTime +
                    ", departmentName='" + departmentName + '\'' +
                    '}';
        }
    }
}
