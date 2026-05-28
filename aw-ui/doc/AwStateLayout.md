# AwStateLayout

四态布局容器：内容 / 加载 / 空 / 错误，支持过渡动画与重试回调。

## 快速开始

```xml
<com.answufeng.ui.statelayout.AwStateLayout
    android:id="@+id/stateLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:enableAnimation="true">

    <RecyclerView
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</com.answufeng.ui.statelayout.AwStateLayout>
```

```kotlin
stateLayout.showLoading()
stateLayout.showEmpty()
stateLayout.showError { loadData() }
stateLayout.showContent()
```

## 自定义状态页

通过 `app:loadingLayout` / `emptyLayout` / `errorLayout` 指定布局；也可在代码中 `setLoadingLayout()` 等。

## 过渡动画

使用 `StateTransition.FADE`、`CROSS_FADE`、`slideFromBottom()` 或自定义 `StateTransition { view, duration -> ... }`。

## Loading 样式

| 样式 | XML | 行为 |
|------|-----|------|
| `spinner`（默认） | `app:state_loadingStyle="spinner"` | 显示 loading 布局，隐藏 content |
| `skeleton` | `app:state_loadingStyle="skeleton"` | **不隐藏 content**，对 content 加 skeleton mask |

```kotlin
stateLayout.loadingStyle = AwStateLayout.LoadingStyle.SKELETON
stateLayout.skeletonConfig = AwSkeletonConfig.default(context)
stateLayout.showLoading()   // content 上显示遮罩
stateLayout.showContent()   // 移除遮罩，显示真实内容
```

skeleton 模式下 content 需具备合理尺寸（如 TextView 设置 `minHeight`），详见 [AwSkeletonLayout](AwSkeletonLayout.md)。

## Demo

- `StateDemoActivity` — 四态与过渡动画
- `SkeletonDemoActivity` — `loadingStyle=skeleton` 联动示例
