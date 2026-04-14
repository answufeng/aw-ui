# aw-ui

[![](https://jitpack.io/v/answufeng/aw-ui.svg)](https://jitpack.io/#answufeng/aw-ui)

Android 通用 UI 组件库，提供 StateLayout、TitleBar、RecyclerView 适配器、对话框、动画和自定义布局。

## 特性

| 组件 | 说明 |
|---|---|
| StateLayout | 四态容器（内容/加载中/空/错误），支持懒加载、自定义过渡动画和暗黑模式 |
| TitleBar | 通用标题栏，支持沉浸式状态栏（WindowInsets API） |
| SimpleAdapter | 单类型 RecyclerView 适配器，内置 DiffUtil + Payload 局部更新 |
| MultiTypeAdapter | 多类型 RecyclerView 适配器，DSL 构建 + AsyncListDiffer + Payload |
| LoadMoreAdapter | 上拉加载更多适配器，内置 DiffUtil + 加载状态管理 |
| AwDialog | Material Design 风格对话框快捷构建器，生命周期安全 |
| LoadingDialog | 全局单例 Loading 对话框，生命周期感知 |
| AwAnim | 视图动画扩展（fadeIn、fadeOut、slideIn、shake、pulse 等），支持创建型/便捷型双 API |
| AwItemAnimator | RecyclerView Item 入场动画，基于 firstVisiblePosition 延迟 |
| RoundLayout | 圆角裁切容器（ViewOutlineProvider 硬件加速），支持描边 |
| FlowLayout | 流式布局（自动换行标签布局），支持 RTL |
| BadgeView | 角标视图（红点 / 数字 Badge） |
| DividerDecoration | RecyclerView 通用分割线，支持 LinearLayoutManager / GridLayoutManager / StaggeredGridLayoutManager |
| ViewBindingDelegate | Activity/Fragment ViewBinding 属性委托 |

## 引入

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.answufeng:aw-ui:1.0.0")
}
```

## 快速开始

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

// 自定义过渡动画
stateLayout.transition = StateTransition.CROSS_FADE
stateLayout.transition = StateTransition.slideFromBottom()
stateLayout.transition = StateTransition { view, duration ->
    view.alpha = 0f
    view.visibility = View.VISIBLE
    view.animate().alpha(1f).setDuration(duration).start()
}

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

内置过渡动画预设：

| 预设 | 说明 |
|---|---|
| `StateTransition.NONE` | 无动画 |
| `StateTransition.FADE` | 淡入（默认） |
| `StateTransition.CROSS_FADE` | 淡入 + 缩放 |
| `StateTransition.slideFromBottom()` | 从底部滑入 |

### TitleBar

```kotlin
titleBar.title = "详情"
titleBar.setOnBackClickListener { onBackPressedDispatcher.onBackPressed() }
titleBar.setRightText("保存") { saveData() }
titleBar.setRightIcon(R.drawable.ic_more) { showMenu() }

// 沉浸式状态栏（使用 WindowInsets API，兼容 Android 15+）
titleBar.applyImmersivePadding()
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
    },
    bind = { binding, item, position ->
        binding.tvName.text = item.name
    }
)

// Payload 局部更新（可选）
val adapterWithPayload = SimpleAdapter<ItemUserBinding, User>(
    inflate = ItemUserBinding::inflate,
    diffCallback = userDiffCallback,
    bind = { binding, item, _ -> bindFull(binding, item) },
    bindWithPayload = { binding, item, _, payloads ->
        payloads.forEach { payload ->
            if (payload is String) binding.tvName.text = payload
        }
    }
)

adapter.setOnItemClickListener { user, pos -> openDetail(user) }
recyclerView.adapter = adapter
adapter.submitList(userList)
```

### MultiTypeAdapter

```kotlin
// DSL 用法（推荐）
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

// 直接构造（类型安全 register）
val adapter = MultiTypeAdapter(
    itemDiff = { old, new -> (old as? HasId)?.id == (new as? HasId)?.id },
    contentDiff = { old, new -> old == new }
)
adapter.register<TextItem, ItemTextBinding>(ItemTextBinding::inflate) { binding, item, _ ->
    binding.tvText.text = item.text
}
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
adapter.submitInitialList(firstPageData)  // 提交首页数据
adapter.loadMore(nextPageData)            // 追加数据
adapter.noMore()                          // 没有更多数据
adapter.loadFailed()                      // 加载失败（可点击重试）
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

