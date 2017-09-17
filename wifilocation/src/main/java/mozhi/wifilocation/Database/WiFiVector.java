package mozhi.wifilocation.Database;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;

/**
 * Created by 24599 on 2017/9/14.
 */

public class WiFiVector extends DataSupport {
    private int id;
    private Location location;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    private ArrayList<Component> components=new ArrayList<Component>();
}
