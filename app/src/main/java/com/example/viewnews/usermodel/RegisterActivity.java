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
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        reg_userAccount = findViewById(R.id.register_userAccount);
        reg_userPwd = findViewById(R.id.register_pwd);
        reg_confirm_userPwd = findViewById(R.id.confirm_pwd);
        registerBtn = findViewById(R.id.register_click);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = reg_userAccount.getText().toString();
                String userPwd = reg_userPwd.getText().toString();
                String secondPwd = reg_confirm_userPwd.getText().toString();
                List<UserInfo> all = LitePal.findAll(UserInfo.class);
                if(TextUtils.isEmpty(userId) || TextUtils.isEmpty(userPwd) || TextUtils.isEmpty(secondPwd)) {

                    Toast.makeText(RegisterActivity.this, "You have to fill the text field ", Toast.LENGTH_SHORT).show();
                } else {

                    if(userPwd.equals(secondPwd)) {
                        List<UserInfo> userInfoList = LitePal.where("userAccount = ?", userId).find(UserInfo.class);
                        if(userInfoList.size() > 0) {
                            Toast.makeText(RegisterActivity.this, "The account is registered, please input another one.", Toast.LENGTH_SHORT).show();
                        } else {
                            UserInfo userInfo = new UserInfo();
                            userInfo.setUserAccount(userId);
                            userInfo.setUserPwd(secondPwd);
                            userInfo.setUserBirthDay("Wait to fill");
                            userInfo.setUserSex("Wait to fill");
                            userInfo.setUserSignature("There's nothing here");
                            userInfo.setNickName("User" + (all.size() + 1));
                            userInfo.save();
                            System.out.println(userInfo);
                            Intent intent = new Intent();
                            intent.putExtra("register_status", "Register successfully!");
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
