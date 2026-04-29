# AwBadgeView

`AwBadgeView` 是一个轻量级的角标组件，支持红点、数字、文字三种模式，自动处理可见性和无障碍支持。

## 功能概览

- 支持四种显示模式：隐藏、红点、数字、文字
- 自动处理可见性，隐藏时不占用空间
- 数字超过最大值自动显示为 `99+` 格式
- 支持无障碍访问（Accessibility）
- 支持状态保存与恢复

## 文件位置

- 组件实现：[AwBadgeView.kt](../src/main/java/com/answufeng/ui/widget/AwBadgeView.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwBadgeView
    android:id="@+id/badge"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:badge_count="8"
    app:badge_bgColor="#FF6B6B"
    app:badge_textColor="#FFFFFF" />
```

### 代码中使用

```kotlin
val badge = findViewById<AwBadgeView>(R.id.badge)

// 显示红点
badge.showDot()

// 设置数字角标
badge.count = 99

// 设置文字角标
badge.setBadgeText("NEW")

// 清除角标
badge.clear()
```

## 显示模式

| 模式 | 说明 |
|---|---|
| `HIDDEN` | 隐藏状态，不占用空间 |
| `DOT` | 红点模式，显示小圆点 |
| `COUNT` | 数字模式，显示数字 |
| `TEXT` | 文字模式，显示自定义文字 |

## XML 属性说明

- `app:badge_count`
  - 类型：`integer`
  - 说明：初始数字值，`0` 显示红点，`-1` 隐藏

- `app:badge_text`
  - 类型：`string`
  - 说明：初始文字值

- `app:badge_bgColor`
  - 类型：`color`
  - 默认值：`Color.RED`

- `app:badge_textColor`
  - 类型：`color`
  - 默认值：`Color.WHITE`

- `app:badge_textSize`
  - 类型：`dimension`
  - 默认值：`10sp`

- `app:badge_dotSize`
  - 类型：`dimension`
  - 默认值：`8dp`

- `app:badge_minHeight`
  - 类型：`dimension`
  - 默认值：`18dp`

- `app:badge_horizontalPadding`
  - 类型：`dimension`
  - 默认值：`6dp`

## Kotlin API

### 状态设置

```kotlin
badge.count = 8              // 设置数字
badge.setBadgeText("NEW")    // 设置文字
badge.showDot()              // 显示红点
badge.clear()                // 清除角标
badge.increment()            // 数字+1
badge.decrement()            // 数字-1
badge.increment(5)           // 数字+5
badge.decrement(3)           // 数字-3
```

### 属性设置

```kotlin
badge.badgeColor = Color.RED           // 背景色
badge.badgeTextColor = Color.WHITE     // 文字颜色
badge.badgeTextSizePx = 12f.sp()       // 文字大小
badge.maxCount = 999                   // 最大显示数字
```

### 状态查询

```kotlin
badge.count           // 当前数字
badge.textBadge       // 当前文字
badge.mode            // 当前模式
```

## 使用示例

### 配合其他组件使用

```kotlin
val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
val badge = AwBadgeView(context).apply {
    count = 10
}
tabLayout.getTabAt(1)?.customView = badge
```

### 在 RecyclerView 中使用

```kotlin
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.badge.count = getItem(position).unreadCount
}
```

## 注意事项

- `count = 0` 会显示红点（DOT 模式）
- `count < 0` 会隐藏角标
- 数字超过 `maxCount` 会显示为 `${maxCount}+`
- 组件会自动处理无障碍支持和状态保存
