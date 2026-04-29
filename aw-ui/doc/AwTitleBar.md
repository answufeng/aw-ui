# AwTitleBar

`AwTitleBar` 是一个轻量级的标题栏组件，支持标题、副标题、返回按钮、右侧操作按钮，以及沉浸式状态栏适配。

## 功能概览

- 支持主标题和副标题
- 可配置返回按钮
- 支持右侧文字和图标操作按钮
- 自定义颜色和高度
- 沉浸式状态栏适配
- 底部分割线开关
- 自动处理返回事件

## 文件位置

- 组件实现：[AwTitleBar.kt](../src/main/java/com/answufeng/ui/widget/AwTitleBar.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwTitleBar
    android:id="@+id/titleBar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:titleBar_title="首页"
    app:titleBar_subtitle="副标题"
    app:titleBar_showBack="true"
    app:titleBar_rightText="更多"
    app:titleBar_showDivider="false"
    app:titleBar_immersive="false" />
```

### 代码中使用

```kotlin
val titleBar = findViewById<AwTitleBar>(R.id.titleBar)

// 设置标题
titleBar.title = "详情页"
titleBar.subtitle = "副标题文字"

// 设置右侧按钮
titleBar.setRightText("保存") {
    // 点击事件
}

titleBar.setRightIcon(R.drawable.ic_share) {
    // 点击事件
}

// 自定义返回按钮点击
titleBar.setOnBackClickListener {
    // 自定义返回逻辑
}
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `titleBar_title` | `string` | - | 主标题文字 |
| `titleBar_subtitle` | `string` | - | 副标题文字 |
| `titleBar_showBack` | `boolean` | `true` | 是否显示返回按钮 |
| `titleBar_leftIcon` | `reference` | `aw_ic_back` | 左侧图标资源 |
| `titleBar_rightText` | `string` | - | 右侧文字 |
| `titleBar_rightIcon` | `reference` | - | 右侧图标资源 |
| `titleBar_titleColor` | `color` | `colorOnSurface` | 标题颜色 |
| `titleBar_subtitleColor` | `color` | `#99000000` | 副标题颜色 |
| `titleBar_rightTextColor` | `color` | `colorPrimary` | 右侧文字颜色 |
| `titleBar_bgColor` | `color` | `colorSurface` | 背景颜色 |
| `titleBar_iconTint` | `color` | - | 图标着色 |
| `titleBar_height` | `dimension` | `56dp` | 标题栏高度（最小 48dp） |
| `titleBar_showDivider` | `boolean` | `false` | 是否显示分割线 |
| `titleBar_dividerColor` | `color` | `#14000000` | 分割线颜色 |
| `titleBar_immersive` | `boolean` | `false` | 是否沉浸式 |

## Kotlin API

### 标题设置

```kotlin
titleBar.title = "新标题"
titleBar.subtitle = "新副标题"
```

### 按钮控制

```kotlin
titleBar.showBackButton = false           // 隐藏返回按钮
titleBar.setLeftIcon(R.drawable.ic_menu)  // 设置左侧图标
titleBar.setRightText("确定") { ... }     // 设置右侧文字及点击
titleBar.setRightIcon(R.drawable.ic_share) { ... }  // 设置右侧图标及点击
```

### 颜色设置

```kotlin
titleBar.titleColor = Color.BLACK
titleBar.subtitleColor = Color.GRAY
titleBar.rightTextColor = Color.BLUE
titleBar.iconTintColor = Color.RED
titleBar.setBackgroundColor(Color.WHITE)
```

### 分割线

```kotlin
titleBar.showDivider = true
titleBar.dividerColor = Color.LTGRAY
```

### 高度设置

```kotlin
titleBar.barHeightPx = 64.dp()
```

### 沉浸式适配

```kotlin
titleBar.applyImmersivePadding()  // 应用沉浸式状态栏padding
```

### 点击监听

```kotlin
titleBar.setOnBackClickListener { ... }
titleBar.setOnRightTextClickListener { ... }
titleBar.setOnRightIconClickListener { ... }
```

### 获取子视图

```kotlin
titleBar.getBackView()         // 获取返回按钮视图
titleBar.getRightTextView()    // 获取右侧文字视图
titleBar.getRightImageView()   // 获取右侧图标视图
```

## 使用示例

### 基础用法

```kotlin
val titleBar = AwTitleBar(context).apply {
    title = "设置"
    showBackButton = true
    showDivider = true
}
```

### 沉浸式标题栏

```xml
<com.answufeng.ui.widget.AwTitleBar
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:titleBar_title="沉浸式标题"
    app:titleBar_immersive="true"
    app:titleBar_bgColor="#FF6B6B"
    app:titleBar_titleColor="#FFFFFF" />
```

### 复杂场景

```kotlin
titleBar.apply {
    title = "订单详情"
    subtitle = "订单号：202401010001"
    setRightIcon(R.drawable.ic_more_vert) { showMenu() }
    setOnBackClickListener { confirmExit() }
}
```

## 注意事项

- 返回按钮默认调用 `onBackPressedDispatcher` 或 `activity.finish()`
- `barHeightPx` 最小值为 48dp
- 副标题为空时自动隐藏
- 沉浸式模式需要配合透明状态栏使用
- 图标着色需要使用支持 tint 的矢量图
