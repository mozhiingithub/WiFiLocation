package mozhi.wifilocation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.List;

import mozhi.wifilocation.Database.CddMac;
import mozhi.wifilocation.Database.CddVtr;
import mozhi.wifilocation.Database.Component;
import mozhi.wifilocation.Database.Leftmix;
import mozhi.wifilocation.Database.Location;
import mozhi.wifilocation.Database.PredictResult;
import mozhi.wifilocation.Database.PredictResult2;
import mozhi.wifilocation.Database.ReferVector;
import mozhi.wifilocation.Database.Rightmix;
import mozhi.wifilocation.Database.ScanVector;
import mozhi.wifilocation.Database.WiFiScanResult;
import mozhi.wifilocation.Database.WiFiVector;

import static org.litepal.crud.DataSupport.deleteAll;
import static org.litepal.crud.DataSupport.where;


/**
 * Created by 24599 on 2017/9/8.
 */

public class WiFiLocationClient {

    private Context context;
    private SQLiteDatabase db;
    private WifiManager wifiManager;
    private int K;
    private int N;
    private int Delay;

    public WiFiLocationClient(Context context){
        this.context=context;
        this.wifiManager=(WifiManager)this.context.getSystemService(Context.WIFI_SERVICE);
        LitePal.initialize(this.context);
        this.db=LitePal.getDatabase();
        this.K=10;
        this.N=10;
        this.Delay =1000;
    }

    public int getK() {
        return K;
    }

    public void setK(int k) {
        K = k;
    }

    public int getN() {
        return N;
    }

    public void setN(int n) {
        N = n;
    }

    public int getDelay() {
        return Delay;
    }

    public void setDelay(int delay) {
        Delay = delay;
    }

    public void Create(String location_name) throws WiFiLocationException, InterruptedException {
        if(!isLocationExist(location_name)){
            //main method
            Location location=new Location();
            location.setName(location_name);
            location.save();
            for (int i=0;i<N;i++){
                WiFiVector wiFiVector=new WiFiVector();
                wiFiVector.setLocation(location);
                wiFiVector.save();
                List<ScanResult> list=getCurrentScanResults();
                for(ScanResult scanResult:list){
                    Component component=new Component();
                    component.setWiFiVector(wiFiVector);
                    component.setMac(scanResult.BSSID);
                    component.setLevel(scanResult.level);
                    component.save();
                }
                Thread.sleep(Delay);
            }
        }
        else {
            throw new WiFiLocationException("地点\""+location_name+"\"已经建立，不能重复建立。");
        }
    }

    public void Create(String location_name,int delay) throws WiFiLocationException, InterruptedException {
        if(!isLocationExist(location_name)){
            //main method
            int original_delay=getDelay();
            setDelay(delay);
            Location location=new Location();
            location.setName(location_name);
            location.save();
            for (int i=0;i<N;i++){
                WiFiVector wiFiVector=new WiFiVector();
                wiFiVector.setLocation(location);
                wiFiVector.save();
                List<ScanResult> list=getCurrentScanResults();
                for(ScanResult scanResult:list){
                    Component component=new Component();
                    component.setWiFiVector(wiFiVector);
                    component.setMac(scanResult.BSSID);
                    component.setLevel(scanResult.level);
                    component.save();
                }
                Thread.sleep(Delay);
            }
            setDelay(original_delay);
        }
        else {
            throw new WiFiLocationException("地点\""+location_name+"\"已经建立，不能重复建立。");
        }
    }

    public void Create(String location_name,int delay,int n) throws WiFiLocationException, InterruptedException {
        if(!isLocationExist(location_name)){
            //main method
            int original_delay=getDelay();
            setDelay(delay);
            int original_n=getN();
            Location location=new Location();
            location.setName(location_name);
            location.save();
            for (int i=0;i<N;i++){
                WiFiVector wiFiVector=new WiFiVector();
                wiFiVector.setLocation(location);
                wiFiVector.save();
                List<ScanResult> list=getCurrentScanResults();
                for(ScanResult scanResult:list){
                    Component component=new Component();
                    component.setWiFiVector(wiFiVector);
                    component.setMac(scanResult.BSSID);
                    component.setLevel(scanResult.level);
                    component.save();
                }
                Thread.sleep(Delay);
            }
            setDelay(original_delay);
            setN(original_n);
        }
        else {
            throw new WiFiLocationException("地点\""+location_name+"\"已经建立，不能重复建立。");
        }
    }

