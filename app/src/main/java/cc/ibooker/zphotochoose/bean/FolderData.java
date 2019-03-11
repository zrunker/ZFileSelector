package cc.ibooker.zphotochoose.bean;

import java.io.File;

/**
 * 文件Data
 *
 * @author 邹峰立
 */
public class FolderData {
    private int maxSize;
    private File currentDir;

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public File getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(File currentDir) {
        this.currentDir = currentDir;
    }
}
