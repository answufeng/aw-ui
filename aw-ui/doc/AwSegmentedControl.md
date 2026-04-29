# AwSegmentedControl

`AwSegmentedControl` 是一个分段/Tab 控件，支持多种选中样式、图标、指示器，以及 `ViewPager2` 联动。

## 功能概览

- 四种选中样式：胶囊、下划线、文字色变化、圆角矩形
- 支持仅文字、仅图标、图标+文字
- 底部小圆点指示器
- 支持 `ViewPager2` 双向联动
- 支持状态保存与恢复
- 选中动画平滑过渡

## 文件位置

- 组件实现：[AwSegmentedControl.kt](../src/main/java/com/answufeng/ui/widget/AwSegmentedControl.kt)
- 辅助类：[SegmentTab.kt](../src/main/java/com/answufeng/ui/widget/SegmentTab.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwSegmentedControl
    android:id="@+id/segmentedControl"
    android:layout_width="match_parent"
    android:layout_height="40dp"
    app:seg_items="@array/segment_items"
    app:seg_selectedIndex="0"
    app:seg_selectionAppearance="pill"
    app:seg_selectedColor="#4F46E5" />
```

### 代码中使用

```kotlin
val segmented = findViewById<AwSegmentedControl>(R.id.segmentedControl)

segmented.items = listOf("选项1", "选项2", "选项3")
segmented.onSelectionChange = { index ->
    // 处理选中变化
}
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `seg_items` | `reference` | - | 字符串数组资源 |
| `seg_selectedIndex` | `integer` | `0` | 初始选中索引 |
| `seg_selectedColor` | `color` | `#FFFFFF` | 选中高亮颜色 |
| `seg_textColor` | `color` | `#99000000` | 未选中文字颜色 |
| `seg_selectedTextColor` | `color` | `#FF000000` | 选中文字颜色 |
| `seg_selectionAppearance` | `enum` | `pill` | 选中样式：pill/underline/text_tint/rect |
| `seg_rectCornerRadius` | `dimension` | `4dp` | rect 样式圆角 |
| `seg_accessoryIndicator` | `enum` | `none` | 辅助指示器：none/dot |
| `seg_iconSize` | `dimension` | `18dp` | 图标尺寸 |

## 选中样式（SelectionAppearance）

| 样式 | 枚举值 | 说明 |
|---|---|---|
| 胶囊 | `PILL` | 圆角胶囊高亮（默认） |
| 下划线 | `UNDERLINE` | 底部线条 |
| 文字色 | `TEXT_TINT` | 仅文字颜色变化 |
| 圆角矩形 | `RECT` | 圆角/直角矩形高亮 |

## SegmentTab

```kotlin
data class SegmentTab(
    val label: String = "",
    @DrawableRes val iconRes: Int = 0
)
```

- `label`：标题文本
- `iconRes`：图标资源 id
- `hasIcon`：是否有图标（`iconRes != 0`）
- `hasLabel`：是否有文字（`label.isNotEmpty()`）

## Kotlin API

### 数据设置

```kotlin
segmented.items = listOf("首页", "发现", "我的")
segmented.tabs = listOf(
    SegmentTab("拍照", R.drawable.ic_camera),
    SegmentTab("相册", R.drawable.ic_gallery)
)
```

### 选中控制

```kotlin
segmented.selectedIndex = 1
segmented.onSelectionChange = { index -> }
```

### 样式设置

```kotlin
segmented.selectionAppearance = AwSegmentedControl.SelectionAppearance.PILL
segmented.accessoryIndicator = AwSegmentedControl.AccessoryIndicator.DOT
segmented.selectedColor = Color.WHITE
segmented.textColor = Color.GRAY
segmented.selectedTextColor = Color.BLACK
```

### ViewPager2 联动

```kotlin
segmented.bindViewPager2(viewPager2)
segmented.unbindViewPager2()
```

## 使用示例

### 配合 ViewPager2

```kotlin
segmented.tabs = listOf(
    SegmentTab("首页", R.drawable.ic_home),
    SegmentTab("发现", R.drawable.ic_discover),
    SegmentTab("我的", R.drawable.ic_me)
)
viewPager2.adapter = MyPagerAdapter(this)
segmented.bindViewPager2(viewPager2)
```

### 仅文字色变化 + 圆点

```xml
<com.answufeng.ui.widget.AwSegmentedControl
    app:seg_selectionAppearance="text_tint"
    app:seg_accessoryIndicator="dot" />
```

## 注意事项

- 绑定 `ViewPager2` 时请保证页数与 `tabs` 数量一致
- 组件在 `onDetachedFromWindow` 时自动解绑 `ViewPager2`
- `PILL` 样式的圆角自动取高度的一半
- `RECT` 样式可通过 `seg_rectCornerRadius` 自定义圆角