    public int LocationRank(String location_name) throws WiFiLocationException {
        if(isLocationExist(location_name)){

            //main method
            DataSupport.deleteAll(CddVtr.class);
            DataSupport.deleteAll(CddMac.class);
            DataSupport.deleteAll(ScanVector.class);
            DataSupport.deleteAll(ReferVector.class);
            DataSupport.deleteAll(WiFiScanResult.class);
            DataSupport.deleteAll(PredictResult.class);
            DataSupport.deleteAll(PredictResult2.class);
            DataSupport.deleteAll(Leftmix.class);
            DataSupport.deleteAll(Rightmix.class);
            LoadCurrentScanResults();
            db.beginTransaction();
            db.execSQL("insert into cddvtr(rid) select distinct wifivector.id\n" +
                    "from component,wifivector \n" +
                    "where/*检索可能的wifi向量，只要其分量含有扫描结果mac，就挑出来*/\n" +
                    "component.wifivector_id=wifivector.id \n" +
                    "and\n" +
                    "component.mac in\n" +
                    "(/*剔除扫描结果中未在库中出现过的mac*/\n" +
                    "select distinct mac from wifiscanresult where mac in\n" +
                    "       (\n" +
                    "       select distinct mac from component\n" +
                    "       )\n" +
                    ");");
            db.execSQL("insert into cddmac(mac) select distinct mac \n" +
                    "from wifivector,component \n" +
                    "where \n" +
                    "component.wifivector_id=wifivector.id /*联结条件*/\n" +
                    "and \n" +
                    "wifivector.id in(/*检索出候选向量的所有mac，去重*/\n" +
                    "select rid from cddvtr\n" +
                    ");");
            db.execSQL("insert into leftmix(rid,mac) select cddvtr.rid ,cddmac.mac from cddvtr,cddmac;");
            db.execSQL("insert into rightmix(rid,mac,level) select  wifivector_id,mac,level\n" +
                    "from wifivector,component \n" +
                    "where\n" +
                    "component.wifivector_id=wifivector.id \n" +
                    "and\n" +
                    "wifivector.id in (select rid from cddvtr);");
            db.execSQL("insert into refervector(rid,mac,level) select leftmix.rid,leftmix.mac,ifnull(rightmix.level,-100)" +
                    " from leftmix left outer join rightmix " +
                    "on " +
                    "leftmix.rid=rightmix.rid " +
                    " and " +
                    " leftmix.mac=rightmix.mac;");
            db.execSQL("insert into scanvector(mac,level) select cddmac.mac,ifnull(wifiscanresult.level,-100) from cddmac left outer join wifiscanresult on cddmac.mac=wifiscanresult.mac ;");
            db.execSQL("insert into predictresult(name,sum) select location.name ,avg(abs(refervector.level-scanvector.level)) as s \n" +
                    "from location,wifivector,refervector,scanvector \n" +
                    "where refervector.mac=scanvector.mac \n" +
                    "and\n" +
                    "refervector.rid=wifivector.id\n" +
                    "and\n" +
                    "wifivector.location_id=location.id\n" +
                    "group by refervector.rid order by s limit "+String.valueOf(K)+";");
            db.execSQL("insert into predictresult2(name,sum) select predictresult.name,count(*) from predictresult group by predictresult.name order by count(*) desc;");
            db.setTransactionSuccessful();
            db.endTransaction();
            List<PredictResult2> list=DataSupport.findAll(PredictResult2.class);
            int rank=0;
            int i=0;
            for(PredictResult2 predictResult2:list){
                i++;
                if(predictResult2.getName()==location_name) rank=i;
            }
            return rank;
        }
        else {
            throw new WiFiLocationException("数据库没有地点\""+location_name+"\"，无法进行排名。");
        }
    }

