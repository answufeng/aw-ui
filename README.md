# aw-ui

[![](https://jitpack.io/v/answufeng/aw-ui.svg)](https://jitpack.io/#answufeng/aw-ui)

Android 通用 UI 组件库，涵盖状态管理、列表适配器、对话框、动画、表单验证、输入增强、进度指示、特殊效果等常用场景。

## 特性

### 基础控件增强

| 组件 | 说明 |
|---|---|
| AwSmartEditText | 智能输入框，支持实时验证、输入限制、自动格式化 |
| AwCodeInputView | 验证码输入框，自动聚焦、粘贴支持 |
| AwPasswordInputView | 密码输入框，带强度检测、显示/隐藏切换 |
| AwSegmentedControl | iOS 风格分段选择器，滑动高亮动画 |

### 表单验证

| 组件 | 说明 |
|---|---|
| AwFormValidator | 统一表单验证框架，链式调用，内置常用规则 |

### 列表和滚动

| 组件 | 说明 |
|---|---|
| AwSimpleAdapter | 单类型 RecyclerView 适配器，DiffUtil + Payload |
| AwMultiTypeAdapter | 多类型适配器，DSL 构建 + AsyncListDiffer + Payload |
| AwLoadMoreAdapter | 上拉加载更多适配器，DiffUtil + 加载状态管理 |
| AwItemAnimator | RecyclerView Item 入场动画 |
| AwDividerDecoration | 通用分割线，支持 Linear/Grid/StaggeredGrid |

### 对话框和提示

| 组件 | 说明 |
|---|---|
| AwDialog | Material Design 风格对话框 Builder 模式构建器 |
| LoadingDialog | Loading 对话框，支持自定义提示文字 |
| AwActionSheetDialog | iOS 风格底部操作菜单，暗黑模式适配 |
| AwBadgeView | 角标视图（红点 / 数字 Badge），支持自定义最大值 |
| AwTooltipView | 气泡提示组件，支持箭头方向 |
| AwBottomSheet | 底部弹窗组件，封装 BottomSheetDialog |

### 进度和加载

| 组件 | 说明 |
|---|---|
| AwCircleProgressBar | 圆形进度条，支持动画和百分比文字 |
| AwSkeletonView | 骨架屏加载，shimmer 闪光动画，支持 autoStart |

### 特殊效果

| 组件 | 说明 |
|---|---|
| AwBannerView | 轮播图组件，ViewPager2 + 自动轮播 + 生命周期感知 |
| AwRoundImageView | 圆角/圆形图片，BitmapShader + 边框 |
| AwRoundLayout | 圆角裁切容器（ViewOutlineProvider 硬件加速） |
| AwExpandableLayout | 可展开/收起布局，高度动画 |

### 布局和导航

| 组件 | 说明 |
|---|---|
| AwStateLayout | 四态容器（内容/加载中/空/错误），自定义过渡动画 |
| AwTitleBar | 通用标题栏，沉浸式状态栏 |
| AwFlowLayout | 流式布局（自动换行），支持 RTL |
| AwSearchView | 搜索栏组件，带搜索图标、清除按钮 |
| AwTagView | 标签选择组件，支持单选/多选 |

### 工具组件

| 组件 | 说明 |
|---|---|
| AwMarqueeTextView | 跑马灯文本，自动滚动 |
| AwSwitchButton | 自定义开关按钮，滑动动画 |
| AwCountDownView | 倒计时组件，支持自动禁用 |
| Anim | 视图动画扩展（ViewPropertyAnimator + ObjectAnimator） |
| ViewBindingDelegate | Activity/Fragment ViewBinding 属性委托 |
| DimenExt | 尺寸扩展属性（dp/sp/px 转换） |
| DiffCallbacks | 常用 DiffUtil.ItemCallback 预设 |

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
    implementation("com.github.answufeng:aw-ui:2.1.0")
}
```

## 快速开始

### AwDialog（Builder 模式）

```kotlin
AwDialog.Builder(context)
    .title("提示")
    .message("操作成功")
    .positiveButton("确定")
    .show()

AwDialog.Builder(context)
    .title("确认")
    .message("确定删除吗？")
    .positiveButton("删除") { deleteItem() }
    .negativeButton("取消")
    .show()

AwDialog.showMessage(context, "提示", "操作成功")
AwDialog.showConfirm(context, "确认", "确定删除吗？") { deleteItem() }
```

### LoadingDialog

```kotlin
LoadingDialog.show(context, "提交中…")
```

### AwBannerView

```kotlin
bannerView.setAdapter(myAdapter)
bannerView.setData(imageList) { container, url, position ->
    val imageView = ImageView(context)
    imageView.load(url)
    container.addView(imageView)
}
bannerView.setOnPageClickListener { position -> openDetail(position) }
bannerView.startAutoScroll()
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `banner_interval` | 自动轮播间隔（ms） | 3000 |
| `banner_indicatorColor` | 未选中指示点颜色 | #80FFFFFF |
| `banner_indicatorSelectedColor` | 选中指示点颜色 | #FFFFFF |

### AwBadgeView

```kotlin
badgeView.count = 3       // 显示数字 "3"
badgeView.count = 0       // 显示红点
badgeView.count = -1      // 隐藏
badgeView.maxCount = 999  // 自定义最大值，超过显示 "999+"
badgeView.increment()     // +1
badgeView.decrement()     // -1
badgeView.clear()         // 隐藏
```

### AwCountDownView

```kotlin
countDownView.formatTime = { seconds -> "${seconds}s remaining" }
countDownView.autoDisable = true  // 倒计时期间自动禁用
countDownView.onFinish = { showToast("Done!") }
countDownView.start(60)
```

### AwStateLayout

```kotlin
stateLayout.showLoading()
stateLayout.showContent()
stateLayout.showEmpty()
stateLayout.showError { retryLoad() }
stateLayout.setContentView(customView)  // 代码设置内容视图

stateLayout.transition = StateTransition.CROSS_FADE
stateLayout.transition = StateTransition.slideFromBottom()
```

### Anim（ViewPropertyAnimator + ObjectAnimator）

```kotlin
view.fadeIn(300L) { /* onEnd */ }
view.fadeOut(300L) { /* onEnd */ }
view.slideInFromBottom(300L)
view.slideOutToBottom(300L)
view.shake(500L)
view.pulse(200L)
view.bounce(400L)
view.fadeSlideIn(300L)
```

### ViewBinding 属性委托

```kotlin
class MainActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityMainBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding.tvTitle.text = "Hello"
    }
}

class MyFragment : Fragment() {
    private val binding by viewBinding(FragmentMyBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTitle.text = "Hello"
    }
}
```

### Dimen 扩展

```kotlin
val width = 16.dp
val height = 48.dp
val textSize = 14.sp
val px = 100.pxToDp
```

### DiffCallbacks 预设

```kotlin
val adapter = AwSimpleAdapter(
    inflate = ItemBinding::inflate,
    diffCallback = stringDiffCallback(),
    bind = { binding, item, _ -> binding.tvName.text = item }
)

val adapter2 = AwSimpleAdapter(
    inflate = ItemBinding::inflate,
    diffCallback = idDiffCallback { it.id },
    bind = { binding, item, _ -> binding.tvName.text = item.name }
)
```

### AwBottomSheet

```kotlin
AwBottomSheet()
    .setContentView(sheetContentView)
    .setPeekHeight(400)
    .setDraggable(true)
    .show(context)
```

### AwSearchView

```kotlin
searchView.hint = "搜索内容"
searchView.onQueryChange = { query -> filterList(query) }
searchView.onQuerySubmit = { query -> performSearch(query) }
```

### AwTagView

```kotlin
tagView.selectionMode = AwTagView.SelectionMode.MULTI
tagView.tags = listOf("Kotlin", "Java", "Python", "Go")
tagView.onSelectionChange = { selectedTags -> updateFilter(selectedTags) }
```

### AwSkeletonView

```kotlin
skeletonView.autoStart = false  // 不自动开始
skeletonView.startShimmer()
skeletonView.stopShimmer()
```

### AwTitleBar

```kotlin
titleBar.title = "详情"
titleBar.setOnBackClickListener { onBackPressedDispatcher.onBackPressed() }
titleBar.setRightText("保存") { saveData() }
titleBar.applyImmersivePadding()
```

### AwFlowLayout

```xml
<com.answufeng.ui.widget.AwFlowLayout
    app:flow_horizontalSpacing="8dp"
    app:flow_verticalSpacing="8dp"
    app:flow_maxLines="3"
    app:flow_gravity="center">
    <TextView ... />
</com.answufeng.ui.widget.AwFlowLayout>
```

`flow_gravity` 支持 `start` / `center` / `end`，兼容 RTL 布局。

### AwFormValidator

```kotlin
val validator = AwFormValidator()
    .addField(usernameInput, AwFormValidator.required(), AwFormValidator.minLength(3))
    .addField(emailInput, AwFormValidator.required(), AwFormValidator.email())
    .addField(phoneInput, AwFormValidator.phone())

if (validator.validate()) {
    submitForm()
} else {
    validator.getErrors().values.forEach { println(it) }
}
```

### 其他组件

更多组件的详细用法请参考源码中的 KDoc 文档。

## 更新日志

### v2.1.0

**新增组件**
- AwSmartEditText — 智能输入框，实时验证、输入限制、自动格式化
- AwCodeInputView — 验证码输入框，自动聚焦、粘贴支持
- AwPasswordInputView — 密码输入框，强度检测、显示/隐藏切换
- AwSegmentedControl — iOS 风格分段选择器
- AwFormValidator — 统一表单验证框架，链式调用
- AwCircleProgressBar — 圆形进度条，动画+百分比文字
- AwSkeletonView — 骨架屏加载，shimmer 闪光动画
- AwBannerView — 轮播图组件，ViewPager2 + 自动轮播
- AwRoundImageView — 圆角/圆形图片，BitmapShader + 边框
- AwExpandableLayout — 可展开/收起布局，高度动画
- AwActionSheetDialog — iOS 风格底部操作菜单
- AwTooltipView — 气泡提示组件
- AwMarqueeTextView — 跑马灯文本
- AwSwitchButton — 自定义开关按钮
- AwCountDownView — 倒计时组件

### v2.0.0

- MultiTypeAdapter 改用 AsyncListDiffer
- LoadMoreAdapter 基于 ListAdapter 重构
- AwAnim 新增创建型 API
- StateLayout 新增 StateTransition 接口
- ViewBinding 属性委托
- RoundLayout ViewOutlineProvider 硬件加速
- FlowLayout RTL 支持
- TitleBar WindowInsets 沉浸式
- 暗黑模式适配

## 许可证

Apache License 2.0，详见 [LICENSE](LICENSE)。