> 所有 AwDialog 方法内置生命周期安全检查，Activity 销毁后调用不会抛出 WindowLeaked。

### LoadingDialog

```kotlin
LoadingDialog.show(context, "提交中…")
LoadingDialog.dismiss()

// 可取消
LoadingDialog.show(context, "加载中…", cancelable = true) { cancelRequest() }

// 自定义视图
LoadingDialog.showWithView(context, customView, cancelable = true)
```

### AwAnim

```kotlin
// ==================== 便捷型 API（自动 start）====================
view.fadeIn()
view.fadeOut()
view.slideInFromBottom()
view.slideOutToTop()
view.slideInFromLeft()
view.slideInFromRight()
view.scaleIn()
view.scaleOut()
view.pulse()
view.shake()
view.bounce()
view.fadeSlideIn()
view.fadeSlideOut()
view.rotate()

// 带回调
view.fadeIn(onEnd = { showToast("动画结束") })

// ==================== 创建型 API（不自动 start，可组合）====================
val out = view.createFadeOut()
val inn = view.createFadeIn()
AnimatorSet().play(out).before(inn).start()

val anim = view.createShake(amplitude = 20f)
anim.startDelay = 200
anim.start()
```

### AwItemAnimator

```kotlin
// 在 Adapter 中使用
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    bind(holder, getItem(position))
    val firstVisible = (recyclerView.layoutManager as? LinearLayoutManager)
        ?.findFirstVisibleItemPosition() ?: 0
    AwItemAnimator.animateItem(holder.itemView, position, firstVisiblePosition = firstVisible)
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

> FlowLayout 自动适配 RTL 布局方向。

### RoundLayout

```xml
<com.answufeng.ui.widget.RoundLayout
    app:round_radius="12dp"
    app:round_strokeColor="#FF0000"
    app:round_strokeWidth="1dp">
    <!-- 子视图将被圆角裁切（API 21+ 使用 ViewOutlineProvider 硬件加速） -->
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

> 支持 LinearLayoutManager、GridLayoutManager 和 StaggeredGridLayoutManager。

### ViewBinding 属性委托

```kotlin
// Activity
class MainActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityMainBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding.tvTitle.text = "Hello"
    }
}

// Fragment
class MyFragment : Fragment(R.layout.fragment_main) {
    private val binding by viewBinding(FragmentMainBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = "Hello"
    }
}
```

## 更新日志

### v2.0.0

**P0 严重优化**
- MultiTypeAdapter 改用 AsyncListDiffer，大列表不再阻塞主线程
- LoadMoreAdapter 基于 ListAdapter 重构，消除闪烁和 ANR
- AwAnim 新增创建型 API（`createXxx`），支持动画组合

**P1 API 易用性**
- MultiTypeAdapter register 方法统一为 reified 泛型，消除手动转型
- SimpleAdapter / MultiTypeAdapter 支持 Payload 局部更新
- StateLayout 新增 StateTransition 接口，支持自定义过渡动画
- 新增 ViewBinding 属性委托（Activity / Fragment）

**P2 暗黑模式**
- 内置状态布局颜色改用主题属性，自动适配暗黑模式
- LoadMoreAdapter Footer 使用 MaterialColors 获取主题色
- DividerDecoration 默认颜色适配暗黑模式

**P3 兼容性**
- RoundLayout API 21+ 使用 ViewOutlineProvider 硬件加速裁切
- FlowLayout 支持 RTL 布局方向
- TitleBar 沉浸式改用 WindowInsets API，兼容 Android 15+
- AwItemAnimator 延迟基于 firstVisiblePosition，滚动动画更自然

**P4 鲁棒性**
- LoadingDialog 改用强引用 + Lifecycle 感知，消除 GC 导致的窗口泄漏
- AwDialog 内置生命周期安全检查，防止 WindowLeaked
- Ext.kt 新增 Context/View 版本 dp/sp 扩展，多窗口/密度场景更准确

**P5 扩展**
- DividerDecoration 支持 StaggeredGridLayoutManager

## 许可证

Apache License 2.0，详见 [LICENSE](LICENSE)。
