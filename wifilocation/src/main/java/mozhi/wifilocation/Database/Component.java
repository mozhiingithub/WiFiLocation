package mozhi.wifilocation.Database;

import org.litepal.crud.DataSupport;

/**
 * Created by 24599 on 2017/9/14.
 */

public class Component extends DataSupport {

    private String mac;
    private int level;
    private WiFiVector wiFiVector;

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setWiFiVector(WiFiVector wiFiVector) {
        this.wiFiVector = wiFiVector;
    }
}
