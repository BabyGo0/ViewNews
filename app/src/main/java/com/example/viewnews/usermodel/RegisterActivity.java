package com.example.viewnews.usermodel;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.example.viewnews.R;
import com.example.viewnews.tools.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.litepal.LitePal;

import java.util.List;

public class RegisterActivity extends BaseActivity {

    private EditText reg_userAccount;
    private EditText reg_userPwd;
    private EditText reg_confirm_userPwd;
    private Button registerBtn;
    private FirebaseAuth mAuth;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_register);
        reg_userAccount = findViewById(R.id.register_userAccount);
        reg_userPwd = findViewById(R.id.register_pwd);
        reg_confirm_userPwd = findViewById(R.id.confirm_pwd);
        registerBtn = findViewById(R.id.register_click);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 首先验证输入是否为空
                String userId = reg_userAccount.getText().toString();
                String userPwd = reg_userPwd.getText().toString();
                String secondPwd = reg_confirm_userPwd.getText().toString();
                List<UserInfo> all = LitePal.findAll(UserInfo.class);
                if(TextUtils.isEmpty(userId) || TextUtils.isEmpty(userPwd) || TextUtils.isEmpty(secondPwd)) {
                    // 判断字符串是否为null或者""
                    Toast.makeText(RegisterActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    // 判断两次输入的密码是否匹配，匹配则写入数据库，并且结束当前活动，自动返回登录界面
                    if(userPwd.equals(secondPwd)) {
                        List<UserInfo> userInfoList = LitePal.where("userAccount = ?", userId).find(UserInfo.class);
                        if(userInfoList.size() > 0) {
                            Toast.makeText(RegisterActivity.this, "当前账号已被注册，请重新输入账号", Toast.LENGTH_SHORT).show();
                        } else {
                            mAuth.createUserWithEmailAndPassword(userId, secondPwd)
                                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d(TAG, "createUserWithEmail:success");
                                                FirebaseUser user = mAuth.getCurrentUser();
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Log.w("createUserWithEmail", task.getException());
                                                Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            UserInfo userInfo = new UserInfo();
                            userInfo.setUserAccount(userId);
                            userInfo.setUserPwd(secondPwd);
                            userInfo.setUserBirthDay("待完善");
                            userInfo.setUserSex("待完善");
                            userInfo.setUserSignature("这个人很懒，TA什么也没留下。");
                            // 给其设置一个用户名
                            userInfo.setNickName("用户" + (all.size() + 1));
                            userInfo.save();
                            System.out.println(userInfo);
                            Intent intent = new Intent();
                            intent.putExtra("register_status", "注册成功");
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Please confirm the passwords are same?", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
