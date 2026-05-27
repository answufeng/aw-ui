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

## Demo

`StateDemoActivity`
