# AlphaZoomScrollView

### 类似QQ空间的布局效果

欢迎大家下载体验本项目，如果使用过程中遇到什么问题，欢迎反馈。(本项目主要参考 https://github.com/jeasonlzy/PullZoomView 稍加改动)

### 联系方式

 * 邮箱地址：wangtao090620@gmail.com


## 演示
 ![](/screenshot/alphaview.gif)

## 温馨提示

使用

```java
     compile project(':alphazoomscrollview')
```


## 自定义属性
<table>
  <tdead>
    <tr>
      <th align="center">自定义属性名字</th>
      <th align="center">参数含义</th>
    </tr>
  </tdead>
  <tbody>
    <tr>
      <td align="center">pzav_sensitive</td>
      <td align="center">图片放大效果相对于手指滑动距离的敏感度，越小越敏感，默认值 1.5</td>
    </tr>
    <tr>
      <td align="center">pzav_isZoomEnable</td>
      <td align="center">是否允许下拉时头部放大效果，默认 true，即为允许</td>
    </tr>
    <tr>
      <td align="center">pzav_isParallax</td>
      <td align="center">滑动时，是否头部具有视差动画，默认 true， 即为有</td>
    </tr>
    <tr>
      <td align="center">pzav_zoomTime</td>
      <td align="center">松手时，缩放头部还原到原始大小的时间，单位毫秒，默认 500毫秒</td>
    </tr>
  </tbody>
</table>

## 使用注意事项

 * `AlphaZoomScrollView` 使用时，需要设置Tag才能正常工作，否者会抛出异常，即在xml布局中对应的View中，加入如下tag，详细请参看Demo。
 * `android:tag="header`
 
渐变的颜色设置，RGB，代码

```
public void setColorRed(int colorRed) {
	this.mColorRed = colorRed;
}

public void setColorGreen(int colorGreen) {
	this.mColorGreen = colorGreen;
}

public void setColorBlue(int colorBlue) {
	this.mColorBlue = colorBlue;
}

```


