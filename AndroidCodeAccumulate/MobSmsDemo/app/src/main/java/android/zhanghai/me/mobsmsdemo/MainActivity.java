package android.zhanghai.me.mobsmsdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText mEditText;
    private EventHandler mEventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = findViewById(R.id.et_phone);

        initEventHandler();
    }

    private void initEventHandler() {
        mEventHandler = new EventHandler() {
            /**
             * 当点击了发送验证码和验证验证码就会调用这个方法
             * @param event 执行事件的类型，如发送验证码是一个事件，验证验证码也是一个事件
             * @param result 事件执行的结果，SMSSDK.RESULT_COMPLETE表示成功，SMSSDK.RESULT_ERROR表示失败
             * @param data
             */
            @Override
            public void afterEvent(int event, int result, Object data) {
                // 1、表示事件执行成功
                if (result == SMSSDK.RESULT_COMPLETE) {
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        // 1.1、提交验证码成功
                        // 返回的data是一个HashMap<String, Object>，保存的key=value数据为：country=86、phone=13435065579
                        //
                        HashMap<String, Object> dataMap = (HashMap<String, Object>) data;
                        final String country = (String) dataMap.get("country");
                        final String phone = (String) dataMap.get("phone");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "验证成功:" +
                                        country + "-" + phone, Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        // 1.2、这里表示成功发送验证码
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // 1.3、其他事件
                        Log.e(TAG, "afterEvent: 其他事件");

                    }
                } else {
                    // 2、表示事件执行失败，失败的原因如下：

                    // 如果result为SMSSDK.RESULT_ERROR，这data为Throwable类型，这个需要注意
                    Throwable throwable = (Throwable) data;
                    if (throwable instanceof UnknownHostException) {
                        // 1、这里是没有网络产生的异常（如果没网，但是没填验证码，验证时不会产生这个异常）
                        // 如果发送验证码时没有网络就会走到这里
                        // 如果填写了验证码或者没有填写，验证的时候没有网络也会走到这里
                        // 如果是以上两种情况就会出现 UnknownHostException异常
                        Log.e(TAG, "afterEvent: 这个异常是UnknownHostException类型");
                        // 打印错误栈
                        throwable.printStackTrace();

                    } else {
                        // 2、来到这里就表示网络正常，但是存在就其他异常情况
                        // 第一种异常：{"httpStatus":400,"status":468,"error":"The user submits the validation verification code error."}：这个是在有网时验证码错误的异常数据
                        // 第二种异常：{"status":"466"}：这个是没有填写验证码的异常数据
                        try {
                            JSONObject jsonObj = new JSONObject(throwable.getMessage());
                            final String statusCode = jsonObj.optString("status");
                            // 第一种异常
                            if ("468".equals(statusCode)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else if ("466".equals(statusCode)) {
                                // 第二种异常
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "请填写验证码", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        };

        SMSSDK.registerEventHandler(mEventHandler);
    }

    public void sendCode(View view) {
        //获取验证码，获取验证码之前需要检测网络是否可用，防止后面出现异常
        SMSSDK.getVerificationCode("86", "13435065579");
    }

    public void verify(View view) {
        // 最好验证的时候检测网络是否可用，以减少后面的代码量
        String verifyCode = mEditText.getText().toString();
        // 第二个参数是国际区号，大陆的是86
        SMSSDK.submitVerificationCode("86", "13435065579", verifyCode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(mEventHandler);
    }
}
