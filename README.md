# DLNA-Cast

|        Author: LIUWEI         |
|-------------------------------|
| Email: liuwei10074180@163.com |

[![Download](https://jitpack.io/v/devin1014/DLNA-Cast.svg)](https://jitpack.io/#devin1014/DLNA-Cast)

# 功能

基于Cling库封装的DLNA投屏库
* 支持移动端设备控制（DMC）功能
* 支持投射本地视频（DMS）或者网络视频
* 支持电视端设备播放器功能（DMR）

Cling库(v2.1.1) 

[Cling Core](http://4thline.org/projects/cling/core/manual/cling-core-manual.xhtml)
[Cling Support](http://4thline.org/projects/cling/support/manual/cling-support-manual.xhtml)


#App示例


![AppScreenshot](https://raw.githubusercontent.com/devin1014/DLNA-Cast/master/screen/device-2021-05-13-155608.png)

## 使用说明
### 引用地址
在项目根gradle中引入
```
allprojects {
	repositories {
		...
        maven { url 'http://4thline.org/m2' }
		maven { url 'https://jitpack.io' }
	}
}
```
在项目模块gradle中引入

```
api 'com.github.devin1014.DLNA-Cast:dlna-dmc:V1.0.0'
```

### 权限申明
在AndroidManifest.xml中需要添加如下

```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
```

### 服务申明
在AndroidManifest.xml中需要添加如下

```
<service android:name="com.android.cast.dlna.dmc.DLNACastService"/>
```

### 注册服务
在Activity或者Fragment中绑定/解绑
```
@Override
protected void onStart() {        
    DLNACastManager.getInstance().bindCastService(this);
}

@Override
protected void onStop() {
    DLNACastManager.getInstance().unbindCastService(this);
}
```

当绑定服务后，会自动搜索设备，也可以手动搜索。
```
DLNACastManager.getInstance().search(null, 60);
```
* type：需要搜索设备的类型，null表示不限制类型
* maxSeconds：搜索最大搜索时长（单位：秒）

### 监听设备
```
DLNACastManager.getInstance().registerDeviceListener(listener);
DLNACastManager.getInstance().unregisterListener(listener);
```
当发现新设备时需要添加到设备列表中用于显示。
* OnDeviceRegistryListener 该接口回调始终在**主线程**线程被调用

### 投屏

```
DLNACastManager.getInstance().cast(device, castObject)
```

* device：已发现的设备（这个设备需要是Renderer类型，支持播放器才能投屏）
* castObject：实现了ICast接口的实现类（主要参数是投屏的url）

### 事件监听
控制器事件监听

```
DLNACastManager.getInstance().registerActionCallbacks(callbacks);
```
* callbacks: 操作事件回调接口，主要有如下事件接口(*投屏、播放、暂停、停止、快进*)
 **CastEventListener**、**PlayEventListener**、**PauseEventListener**、**StopEventListener**、**SeekToEventListener**
以上接口都继承自*IServiceAction.IServiceActionCallback<Long>* 

```
interface IServiceActionCallback<T> {
    void onSuccess(T result); //成功
    void onFailed(String errMsg); //失败
}
```

播放器事件监听
`DLNACastManager.getInstance().registerSubscriptionListener(listener)`
* listener:监听播放器端状态改变（根据测试每个电视端实现不同效果也不同，有的发有的不发！）

```
interface ISubscriptionListener {
    void onSubscriptionTransportStateChanged(TransportState event);
}
```
### 控制
目前支持的控制事件如下

```
interface IControl {
    void cast(Device<?, ?, ?> device, ICast object);
    boolean isCasting(Device<?, ?, ?> device);
    boolean isCasting(Device<?, ?, ?> device, @Nullable String uri);
    void stop();
    void play();
    void pause();
    void seekTo(long position);
    void setVolume(int percent);
    void setMute(boolean mute);
    void setBrightness(int percent);
}
```
### 查询

```
 interface IGetInfo {
    void getMediaInfo(Device<?, ?, ?> device, ICastInterface.GetInfoListener<MediaInfo> listener);
    void getPositionInfo(Device<?, ?, ?> device, ICastInterface.GetInfoListener<PositionInfo> listener);
    void getTransportInfo(Device<?, ?, ?> device, ICastInterface.GetInfoListener<TransportInfo> listener);
    void getVolumeInfo(Device<?, ?, ?> device, ICastInterface.GetInfoListener<Integer> listener);
    void getContent(Device<?, ?, ?> device, ContentType contentType, ICastInterface.GetInfoListener<DIDLContent> listener);
}
```
* getMediaInfo：获取媒体信息
* getPositionInfo：获取视频播放位置
* getTransportInfo：获取当前设备播放状态
* getVolumeInfo：获取当前设备音量信息
* getContent：获取当前设备提供的目录信息（当前设备是DMS）
