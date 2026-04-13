# aw-ui

Android 通用 UI 组件库，提供 StateLayout、TitleBar、RecyclerView 适配器、对话框、动画和自定义布局。

## 引入

```kotlin
dependencies {
    implementation("com.github.answufeng:aw-ui:1.0.0")
}
```

## 功能特性

| 组件 | 说明 |
|---|---|
| StateLayout | 四态容器（内容/加载中/空/错误），支持懒加载和动画 |
| TitleBar | 通用标题栏，支持沉浸式状态栏 |
| SimpleAdapter | 单类型 RecyclerView 适配器，内置 DiffUtil |
| MultiTypeAdapter | 多类型 RecyclerView 适配器，支持 DSL 构建 |
| LoadMoreAdapter | 上拉加载更多适配器，内置加载状态管理 |
| AwDialog | Material Design 风格对话框快捷构建器 |
| LoadingDialog | 全局单例 Loading 对话框 |
| AwAnim | 视图动画扩展（fadeIn、fadeOut、slideIn、shake、pulse 等） |
| AwItemAnimator | RecyclerView Item 入场动画 |
| RoundLayout | 圆角裁切容器，支持描边 |
| FlowLayout | 流式布局（自动换行标签布局） |
| BadgeView | 角标视图（红点 / 数字 Badge） |
| DividerDecoration | RecyclerView 通用分割线 |

## 使用示例

### StateLayout

```kotlin
// 代码切换状态
stateLayout.showLoading()
stateLayout.showContent()
stateLayout.showEmpty()
stateLayout.showError { retryLoad() }

// 自定义状态视图
stateLayout.setLoadingView(R.layout.custom_loading)
stateLayout.setEmptyView(R.layout.custom_empty)
stateLayout.setErrorView(R.layout.custom_error)

// 监听状态变更
stateLayout.setOnStateChangeListener { oldState, newState ->
    Log.d("StateLayout", "$oldState -> $newState")
}
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `loadingLayout` | 加载中布局 | 内置布局 |
| `emptyLayout` | 空数据布局 | 内置布局 |
| `errorLayout` | 错误布局 | 内置布局 |
| `enableAnimation` | 是否启用切换动画 | true |
| `animationDuration` | 动画时长（ms） | 200 |

### TitleBar

```kotlin
titleBar.title = "详情"
titleBar.setOnBackClickListener { onBackPressedDispatcher.onBackPressed() }
titleBar.setRightText("保存") { saveData() }
titleBar.setRightIcon(R.drawable.ic_more) { showMenu() }
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `titleBar_title` | 标题文字 | "" |
| `titleBar_showBack` | 是否显示返回按钮 | true |
| `titleBar_leftIcon` | 自定义左侧图标 | 系统返回图标 |
| `titleBar_rightText` | 右侧按钮文字 | 无 |
| `titleBar_rightIcon` | 右侧图标资源 | 无 |
| `titleBar_titleColor` | 标题颜色 | Material colorOnSurface |
| `titleBar_bgColor` | 背景颜色 | Material colorSurface |
| `titleBar_immersive` | 是否适配沉浸式 | false |

### SimpleAdapter

```kotlin
val adapter = SimpleAdapter<ItemUserBinding, User>(
    inflate = ItemUserBinding::inflate,
    diffCallback = object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(old: User, new: User) = old.id == new.id
        override fun areContentsTheSame(old: User, new: User) = old == new
    }
) { binding, item, position ->
    binding.tvName.text = item.name
}

adapter.setOnItemClickListener { user, pos -> openDetail(user) }
recyclerView.adapter = adapter
adapter.submitList(userList)
```

### MultiTypeAdapter

