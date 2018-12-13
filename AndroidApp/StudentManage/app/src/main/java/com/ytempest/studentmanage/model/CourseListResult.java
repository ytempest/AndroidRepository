package com.ytempest.studentmanage.model;

import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class CourseListResult extends CommonResult {

    public CourseListResult() {
    }

    /**
     * msg : 成功
     * code : 0
     * data : [{"childCourse":{"id":1,"childCourseId":101001,"courseId":101,"name":"计算机学院软件工程","credit":4,"classTime":"2015-2016学年上半年1-10周","state":1,"createTime":1543916311000}},{"childCourse":{"id":2,"childCourseId":102001,"courseId":102,"name":"计算机学院高等数学","credit":4,"classTime":"2015-2016学年上半年","state":1,"createTime":1543916330000}},{"childCourse":{"id":3,"childCourseId":103001,"courseId":103,"name":"计算机学院数据库原理","credit":3,"classTime":"2015-2016学年上半年","state":1,"createTime":1543916710000}}]
     */
    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * childCourse : {"id":1,"childCourseId":101001,"courseId":101,"name":"计算机学院软件工程","credit":4,"classTime":"2015-2016学年上半年1-10周","state":1,"createTime":1543916311000}
         */

        private ChildCourseBean childCourse;

        public ChildCourseBean getChildCourse() {
            return childCourse;
        }

        public void setChildCourse(ChildCourseBean childCourse) {
            this.childCourse = childCourse;
        }

        public static class ChildCourseBean {
            /**
             * id : 1
             * childCourseId : 101001
             * courseId : 101
             * name : 计算机学院软件工程
             * credit : 4.0
             * classTime : 2015-2016学年上半年1-10周
             * state : 1
             * createTime : 1543916311000
             */

            private int id;
            private int childCourseId;
            private int courseId;
            private String name;
            private double credit;
            private String classTime;
            private int state;
            private long createTime;

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

            public int getCourseId() {
                return courseId;
            }

            public void setCourseId(int courseId) {
                this.courseId = courseId;
            }

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

            public long getCreateTime() {
                return createTime;
            }

            public void setCreateTime(long createTime) {
                this.createTime = createTime;
            }

            @Override
            public String toString() {
                return "ChildCourseBean{" +
                        "id=" + id +
                        ", childCourseId=" + childCourseId +
                        ", courseId=" + courseId +
                        ", name='" + name + '\'' +
                        ", credit=" + credit +
                        ", classTime='" + classTime + '\'' +
                        ", state=" + state +
                        ", createTime=" + createTime +
                        '}';
            }
        }


    }

    @Override
    public String toString() {
        return "CourseListResult{" +
                "msg='" + msg + '\'' +
                ", code=" + code +
                ", data=" + data +
                '}';
    }
}
