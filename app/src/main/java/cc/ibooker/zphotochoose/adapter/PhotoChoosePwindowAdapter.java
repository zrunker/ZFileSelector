package cc.ibooker.zphotochoose.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import cc.ibooker.zphotochoose.R;
import cc.ibooker.zphotochoose.bean.FolderBean;
import cc.ibooker.zphotochoose.viewholder.PhotoChoosePwindowHolder;


/**
 * 选择图片弹窗Adapter
 *
 * @author 邹峰立
 */
public class PhotoChoosePwindowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater inflater;
    private ArrayList<FolderBean> mDatas;
    private String dirPath;

    public PhotoChoosePwindowAdapter(Context context, ArrayList<FolderBean> list, String dirPath) {
        this.inflater = LayoutInflater.from(context);
        this.mDatas = list;
        this.dirPath = dirPath;
    }

    public void reflashData(ArrayList<FolderBean> list, String dirPath) {
        this.mDatas = list;
        this.dirPath = dirPath;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoChoosePwindowHolder(inflater.inflate(R.layout.activity_photo_choose_popuwindow_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((PhotoChoosePwindowHolder) holder).onBind(mDatas.get(position), dirPath);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }
}
