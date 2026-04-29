# AwLoadingView

`AwLoadingView` 是一个支持多种加载动画样式的加载指示器组件，提供统一的 API 接口。

## 功能概览

- 五种加载样式：圆形、水平条、点跳动、菊花旋转、条形波动
- 支持自定义颜色着色，默认灰色
- 可配置加载器尺寸
- 支持开始/停止控制
- 支持状态保存与恢复
- 生命周期感知自动管理

## 文件位置

- 组件实现：[AwLoadingView.kt](../src/main/java/com/answufeng/ui/widget/AwLoadingView.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwLoadingView
    android:id="@+id/loading"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:loading_style="circular"
    app:loading_tint="#3B82F6"
    app:loading_size="48dp" />
```

### 代码中使用

```kotlin
val loading = findViewById<AwLoadingView>(R.id.loading)

// 开始加载
loading.start()

// 停止加载
loading.stop()

// 切换样式
loading.style = AwLoadingView.Style.DOTS

// 设置颜色
loading.setColorTint(Color.RED)

// 设置尺寸
loading.loaderSizePx = 64.dp()
```

## 加载样式

| 样式 | 枚举值 | XML 值 | 说明 |
|---|---|---|---|
| 圆形 | `CIRCULAR` | `circular` | Material Design 圆形进度条（默认） |
| 点跳动 | `DOTS` | `dots` | 三个圆点依次跳动 |
| 水平条 | `HORIZONTAL` | `horizontal` | 圆角轨道上来回滑动的指示条 |
| 菊花旋转 | `FLOWER` | `flower` | iOS 风格菊花动画，8 根花瓣位置固定，透明度沿圆周流动 |
| 条形波动 | `BARS` | `bars` | 五根竖条波动动画 |

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `loading_style` | `enum` | `circular` | 加载样式：circular/dots/horizontal/flower/bars |
| `loading_tint` | `color` | `#999999`（灰色） | 着色颜色，不设置时默认灰色 |
| `loading_size` | `dimension` | `48dp` | 加载器尺寸（最小 24dp） |

## Kotlin API

### 样式设置

```kotlin
loading.style = AwLoadingView.Style.CIRCULAR
loading.style = AwLoadingView.Style.DOTS
loading.style = AwLoadingView.Style.HORIZONTAL
loading.style = AwLoadingView.Style.FLOWER
loading.style = AwLoadingView.Style.BARS
```

### 颜色和尺寸

```kotlin
loading.setColorTint(Color.BLUE)
loading.tintColor = Color.GREEN
loading.loaderSizePx = 64.dp()
```

### 控制方法

```kotlin
loading.start()           // 开始动画
loading.stop()            // 停止动画
loading.isAnimating       // 是否正在动画
```

## 动画时长

| 样式 | 时长 | 说明 |
|---|---|---|
| `CIRCULAR` | 系统默认 | 使用系统 ProgressBar 动画 |
| `HORIZONTAL` | 1200ms/周期 | 圆角轨道上来回滑动的指示条 |
| `DOTS` | 120ms/帧 | 逐帧动画，6 帧一个周期 |
| `FLOWER` | 750ms/周期 | 花瓣位置固定，透明度沿圆周流动，8 根花瓣 |
| `BARS` | 800ms/周期 | 正弦波驱动，5 根竖条 |

## 使用示例

### 在网络请求中使用

```kotlin
val loading = findViewById<AwLoadingView>(R.id.loading)

// 开始请求
loading.start()
apiService.fetchData { data ->
    // 请求完成
    loading.stop()
    // 更新 UI
}
```

### 自定义加载弹窗

```kotlin
val dialog = AlertDialog.Builder(context)
    .setView(R.layout.dialog_loading)
    .create()

val loading = dialog.findViewById<AwLoadingView>(R.id.loading)
loading?.style = AwLoadingView.Style.FLOWER

dialog.show()
```

### 配合数据绑定

```kotlin
viewModel.isLoading.observe(this) { isLoading ->
    if (isLoading) {
        loadingView.start()
        contentView.visibility = View.GONE
    } else {
        loadingView.stop()
        contentView.visibility = View.VISIBLE
    }
}
```

## 注意事项

- `loading_size` 最小值为 24dp
- `CIRCULAR` 使用系统 `ProgressBar`，其他样式为自定义动画
- 组件会在 `onAttachedToWindow` 自动恢复动画（如果之前正在运行）
- 组件会在 `onDetachedFromWindow` 自动停止动画
- `tintColor` 为 `null` 时默认使用灰色 `#999999`
- `FLOWER` 样式为 iOS 风格菊花，8 根花瓣位置固定不动，透明度沿圆周方向流动形成旋转视觉效果