    public int LocationRank(String location_name,int k) throws WiFiLocationException {
        if(isLocationExist(location_name)){

            //main method
            int original_k=getK();
            setK(k);
            DataSupport.deleteAll(CddVtr.class);
            DataSupport.deleteAll(CddMac.class);
            DataSupport.deleteAll(ScanVector.class);
            DataSupport.deleteAll(ReferVector.class);
            DataSupport.deleteAll(WiFiScanResult.class);
            DataSupport.deleteAll(PredictResult.class);
            DataSupport.deleteAll(PredictResult2.class);
            DataSupport.deleteAll(Leftmix.class);
            DataSupport.deleteAll(Rightmix.class);
            LoadCurrentScanResults();
            db.beginTransaction();
            db.execSQL("insert into cddvtr(rid) select distinct wifivector.id\n" +
                    "from component,wifivector \n" +
                    "where/*检索可能的wifi向量，只要其分量含有扫描结果mac，就挑出来*/\n" +
                    "component.wifivector_id=wifivector.id \n" +
                    "and\n" +
                    "component.mac in\n" +
                    "(/*剔除扫描结果中未在库中出现过的mac*/\n" +
                    "select distinct mac from wifiscanresult where mac in\n" +
                    "       (\n" +
                    "       select distinct mac from component\n" +
                    "       )\n" +
                    ");");
            db.execSQL("insert into cddmac(mac) select distinct mac \n" +
                    "from wifivector,component \n" +
                    "where \n" +
                    "component.wifivector_id=wifivector.id /*联结条件*/\n" +
                    "and \n" +
                    "wifivector.id in(/*检索出候选向量的所有mac，去重*/\n" +
                    "select rid from cddvtr\n" +
                    ");");
            db.execSQL("insert into leftmix(rid,mac) select cddvtr.rid ,cddmac.mac from cddvtr,cddmac;");
            db.execSQL("insert into rightmix(rid,mac,level) select  wifivector_id,mac,level\n" +
                    "from wifivector,component \n" +
                    "where\n" +
                    "component.wifivector_id=wifivector.id \n" +
                    "and\n" +
                    "wifivector.id in (select rid from cddvtr);");
            db.execSQL("insert into refervector(rid,mac,level) select leftmix.rid,leftmix.mac,ifnull(rightmix.level,-100)" +
                    " from leftmix left outer join rightmix " +
                    "on " +
                    "leftmix.rid=rightmix.rid " +
                    " and " +
                    " leftmix.mac=rightmix.mac;");
            db.execSQL("insert into scanvector(mac,level) select cddmac.mac,ifnull(wifiscanresult.level,-100) from cddmac left outer join wifiscanresult on cddmac.mac=wifiscanresult.mac ;");
            db.execSQL("insert into predictresult(name,sum) select location.name ,avg(abs(refervector.level-scanvector.level)) as s \n" +
                    "from location,wifivector,refervector,scanvector \n" +
                    "where refervector.mac=scanvector.mac \n" +
                    "and\n" +
                    "refervector.rid=wifivector.id\n" +
                    "and\n" +
                    "wifivector.location_id=location.id\n" +
                    "group by refervector.rid order by s limit "+String.valueOf(K)+";");
            db.execSQL("insert into predictresult2(name,sum) select predictresult.name,count(*) from predictresult group by predictresult.name order by count(*) desc;");
            db.setTransactionSuccessful();
            db.endTransaction();
            List<PredictResult2> list=DataSupport.findAll(PredictResult2.class);
            int rank=0;
            int i=0;
            for(PredictResult2 predictResult2:list){
                i++;
                if(predictResult2.getName()==location_name) rank=i;
            }
            setK(original_k);
            return rank;
        }
        else {
            throw new WiFiLocationException("数据库没有地点\""+location_name+"\"，无法进行排名。");
        }
    }

    public List<Location> getLocation(){
        return DataSupport.findAll(Location.class);
    }

