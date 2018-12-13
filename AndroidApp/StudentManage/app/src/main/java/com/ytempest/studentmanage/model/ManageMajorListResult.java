package com.ytempest.studentmanage.model;

import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class ManageMajorListResult extends CommonResult {

    private List<DataBean> data;

    public ManageMajorListResult() {
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
         * majorId : 11001
         * departmentId : null
         * name : 软件工程
         * introductions : 软件工程专业
         * createTime : null
         */

        private Object id;
        private int majorId;
        private Object departmentId;
        private String name;
        private String introductions;
        private Object createTime;

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            this.id = id;
        }

        public int getMajorId() {
            return majorId;
        }

        public void setMajorId(int majorId) {
            this.majorId = majorId;
        }

        public Object getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Object departmentId) {
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

        public Object getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Object createTime) {
            this.createTime = createTime;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "id=" + id +
                    ", majorId=" + majorId +
                    ", departmentId=" + departmentId +
                    ", name='" + name + '\'' +
                    ", introductions='" + introductions + '\'' +
                    ", createTime=" + createTime +
                    '}';
        }
    }
}
