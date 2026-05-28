# AwSkeletonLayout

基于 **mask 模式** 的骨架屏方案：包裹真实 content layout，按子 View bounds 自动遮罩并统一 shimmer，API 收敛为 `showSkeleton()` / `showContent()`。

对标 [Faltenreich/SkeletonLayout](https://github.com/Faltenreich/SkeletonLayout) 的使用体验，底层 shimmer 与 `AwSkeletonView` 共享 `AwSkeletonShimmer` 引擎。

## 组件选型

| 场景 | 推荐组件 |
|------|----------|
| 常规业务页、列表、StateLayout loading | `AwSkeletonLayout` / `applyAwSkeleton` |
| 营销页、精细手拼占位块 | `AwSkeletonView` |

## 文件位置

- 包路径：`com.answufeng.ui.widget.skeleton`
- 核心类：`AwSkeletonLayout`、`AwSkeleton`（接口）、`AwSkeletonConfig`
- 扩展：`AwSkeletonExt.kt`（`createAwSkeleton` / `applyAwSkeleton`）
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 包裹 content

```xml
<com.answufeng.ui.widget.skeleton.AwSkeletonLayout
    android:id="@+id/skeletonLayout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:skeleton_autoShow="true">

    <include layout="@layout/item_feed" />
</com.answufeng.ui.widget.skeleton.AwSkeletonLayout>
```

```kotlin
skeletonLayout.showSkeleton()
viewModel.load { data ->
    skeletonLayout.bindContent { root ->
        // 绑定真实数据
    }
    skeletonLayout.showContent()
}
```

### RecyclerView 列表

```kotlin
val skeleton = recyclerView.applyAwSkeleton(
    itemLayout = R.layout.item_feed,
    itemCount = 6,
)
skeleton.setContentAdapter(adapter)
skeleton.showSkeleton()

viewModel.load { list ->
    adapter.submitList(list)
    skeleton.showContent()
}
```

### ViewPager2

```kotlin
viewPager.applyAwSkeleton(R.layout.page_item, itemCount = 3).showSkeleton()
```

### 任意 ViewGroup

```kotlin
val skeleton = contentRoot.createAwSkeleton()
skeleton.showSkeleton()
// ...
skeleton.showContent(animate = true)
```

## XML 属性（AwSkeletonLayout）

| 属性 | 类型 | 说明 |
|------|------|------|
| `skeleton_maskColor` | color | 遮罩基础色，默认 `aw_color_skeleton_base` |
| `skeleton_shimmerColor` | color | shimmer 高亮色 |
| `skeleton_maskCornerRadius` | dimension | 遮罩圆角 |
| `skeleton_shimmerDuration` | integer | shimmer 周期（毫秒） |
| `skeleton_showShimmer` | boolean | 是否启用 shimmer |
| `skeleton_autoShow` | boolean | attach 后自动 `showSkeleton()` |

## 子 View 精细控制

任意 View 可使用全局属性：

| 属性 | 说明 |
|------|------|
| `app:skeleton_mask="true"` | 强制参与遮罩（即使无尺寸） |
| `app:skeleton_ignore="true"` | 排除不参与遮罩 |

## AwSkeleton 接口

```kotlin
interface AwSkeleton {
    fun showSkeleton()
    fun showContent(animate: Boolean = true)
    val isShowingSkeleton: Boolean
    var config: AwSkeletonConfig
}
```

## AwStateLayout 集成

`loadingStyle=skeleton` 时，`showLoading()` 不对 content 隐藏，而是在 content 上挂载 mask：

```xml
<com.answufeng.ui.statelayout.AwStateLayout
    app:state_loadingStyle="skeleton">
    <!-- content layout -->
</com.answufeng.ui.statelayout.AwStateLayout>
```

```kotlin
stateLayout.loadingStyle = AwStateLayout.LoadingStyle.SKELETON
stateLayout.skeletonConfig = AwSkeletonConfig.default(context)
stateLayout.showLoading()
stateLayout.showContent()
```

详见 [AwStateLayout](AwStateLayout.md)。

## 注意事项

- **空 TextView 高度为 0**：mask 不可见，请设置 `minHeight`、placeholder 文本，或使用 `skeleton_mask="true"`。
- **layout 变化后**：mask 在 layout 完成后自动 rebuild。
- **生命周期**：detach / 失焦时 shimmer 自动停止，避免泄漏。
- **与 spinner loading 互斥**：`AwStateLayout` 的 `loadingStyle` 决定 loading 表现，勿混用手动 visibility 切换。

## Demo

`SkeletonDemoActivity`：静态卡片、`RecyclerView.applyAwSkeleton`、`AwStateLayout` skeleton 模式，以及 `AwSkeletonView` 手拼块对比。
