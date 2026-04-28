# AwBottomTabBar

`AwBottomTabBar` 是一个支持图标/文字、角标、指示器、`ViewPager2` 联动，以及固定/可滚动布局的底部 Tab 组件。

当前默认行为：
- 布局模式：`FIXED`
- 显示模式：`ICON_TEXT`
- 指示器：`NONE`

## 功能概览

- 三种显示模式：图标+文字、仅图标、仅文字
- 两种布局模式：平分、可横向滚动
- 指示器支持关闭、铺满 Tab、跟随文字宽度
- 支持数字角标、文字角标、红点角标
- 支持点击、重复点击、长按事件
- 支持 `ViewPager2` 联动
- 支持运行时增删改查 Tab
- 支持状态恢复

## 文件位置

- 组件实现：[AwBottomTabBar.kt](E:/workspace/ASProjects/AutoKs/viewtest/src/main/java/com/answufeng/ui/widget/AwBottomTabBar.kt)
- 自定义属性：[attrs.xml](E:/workspace/ASProjects/AutoKs/viewtest/src/main/res/values/attrs.xml)
- 主示例页：[MainActivity.kt](E:/workspace/ASProjects/AutoKs/viewtest/src/main/java/com/ail/ail_image/viewtest/MainActivity.kt)
- ViewPager 示例页：[ViewPagerActivity.kt](E:/workspace/ASProjects/AutoKs/viewtest/src/main/java/com/ail/ail_image/viewtest/ViewPagerActivity.kt)

## 快速开始

### 1. XML 中声明

```xml
<com.answufeng.ui.widget.AwBottomTabBar
    android:id="@+id/tabBar"
    android:layout_width="match_parent"
    android:layout_height="68dp"
    app:tab_layout_mode="fixed"
    app:tab_mode="icon_text"
    app:indicator_style="none"
    app:tab_icon_size="24dp"
    app:tab_text_size="12sp"
    app:tab_selected_color="#FF6B3D"
    app:tab_normal_color="#7A7F8F" />
```

### 2. 代码中设置数据

```kotlin
val tabBar = findViewById<AwBottomTabBar>(R.id.tabBar)

tabBar.setItems(
    listOf(
        AwBottomTabBar.TabItem(title = "首页", iconRes = R.drawable.ic_home),
        AwBottomTabBar.TabItem(title = "发现", iconRes = R.drawable.ic_discover),
        AwBottomTabBar.TabItem(title = "消息", iconRes = R.drawable.ic_message),
        AwBottomTabBar.TabItem(title = "我的", iconRes = R.drawable.ic_me)
    )
)
```

## TabItem

```kotlin
AwBottomTabBar.TabItem(
    title = "首页",
    icon = null,
    iconRes = R.drawable.ic_home,
    titleRes = 0
)
```

字段说明：
- `title`：标题文本
- `icon`：直接传入 `Drawable`
- `iconRes`：图标资源 id
- `titleRes`：字符串资源 id

说明：
- `titleRes != 0` 时优先使用 `titleRes`
- `icon != null` 时优先使用 `icon`

## XML 属性说明

### 数据相关

- `app:tab_titles`
  - 类型：`reference`
  - 说明：字符串数组资源

- `app:tab_icons`
  - 类型：`reference`
  - 说明：图标数组资源

### 显示相关

- `app:tab_mode`
  - 可选值：`icon_text`、`icon_only`、`text_only`
  - 默认值：`icon_text`

- `app:tab_layout_mode`
  - 可选值：`fixed`、`scrollable`
  - 默认值：`fixed`

- `app:tab_icon_size`
  - 类型：`dimension`
  - 默认值：`24dp`

- `app:tab_icon_height`
  - 类型：`dimension`
  - 默认值：`0dp`
  - 说明：`0` 表示跟随 `tab_icon_size`

- `app:tab_text_size`
  - 类型：`dimension`
  - 默认值：`12sp`

- `app:tab_icon_text_gap`
  - 类型：`dimension`
  - 默认值：`4dp`

- `app:tab_margin`
  - 类型：`dimension`
  - 默认值：`0dp`
  - 说明：Tab 之间的间距

- `app:tab_min_width`
  - 类型：`dimension`
  - 默认值：`56dp`
  - 说明：在 `scrollable` 模式下建议设置

- `app:tab_selected_color`
  - 类型：`color`
  - 默认值：`@color/tab_selected_default`

- `app:tab_normal_color`
  - 类型：`color`
  - 默认值：`@color/tab_normal_default`

- `app:tab_auto_tint`
  - 类型：`boolean`
  - 默认值：`true`
  - 说明：是否自动给图标着色

- `app:tab_selected_scale`
  - 类型：`float`
  - 默认值：`1.1`

- `app:tab_scale_duration`
  - 类型：`integer`
  - 默认值：`200`

### 指示器相关

- `app:indicator_style`
  - 可选值：`line`、`none`
  - 默认值：`none`

- `app:indicator_width_mode`
  - 可选值：`match_tab`、`follow_text`
  - 默认值：`match_tab`
  - 说明：
    - `match_tab`：宽度等于当前 Tab
    - `follow_text`：宽度跟随文字实际绘制宽度

- `app:indicator_color`
  - 类型：`color`
  - 默认值：`@color/tab_selected_default`

- `app:indicator_height`
  - 类型：`dimension`
  - 默认值：`3dp`

- `app:indicator_marginTop`
  - 类型：`dimension`
  - 默认值：`0dp`

- `app:indicator_corner_radius`
  - 类型：`dimension`
  - 默认值：`-1`
  - 说明：小于 `0` 时自动取半高

- `app:indicator_anim_duration`
  - 类型：`integer`
  - 默认值：`300`

### 角标相关

