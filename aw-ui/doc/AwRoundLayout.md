# AwRoundLayout

`AwRoundLayout` 是一个圆角裁切容器布局，支持统一圆角或四角独立配置，可选描边效果。

## 功能概览

- 统一圆角或四角独立圆角
- 可选描边效果（颜色和宽度）
- 统一圆角时使用 `ViewOutlineProvider`（硬件加速）
- 独立圆角时回退到 `Canvas.clipPath`
- 子视图自动被圆角裁切

## 文件位置

- 组件实现：[AwRoundLayout.kt](../src/main/java/com/answufeng/ui/widget/AwRoundLayout.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwRoundLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:round_radius="12dp"
    app:round_strokeColor="#FF0000"
    app:round_strokeWidth="1dp">

    <!-- 子视图将被圆角裁切 -->
    <TextView ... />
</com.answufeng.ui.widget.AwRoundLayout>
```

### 代码中使用

```kotlin
val roundLayout = findViewById<AwRoundLayout>(R.id.roundLayout)

roundLayout.setRadius(16f.dp())
roundLayout.setRadii(16f.dp(), 0f, 16f.dp(), 0f)  // 四角独立
roundLayout.setStroke(Color.RED, 2f.dp())
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `round_radius` | `dimension` | `0` | 统一圆角半径 |
| `round_topLeftRadius` | `dimension` | `round_radius` | 左上角半径 |
| `round_topRightRadius` | `dimension` | `round_radius` | 右上角半径 |
| `round_bottomLeftRadius` | `dimension` | `round_radius` | 左下角半径 |
| `round_bottomRightRadius` | `dimension` | `round_radius` | 右下角半径 |
| `round_strokeColor` | `color` | 透明 | 描边颜色 |
| `round_strokeWidth` | `dimension` | `0` | 描边宽度 |

## Kotlin API

### 圆角设置

```kotlin
roundLayout.setRadius(12f.dp())                                    // 统一圆角
roundLayout.setRadii(16f.dp(), 0f, 16f.dp(), 0f)                  // 四角独立（左上、右上、右下、左下）
```

### 描边设置

```kotlin
roundLayout.strokeColor = Color.RED
roundLayout.strokeWidth = 2f.dp()
roundLayout.setStroke(Color.RED, 2f.dp())  // 同时设置
```

## 使用示例

### 仅顶部圆角

```xml
<com.answufeng.ui.widget.AwRoundLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:round_topLeftRadius="16dp"
    app:round_topRightRadius="16dp"
    app:round_bottomLeftRadius="0dp"
    app:round_bottomRightRadius="0dp">
    <!-- 内容 -->
</com.answufeng.ui.widget.AwRoundLayout>
```

### 带描边卡片

```xml
<com.answufeng.ui.widget.AwRoundLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:round_radius="12dp"
    app:round_strokeColor="#E0E0E0"
    app:round_strokeWidth="1dp">
    <!-- 内容 -->
</com.answufeng.ui.widget.AwRoundLayout>
```

## 注意事项

- 四角半径相同时使用 `ViewOutlineProvider`（性能更好）
- 四角半径不同时回退到 `Canvas.clipPath`
- `clipPath` 方式在部分低版本设备上可能有性能差异
- 描边绘制在子视图之上
- `round_radius` 作为未单独指定角的默认值
