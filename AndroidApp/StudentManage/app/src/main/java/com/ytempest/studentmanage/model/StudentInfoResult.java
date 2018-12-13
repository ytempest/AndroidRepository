package com.ytempest.studentmanage.model;

/**
 * @author ytempest
 *         Description：
 */
public class StudentInfoResult extends CommonResult {

    /**
     * data : {"id":1,"studentId":111,"password":"123","name":"simon","sex":"male","departmentId":11,"majorId":11001,"grade":2015,"classId":1511001,"politicalStatus":"共产党员","nationality":"汉","enrollmentDate":1543852800000,"phone":"123465","address":"hello","state":1,"image":"/kk/m.jpg","createTime":1543455280000,"departmentName":"计算机学院","majorName":"软件工程","className":"2015级软件工程专业1班"}
     */

    private DataBean data;

    public StudentInfoResult() {
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
         * studentId : 111
         * password : 123
         * name : simon
         * sex : male
         * departmentId : 11
         * majorId : 11001
         * grade : 2015
         * classId : 1511001
         * politicalStatus : 共产党员
         * nationality : 汉
         * enrollmentDate : 1543852800000
         * phone : 123465
         * address : hello
         * state : 1
         * image : /kk/m.jpg
         * createTime : 1543455280000
         * departmentName : 计算机学院
         * majorName : 软件工程
         * className : 2015级软件工程专业1班
         */

        private int id;
        private int studentId;
        private String password;
        private String name;
        private String sex;
        private int departmentId;
        private int majorId;
        private int grade;
        private int classId;
        private String politicalStatus;
        private String nationality;
        private long enrollmentDate;
        private String phone;
        private String address;
        private int state;
        private String image;
        private long createTime;
        private String departmentName;
        private String majorName;
        private String className;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getStudentId() {
            return studentId;
        }

        public void setStudentId(int studentId) {
            this.studentId = studentId;
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

        public int getClassId() {
            return classId;
        }

        public void setClassId(int classId) {
            this.classId = classId;
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

        public String getMajorName() {
            return majorName;
        }

        public void setMajorName(String majorName) {
            this.majorName = majorName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "id=" + id +
                    ", studentId=" + studentId +
                    ", password='" + password + '\'' +
                    ", name='" + name + '\'' +
                    ", sex='" + sex + '\'' +
                    ", departmentId=" + departmentId +
                    ", majorId=" + majorId +
                    ", grade=" + grade +
                    ", classId=" + classId +
                    ", politicalStatus='" + politicalStatus + '\'' +
                    ", nationality='" + nationality + '\'' +
                    ", enrollmentDate=" + enrollmentDate +
                    ", phone='" + phone + '\'' +
                    ", address='" + address + '\'' +
                    ", state=" + state +
                    ", image='" + image + '\'' +
                    ", createTime=" + createTime +
                    ", departmentName='" + departmentName + '\'' +
                    ", majorName='" + majorName + '\'' +
                    ", className='" + className + '\'' +
                    '}';
        }
    }


}