```kotlin
// DSL 用法
val adapter = multiTypeAdapter {
    itemDiff { old, new -> (old as? HasId)?.id == (new as? HasId)?.id }
    contentDiff { old, new -> old == new }

    register<TextItem, ItemTextBinding>(ItemTextBinding::inflate) { binding, item, _ ->
        binding.tvText.text = item.text
    }
    register<ImageItem, ItemImageBinding>(ItemImageBinding::inflate) { binding, item, _ ->
        binding.ivImage.load(item.url)
    }
}
adapter.submitList(items)
```

### LoadMoreAdapter

```kotlin
val adapter = LoadMoreAdapter<ItemBinding, Article>(
    inflate = ItemBinding::inflate,
    diffCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(old: Article, new: Article) = old.id == new.id
        override fun areContentsTheSame(old: Article, new: Article) = old == new
    }
) { binding, item, _ ->
    binding.tvTitle.text = item.title
}

adapter.setOnLoadMoreListener { fetchNextPage() }
adapter.noMore()      // 没有更多数据
adapter.loadFailed()  // 加载失败（可点击重试）
adapter.loadMore(nextPageData)  // 追加数据
```

### AwDialog

```kotlin
// 确认对话框
AwDialog.confirm(context, "提示", "确定删除吗？") { deleteItem() }

// 输入对话框
AwDialog.input(context, "备注", hint = "请输入备注") { text -> saveRemark(text) }

// 列表选择
AwDialog.list(context, "选择", listOf("A", "B", "C")) { index -> select(index) }

// 底部列表
AwDialog.bottomList(context, "操作", listOf("拍照", "相册")) { index -> pick(index) }

// 自定义布局
AwDialog.custom(context, "设置", R.layout.dialog_settings) { view ->
    view.findViewById<Switch>(R.id.switchDarkMode).isChecked = isDarkMode
}
```

### LoadingDialog

```kotlin
LoadingDialog.show(context, "提交中…")
LoadingDialog.dismiss()

// 可取消
LoadingDialog.show(context, "加载中…", cancelable = true) { cancelRequest() }
```

### AwAnim

```kotlin
// 淡入淡出
view.fadeIn()
view.fadeOut()

// 滑入滑出
view.slideInFromBottom()
view.slideOutToTop()
view.slideInFromLeft()
view.slideInFromRight()

// 缩放
view.scaleIn()
view.scaleOut()
view.pulse()

// 抖动与弹跳
view.shake()
view.bounce()

// 组合动画
view.fadeSlideIn()
view.fadeSlideOut()

// 旋转
view.rotate()
```

### AwItemAnimator

```kotlin
// 在 Adapter 中使用
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    bind(holder, getItem(position))
    AwItemAnimator.animateItem(holder.itemView, position)
}

// 重置动画状态（防止复用问题）
override fun onViewRecycled(holder: ViewHolder) {
    super.onViewRecycled(holder)
    AwItemAnimator.resetItem(holder.itemView)
}
```

### BadgeView

```kotlin
badgeView.count = 3   // 显示数字 "3"
badgeView.count = 0   // 显示红点
badgeView.count = -1  // 隐藏
```

### FlowLayout

```xml
<com.answufeng.ui.widget.FlowLayout
    app:flow_horizontalSpacing="8dp"
    app:flow_verticalSpacing="8dp"
    app:flow_maxLines="3"
    app:flow_gravity="center">
    <TextView ... />
    <TextView ... />
</com.answufeng.ui.widget.FlowLayout>
```

### RoundLayout

```xml
<com.answufeng.ui.widget.RoundLayout
    app:round_radius="12dp"
    app:round_strokeColor="#FF0000"
    app:round_strokeWidth="1dp">
    <!-- 子视图将被圆角裁切 -->
</com.answufeng.ui.widget.RoundLayout>
```

### DividerDecoration

```kotlin
recyclerView.addItemDecoration(
    DividerDecoration(
        height = 1.dp,
        color = Color.LTGRAY,
        paddingStart = 16.dp,
        paddingEnd = 16.dp
    )
)
```

## 许可证

Apache License 2.0，详见 [LICENSE](LICENSE)。
