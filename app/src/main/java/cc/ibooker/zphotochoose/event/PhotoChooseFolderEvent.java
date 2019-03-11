package cc.ibooker.zphotochoose.event;

import cc.ibooker.zphotochoose.bean.FolderBean;

/**
 * 本地图片集选中事件
 *
 * @author 邹峰立
 */
public class PhotoChooseFolderEvent {
    private FolderBean folderBean;

    public PhotoChooseFolderEvent(FolderBean folderBean) {
        this.folderBean = folderBean;
    }

    public FolderBean getFolderBean() {
        return folderBean;
    }

    public void setFolderBean(FolderBean folderBean) {
        this.folderBean = folderBean;
    }
}
