# AwExpandableLayout

`AwExpandableLayout` 是一个可展开/收起的布局容器，支持平滑的高度动画效果。

## 功能概览

- 展开/收起平滑动画
- 支持 toggle 切换
- 可配置动画时长
- 支持状态变化回调
- 支持状态保存与恢复

## 文件位置

- 组件实现：[AwExpandableLayout.kt](../src/main/java/com/answufeng/ui/widget/AwExpandableLayout.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwExpandableLayout
    android:id="@+id/expandableLayout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:expandable_expanded="false"
    app:expandable_duration="300">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="可展开/收起的内容" />
</com.answufeng.ui.widget.AwExpandableLayout>
```

### 代码中使用

```kotlin
val expandable = findViewById<AwExpandableLayout>(R.id.expandableLayout)

expandable.expand()      // 展开
expandable.collapse()    // 收起
expandable.toggle()      // 切换

expandable.onExpandChange = { isExpanded ->
    // 状态变化回调
}
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `expandable_expanded` | `boolean` | `false` | 初始是否展开 |
| `expandable_duration` | `integer` | `300` | 动画时长（毫秒） |

## Kotlin API

### 状态控制

```kotlin
expandable.expand()           // 展开
expandable.collapse()         // 收起
expandable.toggle()           // 切换
expandable.expanded = true    // 设置展开状态
```

### 属性设置

```kotlin
expandable.duration = 500L    // 设置动画时长
```

### 回调

```kotlin
expandable.onExpandChange = { isExpanded ->
    // isExpanded: true=展开, false=收起
}
```

## 使用示例

### 配合按钮切换

```kotlin
btnToggle.setOnClickListener {
    expandableLayout.toggle()
}
expandableLayout.onExpandChange = { isExpanded ->
    btnToggle.text = if (isExpanded) "收起" else "展开"
}
```

### 默认展开

```xml
<com.answufeng.ui.widget.AwExpandableLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:expandable_expanded="true"
    app:expandable_duration="500">
    <!-- 子视图 -->
</com.answufeng.ui.widget.AwExpandableLayout>
```

## 注意事项

- 只能有一个直接子视图（继承自 `LinearLayout`）
- 子视图的 `visibility` 为 `GONE` 时不计入高度
- 动画使用 `AccelerateDecelerateInterpolator`
- 收起状态下高度为 0，不占用空间