    public List<PredictResult2> getLocateResult(){
        DataSupport.deleteAll(CddVtr.class);
        DataSupport.deleteAll(CddMac.class);
        DataSupport.deleteAll(ScanVector.class);
        DataSupport.deleteAll(ReferVector.class);
        DataSupport.deleteAll(WiFiScanResult.class);
        DataSupport.deleteAll(PredictResult.class);
        DataSupport.deleteAll(PredictResult2.class);
        DataSupport.deleteAll(Leftmix.class);
        DataSupport.deleteAll(Rightmix.class);
        LoadCurrentScanResults();
        db.beginTransaction();
        db.execSQL("insert into cddvtr(rid) select distinct wifivector.id\n" +
                "from component,wifivector \n" +
                "where/*检索可能的wifi向量，只要其分量含有扫描结果mac，就挑出来*/\n" +
                "component.wifivector_id=wifivector.id \n" +
                "and\n" +
                "component.mac in\n" +
                "(/*剔除扫描结果中未在库中出现过的mac*/\n" +
                "select distinct mac from wifiscanresult where mac in\n" +
                "       (\n" +
                "       select distinct mac from component\n" +
                "       )\n" +
                ");");
        db.execSQL("insert into cddmac(mac) select distinct mac \n" +
                "from wifivector,component \n" +
                "where \n" +
                "component.wifivector_id=wifivector.id /*联结条件*/\n" +
                "and \n" +
                "wifivector.id in(/*检索出候选向量的所有mac，去重*/\n" +
                "select rid from cddvtr\n" +
                ");");
        db.execSQL("insert into leftmix(rid,mac) select cddvtr.rid ,cddmac.mac from cddvtr,cddmac;");
        db.execSQL("insert into rightmix(rid,mac,level) select  wifivector_id,mac,level\n" +
                "from wifivector,component \n" +
                "where\n" +
                "component.wifivector_id=wifivector.id \n" +
                "and\n" +
                "wifivector.id in (select rid from cddvtr);");
        db.execSQL("insert into refervector(rid,mac,level) select leftmix.rid,leftmix.mac,ifnull(rightmix.level,-100)" +
                " from leftmix left outer join rightmix " +
                "on " +
                "leftmix.rid=rightmix.rid " +
                " and " +
                " leftmix.mac=rightmix.mac;");
        db.execSQL("insert into scanvector(mac,level) select cddmac.mac,ifnull(wifiscanresult.level,-100) from cddmac left outer join wifiscanresult on cddmac.mac=wifiscanresult.mac ;");
        db.execSQL("insert into predictresult(name,sum) select location.name ,avg(abs(refervector.level-scanvector.level)) as s \n" +
                "from location,wifivector,refervector,scanvector \n" +
                "where refervector.mac=scanvector.mac \n" +
                "and\n" +
                "refervector.rid=wifivector.id\n" +
                "and\n" +
                "wifivector.location_id=location.id\n" +
                "group by refervector.rid order by s limit "+String.valueOf(K)+";");
        db.execSQL("insert into predictresult2(name,sum) select predictresult.name,count(*) from predictresult group by predictresult.name order by count(*) desc;");
        db.setTransactionSuccessful();
        db.endTransaction();
        List<PredictResult2> list=DataSupport.findAll(PredictResult2.class);
        return DataSupport.findAll(PredictResult2.class);
    }

    public List<PredictResult2> getLocateResult(int k){
        int original_k=getK();
        setK(k);
        DataSupport.deleteAll(CddVtr.class);
        DataSupport.deleteAll(CddMac.class);
        DataSupport.deleteAll(ScanVector.class);
        DataSupport.deleteAll(ReferVector.class);
        DataSupport.deleteAll(WiFiScanResult.class);
        DataSupport.deleteAll(PredictResult.class);
        DataSupport.deleteAll(PredictResult2.class);
        DataSupport.deleteAll(Leftmix.class);
        DataSupport.deleteAll(Rightmix.class);
        LoadCurrentScanResults();
        db.beginTransaction();
        db.execSQL("insert into cddvtr(rid) select distinct wifivector.id\n" +
                "from component,wifivector \n" +
                "where/*检索可能的wifi向量，只要其分量含有扫描结果mac，就挑出来*/\n" +
                "component.wifivector_id=wifivector.id \n" +
                "and\n" +
                "component.mac in\n" +
                "(/*剔除扫描结果中未在库中出现过的mac*/\n" +
                "select distinct mac from wifiscanresult where mac in\n" +
                "       (\n" +
                "       select distinct mac from component\n" +
                "       )\n" +
                ");");
        db.execSQL("insert into cddmac(mac) select distinct mac \n" +
                "from wifivector,component \n" +
                "where \n" +
                "component.wifivector_id=wifivector.id /*联结条件*/\n" +
                "and \n" +
                "wifivector.id in(/*检索出候选向量的所有mac，去重*/\n" +
                "select rid from cddvtr\n" +
                ");");
        db.execSQL("insert into leftmix(rid,mac) select cddvtr.rid ,cddmac.mac from cddvtr,cddmac;");
        db.execSQL("insert into rightmix(rid,mac,level) select  wifivector_id,mac,level\n" +
                "from wifivector,component \n" +
                "where\n" +
                "component.wifivector_id=wifivector.id \n" +
                "and\n" +
                "wifivector.id in (select rid from cddvtr);");
        db.execSQL("insert into refervector(rid,mac,level) select leftmix.rid,leftmix.mac,ifnull(rightmix.level,-100)" +
                " from leftmix left outer join rightmix " +
                "on " +
                "leftmix.rid=rightmix.rid " +
                " and " +
                " leftmix.mac=rightmix.mac;");
        db.execSQL("insert into scanvector(mac,level) select cddmac.mac,ifnull(wifiscanresult.level,-100) from cddmac left outer join wifiscanresult on cddmac.mac=wifiscanresult.mac ;");
        db.execSQL("insert into predictresult(name,sum) select location.name ,avg(abs(refervector.level-scanvector.level)) as s \n" +
                "from location,wifivector,refervector,scanvector \n" +
                "where refervector.mac=scanvector.mac \n" +
                "and\n" +
                "refervector.rid=wifivector.id\n" +
                "and\n" +
                "wifivector.location_id=location.id\n" +
                "group by refervector.rid order by s limit "+String.valueOf(K)+";");
        db.execSQL("insert into predictresult2(name,sum) select predictresult.name,count(*) from predictresult group by predictresult.name order by count(*) desc;");
        db.setTransactionSuccessful();
        db.endTransaction();
        setK(original_k);
        return DataSupport.findAll(PredictResult2.class);
    }

