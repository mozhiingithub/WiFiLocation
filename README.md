# WiFiLocation
WiFiLocation是一款基于K-NN算法的简易WiFi定位系统。它借助 **[LitePal](https://github.com/LitePalFramework/LitePal)** 建立本地的WiFi指纹数据库，通过一系列API实现目标地点的WiFi指纹搜集、更新、重置或删除，以及当前位置的定位识别。

相较于传统的C/S架构的WiFi定位系统，本系统实现了完全本地化的运行，所有功能均不会产生任何网络费用。另一方面，完全依赖本地的指纹数据库以及靠手机本身进行定位运算，也大大限制了这款定位系统的定位准确度和效率。

即便性能远不如主流的GPS、移动蜂窝网络及各大地图运营商所提供的在线WiFi室内定位系统，WiFiLocation依旧拥有其独特的使用场景需求：
#### 1.不借助GPS或蜂窝网络进行常见地点的定位识别

这里所指的常见地点，往往为用户的住处、工作场所或教室。在常见地点有限的前提下，WiFiLocation可以实现与GPS或蜂窝网络大体相近的定位识别而不会产生额外的网络费用。
#### 2.主流地图运营商没有覆盖的室内空间定位

地图运营商的室内WiFi定位往往局限于热门地点，例如大型商场。对于诸如学生宿舍楼层内空间等非热门地点，运营商往往无法提供较为准确的定位。而利用WiFiLocation，用户可以自行构建室内定位坐标，实现室内定位。

对于以下使用需求，作者强烈不建议使用WifiLocation：
#### 1.高精度定位

由于WiFiLocation是基于K-NN算法的定位系统，定位算法和条件判断较为简单，所以该系统的定位精度较差。对精度要求较高的开发者，作者建议使用主流网络运营商提供的GPS或蜂窝网络定位方案。

#### 2.大量地点的识别定位

WiFiLocation是完全本地化的定位系统，因而它的可存储地点信息数目受限于Sqlite数据库的性能以及手机的内存，其定位速度也受限于地点数目及手机性能。当数据库储存的地点信息过多时，WiFiLocation单次定位的所需时间会过长，“实时”定位的效果不再显现，整个系统的运行效益将不复存在。对于需要存储大量地点的开发者，作者同样不建议使用本系统。

#### 3.高频定位
在前期测试当中，作者发现，WiFiLocation单次定位运算所需时间，普遍不超过500毫秒，但考虑到测试集地点较少，遂估计在录入地点较多的情况下，单次定位运算时间可能会接近1秒。若开发者以低于1秒的间隔频繁调用定位方法，很可能导致系统崩溃。所以对于需要以极高频率获取定位的开发者，作者建议使用其他的定位方案。

下面介绍WiFiLocation的安装和使用。

## 下载
* **[WiFiLocation.jar](https://raw.githubusercontent.com/mozhiingithub/WiFiLocation/master/WiFiLocation.jar)**

## 安装
#### 1.加载jar包

打开Android Studio，创建项目，将下载好的WiFiLocation.jar复制到libs文件夹当中，并右键点击“Add As Library”。

#### 2.添加权限

在 **AndroidManifest.xml** 文件中添加以下代码：

``` xml
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
**注意：自 Android 6.0后，系统在获取WiFi扫描结果前需保证手机GPS定位选项开启，且程序中关于定位的权限必须为“允许”，否则系统每次获取扫描结果将为空值。开发者需自行设计定位选项状态及权限获取状态检查机制。**

#### 3.添加LitePal依赖

打开 **build.gradle** 文件并添加以下依赖：
``` groovy
dependencies {
    compile 'org.litepal.android:core:1.6.0'
}
```

#### 4.配置litepal.xml

在main文件夹中，新建 **assets** 文件夹，并新建 **litepal.xml** 文件，用以下代码进行覆盖：
``` xml
<?xml version="1.0" encoding="utf-8"?>
<litepal>
    <dbname value="location" />
    <version value="6" />
    <list>
        <mapping class="mozhi.wifilocation.Database.WiFiScanResult"></mapping>
        <mapping class="mozhi.wifilocation.Database.Component"></mapping>
        <mapping class="mozhi.wifilocation.Database.WiFiVector"></mapping>
        <mapping class="mozhi.wifilocation.Database.Location"></mapping>
        <mapping class="mozhi.wifilocation.Database.CddVtr"></mapping>
        <mapping class="mozhi.wifilocation.Database.CddMac"></mapping>
        <mapping class="mozhi.wifilocation.Database.Leftmix"></mapping>
        <mapping class="mozhi.wifilocation.Database.Rightmix"></mapping>
        <mapping class="mozhi.wifilocation.Database.ReferVector"></mapping>
        <mapping class="mozhi.wifilocation.Database.ScanVector"></mapping>
        <mapping class="mozhi.wifilocation.Database.PredictResult"></mapping>
        <mapping class="mozhi.wifilocation.Database.PredictResult2"></mapping>


    </list>
</litepal>
```
#### 5.初始化定位对象WiFiLocationClient

在onCreate方法中对定位对象WifiLocationClient进行初始化：

```java
public class Application {

    @Override
    public void onCreate() {
        super.onCreate();
        WiFiLocationClient wiFiLocationClient=new WiFiLocationClient(this);
    }
    ...
}
```

至此，我们完成了WiFiLocation的安装操作。

## 使用

### 录入、更新、删除相关方法

#### 1.获取和设定K值

WiFiLocation是基于K-NN算法的定位系统，我们可以通过 **getK()** 获取系统当前K值，或 **setK(int k)** 以设定系统的K值。系统默认的K值为10。

```java
        int k_get=wiFiLocationClient.getK();
        int k_set=7;
        wiFiLocationClient.setK(k_set);
```

#### 2.获取和设定N值

在WiFiLocation当中，调用一次涉及WiFi指纹录入操作的API，系统会自动录入N次，我们可以通过 **getN()** 获取系统当前N值，或 **setN(int n)** 以设定系统的N值。系统默认的N值为10。

```java
        int n_get=wiFiLocationClient.getN();
        int n_set=1;
        wiFiLocationClient.setN(n_set);
```

#### 3.获取和设定Delay值

在WiFiLocation当中，调用一次涉及WiFi指纹录入操作的API，系统录入N次指纹的间隔为Delay毫秒，我们可以通过 **getDelay()** 获取系统当前Delay值，或 **setDelay(int delay)** 以设定系统的Delay值。系统默认的Delay值为1000。虽无强制规定，但作者**强烈不建议**将Delay设定为低于1000的值。

```java
        int delay_get=wiFiLocationClient.Delay();
        int delay_set=3000;
        wiFiLocationClient.setDelay(delay_set);
```

#### 4.创建新地点

我们可以使用 **Create(String location_name)** 方法创建新地点。该方法首先会判断输入的地点名在数据库中是否存在。若存在，则系统抛出 **WiFiLocationException**异常 ；若不存在，则系统创建以 **location_name** 为名的新地点，并以Delay毫秒为间隔，扫描并录入N次WiFi指纹信息。

**Create(String location_name)** 还有两个重载形式，分别为：
 * **Create(String location_name,int delay)** 
 * **Create(String location_name,int delay,int n)** 
 
 这两个重载形式可以修改设定本次创建操作的Delay值或N值，但不会修改系统的Delay值或N值。

由于**Create(String location_name)** 是耗时操作，所以开发者不应在主线程当中使用本方法。
```java
String name="name_of_place";
try {
        wiFiLocationClient.Create(name);
        } catch (WiFiLocationException e) {
        e.printStackTrace();
        } catch (InterruptedException e) {
        e.printStackTrace();
        }

```

#### 5.更新地点指纹信息

我们可以使用 **Update(String location_name)** 方法来更新某地点的WiFi指纹信息。该方法首先会判断输入的地点名在数据库中是否存在。若不存在，则系统抛出 **WiFiLocationException**异常 ；若存在，则系统将为以**location_name** 为名的地点，以Delay毫秒为间隔，扫描并录入N次WiFi指纹信息。

**Update(String location_name)** 还有两个重载形式，分别为：
 * **Update(String location_name,int delay)** 
 * **Update(String location_name,int delay,int n)** 
 
 这两个重载形式可以修改设定本次更新操作的Delay值或N值，但不会修改系统的Delay值或N值。

由于**Update(String location_name)** 是耗时操作，所以开发者不应在主线程当中使用本方法。
```java
String name="name_of_place";
try {
        wiFiLocationClient.Update(name);
        } catch (WiFiLocationException e) {
        e.printStackTrace();
        } catch (InterruptedException e) {
        e.printStackTrace();
        }

```

#### 6.重置地点指纹信息

我们可以使用 **Reset(String location_name)** 方法来重置某地点的WiFi指纹信息。该方法首先会判断输入的地点名在数据库中是否存在。若不存在，则系统抛出 **WiFiLocationException**异常 ；若存在，则系统将清除以**location_name** 为名的地点的所有WiFi指纹信息，然后以Delay毫秒为间隔，重新扫描并录入N次WiFi指纹信息。

**Reset(String location_name)** 还有两个重载形式，分别为：
 * **Reset(String location_name,int delay)** 
 * **Reset(String location_name,int delay,int n)** 
 
 这两个重载形式可以修改设定本次重置操作的Delay值或N值，但不会修改系统的Delay值或N值。

由于**Reset(String location_name)** 是耗时操作，所以开发者不应在主线程当中使用本方法。
```java
String name="name_of_place";
try {
        wiFiLocationClient.Reset(name);
        } catch (WiFiLocationException e) {
        e.printStackTrace();
        } catch (InterruptedException e) {
        e.printStackTrace();
        }

```

#### 7.删除地点

我们可以使用 **Clear(String location_name)** 方法来删除某地点。该方法首先会判断输入的地点名在数据库中是否存在。若不存在，则系统抛出 **WiFiLocationException**异常 ；若存在，则系统将清除以**location_name** 为名的地点及其所有WiFi指纹信息。

```java
String name="name_of_place";
try {
        wiFiLocationClient.Clear(name);
        } catch (WiFiLocationException e) {
        e.printStackTrace();
        } catch (InterruptedException e) {
        e.printStackTrace();
        }

```

#### 8.删除所有地点

我们可以使用 **ClearAll()** 来删除数据库中的所有地点及WiFi指纹信息。
```java
wiFiLocationClient.ClearAll();
```
#### 9.获取所有地点

我们可以使用 **getLocation()** 来获取数据库中的所有地点。
```java
List<Location> locations=wifiLocationClient.getLocation();
```

### 定位相关方法

#### 1.获取和设定M值
WiFiLocation是基于K-NN算法的定位系统，但考虑到定位算法本身较为简单，极端情况下，会出现当前位置离系统预测地点较远时，系统仍依据K-NN算法判定当前位置已接近预测低点，导致较大误差的出现，遂引入曼哈顿距离阈值的机制，对K-NN的定位判断进行距离层面的修正，从而提高定位准确度。在本SDK中，曼哈顿距离阈值记为 **M** 值。

我们可以通过 **getM()** 获取系统当前M值，或 **setM(int m)** 以设定系统的M值。系统默认的M值为10。

```java
        int m_get=wiFiLocationClient.getM();
        int m_set=15;
        wiFiLocationClient.setM(m_set);
```



#### 2.获取目标地点定位排名

WiFiLocation是基于K-NN算法的定位系统，单次扫描后，计算库中所有可能属于当前位置的WiFi指纹信息与当前位置的WiFi指纹信息的曼哈顿距离，由小到大进行排序，获取前K个匹配度最高的WiFi指纹信息，再对这K个以地点作阈值为M的筛选，剔除超过M值的WiFi指纹信息，再以地址为单位进行count运算，得到候选地点各自的指纹信息数，由大到小进行排序，最终获得一个排名。依据K-NN算法，我们可以认定，排名第一的地点，最有可能是当前地点，而排名靠后的地点，亦可能在当前地点的附近。

我们可以使用 **LocationRank(String location_name)** 方法以获取以**location_name** 为名的地点在本次定位结果中的排名，返回值类型为int。

该方法首先会判断输入的地点名在数据库中是否存在。若不存在，则系统抛出 **WiFiLocationException**异常 ；若存在，则系统将返回排名值。若本次定位结果中没有目标地点，系统将返回 **0** 值。

**LocationRank(String location_name)** 还有两个重载形式：
 * **LocationRank(String location_name,int k)** 
 * **LocationRank(String location_name,int k,int m)** 
 
 这个重载形式可以修改设定本次定位操作的K值或M值，但不会修改系统的K值或M值。

 值得注意的是，系统没有提供单次仅修改M值的重载形式，所以开发者可选择调用 **getM()** 和 **setM(int m)** 方法来实现该效果。
 
 由于**LocationRank(String location_name)** 是耗时操作，所以开发者不应在主线程当中使用本方法。
```java
String name="name_of_place";
try {
        int rank=wiFiLocationClient.LocationRank(name);
        } catch (WiFiLocationException e) {
        e.printStackTrace();
        } catch (InterruptedException e) {
        e.printStackTrace();
        }

```
#### 3.获取目标地点定位排名列表

我们可以使用 **getLocateResult()** 方法来获取定位算法生成的排名列表。其定位过程与 **LocationRank(String location_name)** 方法相同。

 **getLocateResult()** 还有两个重载形式：
 *  **getLocateResult(int k)**
 *  **getLocateResult(int k,int m)** 

 这个重载形式可以修改设定本次定位操作的K值或M值，但不会修改系统的K值或M值。

 值得注意的是，系统没有提供单次仅修改M值的重载形式，所以开发者可选择调用 **getM()** 和 **setM(int m)** 方法来实现该效果。

由于 **getLocateResult()**  是耗时操作，所以开发者不应在主线程当中使用本方法。
```java
List<PredictResult2> result2s=wiFiLocationClient.getLocateResult();
```
## 更新

#### 17.9.25
* 修改了系统的定位算法，引入曼哈顿距离阈值机制
* 新增 **getM()** 和 **setM(int m)** 方法
* 修改 **LocationRank(String location_name)** 和 **getLocateResult()** 方法，并增加了重载形式

#### 17.9.22
* 修复 **Create(String location_name, int delay, int n)** 漏洞

#### 17.9.21
* 添加 **getLocation()** 方法
* 添加 **getLocateResult()** 方法

## 联系作者

由于时间久远，且立项时未撰写相关文档，故难以回答诸多实现细节（说人话：写完太久了，我自己都看不懂了。。。），遂就该项目不再提供答疑，非常抱歉。2020.3.2
