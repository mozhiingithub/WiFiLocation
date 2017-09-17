package mozhi.wifilocation.Database;

import org.litepal.crud.DataSupport;

/**
 * Created by 24599 on 2017/7/21.
 */

public class PredictResult extends DataSupport {
    private String name;
    private int sum;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }
}
