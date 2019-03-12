package cc.ibooker.zphotochoose.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cc.ibooker.zphotochoose.R;
import cc.ibooker.zphotochoose.adapter.PhotoChooseAdapter;
import cc.ibooker.zphotochoose.bean.FolderBean;
import cc.ibooker.zphotochoose.bean.FolderData;
import cc.ibooker.zphotochoose.event.PhotoChooseCheckBoxEvent;
import cc.ibooker.zphotochoose.event.PhotoChooseFolderEvent;
import cc.ibooker.zphotochoose.utils.ClickUtil;
import cc.ibooker.zphotochoose.zview.PhotoChoosePopuwindow;

/**
 * 选择图片Activity
 *
 * @author 邹峰立
 */
public class PhotoChooseActivity extends AppCompatActivity implements View.OnClickListener {
    private GridView gridView;
    private PhotoChooseAdapter photoChooseAdapter;
    private RelativeLayout bottomLayout;
    private TextView dirNameTv, photoNumTv, completeTv;
    private ProgressBar progressBar;
    private PhotoChoosePopuwindow photoChoosePopuwindow;
    private EventBus eventBus;
    private int canChooseNum;

    // 权限申请模块
    private String[] needPermissions = {
            // SDK在Android 6.0+需要进行运行检测的权限如下：
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private final int NEEDPERMISSIONS_REQUESTCODE = 1000;

    // 保存当前选中文件相关数据
    private FolderData folderData = new FolderData();
    // 保存底部弹窗数据
    private ArrayList<FolderBean> folderBeans;
    // 保存选中图片
    private ArrayList<String> selectedSet = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_choose);

        canChooseNum = getIntent().getIntExtra("canChooseNum", 3);

        initView();

        // 申请权限
        if (!hasPermission(needPermissions)) {
            requestPermission(NEEDPERMISSIONS_REQUESTCODE, needPermissions);
        } else {
            // 初始化数据
            initData(this);
        }

