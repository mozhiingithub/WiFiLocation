package mozhi.wifilocation.Database;

import org.litepal.crud.DataSupport;

/**
 * Created by 24599 on 2017/7/21.
 */

public class ScanVector extends DataSupport {
    private String mac;
    private int level;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
