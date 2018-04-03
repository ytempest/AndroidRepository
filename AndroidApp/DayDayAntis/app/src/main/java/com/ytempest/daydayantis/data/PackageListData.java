package com.ytempest.daydayantis.data;

import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class PackageListData extends BaseDataResult {

    /**
     * data : {"meal":[{"meal_name":"2000金币","meal_coin":"2000","meal_desc":"建议工人选择该套餐"},{"meal_name":"3000金币","meal_coin":"3000","meal_desc":"建议团队选择该套餐"},{"meal_name":"5000金币","meal_coin":"5000","meal_desc":"建议工程老板选择该套餐"}]}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<MealBean> meal;

        public List<MealBean> getMeal() {
            return meal;
        }

        public void setMeal(List<MealBean> meal) {
            this.meal = meal;
        }

        public static class MealBean {
            /**
             * meal_name : 2000金币
             * meal_coin : 2000
             * meal_desc : 建议工人选择该套餐
             */

            private String meal_name;
            private String meal_coin;
            private String meal_desc;

            public String getMeal_name() {
                return meal_name;
            }

            public void setMeal_name(String meal_name) {
                this.meal_name = meal_name;
            }

            public String getMeal_coin() {
                return meal_coin;
            }

            public void setMeal_coin(String meal_coin) {
                this.meal_coin = meal_coin;
            }

            public String getMeal_desc() {
                return meal_desc;
            }

            public void setMeal_desc(String meal_desc) {
                this.meal_desc = meal_desc;
            }
        }
    }
}