        eventBus = EventBus.getDefault();
        eventBus.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventBus != null) {
            eventBus.removeStickyEvent(PhotoChooseFolderEvent.class);
            eventBus.removeStickyEvent(PhotoChooseCheckBoxEvent.class);
            eventBus.unregister(this);
        }
        if (myHandler != null) {
            myHandler.removeCallbacksAndMessages(null);
            myHandler = null;
        }
    }

    // 执行切换图片集事件
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void executePhotoChooseFolderEvent(PhotoChooseFolderEvent event) {
        if (photoChoosePopuwindow != null)
            photoChoosePopuwindow.dismiss();
        reflashFolderData(event.getFolderBean());
        eventBus.removeStickyEvent(event);
    }

    // 执行选中图片事件
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void executePhotoChooseCheckBoxEvent(PhotoChooseCheckBoxEvent event) {
        updateSelectedSet(event.getFilePath());
        photoChooseAdapter.setSelectedSet(selectedSet);
        completeTv.setText("完成（" + selectedSet.size() + "）");
        eventBus.removeStickyEvent(event);
    }

    // 初始化控件
    private void initView() {
        TextView backTv = findViewById(R.id.tv_back);
        backTv.setOnClickListener(this);
        completeTv = findViewById(R.id.tv_complete);
        completeTv.setOnClickListener(this);
        progressBar = findViewById(R.id.progressbar);
        gridView = findViewById(R.id.gridview);

        bottomLayout = findViewById(R.id.layout_bottom);
        dirNameTv = findViewById(R.id.tv_dir_name);
        dirNameTv.setOnClickListener(this);
        photoNumTv = findViewById(R.id.tv_photo_num);
    }

    /**
     * 权限检查方法，false代表没有该权限，ture代表有该权限
     */
    private boolean hasPermission(String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 权限请求方法
     */
    private void requestPermission(int code, String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, code);
    }

    /**
     * 处理请求权限结果事件
     *
     * @param requestCode  请求码
     * @param permissions  权限组
     * @param grantResults 结果集
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doRequestPermissionsResult(requestCode, grantResults);
    }

    /**
     * 处理请求权限结果事件
     *
     * @param requestCode  请求码
     * @param grantResults 结果集
     */
    private void doRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (NEEDPERMISSIONS_REQUESTCODE == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 申请权限成功
                // 初始化数据
                initData(this);
            }
        }
    }

    // 初始化数据-从手机中扫描图片，并返回
    private void initData(Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            Toast.makeText(this, "当前SD卡不可用！", Toast.LENGTH_SHORT).show();
        else {
            setProgressBarVisibility(View.VISIBLE);
            scanPhoto(context, folderData, new OnScanPhotoCompleteListener() {
                @Override
                public void onScanPhotoComplete(ArrayList<FolderBean> list, FolderData data) {
                    folderData = data;
                    folderBeans = list;
                    // 子线程切换主线程 - 操作界面
                    myHandler.sendEmptyMessage(100);
                }
            });
        }
    }


    // 扫描手机中所有图片
    private void scanPhoto(final Context context, final FolderData folderData, final OnScanPhotoCompleteListener onScanPhotoCompleteListener) {
        final ArrayList<FolderBean> list = new ArrayList<>();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            new Thread() {
                @Override
                public void run() {
                    Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    ContentResolver resolver = context.getContentResolver();
                    Cursor cursor = resolver.query(uri, null, MediaStore.Images.Media.MIME_TYPE + "= ? or " + MediaStore.Images.Media.MIME_TYPE + "= ?", new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
                    if (cursor != null) {
                        Set<String> dirSet = new HashSet<>();
                        while (cursor.moveToNext()) {
                            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                            File parentFile = new File(path).getParentFile();
                            if (parentFile == null)
                                continue;
                            String dirPath = parentFile.getAbsolutePath();

                            FolderBean folderBean;
                            if (dirSet.contains(dirPath)) {
                                continue;
                            } else {
                                dirSet.add(dirPath);
                                folderBean = new FolderBean();
                                folderBean.setDir(dirPath);
                                folderBean.setFirstImgPath(path);
                            }
                            // 获取图片数量
                            if (parentFile.list() == null)
                                continue;
                            int picSize = parentFile.list(new FilenameFilter() {
                                @Override
                                public boolean accept(File file, String filename) {
                                    return filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg");
                                }
                            }).length;
                            folderBean.setCount(picSize);

                            list.add(folderBean);

                            if (picSize > folderData.getMaxSize()) {
                                folderData.setMaxSize(picSize);
                                folderData.setCurrentDir(parentFile);
                            }
                        }
                        cursor.close();
                    }

                    // 扫描完成回调
                    if (onScanPhotoCompleteListener != null)
                        onScanPhotoCompleteListener.onScanPhotoComplete(list, folderData);
                }
            }.start();
    }


    // 设置进度条的显示或隐藏
    private void setProgressBarVisibility(int visibility) {
        if (visibility == View.VISIBLE || visibility == View.GONE)
            progressBar.setVisibility(visibility);
    }

    // 实现setPhotoChooseAdapter
    private void setPhotoChooseAdapter() {
        if (folderData != null
                && folderData.getCurrentDir() != null
                && folderData.getCurrentDir().list() != null) {
            setPhotoChooseAdapter(Arrays.asList(folderData.getCurrentDir().list()),
                    folderData.getCurrentDir().getAbsolutePath(), selectedSet);
            setDirNameTvText(folderData.getCurrentDir().getName());
            setPhotoNumTvText(folderData.getMaxSize() + "");
        }
    }

    // 自定义setPhotoChooseAdapter-刷新布局
    private void setPhotoChooseAdapter(List<String> list, String dirPath, ArrayList<String> selectedSet) {
        if (photoChooseAdapter == null) {
            photoChooseAdapter = new PhotoChooseAdapter(this, list, dirPath, selectedSet, canChooseNum);
            gridView.setAdapter(photoChooseAdapter);
        } else {
            photoChooseAdapter.reflashData(list, dirPath, selectedSet, canChooseNum);
        }
    }

    // 设置文件目录TextView文本
    private void setDirNameTvText(String text) {
        dirNameTv.setText(text);
    }

    // 设置图片张数TextView文本
    private void setPhotoNumTvText(String text) {
        photoNumTv.setText(text);
    }

    // 点击事件监听
    @Override
    public void onClick(View v) {
        if (ClickUtil.isFastClick()) return;
        int currentId = v.getId();
        if (currentId == R.id.tv_back) {
            finish();
        } else if (currentId == R.id.tv_dir_name) {// 选择文件夹
            showPhotoChoosePopuwindow(folderBeans, folderData.getCurrentDir().getAbsolutePath());
        } else if (currentId == R.id.tv_complete) {// 完成
            Intent intent = new Intent();
            intent.putExtra("data", selectedSet);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    // 展示图片选择Popuwindow
    private void showPhotoChoosePopuwindow(ArrayList<FolderBean> list, String dirPath) {
        if (photoChoosePopuwindow == null) {
            photoChoosePopuwindow = new PhotoChoosePopuwindow(this);
        }
        photoChoosePopuwindow.setPhotoChoosePwindowAdapter(list, dirPath);
//        photoChoosePopuwindow.showViewTop(bottomLayout, 0);
        photoChoosePopuwindow.showBottom();
    }

    // 刷新GridView界面数据
    private void reflashFolderData(FolderBean folderBean) {
        File parentFile = new File(folderBean.getDir());
        folderData.setCurrentDir(parentFile);
        folderData.setMaxSize(parentFile.list().length);

        setPhotoChooseAdapter();
    }

    // 更新选中图片数据
    private void updateSelectedSet(String filePath) {
        if (selectedSet.contains(filePath)) {
            selectedSet.remove(filePath);
        } else {
            selectedSet.add(filePath);
        }
    }

    // 该Handler用于切换到主线程，刷新数据
    private MyHandler myHandler = new MyHandler(this);

    static class MyHandler extends Handler {

        WeakReference<PhotoChooseActivity> mPhotoChoosePresenter;

        MyHandler(PhotoChooseActivity photoChoosePresenter) {
            mPhotoChoosePresenter = new WeakReference<>(photoChoosePresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    mPhotoChoosePresenter.get().setProgressBarVisibility(View.GONE);
                    mPhotoChoosePresenter.get().setPhotoChooseAdapter();
                    break;
            }
        }
    }

    // Photo扫描完成回调方法
    public interface OnScanPhotoCompleteListener {
        void onScanPhotoComplete(ArrayList<FolderBean> list, FolderData folderData);
    }
}
