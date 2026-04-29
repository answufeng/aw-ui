# AwSwipeRefreshLayout

`AwSwipeRefreshLayout` 是一个下拉刷新布局，支持多种内置刷新样式、自定义刷新视图和布局跟随下拉。

## 功能概览

- 4 种内置刷新样式：系统、菊花、箭头、文字
- 支持自定义刷新视图（View 或布局资源）
- 支持 `RefreshHeaderView` 接口实现自定义刷新头部（推荐方式）
- 支持通过 XML 属性指定自定义刷新头部布局
- 支持自定义刷新指示器颜色
- 支持启用/禁用刷新
- 支持代码触发刷新
- 布局跟随下拉：下拉时内容布局跟随刷新头部一起移动

## 文件位置

- 组件实现：[AwSwipeRefreshLayout.kt](../src/main/java/com/answufeng/ui/widget/AwSwipeRefreshLayout.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwSwipeRefreshLayout
    android:id="@+id/refreshLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:refreshStyle="system"
    app:refreshTintColor="#999999">

    <androidx.recyclerview.widget.RecyclerView
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</com.answufeng.ui.widget.AwSwipeRefreshLayout>
```

### 代码中使用

```kotlin
val refreshLayout = findViewById<AwSwipeRefreshLayout>(R.id.refreshLayout)

refreshLayout.refreshListener = {
    viewModel.refreshData {
        refreshLayout.finishRefresh()
    }
}
```

## 刷新样式

| 样式 | XML 值 | 说明 |
|---|---|---|
| SYSTEM | `system` | 系统默认圆形旋转指示器 |
| FLOWER | `flower` | iOS 风格菊花旋转指示器 |
| ARROW | `arrow` | 箭头指示器，下拉时显示箭头，刷新时旋转 |
| TEXT | `text` | 文字 + 小菊花，显示"下拉刷新"→"正在刷新..." |

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `refreshStyle` | `enum` | `system` | 刷新样式：system/flower/arrow/text |
| `refreshTintColor` | `color` | 主题 colorPrimary | 刷新指示器颜色 |
| `refreshText` | `string` | `下拉刷新` | 文字样式下的提示文字 |
| `refreshTextColor` | `color` | `0` | 文字样式下的文字颜色（0 表示跟随 tintColor） |
| `refreshTextSize` | `dimension` | `0` | 文字样式下的文字大小 |
| `customRefreshHeaderLayout` | `reference` | 无 | 自定义刷新头部布局资源 ID |

## Kotlin API

### 刷新控制

```kotlin
refreshLayout.refreshListener = { /* 执行刷新操作 */ }
refreshLayout.finishRefresh()            // 停止刷新
refreshLayout.isRefreshing = true        // 开始刷新（代码触发）
refreshLayout.enableRefresh = false      // 禁用刷新
```

### 样式设置

```kotlin
refreshLayout.refreshStyle = AwSwipeRefreshLayout.RefreshStyle.FLOWER
refreshLayout.refreshTintColor = Color.BLUE
refreshLayout.refreshText = "下拉刷新"
```

### 自定义刷新视图

```kotlin
// 方式一：使用 View（简单方式，不支持刷新状态回调）
val customView = MyCustomRefreshView(context)
refreshLayout.setCustomHeaderView(customView)

// 方式二：使用布局资源
refreshLayout.setCustomHeaderView(R.layout.my_refresh_header)

// 方式三（推荐）：实现 RefreshHeaderView 接口
val customHeader = MyRefreshHeaderView(context)
refreshLayout.setCustomHeaderView(customHeader)
```

### 颜色设置

```kotlin
refreshLayout.setColorSchemeColors(Color.BLUE, Color.GREEN, Color.RED)
```

### 回调

```kotlin
refreshLayout.setOnRefreshListener {
    // 执行刷新操作
}
```

## 自定义刷新头部（高级）

### RefreshHeaderView 接口

实现 `AwSwipeRefreshLayout.RefreshHeaderView` 接口可以让自定义头部响应下拉进度和刷新状态变化：

```kotlin
interface RefreshHeaderView {
    /**
     * 刷新状态变化回调
     * @param pullProgress 下拉进度 0~1，0=未下拉，1=达到触发阈值
     * @param isRefreshing 是否正在刷新
     */
    fun onRefreshStateChanged(pullProgress: Float, isRefreshing: Boolean)

    /**
     * 颜色变化回调
     * @param color 用户通过 refreshTintColor 设置的颜色
     */
    fun onTintColorChanged(@ColorInt color: Int)
}
```

### 实现自定义刷新头部示例

```kotlin
class MyRefreshHeaderView(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs), AwSwipeRefreshLayout.RefreshHeaderView {

    private val progressBar = ProgressBar(context)
    private val textView = TextView(context)

    init {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            (56 * resources.displayMetrics.density).toInt()
        )
        addView(progressBar)
        addView(textView)
        progressBar.visibility = GONE
    }

    override fun onRefreshStateChanged(pullProgress: Float, isRefreshing: Boolean) {
        if (isRefreshing) {
            progressBar.visibility = VISIBLE
            textView.text = "正在刷新..."
        } else {
            progressBar.visibility = GONE
            if (pullProgress >= 1f) {
                textView.text = "释放刷新"
            } else {
                textView.text = "下拉刷新"
            }
        }
        alpha = pullProgress.coerceIn(0.3f, 1f)
    }

    override fun onTintColorChanged(color: Int) {
        textView.setTextColor(color)
        progressBar.indeterminateDrawable.setTint(color)
    }
}
```

### 在 XML 中指定自定义头部布局

```xml
<com.answufeng.ui.widget.AwSwipeRefreshLayout
    android:id="@+id/refreshLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:customRefreshHeaderLayout="@layout/my_refresh_header">

    <androidx.recyclerview.widget.RecyclerView
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</com.answufeng.ui.widget.AwSwipeRefreshLayout>
```

> **注意**：通过 XML 指定的自定义布局如果未实现 `RefreshHeaderView` 接口，将无法接收下拉进度和刷新状态回调。如需完整功能，建议在代码中通过 `setCustomHeaderView()` 设置实现了 `RefreshHeaderView` 接口的 View。

## 内置刷新视图

### FlowerIndicatorView

iOS 风格菊花旋转指示器，可独立使用：

```kotlin
val flowerView = AwSwipeRefreshLayout.FlowerIndicatorView(context)
flowerView.tintColor = Color.GRAY
flowerView.startSpin()
flowerView.stopSpin()
```

### ArrowIndicatorView

箭头指示器，下拉时显示箭头，刷新时旋转弧线：

```kotlin
val arrowView = AwSwipeRefreshLayout.ArrowIndicatorView(context)
arrowView.tintColor = Color.GRAY
arrowView.progress = 0.5f  // 下拉进度
arrowView.startSpin()
arrowView.stopSpin()
```

## 使用示例

### 配合 RecyclerView

```kotlin
refreshLayout.refreshListener = {
    viewModel.refreshData { newData ->
        adapter.submitList(newData)
        refreshLayout.finishRefresh()
    }
}
```

### 切换刷新样式

```kotlin
// 在代码中动态切换
refreshLayout.refreshStyle = AwSwipeRefreshLayout.RefreshStyle.FLOWER
refreshLayout.refreshStyle = AwSwipeRefreshLayout.RefreshStyle.TEXT
```

### 自定义刷新头部

```kotlin
// 简单方式：任意 View 即可
val headerView = LinearLayout(context).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER
    addView(ProgressBar(context))
    addView(TextView(context).apply { text = "加载中..." })
}
refreshLayout.setCustomHeaderView(headerView)

