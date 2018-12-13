package com.ytempest.studentmanage.model;

/**
 * @author ytempest
 *         Description：
 */
public class CourseInfoResult extends CommonResult {

    /**
     * data : {"id":1,"childCourseId":101001,"studentId":111,"dailyScore":20,"examScore":50,"finalScore":80,"credit":3.2,"state":1,"createTime":1543916386000,"courseDetail":{"name":"计算机学院软件工程","credit":4,"classTime":"2015-2016学年上半年1-10周","state":1,"courseDto":{"introductions":"介绍","departmentName":"计算机学院"}},"teacherBase":{"teacherId":10000,"teacherName":"朱老师"}}
     */

    private DataBean data;

    public CourseInfoResult() {
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
         * childCourseId : 101001
         * studentId : 111
         * dailyScore : 20
         * examScore : 50
         * finalScore : 80
         * credit : 3.2
         * state : 1
         * createTime : 1543916386000
         * courseDetail : {"name":"计算机学院软件工程","credit":4,"classTime":"2015-2016学年上半年1-10周","state":1,"courseDto":{"introductions":"介绍","departmentName":"计算机学院"}}
         * teacherBase : {"teacherId":10000,"teacherName":"朱老师"}
         */

        private int id;
        private int childCourseId;
        private int studentId;
        private int dailyScore;
        private int examScore;
        private int finalScore;
        private double credit;
        private int state;
        private long createTime;
        private CourseDetailBean courseDetail;
        private TeacherBaseBean teacherBase;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getChildCourseId() {
            return childCourseId;
        }

        public void setChildCourseId(int childCourseId) {
            this.childCourseId = childCourseId;
        }

        public int getStudentId() {
            return studentId;
        }

        public void setStudentId(int studentId) {
            this.studentId = studentId;
        }

        public int getDailyScore() {
            return dailyScore;
        }

        public void setDailyScore(int dailyScore) {
            this.dailyScore = dailyScore;
        }

        public int getExamScore() {
            return examScore;
        }

        public void setExamScore(int examScore) {
            this.examScore = examScore;
        }

        public int getFinalScore() {
            return finalScore;
        }

        public void setFinalScore(int finalScore) {
            this.finalScore = finalScore;
        }

        public double getCredit() {
            return credit;
        }

        public void setCredit(double credit) {
            this.credit = credit;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public CourseDetailBean getCourseDetail() {
            return courseDetail;
        }

        public void setCourseDetail(CourseDetailBean courseDetail) {
            this.courseDetail = courseDetail;
        }

        public TeacherBaseBean getTeacherBase() {
            return teacherBase;
        }

        public void setTeacherBase(TeacherBaseBean teacherBase) {
            this.teacherBase = teacherBase;
        }

        public static class CourseDetailBean {
            /**
             * name : 计算机学院软件工程
             * credit : 4.0
             * classTime : 2015-2016学年上半年1-10周
             * state : 1
             * courseDto : {"introductions":"介绍","departmentName":"计算机学院"}
             */

            private String name;
            private double credit;
            private String classTime;
            private int state;
            private CourseDtoBean courseDto;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public double getCredit() {
                return credit;
            }

            public void setCredit(double credit) {
                this.credit = credit;
            }

            public String getClassTime() {
                return classTime;
            }

            public void setClassTime(String classTime) {
                this.classTime = classTime;
            }

            public int getState() {
                return state;
            }

            public void setState(int state) {
                this.state = state;
            }

            public CourseDtoBean getCourseDto() {
                return courseDto;
            }

            public void setCourseDto(CourseDtoBean courseDto) {
                this.courseDto = courseDto;
            }

            public static class CourseDtoBean {
                /**
                 * introductions : 介绍
                 * departmentName : 计算机学院
                 */

                private String introductions;
                private String departmentName;

                public String getIntroductions() {
                    return introductions;
                }

                public void setIntroductions(String introductions) {
                    this.introductions = introductions;
                }

                public String getDepartmentName() {
                    return departmentName;
                }

                public void setDepartmentName(String departmentName) {
                    this.departmentName = departmentName;
                }
            }
        }

        public static class TeacherBaseBean {
            /**
             * teacherId : 10000
             * teacherName : 朱老师
             */

            private int teacherId;
            private String teacherName;

            public int getTeacherId() {
                return teacherId;
            }

            public void setTeacherId(int teacherId) {
                this.teacherId = teacherId;
            }

            public String getTeacherName() {
                return teacherName;
            }

            public void setTeacherName(String teacherName) {
                this.teacherName = teacherName;
            }
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "id=" + id +
                    ", childCourseId=" + childCourseId +
                    ", studentId=" + studentId +
                    ", dailyScore=" + dailyScore +
                    ", examScore=" + examScore +
                    ", finalScore=" + finalScore +
                    ", credit=" + credit +
                    ", state=" + state +
                    ", createTime=" + createTime +
                    ", courseDetail=" + courseDetail +
                    ", teacherBase=" + teacherBase +
                    '}';
        }
    }
}
