package com.ytempest.studentmanage.model;

import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class ManageDepartmentListResult extends CommonResult {

    private List<DataBean> data;

    public ManageDepartmentListResult() {
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 1
         * departmentId : 11
         * name : 计算机学院
         * introductions : 这是计算机学院的简介
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

        @Override
        public String toString() {
            return "DataBean{" +
                    "id=" + id +
                    ", departmentId=" + departmentId +
                    ", name='" + name + '\'' +
                    ", introductions='" + introductions + '\'' +
                    ", createTime=" + createTime +
                    '}';
        }
    }
}
