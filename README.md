DLNA-Cast
=========

|        Author: LIUWEI         |
|-------------------------------|
| Email: liuwei10074180@163.com |


[![dlna-cast]()](https://github.com/devin1014/DLNA-Cast)
[![version]()]()


基于Cling库封装的投屏库

- *支持移动端设备（DMC）搜索、连接、控制设备*
- *支持移动端设备和电视端设备状态同步（播放进度、音量控制）*
- *支持电视端设备播放器功能（DMP）


**`索引`**

[Dependencies](#dependencies)

[Proguard](#proguard)

[Guide](#guide)


Issue
-----
- [ ]  **部分情景下可能导致手机发热，耗电过快**
- [ ]  **已发现设备可能已过期**


Dependencies
------------

```groovy
dependencies {
    implementation 'com.liuwei.android.cast-dmc:${version}' // 手机端
    implementation 'com.liuwei.android.cast-dmp:${version}' // 电视端
}
```


Proguard
--------

```text
暂无
```

Guide
-----

Step1:
在AndroidManifest.xml中添加服务
```xml
<!-- 手机端服务 -->
<service android:name="com.liuwei.android.upnpcast.service.NLUpnpCastService" /> 

```

Step2:
在页面中注册回调事件
```java
class DemoActivity{
    public void onCreate(){
        NLUpnpCastManager.getInstance().addCastEventListener(mEventListener);        // 添加投屏事件回调
        NLUpnpCastManager.getInstance().addRegistryDeviceListener(mRegistryListener);// 添加发现设备事件
    }
    
    public void onDestroy(){
        NLUpnpCastManager.getInstance().removeRegistryListener(mRegistryListener);   // 移除投屏事件回调
        NLUpnpCastManager.getInstance().removeCastEventListener(mEventListener);     // 移除发现设备事件
    }
}
```

```java
public interface OnRegistryDeviceListener{
    void onDeviceAdded(CastDevice device);   // DLNA服务发现了有效设备
    void onDeviceRemoved(CastDevice device); // DLNA服务发现设备过期了
}

public interface ICastEventListener extends ICastControlListener{
    void onConnecting(CastDevice castDevice); // 用户正在尝试连接发现的设备
    void onConnected(CastDevice castDevice, TransportInfo transportInfo, MediaInfo mediaInfo, int volume); // 连接成功
    void onDisconnect(); // 用户断开已连接的设备
}

public interface ICastControlListener{
    void onCast(CastObject castObject); //用户尝试投屏
    void onStart(); // 投屏成功，电视端开始播放视频
    void onPause(); // 暂停视频
    void onStop();  // 停止投屏（不断开设备）
    void onSeekTo(long position);     // 快进
    void onError(String errorMsg);    // 电视端播放视频出错
    void onVolume(int volume);        // 电视端音量 
    void onBrightness(int brightness);// 电视端明亮度
    void onUpdatePositionInfo(PositionInfo positionInfo); // 电视端视频播放信息
}
```


Step3:
在页面中注册服务
```java
class DemoActivity {
    //...省略部分代码
    protected void onResume(){ 
        super.onResume();
        NLUpnpCastManager.getInstance().bindUpnpCastService(this);
    }
    
    protected void onPause(){ 
        NLUpnpCastManager.getInstance().unbindUpnpCastService(this);
        super.onPause();
    }
}
```

所有投屏行为事件的入口是NLUpnpCastManager类，这是一个单例类。