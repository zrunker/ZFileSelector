package cc.ibooker.zphotochoose.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import cc.ibooker.zimageviewlib.ScaleImageView;
import cc.ibooker.zphotochoose.R;
import cc.ibooker.zphotochoose.utils.ClickUtil;

/**
 * 图片预览Activity
 *
 * @author 邹峰立
 */
public class ImageViewPreViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview_preview);

        String filePath = getIntent().getStringExtra("filePath");
        TextView backTv = findViewById(R.id.tv_back);
        backTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ClickUtil.isFastClick()) return;
                finish();
            }
        });

        ScaleImageView scaleImageView = findViewById(R.id.scaleimageview);
        scaleImageView.setOnMyClickListener(new ScaleImageView.OnMyClickListener() {
            @Override
            public void onMyClick(View view) {
                if (ClickUtil.isFastClick()) return;
                finish();
            }
        });

        if (TextUtils.isEmpty(filePath))
            filePath = getIntent().getStringExtra("filePath");
        Glide.with(this)
                .load(filePath)
                .into(scaleImageView);
    }
}
