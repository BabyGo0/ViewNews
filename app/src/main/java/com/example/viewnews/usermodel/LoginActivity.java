package com.example.viewnews.usermodel;

import androidx.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.viewnews.MainActivity;
import com.example.viewnews.R;
import com.example.viewnews.tools.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.litepal.LitePal;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class LoginActivity extends BaseActivity {

    private EditText userAccount;
    private EditText userPwd;
    private Button loginBtn;

    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userAccount = findViewById(R.id.login_userAccount);
        userPwd = findViewById(R.id.login_pwd);

        Intent intent = getIntent();
        String status = intent.getStringExtra("loginStatus");
        if(status != null) {
            Toast.makeText(LoginActivity.this, status, Toast.LENGTH_SHORT).show();
        }

        loginBtn = findViewById(R.id.login_on);
        registerBtn = findViewById(R.id.login_register);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numId = userAccount.getText().toString();
                String pwd = userPwd.getText().toString();
                if(TextUtils.isEmpty(numId) || TextUtils.isEmpty(pwd)) {
                    Toast.makeText(LoginActivity.this, "You have to fill the text field", Toast.LENGTH_SHORT).show();
                } else {

                    List<UserInfo> userInfos = LitePal.where("userAccount = ?", numId).find(UserInfo.class);
                    System.out.println(userInfos);
                    if(userInfos.size() == 0) {

                        Toast.makeText(LoginActivity.this, "Account dose not exit! Please register first.", Toast.LENGTH_SHORT).show();
                    } else {

                        if(!pwd.equals(userInfos.get(0).getUserPwd())) {
                            Toast.makeText(LoginActivity.this, "Please input the correct password", Toast.LENGTH_SHORT).show();
                        } else {

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("userID", numId);
                            intent.putExtra("userNick", userInfos.get(0).getNickName());
                            intent.putExtra("userSign", userInfos.get(0).getUserSignature());
                            intent.putExtra("imagePath", userInfos.get(0).getImagePath());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);

                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 2:
                if(resultCode == RESULT_OK) {
                    Toast.makeText(LoginActivity.this, data.getStringExtra("register_status"), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        String inputIdText = userAccount.getText().toString();
        save(inputIdText);

    }


    public void save(String inputText) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput("data", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(inputText);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
