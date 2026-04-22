# aw-ui

[![](https://jitpack.io/v/answufeng/aw-ui.svg)](https://jitpack.io/#answufeng/aw-ui)

Android 通用 UI 组件库，涵盖状态管理、列表适配器、对话框、动画、表单验证、输入增强、进度指示、特殊效果等常用场景。

## 文档导读

1. [环境要求](#环境要求) → [工程品质与发版检查](#工程品质与发版检查) → [特性与 API 速览](#特性)  
2. 集成清单：[ProGuard / 混淆](#proguard--混淆)、[常见问题](#常见问题faq)  
3. 演示：[demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md)（含 `BottomTabBarDemoActivity`）与 demo 内 **「演示清单」**

## 环境要求

| 依赖项 | 要求 |
|--------|------|
| minSdk | 24+ |
| compileSdk | 35 |
| targetSdk（demo 验证） | 35 |
| Kotlin | 2.0+ |
| AGP | 8.0+ |
| Java | 17 |
| ViewBinding | 需要启用 |

### 工程品质与发版检查

- **CI**：[`.github/workflows/ci.yml`](.github/workflows/ci.yml) — `assembleRelease`、`ktlintCheck`、`lintRelease`、`:demo:assembleRelease`（R8）。
- **本地建议**：`./gradlew :aw-ui:assembleRelease :aw-ui:ktlintCheck :aw-ui:lintRelease :demo:assembleRelease`
- **演示**：[demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md)；主页标题栏 **「演示清单」** 可速览 Showcase / 状态页 / Banner / **底部导航（AwBottomTabBar）** 等分工。
- **可访问性**：列表/状态类组件请在业务侧为关键控件补 **`contentDescription`**、焦点顺序与 TalkBack 文案（本库保持控件语义中性）。
- **上线前**：对照矩阵在 **真机** 跑一轮列表滚动、对话框与低内存；自定义动画注意与系统「减少动画」设置协调。

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
| AwSwipeRefreshLayout | 下拉刷新容器（基于 SwipeRefreshLayout，主题色指示器） |
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

XML 属性（`app:` 前缀）：

| 属性 | 说明 | 典型值 |
|------|------|--------|
| `titleBar_title` | 标题文案 | 字符串或 `@string/...` |
| `titleBar_showBack` | 是否显示返回区 | 默认 true |
| `titleBar_leftIcon` | 左侧图标 | `@drawable/...` |
| `titleBar_rightText` | 右侧文字 | 字符串 |
| `titleBar_rightIcon` | 右侧图标 | `@drawable/...` |
| `titleBar_titleColor` / `titleBar_bgColor` | 标题色 / 背景色 | 颜色 |
| `titleBar_immersive` | 是否按沉浸式处理 | 与 `applyImmersivePadding()` 配合 |

### AwBottomTabBar（底部导航）

Material 风格的底部 Tab：图标 + 文案（或可切换为仅图标 / 仅文字）、底部指示条（线 / 圆点 / 色块 / 无）、数字或文字角标，可与 [ViewPager2](https://developer.android.com/jetpack/androidx/releases/viewpager2) 双向联动（滑动页面时指示器与选中态跟随）。

可运行示例见 demo 模块 [`BottomTabBarDemoActivity`](demo/src/main/java/com/answufeng/ui/demo/BottomTabBarDemoActivity.kt)（`bindFragments`、角标、`setOnTabReselectedListener`）。

**展示模式 `tab_mode`（XML） / `tabMode`（代码）**

| 值 | 含义 |
|----|------|
| `icon_text`（0） | 上图标下标题，默认 |
| `icon_only`（1） | 仅图标 |
| `text_only`（2） | 仅标题 |

**指示器 `indicator_style` / `indicatorStyle`**

| 值 | 含义 |
|----|------|
| `line`（0） | 底部圆角横条，宽度可铺满整个 Tab 或跟随文字宽度 |
| `dot`（1） | 底部小圆点 |
| `block`（2） | 底部大圆角矩形条（略缩进） |
| `none`（3） | 不绘制指示器 |

**XML 属性一览**

| 属性 | 说明 | 备注 |
|------|------|------|
| `tab_titles` | `@array` 字符串数组 | 与 `tab_icons` 成对可在 XML 中预置 Tab |
| `tab_icons` | `@array` 的 drawable 引用数组 | 与 `tab_titles` 逐项对应 |
| `tab_selected_color` | 选中时图标（着色开启时）与文字颜色 | 默认为主题色 |
| `tab_normal_color` | 未选中颜色 | |
| `tab_icon_size` | 图标边长 | dimension |
| `tab_text_size` | 标题字号 | dimension（px） |
| `tab_margin` | 相邻 Tab 之间的左边距 | 首项不加 |
| `tab_auto_tint` | 是否用选中/未选中色对图标做 tint | 矢量图 / 单色图标建议 true |
| `tab_selected_scale` | 选中时图标或文字的缩放倍数 | 默认 1.1，`1` 关闭缩放动画 |
| `indicator_color` | 指示器填充色 | |
| `indicator_height` | 指示器厚度（线/块高度或点直径相关） | |
| `indicator_marginTop` | 指示器距底边的向上偏移 | 微调与底边的距离 |
| `indicator_width_follows_text` | 线型指示器宽度是否对齐文字区域 | 无文字 Tab 时回退为居中 40% 宽度 |
| `indicator_anim_duration` | 指示器位移动画时长（ms） | 默认 300 |
| `badge_background_color` | 角标圆点/数字底背景色 | |

**代码示例**

```kotlin
// 动态配置（与 XML 二选一或混用：XML 预置后仍可用 setItems 覆盖）
tabBar.setItems(
    listOf(
        AwBottomTabBar.TabItem("首页", iconRes = R.drawable.ic_home),
        AwBottomTabBar.TabItem("消息", iconRes = R.drawable.ic_msg),
        AwBottomTabBar.TabItem(titleRes = R.string.tab_me, iconRes = R.drawable.ic_me),
    )
)
tabBar.bindViewPager(viewPager)
// 或一次性挂上 Fragment + ViewPager2：
// tabBar.bindFragments(this, listOf(f1, f2, f3), viewPager)

tabBar.setOnTabSelectedListener { index -> /* 用户点选切换到 index（与当前不同）*/ }
tabBar.setOnTabReselectedListener { index -> /* 再次点击已选中的 Tab */ }
tabBar.setOnTabLongClickListener { index -> /* 长按 */ }

tabBar.setBadgeCount(1, 3)           // 数字角标，>99 显示为 "99+"
tabBar.setBadgeText(2, "新")         // 自定义短文案角标
tabBar.clearBadge(1)

tabBar.setCurrentIndex(0, animate = true)  // 程序化切换
```

**XML 预置标题与图标**

```xml
<com.answufeng.ui.widget.AwBottomTabBar
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:background="?attr/colorSurface"
    app:tab_mode="icon_text"
    app:indicator_style="line"
    app:tab_titles="@array/main_tabs"
    app:tab_icons="@array/main_tab_icons"
    app:tab_selected_color="?attr/colorPrimary"
    app:tab_normal_color="#99000000"
    app:indicator_width_follows_text="true" />
```

**生命周期**

- `Activity`/`Fragment` 销毁前若曾调用 `bindViewPager`，控件在 `onDetachedFromWindow` 时会自动 `unbindViewPager`；若在多层嵌套中手动持有 `ViewPager2` 引用，仍建议在页面销毁时取消对监听器的依赖，避免泄漏。

**无障碍**：会填充 `AccessibilityEvent` 的 `itemCount` / `currentItemIndex`，建议在业务侧为每个 Tab 的语义补充 `contentDescription`（例如未读数变化时更新文案）。

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

### AwActionSheetDialog（iOS 风格底部操作菜单）

```kotlin
AwActionSheetDialog(context)
    .setTitle("选择操作")
    .addItem("拍照") { takePhoto() }
    .addItem("从相册选择") { pickFromGallery() }
    .addItem("取消", isCancel = true)
    .show()

// 支持暗黑模式
AwActionSheetDialog(context, isDarkMode = true)
    .setTitle("确认删除")
    .addItem("删除", isDestructive = true) { deleteItem() }
    .addItem("取消", isCancel = true)
    .show()
```

### AwRoundImageView（圆角/圆形图片）

```kotlin
// XML 配置（属性前缀为 roundImg_，与 attrs 一致）
<com.answufeng.ui.widget.AwRoundImageView
    android:layout_width="80dp"
    android:layout_height="80dp"
    app:roundImg_radius="8dp"
    app:roundImg_borderWidth="2dp"
    app:roundImg_borderColor="#FFFFFF"
    app:roundImg_isCircle="false" />

// 代码设置（圆角半径为像素值）
roundImageView.setImageResource(R.drawable.avatar)
roundImageView.radius = 16f * resources.displayMetrics.density
roundImageView.borderWidth = 4f
roundImageView.borderColor = Color.WHITE
roundImageView.isCircle = true
```

### AwRoundLayout（圆角容器）

子 View 按圆角裁切，支持统一圆角或四角独立半径及描边。属性见 [attrs.xml](aw-ui/src/main/res/values/attrs.xml) 中 `AwRoundLayout`：`round_radius`、`round_topLeftRadius` 等。

```xml
<com.answufeng.ui.widget.AwRoundLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:round_radius="12dp"
    app:round_strokeWidth="1dp"
    app:round_strokeColor="#33000000">
    <!-- 子内容 -->
</com.answufeng.ui.widget.AwRoundLayout>
```

### AwSwipeRefreshLayout（下拉刷新）

继承 `SwipeRefreshLayout`，在 `init` 中用主题 `colorPrimary` 设置刷新环颜色。请将可垂直滚动的列表作为直接子 View，结束刷新时设置 `isRefreshing = false`。可与 `AwLoadMoreAdapter` 同屏使用，注意刷新与加载更多的并发与列表数据源一致性。

### AwLoadMoreAdapter（加载更多）

仅对 [LinearLayoutManager](https://developer.android.com/reference/androidx/recyclerview/widget/LinearLayoutManager) 自动检测触底；底部 Footer 展示加载中、失败（可点击重试）、无更多。

```kotlin
val adapter = AwLoadMoreAdapter(ItemBinding::inflate, diffCallback) { b, item, _ ->
    b.tvTitle.text = item.title
}
adapter.preloadOffset = 3 // 距底部还剩几个 item 时预加载，默认 3
adapter.setOnLoadMoreListener {
    // 异步结束后：有下一页数据 -> loadMore(list)；无后续 -> noMore()；失败 -> loadFailed()
}
adapter.submitInitialList(firstPage) // 或 refreshAll(list) 替换全表并回到 IDLE

// 文案
adapter.loadingText = "正在加载…"
adapter.noMoreText = "—— 没有更多了 ——"
adapter.failedText = "加载失败，点击重试"
```

与下拉刷新组合时：**刷新**请调用 `submitInitialList` / `refreshAll` 重置数据与加载状态，并递增你自己的「请求代数」以丢弃旧的加载回调（示例见 demo [`SwipeRefreshListDemoActivity`](demo/src/main/java/com/answufeng/ui/demo/SwipeRefreshListDemoActivity.kt)）。

### AwExpandableLayout（可展开/收起布局）

```kotlin
expandableLayout.expand()     // 展开
expandableLayout.collapse()   // 收起
expandableLayout.toggle()     // 切换状态

expandableLayout.setOnExpandChangeListener { isExpanded ->
    arrow.rotation = if (isExpanded) 180f else 0f
}
```

### AwMarqueeTextView（跑马灯文本）

```kotlin
marqueeTextView.text = "这是一条很长的跑马灯文字..."
marqueeTextView.startScroll()     // 开始滚动
marqueeTextView.stopScroll()      // 停止滚动
marqueeTextView.isFocused = true  // 需要获取焦点才能滚动
```

### AwSwitchButton（自定义开关）

```kotlin
switchButton.isChecked = true
switchButton.setOnCheckedChangeListener { _, isChecked ->
    updateUI(isChecked)
}
switchButton.setThumbColor(normalColor, checkedColor)
switchButton.setTrackColor(normalColor, checkedColor)
```

### AwCircleProgressBar（圆形进度条）

```kotlin
progressBar.progress = 60           // 设置进度（0-100）
progressBar.maxProgress = 100       // 最大值
progressBar.setProgressText("60%") // 显示文字
progressBar.setProgressColor(Color.BLUE)
progressBar.setBackgroundColor(Color.GRAY)
progressBar.startAnimation()        // 开始动画
progressBar.stopAnimation()          // 停止动画
```

### AwTooltipView（气泡提示）

```kotlin
tooltipView.setText("提示内容")
tooltipView.setTextColor(Color.WHITE)
tooltipView.setBackgroundColor(Color.BLACK)
tooltipView.setArrowDirection(AwTooltipView.ArrowDirection.BOTTOM)
tooltipView.show(anchorView)       // 显示在指定视图下方
tooltipView.dismiss()               // 隐藏
```

### AwSegmentedControl（iOS 风格分段选择器）

```kotlin
segmentedControl.setItems(listOf("选项1", "选项2", "选项3"))
segmentedControl.setSelectedIndex(0)
segmentedControl.setOnSelectionChangeListener { index, text ->
    Log.d("Segment", "Selected: $index - $text")
}
segmentedControl.setSelectedTextColor(Color.WHITE)
segmentedControl.setUnselectedTextColor(Color.GRAY)
```

### AwSmartEditText（智能输入框）

```kotlin
smartEditText.setRule(AwSmartEditText.Rule.PHONE)     // 手机号
smartEditText.setRule(AwSmartEditText.Rule.EMAIL)    // 邮箱
smartEditText.setRule(AwSmartEditText.Rule.ID_CARD)  // 身份证
smartEditText.setRule(AwSmartEditText.Rule.CUSTOM, regex = "^[a-zA-Z0-9]+$")
smartEditText.setMaxLength(11)                        // 最大长度
smartEditText.setOnValidityChangeListener { isValid ->
    submitButton.isEnabled = isValid
}
```

### AwCodeInputView（验证码输入框）

```kotlin
codeInputView.codeLength = 6                    // 验证码长度
codeInputView.setOnCodeCompleteListener { code ->
    verifyCode(code)
}
codeInputView.setOnCodeChangeListener { code ->
    updateUI(code.length)
}
// 自动聚焦
codeInputView.requestFocus()
```

### AwPasswordInputView（密码输入框）

```kotlin
passwordInputView.setOnPasswordChangeListener { password, isStrong ->
    strengthIndicator.level = if (isStrong) 3 else 1
}
passwordInputView.passwordStrengthRule = AwPasswordInputView.StrengthRule(
    weak = ".*",
    medium = "^(?=.*[a-zA-Z])(?=.*[0-9]).{6,}$",
    strong = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#\$%^&*]).{8,}$"
)
passwordInputView.showPasswordToggle = true       // 显示/隐藏切换按钮
```

## API 速查

### Widget 组件

| API | 说明 | 关键属性/方法 |
|-----|------|--------------|
| `AwBannerView` | 轮播图 | `setData()`, `startAutoScroll()`, `setOnPageClickListener` |
| `AwBadgeView` | 角标视图 | `count`, `maxCount`, `increment()`, `decrement()` |
| `AwBottomTabBar` | 底部导航栏 | `setItems()`, `bindViewPager()`, `setOnTabSelectedListener` |
| `AwCodeInputView` | 验证码输入 | `codeLength`, `setOnCodeCompleteListener` |
| `AwCountDownView` | 倒计时 | `start(seconds)`, `onFinish`, `autoDisable` |
| `AwExpandableLayout` | 展开/收起布局 | `expand()`, `collapse()`, `toggle()` |
| `AwFlowLayout` | 流式布局 | `flow_horizontalSpacing`, `flow_maxLines` |
| `AwMarqueeTextView` | 跑马灯文本 | `startScroll()`, `stopScroll()` |
| `AwPasswordInputView` | 密码输入 | `setOnPasswordChangeListener`, `showPasswordToggle` |
| `AwRoundImageView` | 圆角/圆形图片 | `radius`, `isCircle`, `borderWidth`（XML：`roundImg_*`） |
| `AwRoundLayout` | 圆角容器 | `setRadius`, `setRadii`, `setStroke`；XML：`round_*` |
| `AwSearchView` | 搜索栏 | `onQueryChange`, `onQuerySubmit` |
| `AwSegmentedControl` | 分段选择器 | `items`, `selectedIndex`, `onSelectionChange` |
| `AwSkeletonView` | 骨架屏 | `startShimmer()`, `stopShimmer()` |
| `AwSmartEditText` | 智能输入框 | `setRule()`, `setMaxLength()` |
| `AwSwitchButton` | 开关按钮 | `isChecked`, `setOnCheckedChangeListener` |
| `AwTagView` | 标签选择 | `tags`, `selectionMode`, `onSelectionChange` |
| `AwTooltipView` | 气泡提示 | `setText()`, `show(anchorView)`, `dismiss()` |

### 对话框组件

| API | 说明 | 关键方法 |
|-----|------|---------|
| `AwDialog.Builder` | 对话框构建器 | `title()`, `message()`, `show()` |
| `LoadingDialog` | Loading 对话框 | `show(context, message)` |
| `AwActionSheetDialog` | 底部操作菜单 | `setTitle()`, `addItem()`, `show()` |
| `AwBottomSheet` | 底部弹窗 | `setContentView()`, `setPeekHeight()`, `show()` |

### 列表组件

| API | 说明 | 关键方法 |
|-----|------|---------|
| `AwSimpleAdapter` | 单类型适配器 | `submitList()`, 可选 `bindWithPayload`, `setOnItemClickListener`, `setEmptyView` |
| `AwMultiTypeAdapter` | 多类型适配器 | `register()`, `submitList()` |
| `AwLoadMoreAdapter` | 加载更多适配器 | `setOnLoadMoreListener`, `submitInitialList`, `loadMore`, `noMore`, `loadFailed`, `refreshAll` |
| `AwSwipeRefreshLayout` | 下拉刷新 | `setOnRefreshListener { }`，结束刷新设 `isRefreshing = false` |
| `AwDividerDecoration` | 分割线 | 构造：`height(px)`, `color`, `paddingStart/End(px)`；依赖 `LinearLayoutManager` 等 |
| `AwItemAnimator` | 入场动画 | `recyclerView.itemAnimator = AwItemAnimator()` |

### 其他组件

| API | 说明 | 关键方法 |
|-----|------|---------|
| `AwStateLayout` | 四态容器 | `showLoading()`, `showContent()`, `showEmpty()`, `showError()` |
| `AwTitleBar` | 标题栏 | `title`, `setRightText()`, `applyImmersivePadding()` |
| `AwFormValidator` | 表单验证 | `addField()`, `validate()`, `getErrors()` |
| `viewBinding()` | ViewBinding 委托 | `by viewBinding(BindingClass::bind)` |
| `16.dp` | 尺寸扩展 | `Int.dp`, `Float.sp`, `Int.pxToDp` |

## ProGuard / 混淆

aw-ui 已内置 consumer-rules.pro，在被宿主应用混淆时会自动应用以下规则：

- 保留自定义 View 类名（XML 布局 inflation 需要）
- 保留 RecyclerView 适配器相关类
- 保留表单验证相关类
- 保留 Kotlin 元数据和注解

**宿主应用无需额外配置**，库会自动处理混淆规则。

## 常见问题（FAQ）

### 1. 为什么我的 ViewBinding 委托不起作用？

请确保：
- 在 `onCreate`（Activity）或 `onViewCreated`（Fragment）中调用 `setContentView` 或返回 `binding.root`
- ViewBinding 类名与布局文件名匹配（如 `activity_main.xml` 对应 `ActivityMainBinding`）

### 2. 为什么 AwBannerView 不自动轮播？

请确保：
- 调用了 `startAutoScroll()` 方法
- 至少有 2 个页面
- 未在 `onPause` 时停止轮播

### 3. 为什么 AwStateLayout 切换动画不生效？

请确保：
- 内容视图是通过 `setContentView` 或 XML 设置的
- 切换状态时视图已经加载完成

### 4. 如何自定义 AwDialog 的样式？

可以通过继承 `AwDialog.Builder` 或使用 `setView()` 方法设置自定义布局。

### 5. AwFlowLayout 在 RTL 布局下如何工作？

`flow_gravity` 属性支持 `start` / `center` / `end`，在 RTL 布局下会自动适配。

## 许可证

Apache License 2.0，详见 [LICENSE](LICENSE)。

# Last updated: 2026年 4月 22日（BottomTabBar 演示；下拉刷新页接入加载更多与 README API 修正）
