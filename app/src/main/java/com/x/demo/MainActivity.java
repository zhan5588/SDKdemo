package com.x.demo;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.bt.sdk.BTSDKManager;
import com.bt.sdk.listener.LoginErrorMsg;
import com.bt.sdk.listener.LogincallBack;
import com.bt.sdk.listener.OnLoginListener;
import com.bt.sdk.listener.OnPaymentListener;
import com.bt.sdk.listener.OnSwitchAccountListener;
import com.bt.sdk.listener.PaymentCallbackInfo;
import com.bt.sdk.listener.PaymentErrorMsg;
import com.bt.sdk.net.Constants;
import com.bt.sdk.util.MResource;
import com.bt.sdk.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements View.OnClickListener{


    private BTSDKManager moxsdkmanager;
    private EditText sdkChargeEt;

    private Button sdkLoginBtn,sdkChargeBtn,sdkSetroleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //取消标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(MResource.getIdByName(getApplication(), Constants.Resouce.LAYOUT, "activity_sdk"));
        sdkLoginBtn = (Button) findViewById(MResource.getIdByName(getApplication(), Constants.Resouce.ID, "sdk_login_btn"));
        sdkChargeEt = (EditText) findViewById(MResource.getIdByName(getApplication(), "id", "sdk_charge_et"));
        sdkChargeBtn = (Button) findViewById(MResource.getIdByName(getApplication(), "id", "sdk_charge_btn"));
        sdkSetroleBtn = (Button) findViewById(MResource.getIdByName(getApplication(), "id", "sdk_setrole_btn"));


        moxsdkmanager = BTSDKManager.getInstance(this);
        initSdkListener();
        sdkLoginBtn.setOnClickListener(this);
        sdkChargeBtn.setOnClickListener(this);
        sdkSetroleBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(sdkLoginBtn.getId() == v.getId()){ //登录
            moxsdkmanager.showLogin(MainActivity.this, moxsdkmanager.getOnLoginListener(),moxsdkmanager.getOnSwitchAccountListener() );
            return;
        }
        if(sdkChargeBtn.getId() == v.getId()){ //充值
            String money = sdkChargeEt.getText().toString().trim();
            if ( !(Float.parseFloat(money) >= 0.01f) ){
                Utils.showToastCenter(MainActivity.this,"最低充值0.01元：");
                return;
            }
            moxsdkmanager.showPay(MainActivity.this, "roleid", money, sdkChargeEt.getText().toString(), "魔神", "金币", "扩展参数", new OnPaymentListener() {
                @Override
                public void paymentSuccess(PaymentCallbackInfo callbackInfo) {
                    Utils.showToastCenter(MainActivity.this,"充值金额数：" + callbackInfo.money + "，消息提示：" + callbackInfo.msg);
                }
                @Override
                public void paymentError(PaymentErrorMsg errorMsg) {
                    Utils.showToastCenter(MainActivity.this,"充值失败：code:" + errorMsg.code + "，ErrorMsg:" + errorMsg.msg
                            + "，预充值的金额：" + errorMsg.money);
                }
            });
            return;
        }
        if(sdkSetroleBtn.getId() == v.getId()){ //设置角色
            JSONObject json;
            try {
                json = new JSONObject("{\"json\":\"123456465\"}");
                moxsdkmanager.setRoleData(MainActivity.this,"py_Roleid", "py_rolename", "py_level", "1", "彭洋一区", json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    //需要设置游戏全局的登录监听和切换用户的监听  相应的游戏做的处理应写入回调里
    private void initSdkListener(){
        OnLoginListener loginListener= new OnLoginListener() {
            @Override
            public void loginSuccess(LogincallBack logincallback) {
                moxsdkmanager.isShowLogin=true;
//                Utils.showToastCenter(MainActivity.this, "sign:" + logincallback.sign + " logintime:"
//                        + logincallback.logintime + " username:" + logincallback.username);
                moxsdkmanager.showFloatView();
            }

            @Override
            public void loginError(LoginErrorMsg errorMsg) {
                moxsdkmanager.isShowLogin=false;
                Utils.showToastCenter(MainActivity.this, "登录失败,msg:" + errorMsg.msg + ",code:" + errorMsg.code);
            }
        };
        OnSwitchAccountListener onSwitchAccountListener=new OnSwitchAccountListener() {
            @Override
            public void switchAccount() { //切换账号
                Utils.showToastCenter(MainActivity.this,"你已经退出登录");
                moxsdkmanager.hideFloatView();//隐藏悬浮球

                //切换用户的时候自动调用登录
                moxsdkmanager.showLogin(MainActivity.this, moxsdkmanager.getOnLoginListener(),moxsdkmanager.getOnSwitchAccountListener() );
            }
        };
        moxsdkmanager.setOnLoginListener(loginListener);
        moxsdkmanager.setOnSwitchAccountListener(onSwitchAccountListener);
    }
    @Override
    protected void onResume() {
        super.onResume();
        moxsdkmanager.showFloatView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        moxsdkmanager.hideFloatView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        moxsdkmanager.recycle(this);

    }

    /**
     * 横竖屏切换，如果游戏中不存在切换，可忽略
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            // Toast.makeText(getApplicationContext(), "横屏", Toast.LENGTH_SHORT).show();
            BTSDKManager.initDisplayMetrics(this);

        }else{
            // Toast.makeText(getApplicationContext(), "竖屏", Toast.LENGTH_SHORT).show();
            BTSDKManager.initDisplayMetrics(this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
       /* if (Utils.isFirstClick()){
            return false;
        }*/
        if(keyCode == KeyEvent.KEYCODE_BACK){
            // moxsdkmanager.showExitDialog(this);
            BTSDKManager.getInstance(this).showExitDialog(this);
        }
        //如果需要在执行
        return true;
    }
}
