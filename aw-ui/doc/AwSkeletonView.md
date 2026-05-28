# AwSkeletonView

`AwSkeletonView` 是**底层 shimmer 占位块**，适合营销页、精细手拼 skeleton layout。

常规业务场景（自动按 content 子 View 遮罩）请优先使用 [`AwSkeletonLayout`](AwSkeletonLayout.md) 或 `RecyclerView.applyAwSkeleton`。

## 功能概览

- 闪光动画效果（Shimmer）
- 可配置基础色和高亮色
- 可配置圆角
- 可配置动画时长
- 支持开始/停止控制

## 文件位置

- 组件实现：[AwSkeletonView.kt](../src/main/java/com/answufeng/ui/widget/AwSkeletonView.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwSkeletonView
    android:id="@+id/skeletonView"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    app:skeleton_baseColor="#E0E0E0"
    app:skeleton_highlightColor="#F5F5F5"
    app:skeleton_cornerRadius="8dp"
    app:skeleton_duration="1500" />
```

### 代码中使用

```kotlin
val skeleton = findViewById<AwSkeletonView>(R.id.skeletonView)

skeleton.startShimmer()
// 数据加载完成后
skeleton.stopShimmer()
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `skeleton_baseColor` | `color` | `#E0E0E0` | 基础颜色 |
| `skeleton_highlightColor` | `color` | `#F5F5F5` | 高亮颜色 |
| `skeleton_cornerRadius` | `dimension` | `8dp` | 圆角半径 |
| `skeleton_duration` | `integer` | `1500` | 动画周期（毫秒） |

## Kotlin API

### 动画控制

```kotlin
skeleton.startShimmer()    // 开始动画
skeleton.stopShimmer()     // 停止动画
skeleton.isShimmering      // 是否正在运行
```

### 属性设置

```kotlin
skeleton.baseColor = Color.parseColor("#E0E0E0")
skeleton.highlightColor = Color.parseColor("#F5F5F5")
skeleton.cornerRadius = 8f.dp()
skeleton.animationDuration = 1500L
```

## 使用示例

### 多行手拼骨架屏

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <com.answufeng.ui.widget.AwSkeletonView
        android:layout_width="120dp"
        android:layout_height="16dp"
        android:layout_marginBottom="8dp"
        app:skeleton_cornerRadius="4dp" />

    <com.answufeng.ui.widget.AwSkeletonView
        android:layout_width="match_parent"
        android:layout_height="12dp"
        android:layout_marginBottom="4dp"
        app:skeleton_cornerRadius="4dp" />

    <com.answufeng.ui.widget.AwSkeletonView
        android:layout_width="200dp"
        android:layout_height="12dp"
        app:skeleton_cornerRadius="4dp" />
</LinearLayout>
```

## 注意事项

- 组件在 `onDetachedFromWindow` 时自动停止动画
- `stopShimmer()` 不会隐藏视图，需手动设置 `visibility`
- 动画使用 `ValueAnimator`，注意避免内存泄漏
- 多个 `AwSkeletonView` 可组合使用模拟复杂布局
- shimmer 绘制由内部 `AwSkeletonShimmer` 实现，与 `AwSkeletonLayout` 共享引擎
