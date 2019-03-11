package cc.ibooker.zphotochoose.zview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;

import cc.ibooker.zphotochoose.R;
import cc.ibooker.zphotochoose.adapter.PhotoChoosePwindowAdapter;
import cc.ibooker.zphotochoose.bean.FolderBean;
import cc.ibooker.zpopupwindowlib.ZPopupWindow;

/**
 * 图片选择弹窗
 *
 * @author 邹峰立
 */
public class PhotoChoosePopuwindow extends ZPopupWindow {
    private Context context;
    private View view;
    private RecyclerView recyclerView;
    private PhotoChoosePwindowAdapter photoChoosePwindowAdapter;

    public PhotoChoosePopuwindow(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected View generateCustomView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.activity_photo_choose_popuwindow, null);
        initView();
        return view;
    }

    // 初始化控件
    private void initView() {
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
    }

    // 自定义setAdapter
    public void setPhotoChoosePwindowAdapter(ArrayList<FolderBean> list, String dirPath) {
        if (photoChoosePwindowAdapter == null) {
            photoChoosePwindowAdapter = new PhotoChoosePwindowAdapter(context, list, dirPath);
            recyclerView.setAdapter(photoChoosePwindowAdapter);
        } else {
            photoChoosePwindowAdapter.reflashData(list, dirPath);
        }
    }
}
