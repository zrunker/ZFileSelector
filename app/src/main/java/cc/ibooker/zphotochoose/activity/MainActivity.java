package cc.ibooker.zphotochoose.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import cc.ibooker.zphotochoose.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void enterPhotoChoose(View view) {
        Intent intent = new Intent(this, PhotoChooseActivity.class);
        intent.putExtra("canChooseNum", 3);
        startActivity(intent);
    }
}
