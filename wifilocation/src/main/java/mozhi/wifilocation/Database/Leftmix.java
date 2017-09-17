package mozhi.wifilocation.Database;

import org.litepal.crud.DataSupport;

/**
 * Created by 24599 on 2017/7/25.
 */

public class Leftmix extends DataSupport {

    private int rid;
    private String mac;

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