    public void Update(String location_name) throws WiFiLocationException, InterruptedException {
        if(isLocationExist(location_name)){
            Location location=GetLocationFromName(location_name);
            for (int i=0;i<N;i++){
                WiFiVector wiFiVector=new WiFiVector();
                wiFiVector.setLocation(location);
                wiFiVector.save();
                List<ScanResult> list=getCurrentScanResults();
                for(ScanResult scanResult:list){
                    Component component=new Component();
                    component.setWiFiVector(wiFiVector);
                    component.setMac(scanResult.BSSID);
                    component.setLevel(scanResult.level);
                    component.save();
                }
                Thread.sleep(Delay);
            }
        }
        else {
            throw new WiFiLocationException("数据库没有地点\""+location_name+"\"，无法更新。");
        }

    }

    public void Update(String location_name,int delay) throws WiFiLocationException, InterruptedException {
        if(isLocationExist(location_name)){
            int original_delay=getDelay();
            setDelay(delay);
            Location location=GetLocationFromName(location_name);
            for (int i=0;i<N;i++){
                WiFiVector wiFiVector=new WiFiVector();
                wiFiVector.setLocation(location);
                wiFiVector.save();
                List<ScanResult> list=getCurrentScanResults();
                for(ScanResult scanResult:list){
                    Component component=new Component();
                    component.setWiFiVector(wiFiVector);
                    component.setMac(scanResult.BSSID);
                    component.setLevel(scanResult.level);
                    component.save();
                }
                Thread.sleep(Delay);
            }
            setDelay(original_delay);
        }
        else {
            throw new WiFiLocationException("数据库没有地点\""+location_name+"\"，无法更新。");
        }

    }

    public void Update(String location_name,int delay,int n) throws WiFiLocationException, InterruptedException {
        if(isLocationExist(location_name)){
            int original_delay=getDelay();
            setDelay(delay);
            int original_n=getN();
            setN(n);
            Location location=GetLocationFromName(location_name);
            for (int i=0;i<N;i++){
                WiFiVector wiFiVector=new WiFiVector();
                wiFiVector.setLocation(location);
                wiFiVector.save();
                List<ScanResult> list=getCurrentScanResults();
                for(ScanResult scanResult:list){
                    Component component=new Component();
                    component.setWiFiVector(wiFiVector);
                    component.setMac(scanResult.BSSID);
                    component.setLevel(scanResult.level);
                    component.save();
                }
                Thread.sleep(Delay);
            }
            setDelay(original_delay);
            setN(original_n);
        }
        else {
            throw new WiFiLocationException("数据库没有地点\""+location_name+"\"，无法更新。");
        }

    }

    public void Reset(String location_name) throws WiFiLocationException, InterruptedException {
        if(isLocationExist(location_name)){
            Location location=GetLocationFromName(location_name);
            int location_id=location.getId();
            DataSupport.deleteAll(WiFiVector.class,"location_id=?",String.valueOf(location_id));
            for (int i=0;i<N;i++){
                WiFiVector wiFiVector=new WiFiVector();
                wiFiVector.setLocation(location);
                wiFiVector.save();
                List<ScanResult> list=getCurrentScanResults();
                for(ScanResult scanResult:list){
                    Component component=new Component();
                    component.setWiFiVector(wiFiVector);
                    component.setMac(scanResult.BSSID);
                    component.setLevel(scanResult.level);
                    component.save();
                }
                Thread.sleep(Delay);
            }
        }
        else {
            throw new WiFiLocationException("数据库没有地点\""+location_name+"\"，无法重置。");
        }
    }

