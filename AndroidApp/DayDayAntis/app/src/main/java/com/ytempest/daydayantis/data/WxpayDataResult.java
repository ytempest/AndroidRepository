package com.ytempest.daydayantis.data;

/**
 * @author ytempest
 *         Description：使用微信进行支付时从后台获取的参数
 */
public class WxpayDataResult extends BaseDataResult {

    /**
     * data : {"payinfo":{"appid":"wxa8080d15a32e2ff7","noncestr":"Oj1d9lcxKGNpgaUWxXEFHoo8q4YK9uyM","partnerid":"1270695501","prepayid":"wx2018040310234551c88da4960334998394","timestamp":1522722225,"sign":"47C5BCA8B326BEA66E5B83D606B01BE9","package_value":"Sign=WXPay"}}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * payinfo : {"appid":"wxa8080d15a32e2ff7","noncestr":"Oj1d9lcxKGNpgaUWxXEFHoo8q4YK9uyM","partnerid":"1270695501","prepayid":"wx2018040310234551c88da4960334998394","timestamp":1522722225,"sign":"47C5BCA8B326BEA66E5B83D606B01BE9","package_value":"Sign=WXPay"}
         */

        private PayinfoBean payinfo;

        public PayinfoBean getPayinfo() {
            return payinfo;
        }

        public void setPayinfo(PayinfoBean payinfo) {
            this.payinfo = payinfo;
        }

        public static class PayinfoBean {
            /**
             * appid : wxa8080d15a32e2ff7
             * noncestr : Oj1d9lcxKGNpgaUWxXEFHoo8q4YK9uyM
             * partnerid : 1270695501
             * prepayid : wx2018040310234551c88da4960334998394
             * timestamp : 1522722225
             * sign : 47C5BCA8B326BEA66E5B83D606B01BE9
             * package_value : Sign=WXPay
             */

            private String appid;
            private String noncestr;
            private String partnerid;
            private String prepayid;
            private int timestamp;
            private String sign;
            private String package_value;

            public String getAppid() {
                return appid;
            }

            public void setAppid(String appid) {
                this.appid = appid;
            }

            public String getNoncestr() {
                return noncestr;
            }

            public void setNoncestr(String noncestr) {
                this.noncestr = noncestr;
            }

            public String getPartnerid() {
                return partnerid;
            }

            public void setPartnerid(String partnerid) {
                this.partnerid = partnerid;
            }

            public String getPrepayid() {
                return prepayid;
            }

            public void setPrepayid(String prepayid) {
                this.prepayid = prepayid;
            }

            public int getTimestamp() {
                return timestamp;
            }

            public void setTimestamp(int timestamp) {
                this.timestamp = timestamp;
            }

            public String getSign() {
                return sign;
            }

            public void setSign(String sign) {
                this.sign = sign;
            }

            public String getPackage_value() {
                return package_value;
            }

            public void setPackage_value(String package_value) {
                this.package_value = package_value;
            }
        }
    }
}
