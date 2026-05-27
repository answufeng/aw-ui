# AwMarqueeTextView

`AwMarqueeTextView` 是一个跑马灯文本视图，支持水平滚动和自定义速度/方向/暂停时长。

> **使用建议**：属于**特殊场景**组件（公告条、单行标题溢出等）。列表内长文本优先用 `RecyclerView`；系统自带 `android:ellipsize="marquee"` 可满足简单需求。库内**不计划扩展**更多能力。

## 功能概览

- 文字超出宽度时自动滚动
- 支持从右到左和从左到右方向
- 可配置滚动速度和暂停时长
- 滚动一轮后暂停再继续
- 自动处理窗口附着/分离

## 文件位置

- 组件实现：[AwMarqueeTextView.kt](../src/main/java/com/answufeng/ui/widget/AwMarqueeTextView.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwMarqueeTextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="这是一条滚动公告：支持超长文本自动滚动展示。"
    android:textSize="14sp"
    app:marquee_speed="2"
    app:marquee_pauseDuration="2000"
    app:marquee_direction="right_to_left" />
```

### 代码中使用

```kotlin
val marquee = findViewById<AwMarqueeTextView>(R.id.marquee)
marquee.setText("新的滚动文本内容")
marquee.speed = 1.5f
marquee.pauseDuration = 1500L
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `marquee_speed` | `float` | `1` | 滚动速度（越大越快） |
| `marquee_pauseDuration` | `integer` | `1000` | 一轮滚动结束后暂停时长（毫秒） |
| `marquee_direction` | `enum` | `right_to_left` | 滚动方向：left_to_right/right_to_left |

## 滚动方向

| 方向 | 枚举值 | 说明 |
|---|---|---|
| 从右到左 | `RIGHT_TO_LEFT` | 文字从右侧滑入（默认） |
| 从左到右 | `LEFT_TO_RIGHT` | 文字从左侧滑入 |

## Kotlin API

### 属性设置

```kotlin
marquee.speed = 2f                              // 滚动速度
marquee.pauseDuration = 2000L                   // 暂停时长
marquee.direction = AwMarqueeTextView.Direction.LEFT_TO_RIGHT  // 方向
```

## 使用示例

### 公告栏

```xml
<com.answufeng.ui.widget.AwMarqueeTextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="重要通知：系统将于今晚22:00进行维护升级，届时服务将暂停，请提前保存数据。"
    android:textColor="#FF6B6B"
    android:textSize="14sp"
    app:marquee_speed="1"
    app:marquee_pauseDuration="3000" />
```

### 动态更新文本

```kotlin
marqueeTextView.setText("新的公告内容：${announcement}")
```

## 注意事项

- 文字宽度不超过视图宽度时不会滚动
- 组件在 `onAttachedToWindow` 自动开始滚动
- 组件在 `onDetachedFromWindow` 自动停止滚动
- 速度最小值为 0.1，低于此值会被强制调整
