# AwBannerView

`AwBannerView` 是一个支持无限循环、自动轮播、指示器交互的 Banner 轮播组件，兼容 `ViewPager` 和 `ViewPager2`。

## 功能概览

- 支持无限循环滚动
- 自动轮播功能，支持暂停/恢复
- 底部指示器，支持点击切换
- 兼容 `ViewPager` 和 `ViewPager2`
- 生命周期感知，自动处理暂停/恢复
- 支持页面点击和指示器点击回调

## 文件位置

- 组件实现：[AwBannerView.kt](E:/workspace/ASProjects/AutoKs/viewtest/src/main/java/com/answufeng/ui/widget/AwBannerView.kt)
- 自定义属性：[attrs.xml](E:/workspace/ASProjects/AutoKs/viewtest/src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwBannerView
    android:id="@+id/banner"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    app:banner_interval="3000"
    app:banner_autoStart="true"
    app:banner_infiniteLoop="true"
    app:banner_showIndicators="true"
    app:banner_indicatorColor="#80FFFFFF"
    app:banner_indicatorSelectedColor="#FFFFFF" />
```

### 代码中使用

```kotlin
val banner = findViewById<AwBannerView>(R.id.banner)

// 方式1：使用 setData 快速设置
banner.setData(listOf("https://example.com/img1.jpg", "https://example.com/img2.jpg")) { container, url, position ->
    val imageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        // 加载图片...
    }
    container.addView(imageView)
}

// 方式2：自定义 RecyclerView.Adapter
val adapter = MyBannerAdapter(dataList)
banner.setAdapter(adapter)

// 设置页面点击监听
banner.setOnPageClickListener { position ->
    // 处理页面点击
}

// 设置指示器点击监听
banner.setOnIndicatorClickListener { position ->
    // 处理指示器点击
}
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `banner_interval` | `integer` | `3000` | 自动轮播间隔（毫秒），最小 1000 |
| `banner_autoStart` | `boolean` | `true` | 是否自动开始轮播 |
| `banner_infiniteLoop` | `boolean` | `true` | 是否无限循环 |
| `banner_showIndicators` | `boolean` | `true` | 是否显示指示器 |
| `banner_indicatorColor` | `color` | `#80FFFFFF` | 指示器未选中颜色 |
| `banner_indicatorSelectedColor` | `color` | `#FFFFFF` | 指示器选中颜色 |

## Kotlin API

### 状态控制

```kotlin
banner.startAutoScroll()    // 开始自动轮播
banner.stopAutoScroll()     // 停止自动轮播
banner.isAutoScrolling      // 是否正在自动轮播
```

### 属性设置

```kotlin
banner.interval = 4000L                 // 设置轮播间隔
banner.isInfiniteLoop = false           // 关闭无限循环
banner.showIndicators = false           // 隐藏指示器
banner.indicatorColor = Color.GRAY      // 指示器颜色
banner.indicatorSelectedColor = Color.WHITE  // 选中颜色
```

### 页面控制

```kotlin
banner.setCurrentItem(2, smoothScroll = true)  // 切换到指定页面
banner.getCurrentRealItem()                    // 获取当前真实位置
```

### 监听设置

```kotlin
banner.setOnPageClickListener { position -> }
banner.setOnIndicatorClickListener { position -> }
```

### 适配器设置

```kotlin
// ViewPager2 (推荐)
banner.setAdapter(recyclerViewAdapter, knownItemCount = null)

// ViewPager (兼容旧版)
banner.setPagerAdapter(viewPagerAdapter, knownItemCount = null)

// 简化数据绑定
banner.setData(items) { container, item, position ->
    // 绑定视图
}
```

## 生命周期集成

组件会自动感知 `Lifecycle`：
- `ON_PAUSE`：暂停自动轮播
- `ON_RESUME`：恢复之前的自动轮播状态

## 使用示例

### 配合 Glide 加载图片

```kotlin
banner.setData(imageUrls) { container, url, _ ->
    ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        Glide.with(context).load(url).into(this)
    }.also { container.addView(it) }
}
```

### 自定义指示器交互

```kotlin
banner.setOnIndicatorClickListener { position ->
    banner.setCurrentItem(position, smoothScroll = true)
    Toast.makeText(context, "点击了指示器 $position", Toast.LENGTH_SHORT).show()
}
```

## 注意事项

- `interval` 最小值为 1000 毫秒
- 数据量小于等于 1 时不会自动轮播
- 使用 `setData` 时，内部会创建无限循环的 Adapter
- `getCurrentRealItem()` 返回真实数据位置（0 到 itemCount-1）
