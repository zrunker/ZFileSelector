package cc.ibooker.zphotochoose.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cc.ibooker.zphotochoose.R;
import cc.ibooker.zphotochoose.activity.ImageViewPreViewActivity;
import cc.ibooker.zphotochoose.event.PhotoChooseCheckBoxEvent;
import cc.ibooker.zphotochoose.utils.ClickUtil;
import cc.ibooker.zphotochoose.utils.PhotoLoader;

/**
 * 选择图片Adapter
 *
 * @author 邹峰立
 */
public class PhotoChooseAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<String> mDatas;
    private String dirPath;
    private ArrayList<String> selectedSet;
    private int screenWidth = 0;
    private int canChooseNum;

    public PhotoChooseAdapter(Context context, List<String> list, String dirPath, ArrayList<String> selectedSet, int canChooseNum) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mDatas = list;
        this.dirPath = dirPath;
        this.selectedSet = selectedSet;
        this.canChooseNum = canChooseNum;

        // 获取屏幕宽度
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;
        }
    }

    public void reflashData(List<String> list, String dirPath, ArrayList<String> selectedSet, int canChooseNum) {
        this.mDatas = list;
        this.dirPath = dirPath;
        this.selectedSet = selectedSet;
        this.notifyDataSetChanged();
        this.canChooseNum = canChooseNum;
    }

    // 刷新选中图片缓存数据
    public void setSelectedSet(ArrayList<String> selectedSet) {
        this.selectedSet = selectedSet;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.activity_photo_choose_item, viewGroup, false);
            holder.imageView = view.findViewById(R.id.imageview);
            holder.checkBox = view.findViewById(R.id.checkbox);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final String filePath = dirPath + "/" + mDatas.get(i);

        holder.checkBox.setChecked(selectedSet.contains(filePath));
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ClickUtil.isFastClick()) return;
                boolean isChecked = holder.checkBox.isChecked();
                // 发送通讯
                EventBus.getDefault().postSticky(new PhotoChooseCheckBoxEvent(filePath));

                // 上一次选中的数量
                if (selectedSet.size() > canChooseNum && isChecked) {
                    Toast.makeText(context, "只能选择" + canChooseNum + "张图片！", Toast.LENGTH_SHORT).show();
                    holder.checkBox.setChecked(false);
                    // 发送通讯
                    EventBus.getDefault().postSticky(new PhotoChooseCheckBoxEvent(filePath));
                }

            }
        });

        // 图片加载，也可以采用Glide进行加载
//        Glide.with(context)
//                .load(filePath)
//                .override(screenWidth / 3, screenWidth / 3)
//                .into(holder.imageView);
        if (screenWidth > 0) {
            holder.imageView.setMaxHeight(screenWidth / 3);
            holder.imageView.setMaxWidth(screenWidth / 3);
            holder.imageView.setMinimumHeight(screenWidth / 3 - 6);
        }
        PhotoLoader.getInstance(3, PhotoLoader.Type.LIFO)
                .loadImage(filePath, holder.imageView);

        holder.imageView.setColorFilter(null);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// 跳转到图片预览界面
                if (ClickUtil.isFastClick()) return;
                Intent intent = new Intent(context, ImageViewPreViewActivity.class);
                intent.putExtra("filePath", filePath);
                context.startActivity(intent);
            }
        });
        return view;
    }

    private static class ViewHolder {
        ImageView imageView;
        CheckBox checkBox;
    }
}
