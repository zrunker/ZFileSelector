package cc.ibooker.zphotochoose.event;

/**
 * 图片选择，单张图片选择事件
 *
 * @author 邹峰立
 */
public class PhotoChooseCheckBoxEvent {
    private String filePath;

    public PhotoChooseCheckBoxEvent(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
