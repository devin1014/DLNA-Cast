# DLNA-Cast

|        Author: LIUWEI         |
|-------------------------------|
| Email: liuwei10074180@163.com |

[![Download](https://jitpack.io/v/devin1014/DLNA-Cast.svg)](https://jitpack.io/#devin1014/DLNA-Cast)

整理重构中....
整理重构中....
整理重构中....

有时间会更新这个库，好多人在问Dmr的问题，我这个只是最简单的一个VideoView示例，具体还是需要自己集成播放器，实现各种格式的流。
另外我也在做DLNA的Flutter库，差不多了也会开源出来。

投屏 爱奇艺、优酷、腾讯 TV端的时候，m3u8格式会失败，url需要带参数，带上之后就可以了，这个有时间再研究下（自己模拟了下，可以正常投屏了，但是应该有有效期，暂不清楚具体的机制）
国内Tv App很多不支持多码率的流，用单码率就就可以播放，但是爱奇艺不行，app应该有特殊的限制。

# 功能

基于Cling库封装的DLNA投屏库
* 支持移动端设备发现控制投射功能（DMC）
* 支持电视端设备播放器功能（DMR）
* 支持服务端设备共享内容（DMS）

Cling库(v2.1.1) 

[Cling Core](http://4thline.org/projects/cling/core/manual/cling-core-manual.xhtml)
[Cling Support](http://4thline.org/projects/cling/support/manual/cling-support-manual.xhtml)


#App示例

![AppScreenshot](https://raw.githubusercontent.com/devin1014/DLNA-Cast/master/screen/Screenshot_20230801_173015.png)
![AppScreenshot](https://raw.githubusercontent.com/devin1014/DLNA-Cast/master/screen/Screenshot_20230801_173051.png)
![AppScreenshot](https://raw.githubusercontent.com/devin1014/DLNA-Cast/master/screen/Screenshot_20230801_173059.png)
![AppScreenshot](https://raw.githubusercontent.com/devin1014/DLNA-Cast/master/screen/Screenshot_20230801_173117.png)

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
<service android:name="com.android.cast.dlna.dmr.DLNARendererService"/>
<service android:name="com.android.cast.dlna.dms.DLNAContentSercice"/>
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
DLNACastManager.getInstance().search();
```

### 监听设备
```
DLNACastManager.getInstance().registerDeviceListener(listener);
DLNACastManager.getInstance().unregisterListener(listener);
```
当发现新设备时需要添加到设备列表中用于显示。
* OnDeviceRegistryListener 该接口回调始终在**主线程**线程被调用

### 连接设备
```
deviceControl: DeviceControl = DLNACastManager.connectDevice(device, callback)

DeviceControl接口如下：
DeviceControl {
    // 投射当前视频
    fun setAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<Unit>?) {}
    // 投射下一个视频（不是每个播放器都支持这个功能，当前播放结束自动播放下一个）
    fun setNextAVTransportURI(uri: String, title: String, callback: ServiceActionCallback<Unit>?) {}
    // 播放
    fun play(speed: String, callback: ServiceActionCallback<Unit>?) {}
    // 暂停
    fun pause(callback: ServiceActionCallback<Unit>?) {}
    // 停止
    fun stop(callback: ServiceActionCallback<Unit>?) {}
    // 快进/快退
    fun seek(millSeconds: Long, callback: ServiceActionCallback<Unit>?) {}
    // 播放下一个视频
    fun next(callback: ServiceActionCallback<Unit>?) {}
    // 播放上一个视频
    fun previous(callback: ServiceActionCallback<Unit>?) {}
    // 获取当前投射视频的播放信息，当前时间/总时间
    fun getPositionInfo(callback: ServiceActionCallback<PositionInfo>?) {}
    // 获取当前视频信息
    fun getMediaInfo(callback: ServiceActionCallback<MediaInfo>?) {}
    // 获取当前播放状态等
    fun getTransportInfo(callback: ServiceActionCallback<TransportInfo>?) {}
    // 设置音量
    fun setVolume(volume: Int, callback: ServiceActionCallback<Unit>?) {}
    // 获取音量
    fun getVolume(callback: ServiceActionCallback<Int>?) {}
    // 设置静音
    fun setMute(mute: Boolean, callback: ServiceActionCallback<Unit>?) {}
    // 获取是否静音
    fun getMute(callback: ServiceActionCallback<Boolean>?) {}
    // 查询objectId的信息（‘0’默认值即所有信息）
    fun browse(objectId: String, flag: BrowseFlag, filter: String, firstResult: Int, maxResults: Int, callback: ServiceActionCallback<DIDLContent>?) {}
    // 查找objectId的信息
    fun search(containerId: String, searchCriteria: String, filter: String, firstResult: Int, maxResults: Int, callback: ServiceActionCallback<DIDLContent>?) {}
}

每个操作都有相应的参数和事件回调接口，监听操作是否成功
```
