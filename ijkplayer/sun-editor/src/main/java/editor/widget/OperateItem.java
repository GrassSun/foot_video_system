package editor.widget;

/**
 * Created by sunweijun on 18-4-20
 */

public class OperateItem {
    public String operateName = null;
    public String operateInfo = null;
    public OperateItem() {
        operateName = "";
        operateInfo = "";
    }

    public OperateItem(String _operateName, String _operateInfo) {
        operateName = _operateName;
        operateInfo = _operateInfo;
    }
}