- `app:badge_background_color`
  - 类型：`color`
  - 默认值：`@color/tab_badge_default`

- `app:badge_text_color`
  - 类型：`color`
  - 默认值：`#FFFFFFFF`

- `app:badge_min_width`
  - 类型：`dimension`
  - 默认值：`16dp`

- `app:badge_padding`
  - 类型：`dimension`
  - 默认值：`4dp`

### 背景和交互相关

- `app:background_color`
  - 类型：`color`
  - 说明：组件背景色

- `app:enable_ripple`
  - 类型：`boolean`
  - 默认值：`true`

- `app:ripple_color`
  - 类型：`color`
  - 默认值：`0x33000000`

- `app:tabbar_elevation`
  - 类型：`dimension`
  - 默认值：`8dp`

- `app:corner_radius`
  - 类型：`dimension`
  - 默认值：`0dp`

- `app:enable_scroll_sync`
  - 类型：`boolean`
  - 默认值：`true`
  - 说明：是否在 `ViewPager2` 滑动过程中同步指示器动画

## Kotlin API

### 数据操作

```kotlin
tabBar.setItems(items)
tabBar.addItem(item)
tabBar.insertItem(index, item)
tabBar.updateItem(index, item)
tabBar.removeItem(index)
tabBar.clearItems()
```

读取方法：

```kotlin
tabBar.getItemCount()
tabBar.getItem(index)
tabBar.getItems()
tabBar.containsItem(index)
tabBar.getCurrentIndex()
```

切换选中项：

```kotlin
tabBar.setCurrentIndex(index, animate = true)
```

### 运行时属性设置

```kotlin
tabBar.tabMode = AwBottomTabBar.TabMode.ICON_TEXT
tabBar.layoutMode = AwBottomTabBar.LayoutMode.FIXED
tabBar.indicatorStyle = AwBottomTabBar.IndicatorStyle.NONE
tabBar.indicatorWidthMode = AwBottomTabBar.IndicatorWidthMode.FOLLOW_TEXT

tabBar.selectedColor = Color.parseColor("#FF6B3D")
tabBar.normalColor = Color.parseColor("#7A7F8F")
tabBar.indicatorColor = Color.parseColor("#FF6B3D")
tabBar.tabMinWidth = 72f * resources.displayMetrics.density
```

### 角标 API

数字角标：

```kotlin
tabBar.setBadgeCount(2, 8)
```

文字角标：

```kotlin
tabBar.setBadgeText(1, "NEW")
```

红点角标：

```kotlin
tabBar.showBadgeDot(3)
```

颜色和清理：

```kotlin
tabBar.setBadgeTextColor(1, Color.WHITE)
tabBar.clearBadge(1)
tabBar.clearAllBadges()
tabBar.getBadgeText(1)
tabBar.hasBadge(1)
```

说明：
- `setBadgeCount(index, count <= 0)` 会清除角标
- 大于 `99` 的数字会显示为 `99+`

### 监听器

```kotlin
tabBar.setOnTabSelectedListener { index ->
    // 选中新的 tab
}

tabBar.setOnTabReselectedListener { index ->
    // 重复点击当前 tab
}

tabBar.setOnTabLongClickListener { index ->
    // 长按 tab
}
```

## ViewPager2 联动

### 直接绑定已有 ViewPager2

```kotlin
tabBar.bindViewPager(viewPager)
```

### 直接绑定 Fragment 列表

```kotlin
tabBar.bindFragments(this, fragments, viewPager)
```

解绑：

```kotlin
tabBar.unbindViewPager()
```

行为说明：
- 点击 Tab 会切换 `ViewPager2`
- 滑动 `ViewPager2` 会同步更新选中项
- `scrollable` 模式下会自动滚动到当前项附近

## 布局模式说明

### FIXED

- 所有 Tab 平分宽度
- 适合 3 到 5 个固定入口
- 当前默认值

### SCROLLABLE

- Tab 按内容宽度排列
- 数量多时可左右滑动
- 适合频道、分类、动态增减的场景
- 建议同时设置 `tab_min_width`

## 指示器说明

### `indicator_style = none`

- 不显示指示器
- 当前默认值

### `indicator_style = line`

- 底部显示线条指示器

### `indicator_width_mode = follow_text`

- 按文字实际宽度计算
- 当前实现已修正居中问题

## 推荐配置

### 经典底部导航

```xml
app:tab_layout_mode="fixed"
app:tab_mode="icon_text"
app:indicator_style="none"
```

### 可滑动频道栏

```xml
app:tab_layout_mode="scrollable"
app:tab_mode="text_only"
app:tab_min_width="72dp"
app:indicator_style="line"
app:indicator_width_mode="follow_text"
```

## 示例

主示例页展示了：
- 显示模式切换
- 布局模式切换
- 指示器开关和宽度模式切换
- Tab 的增删改
- 角标切换

ViewPager 示例页展示了：
- 与 `ViewPager2` 的联动
- 滑动同步
- 指示器跟随动画

## 注意事项

- 当前工程里没有 `gradlew.bat`，仓库内无法直接执行标准 Wrapper 构建
- 如果使用 `scrollable` 模式，建议明确设置 `tab_min_width`
- 如果使用自定义 `Drawable` 且不希望染色，关闭 `tab_auto_tint`

## 默认值汇总

| 属性 | 默认值 |
|---|---|
| `tab_mode` | `icon_text` |
| `tab_layout_mode` | `fixed` |
| `indicator_style` | `none` |
| `indicator_width_mode` | `match_tab` |
| `tab_icon_size` | `24dp` |
| `tab_text_size` | `12sp` |
| `tab_min_width` | `56dp` |
| `tab_selected_scale` | `1.1` |
| `enable_ripple` | `true` |
| `enable_scroll_sync` | `true` |
