# WiFiLocation
WiFiLocation是一款基于K-NN算法的简易WiFi定位系统。它借助 **[LitePal](https://github.com/LitePalFramework/LitePal)** 建立本地的WiFi指纹数据库，通过一系列API实现目标地点的WiFi指纹搜集、更新、重置或删除，以及输入目标地点，根据实时的WiFi指纹信息，输出当前位置为目标地点的可能性。

相较于传统的C/S架构的WiFi定位系统，本系统实现了完全本地化的运行，所有功能均不会产生任何网络费用。另一方面，完全依赖本地的指纹数据库以及靠手机本身进行定位运算，也大大限制了这款定位系统的定位准确度和效率。

即便性能远不如主流的GPS、移动蜂窝网络及各大地图运营商所提供的在线WiFi室内定位系统，WiFiLocation依旧拥有其独特的使用场景需求：
#### 1.不借助GPS或蜂窝网络进行常见地点的定位识别

这里所指的常见地点，往往为用户的住处、工作场所或教室。在常见地点有限的前提下，WiFiLocation可以实现与GPS或蜂窝网络大体相近的定位识别而不会产生额外的网络费用。
#### 2.主流地图运营商没有覆盖的室内空间定位

地图运营商的室内WiFi定位往往局限于热门地点，例如大型商场。对于诸如学生宿舍楼层内空间等非热门地点，运营商往往无法提供较为准确的定位。而利用WiFiLocation，用户可以自行构建室内定位坐标，实现室内定位。

对于以下使用需求，我们强烈不建议使用WifiLocation：
#### 1.高精度定位

由于WiFiLocation是基于K-NN算法的定位系统，定位算法和条件判断较为简单，所以该系统的定位精度较差。对精度要求较高的开发者，我们建议使用主流网络运营商提供的GPS或蜂窝网络定位方案。

#### 2.大量地点的识别定位

WiFiLocation是完全本地化的定位系统，因而它的可存储地点信息数目受限于Sqlite数据库的性能以及手机的内存，其定位速度也受限于地点数目及手机性能。当数据库储存的地点信息过多时，WiFiLocation单次定位的所需时间会过长，“实时”定位的效果不再显现，整个系统的运行效益将不复存在。对于需要存储大量地点的开发者，我们同样不建议使用本系统。

下面我们介绍WiFiLocation的安装和使用。

## 下载
* **[WiFiLocation.jar](https://github.com/mozhiingithub/WiFiLocation/raw/master/WiFiLocation.jar)**

## 安装
#### 1.加载jar包

打开Android Studio，创建项目，将下载好的WiFiLocation.jar复制到libs文件夹当中，并右键点击“Add As Library”。

#### 2.添加权限

在 **AndroidManifest.xml** 文件中加入以下代码：

``` xml
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

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
#### 5.初始化WiFiLocationClient

在onCreate方法中对WifiLocationClient进行初始化：

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

#### 1.获取和设定K值

WifiLocation是基于K-NN算法的定位系统，我们可以通过 **getK()** 获取系统当前K值，或 **setK(int k)** 以设定系统的K值。系统默认的K值为10。

```java
        int k_get=wiFiLocationClient.getK();
        int k_set=7;
        wiFiLocationClient.setK(k_set);
```

#### 2.获取和设定N值

在WifiLocation当中，调用一次涉及WiFi指纹录入操作的API，系统会自动录入N次，我们可以通过 **getN()** 获取系统当前N值，或 **setN(int n)** 以设定系统的N值。系统默认的N值为10。

```java
        int n_get=wiFiLocationClient.getN();
        int n_set=1;
        wiFiLocationClient.setN(n_set);
```

#### 3.获取和设定Delay值

在WifiLocation当中，调用一次涉及WiFi指纹录入操作的API，系统录入N次指纹的间隔为Delay毫秒，我们可以通过 **getDelay()** 获取系统当前Delay值，或 **setDelay(int delay)** 以设定系统的Delay值。系统默认的Delay值为1000。**虽无强制规定，但我们强烈不建议将Delay设定为低于1000的值。**

```java
        int delay_get=wiFiLocationClient.Delay();
        int delay_set=3000;
        wiFiLocationClient.setDelay(delay_set);
```

#### 4.创建新地点

我们可以使用 **Create(String location_name)** 方法创建新地点。该方法首先会判断输入的地点名是否已在数据库中存在。若存在，则系统抛出 **WiFiLocationException异常** ；若不存在，则系统创建以 **location_name** 为名的新地点，并以Delay毫秒为间隔，扫描并获取N次WiFi指纹信息。

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
