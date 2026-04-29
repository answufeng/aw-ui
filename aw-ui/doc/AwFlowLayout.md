# AwFlowLayout

`AwFlowLayout` 是一个流式布局，子 View 从左到右排列，当前行剩余宽度不够时自动折行。适用于标签列表、搜索历史等场景。

## 功能概览

- 子 View 自动换行排列
- 支持水平/垂直间距
- 支持最大行数限制
- 支持行内对齐方式（左/中/右）
- 支持 RTL 布局
- 支持 MarginLayoutParams

## 文件位置

- 组件实现：[AwFlowLayout.kt](../src/main/java/com/answufeng/ui/widget/AwFlowLayout.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwFlowLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:flow_horizontalSpacing="8dp"
    app:flow_verticalSpacing="8dp"
    app:flow_maxLines="3"
    app:flow_gravity="start">

    <TextView ... />
    <TextView ... />
</com.answufeng.ui.widget.AwFlowLayout>
```

### 代码中使用

```kotlin
val flowLayout = findViewById<AwFlowLayout>(R.id.flowLayout)

val tags = listOf("Kotlin", "Android", "Material3", "ViewBinding")
for (tag in tags) {
    val tv = TextView(context).apply {
        text = tag
        setPadding(24, 12, 24, 12)
    }
    flowLayout.addView(tv)
}
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `flow_horizontalSpacing` | `dimension` | `8dp` | 子 View 水平间距 |
| `flow_verticalSpacing` | `dimension` | `8dp` | 子 View 垂直间距（行间距） |
| `flow_maxLines` | `integer` | `0` | 最大行数（0 = 不限制） |
| `flow_gravity` | `enum` | `start` | 行内对齐方式：start/center/end |

## Kotlin API

### 属性设置

```kotlin
flowLayout.horizontalSpacing = 12.dp()   // 水平间距
flowLayout.verticalSpacing = 8.dp()      // 垂直间距
flowLayout.maxLines = 3                  // 最大行数
flowLayout.flowGravity = Gravity.CENTER  // 行内居中
```

## 对齐方式说明

| 值 | 说明 |
|---|---|
| `Gravity.START` | 左对齐（默认） |
| `Gravity.CENTER_HORIZONTAL` | 居中对齐 |
| `Gravity.END` | 右对齐 |

## 使用示例

### 标签列表

```kotlin
val tags = listOf("Kotlin", "Java", "Python", "Go", "Rust", "Swift")
flowLayout.removeAllViews()
for (tag in tags) {
    val tv = TextView(context).apply {
        text = tag
        setPadding(24, 12, 24, 12)
        textSize = 14f
        background = GradientDrawable().apply {
            cornerRadius = 16f
            setColor(Color.parseColor("#F0F0F0"))
        }
    }
    flowLayout.addView(tv)
}
```

### 限制行数

```xml
<com.answufeng.ui.widget.AwFlowLayout
    app:flow_maxLines="2"
    app:flow_gravity="center" />
```

## 注意事项

- 子 View 需要使用 `MarginLayoutParams`（默认就是）
- `maxLines = 0` 表示不限制行数
- 超出 `maxLines` 的子 View 不会被绘制
- 自动支持 RTL 布局方向
