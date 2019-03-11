package cc.ibooker.zphotochoose.bean;

/**
 * 文件Bean
 */
public class FolderBean {
    private String dir;
    private String firstImgPath;
    private String name;
    private int count;

    public FolderBean() {
        super();
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndex = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndex + 1);
    }

    public String getFirstImgPath() {
        return firstImgPath;
    }

    public void setFirstImgPath(String firstImgPath) {
        this.firstImgPath = firstImgPath;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
