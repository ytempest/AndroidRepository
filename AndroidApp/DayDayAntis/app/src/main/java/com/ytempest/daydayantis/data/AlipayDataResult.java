package com.ytempest.daydayantis.data;

/**
 * @author ytempest
 *         Description：使用支付宝进行支付时从后台获取的参数
 */
public class AlipayDataResult extends BaseDataResult {

    /**
     * data : {"payinfo":"_input_charset=\"utf-8\"&app_id=\"1\"&it_b_pay=\"15d\"&notify_url=\"http://v2.ffu365.com/index.php/Api/Gateway/aliPayGateway.html\"&out_trade_no=\"20180403104143808948\"&partner=\"2088612417094800\"&payment_type=\"1\"&seller_id=\"3185016348@qq.com\"&service=\"mobile.securitypay.pay\"&subject=\"金币充值\"&total_fee=\"0.1\"&sign=\"c16zfsu%2FJIO8tWVLGnVk8HPoeS31mv%2B7nX1Ctzcq2GOdGYFIrh40gI7bU8E%2Bp6Gm5mf4sXAOO99raFvJQ1dyy6sol1oCFsGmrRwLOCihFhF2z3C048zLR6AWtyMh%2BkmrmuN4lOLhjIBWbiZBpMEoc4VQYL0gk1XNiHZ%2FGvz9H6k%3D\"&sign_type=\"RSA\""}
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
         * payinfo : _input_charset="utf-8"&app_id="1"&it_b_pay="15d"&notify_url="http://v2.ffu365.com/index.php/Api/Gateway/aliPayGateway.html"&out_trade_no="20180403104143808948"&partner="2088612417094800"&payment_type="1"&seller_id="3185016348@qq.com"&service="mobile.securitypay.pay"&subject="金币充值"&total_fee="0.1"&sign="c16zfsu%2FJIO8tWVLGnVk8HPoeS31mv%2B7nX1Ctzcq2GOdGYFIrh40gI7bU8E%2Bp6Gm5mf4sXAOO99raFvJQ1dyy6sol1oCFsGmrRwLOCihFhF2z3C048zLR6AWtyMh%2BkmrmuN4lOLhjIBWbiZBpMEoc4VQYL0gk1XNiHZ%2FGvz9H6k%3D"&sign_type="RSA"
         */

        private String payinfo;

        public String getPayinfo() {
            return payinfo;
        }

        public void setPayinfo(String payinfo) {
            this.payinfo = payinfo;
        }
    }
}
