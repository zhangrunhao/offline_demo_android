package com.example.offlinedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.expamle.myfirestapp.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void  sendMessage(View view) {
        // 首先要使用Context参数, 因为Activity类是Context类的子类.
        // Class参数是app组件, 也就是传入intent的地方, 换句话, 这是这个activity开始的地方
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        EditText editText = findViewById(R.id.editText);
        String message = editText.getText().toString();
        // intent把值添加进去
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
