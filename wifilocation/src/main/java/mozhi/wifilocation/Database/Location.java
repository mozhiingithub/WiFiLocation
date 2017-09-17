package mozhi.wifilocation.Database;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;

/**
 * Created by 24599 on 2017/9/14.
 */

public class Location extends DataSupport {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private ArrayList<WiFiVector> wiFiVectors=new ArrayList<WiFiVector>();

}
