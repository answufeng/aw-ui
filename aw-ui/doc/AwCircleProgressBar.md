# AwCircleProgressBar

`AwCircleProgressBar` 是一个轻量级的圆形进度条组件，支持自定义颜色、描边宽度、文字显示，以及平滑动画效果。

## 功能概览

- 支持自定义进度颜色和轨道颜色
- 可配置描边宽度
- 可选显示百分比文字
- 支持进度动画
- 支持状态保存与恢复
- 自定义起始角度和后缀文字

## 文件位置

- 组件实现：[AwCircleProgressBar.kt](E:/workspace/ASProjects/AutoKs/viewtest/src/main/java/com/answufeng/ui/widget/AwCircleProgressBar.kt)
- 自定义属性：[attrs.xml](E:/workspace/ASProjects/AutoKs/viewtest/src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwCircleProgressBar
    android:id="@+id/progressBar"
    android:layout_width="120dp"
    android:layout_height="120dp"
    app:circleProgress_progress="50"
    app:circleProgress_max="100"
    app:circleProgress_strokeWidth="8dp"
    app:circleProgress_progressColor="#4CAF50"
    app:circleProgress_bgColor="#E0E0E0"
    app:circleProgress_showText="true"
    app:circleProgress_textSize="14sp"
    app:circleProgress_textColor="#333333" />
```

### 代码中使用

```kotlin
val progressBar = findViewById<AwCircleProgressBar>(R.id.progressBar)

// 设置进度
progressBar.progress = 75f

// 设置进度（带动画）
progressBar.setProgressWithAnimation(75f, duration = 800L)

// 设置最大值
progressBar.max = 200f

// 同时设置进度和最大值
progressBar.setProgressAndMax(150f, 200f)
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `circleProgress_progress` | `float` | `0` | 当前进度值 |
| `circleProgress_max` | `float` | `100` | 最大值 |
| `circleProgress_strokeWidth` | `dimension` | `8dp` | 描边宽度 |
| `circleProgress_progressColor` | `color` | `#4CAF50` | 进度颜色 |
| `circleProgress_bgColor` | `color` | `#E0E0E0` | 轨道颜色 |
| `circleProgress_showText` | `boolean` | `true` | 是否显示文字 |
| `circleProgress_textSize` | `dimension` | `14sp` | 文字大小 |
| `circleProgress_textColor` | `color` | `#333333` | 文字颜色 |
| `circleProgress_startAngle` | `float` | `-90` | 起始角度（度） |
| `circleProgress_suffix` | `string` | `%` | 文字后缀 |

## Kotlin API

### 进度设置

```kotlin
progressBar.progress = 50f                    // 直接设置进度
progressBar.max = 100f                        // 设置最大值
progressBar.setProgressWithAnimation(75f)     // 带动画设置进度
progressBar.setProgressAndMax(150f, 200f)    // 同时设置进度和最大值
```

### 属性设置

```kotlin
progressBar.progressColor = Color.GREEN       // 进度颜色
progressBar.trackColor = Color.GRAY           // 轨道颜色
progressBar.strokeWidthPx = 12f.dp()          // 描边宽度（像素）
progressBar.showText = false                  // 隐藏文字
progressBar.textColor = Color.BLACK           // 文字颜色
progressBar.textSizePx = 16f.sp()             // 文字大小（像素）
progressBar.startAngle = 0f                   // 起始角度
progressBar.progressSuffix = "/"              // 文字后缀
```

### 状态查询

```kotlin
progressBar.progress    // 当前进度
progressBar.max         // 最大值
```

## 使用示例

### 显示下载进度

```kotlin
val progressBar = AwCircleProgressBar(context).apply {
    max = 100f
    progressColor = Color.BLUE
    trackColor = Color.LTGRAY
    strokeWidthPx = 6f.dp()
}

// 更新进度
downloadManager.setOnProgressListener { progress ->
    progressBar.setProgressWithAnimation(progress)
}
```

### 自定义起始角度

```kotlin
// 从右侧开始（0度）
progressBar.startAngle = 0f

// 从底部开始（90度）
progressBar.startAngle = 90f
```

### 自定义后缀

```kotlin
// 显示 "50/100"
progressBar.progressSuffix = "/100"
progressBar.progress = 50f
```

## 注意事项

- 进度值会自动限制在 `0` 到 `max` 之间
- 组件会自动保持正方形比例
- 动画会在 `onDetachedFromWindow` 时自动取消
- `startAngle` 以度为单位，`-90` 表示从顶部开始（默认）
