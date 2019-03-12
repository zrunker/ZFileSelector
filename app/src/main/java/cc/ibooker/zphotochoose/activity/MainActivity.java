package cc.ibooker.zphotochoose.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import cc.ibooker.zphotochoose.R;

public class MainActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.tv);
    }

    public void enterPhotoChoose(View view) {
        Intent intent = new Intent(this, PhotoChooseActivity.class);
        intent.putExtra("canChooseNum", 3);
        startActivityForResult(intent, 222);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 222 && data != null) {// 接收返回图片信息
                ArrayList<String> datas = data.getStringArrayListExtra("data");
                textView.setText(datas.toString());
            }
        }
    }
}
