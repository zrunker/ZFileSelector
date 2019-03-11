package cc.ibooker.zphotochoose.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import cc.ibooker.zphotochoose.R;
import cc.ibooker.zphotochoose.bean.FolderBean;
import cc.ibooker.zphotochoose.event.PhotoChooseFolderEvent;

/**
 * 选择图片ViewHolder
 *
 * @author 邹峰立
 */
public class PhotoChoosePwindowHolder extends RecyclerView.ViewHolder {
    private ImageView imageView;
    private TextView dirNameTv, photoNumTv;
    private RadioButton radioButton;

    public PhotoChoosePwindowHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageview);
        dirNameTv = itemView.findViewById(R.id.tv_dir_name);
        photoNumTv = itemView.findViewById(R.id.tv_photo_num);
        radioButton = itemView.findViewById(R.id.rbtn);
    }

    public void onBind(final FolderBean data, String dirPath) {
        if (data != null) {
            Glide.with(itemView.getContext())
                    .load(data.getFirstImgPath())
                    .into(imageView);
            dirNameTv.setText(data.getName());
            photoNumTv.setText(data.getCount() + "张");
            radioButton.setChecked(data.getDir().equals(dirPath));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    radioButton.toggle();
                    // 发送通讯
                    EventBus.getDefault().postSticky(new PhotoChooseFolderEvent(data));
                }
            });
        }
    }
}