// 推荐方式：实现 RefreshHeaderView 接口
class MyAppRefreshHeader(context: Context) :
    FrameLayout(context), AwSwipeRefreshLayout.RefreshHeaderView {

    private val progressBar = ProgressBar(context)
    private val label = TextView(context)

    init {
        addView(progressBar)
        addView(label)
    }

    override fun onRefreshStateChanged(pullProgress: Float, isRefreshing: Boolean) {
        progressBar.isIndeterminate = isRefreshing
        label.text = if (isRefreshing) "刷新中..." else "下拉刷新"
        alpha = pullProgress.coerceIn(0.3f, 1f)
    }

    override fun onTintColorChanged(color: Int) {
        label.setTextColor(color)
    }
}
```

## 注意事项

- 只能有一个直接子视图
- 子视图应为可滚动视图（如 RecyclerView、ScrollView）
- 下拉时内容布局会跟随刷新头部一起移动
- `refreshListener` 设置刷新回调，`finishRefresh()` 停止刷新
- 使用 `post` 延迟设置 `isRefreshing` 以确保布局完成
- 自定义刷新视图通过 `setCustomHeaderView` 设置后会替换当前头部
- 实现 `RefreshHeaderView` 接口的自定义头部可以接收下拉进度和刷新状态回调
- FLOWER 样式使用花瓣透明度流动动画，8 根花瓣，750ms/周期
- ARROW 样式下拉时显示箭头，释放后切换为旋转弧线
- TEXT 样式显示小菊花 + 文字，支持"下拉刷新"→"释放刷新"→"正在刷新..."状态切换