    public void Reset(String location_name,int delay) throws WiFiLocationException, InterruptedException {
        if(isLocationExist(location_name)){
            int original_delay=getDelay();
            setDelay(delay);
            Location location=GetLocationFromName(location_name);
            int location_id=location.getId();
            DataSupport.deleteAll(WiFiVector.class,"location_id=?",String.valueOf(location_id));
            for (int i=0;i<N;i++){
                WiFiVector wiFiVector=new WiFiVector();
                wiFiVector.setLocation(location);
                wiFiVector.save();
                List<ScanResult> list=getCurrentScanResults();
                for(ScanResult scanResult:list){
                    Component component=new Component();
                    component.setWiFiVector(wiFiVector);
                    component.setMac(scanResult.BSSID);
                    component.setLevel(scanResult.level);
                    component.save();
                }
                Thread.sleep(Delay);
            }
            setDelay(original_delay);
        }
        else {
            throw new WiFiLocationException("数据库没有地点\""+location_name+"\"，无法重置。");
        }
    }

    public void Reset(String location_name,int delay,int n) throws WiFiLocationException, InterruptedException {
        if(isLocationExist(location_name)){
            int original_delay=getDelay();
            setDelay(delay);
            int original_n=getN();
            setN(n);
            Location location=GetLocationFromName(location_name);
            int location_id=location.getId();
            DataSupport.deleteAll(WiFiVector.class,"location_id=?",String.valueOf(location_id));
            for (int i=0;i<N;i++){
                WiFiVector wiFiVector=new WiFiVector();
                wiFiVector.setLocation(location);
                wiFiVector.save();
                List<ScanResult> list=getCurrentScanResults();
                for(ScanResult scanResult:list){
                    Component component=new Component();
                    component.setWiFiVector(wiFiVector);
                    component.setMac(scanResult.BSSID);
                    component.setLevel(scanResult.level);
                    component.save();
                }
                Thread.sleep(Delay);
            }
            setDelay(original_delay);
            setN(original_n);
        }
        else {
            throw new WiFiLocationException("数据库没有地点\""+location_name+"\"，无法重置。");
        }
    }

    public void Clear(String location_name) throws WiFiLocationException {
        if(isLocationExist(location_name)){
            Location location=GetLocationFromName(location_name);
            int location_id=location.getId();
            DataSupport.deleteAll(WiFiVector.class,"location_id=?",String.valueOf(location_id));
            DataSupport.delete(Location.class,location_id);
        }
        else {
            throw new WiFiLocationException("数据库没有地点\""+location_name+"\"，无法清除。");
        }
    }

    public void ClearAll(){
        DataSupport.deleteAll(Location.class);
        DataSupport.deleteAll(WiFiVector.class);
        DataSupport.deleteAll(Component.class);
        DataSupport.deleteAll(CddVtr.class);
        DataSupport.deleteAll(CddMac.class);
        DataSupport.deleteAll(ScanVector.class);
        DataSupport.deleteAll(ReferVector.class);
        DataSupport.deleteAll(WiFiScanResult.class);
        DataSupport.deleteAll(PredictResult.class);
        DataSupport.deleteAll(PredictResult2.class);
        DataSupport.deleteAll(Leftmix.class);
        DataSupport.deleteAll(Rightmix.class);
    }

    private void LoadCurrentScanResults(){
        DataSupport.deleteAll(WiFiScanResult.class);
        List<ScanResult> list=this.getCurrentScanResults();
        for(ScanResult scanResult:list){
            WiFiScanResult wiFiScanResult=new WiFiScanResult();
            wiFiScanResult.setMac(scanResult.BSSID);
            wiFiScanResult.setLevel(scanResult.level);
            wiFiScanResult.save();
        }
    }

    private List<ScanResult> getCurrentScanResults(){
        wifiManager.startScan();
        return wifiManager.getScanResults();
    }

    private boolean isLocationExist(String location_name){
        return where(" name = ? ",location_name).count(Location.class)>0;
    }

    private Location GetLocationFromName(String location_name){
        return DataSupport.where("name=?",location_name).findFirst(Location.class);
    }
}
