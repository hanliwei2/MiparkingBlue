package com.sziton.miparking.activity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sziton.miparking.R;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.daoimpl.PersonalDaoImpl;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.utils.DialogUtil;
import com.sziton.miparking.utils.EncryptUtil;
import com.sziton.miparking.utils.Paths;
import com.sziton.miparking.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fwj on 2017/11/29.
 */

public class NickNameActivity extends Activity implements View.OnClickListener{
    private RelativeLayout backRL;
    private EditText nickNameET;
    private TextView saveTV;
    private MySharedPreferences mySharedPreferences;
    private String nickNameValue;
    private Dialog loadingDialog;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DialogUtil.closeDialog(loadingDialog);
            switch(msg.what){
                case Constants.PERSONAL_REQUEST_SUCCESS:
                    JSONObject jsonObject= (JSONObject) msg.obj;
                    try {
                        String success=jsonObject.getString("Success");
                        if(success.equals("True")){
                            //修改成功
                            mySharedPreferences.setStringValue(Constants.REGISTER_NAME_DEFAULT_KEY,nickNameValue);//用户名称

                            ToastUtil.shortToast(NickNameActivity.this,"修改成功！");
                            finish();
                        }else{
                            //修改失败
                            String errorMessage=jsonObject.getString("ErrorMessage");
                            ToastUtil.shortToast(NickNameActivity.this,errorMessage+"！");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case Constants.PERSONAL_REQUEST_FAILURE:
                    ToastUtil.shortToast(NickNameActivity.this,NickNameActivity.this.getResources().getString(R.string.internet_error_text));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);
        initView();
        initData();
    }

    private void initView(){
        backRL= (RelativeLayout) findViewById(R.id.nickNameBackRL);
        nickNameET= (EditText) findViewById(R.id.nickNameET);
        saveTV= (TextView) findViewById(R.id.nickNameSaveTV);

        backRL.setOnClickListener(this);
        saveTV.setOnClickListener(this);
    }

    private void initData(){
        mySharedPreferences= MySharedPreferences.getInstance(this);
        //打开修改昵称页面时就显示自己的昵称
        if(!TextUtils.isEmpty(mySharedPreferences.getStringValue(Constants.REGISTER_NAME_DEFAULT_KEY))){
            nickNameET.setText(mySharedPreferences.getStringValue(Constants.REGISTER_NAME_DEFAULT_KEY));
            nickNameET.setSelection(mySharedPreferences.getStringValue(Constants.REGISTER_NAME_DEFAULT_KEY).length());
        }
    }

    /**
     * 返回
     */
    private void handleBack(){
        finish();
    }

    /**
     * 保存
     */
    private void handleSave(){
        String url= Paths.appUrl;
        //以下是接口需要的参数
        String timestamp= EncryptUtil.getTimestamp();
        String signatureNonce=EncryptUtil.getSignatureNonce();
        String action= Constants.PERSONAL_ACTION;

        String uid=mySharedPreferences.getStringValue(Constants.REGISTER_PHONE);
        String nickNameKey=Constants.PERSONAL_NICKNAME_KEY;//map中昵称的key
        nickNameValue=nickNameET.getText().toString();//填写的昵称

        //通过签名算法得到的Signature
        Map<String,Object> paramsMap=new HashMap<>();
        paramsMap.put("Timestamp",timestamp);
        paramsMap.put("SignatureNonce",signatureNonce);
        paramsMap.put("Action",action);
        paramsMap.put("Uid",uid);
        paramsMap.put(nickNameKey,nickNameValue);
        String signature=EncryptUtil.getSignature(paramsMap);

        if(TextUtils.isEmpty(nickNameValue)){
            //不符合要求,return掉
            ToastUtil.shortToast(this,"昵称不能为空！");
            return;
        }else{
            //符合要求，提交服务器
            loadingDialog= DialogUtil.createLoadingDialog(this);
            PersonalDaoImpl personalDaoImpl=new PersonalDaoImpl();
            personalDaoImpl.postPersonal(url,signature,timestamp,signatureNonce,action,uid,nickNameKey,nickNameValue,mHandler);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nickNameBackRL:
                handleBack();
                break;

            case R.id.nickNameSaveTV:
                handleSave();
                break;
        }
    }
}
