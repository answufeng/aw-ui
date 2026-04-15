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
| SimpleAdapter | 单类型 RecyclerView 适配器，DiffUtil + Payload |
| MultiTypeAdapter | 多类型适配器，DSL 构建 + AsyncListDiffer + Payload |
| LoadMoreAdapter | 上拉加载更多适配器，DiffUtil + 加载状态管理 |
| AwItemAnimator | RecyclerView Item 入场动画 |
| DividerDecoration | 通用分割线，支持 Linear/Grid/StaggeredGrid |

### 对话框和提示

| 组件 | 说明 |
|---|---|
| AwDialog | Material Design 风格对话框快捷构建器 |
| LoadingDialog | Loading 对话框 |
| AwActionSheetDialog | iOS 风格底部操作菜单 |
| AwBadgeView | 角标视图（红点 / 数字 Badge） |
| AwTooltipView | 气泡提示组件，支持箭头方向 |

### 进度和加载

| 组件 | 说明 |
|---|---|
| AwCircleProgressBar | 圆形进度条，支持动画和百分比文字 |
| AwSkeletonView | 骨架屏加载，shimmer 闪光动画 |

### 特殊效果

| 组件 | 说明 |
|---|---|
| AwBannerView | 轮播图组件，ViewPager2 + 自动轮播 + 指示器 |
| AwRoundImageView | 圆角/圆形图片，BitmapShader + 边框 |
| AwRoundLayout | 圆角裁切容器（ViewOutlineProvider 硬件加速） |
| AwExpandableLayout | 可展开/收起布局，高度动画 |

### 布局和导航

| 组件 | 说明 |
|---|---|
| AwStateLayout | 四态容器（内容/加载中/空/错误），自定义过渡动画 |
| AwTitleBar | 通用标题栏，沉浸式状态栏 |
| AwFlowLayout | 流式布局（自动换行），支持 RTL |

### 工具组件

| 组件 | 说明 |
|---|---|
| AwMarqueeTextView | 跑马灯文本，自动滚动 |
| AwSwitchButton | 自定义开关按钮，滑动动画 |
| AwCountDownView | 倒计时组件 |
| AwAnim | 视图动画扩展（fadeIn、fadeOut、shake 等） |
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

### AwSmartEditText

```kotlin
smartEditText.maxLength = 11
smartEditText.inputFilter = { it.isDigit() }
smartEditText.onFormat = { raw -> raw.chunked(4).joinToString("-") }
smartEditText.addValidator("phone", { it.length == 11 }, "请输入11位手机号")
val valid = smartEditText.validate()
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `smart_maxLength` | 最大输入长度 | 0（不限） |
| `smart_errorColor` | 错误提示文字颜色 | 红色 |

### AwCodeInputView

```kotlin
codeInputView.codeLength = 4
codeInputView.onCodeComplete = { code -> verifyCode(code) }
val entered = codeInputView.code
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `code_length` | 验证码位数 | 6 |
| `code_boxSize` | 输入框尺寸 | 48dp |
| `code_boxSpacing` | 输入框间距 | 8dp |
| `code_boxStrokeColor` | 输入框边框颜色 | #CCCCCC |
| `code_boxStrokeWidth` | 输入框边框宽度 | 2dp |
| `code_textColor` | 数字文字颜色 | 黑色 |
| `code_textSize` | 数字文字大小 | 18sp |

### AwPasswordInputView

```kotlin
passwordInput.password = "MyP@ss1"
val strength = passwordInput.strength  // WEAK / MEDIUM / STRONG
passwordInput.onStrengthChange = { s -> updateStrengthLabel(s) }
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `password_hint` | 输入框提示文字 | "Password" |
| `password_showToggle` | 显示密码可见性切换 | true |
| `password_showStrength` | 显示强度指示条 | true |

### AwSegmentedControl

```kotlin
segmentedControl.items = listOf("Tab 1", "Tab 2", "Tab 3")
segmentedControl.onSelectionChange = { index -> switchTab(index) }
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `seg_items` | 分段标签数组引用 | 空 |
| `seg_selectedIndex` | 初始选中索引 | 0 |
| `seg_selectedColor` | 滑块背景色 | 白色 |
| `seg_textColor` | 未选中文字颜色 | 灰色 |
| `seg_selectedTextColor` | 选中文字颜色 | 黑色 |

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

内置规则：

| 规则 | 说明 |
|---|---|
| `required()` | 非空校验 |
| `minLength(n)` | 最小长度 |
| `maxLength(n)` | 最大长度 |
| `email()` | 邮箱格式 |
| `phone()` | 手机号格式（11位） |
| `pattern(regex)` | 正则匹配 |
| `custom(predicate)` | 自定义校验 |

### AwCircleProgressBar

```kotlin
circleProgressBar.progress = 75f
circleProgressBar.setProgressWithAnimation(75f)
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `circleProgress_progress` | 初始进度 | 0 |
| `circleProgress_max` | 最大值 | 100 |
| `circleProgress_strokeWidth` | 环形宽度 | 8dp |
| `circleProgress_progressColor` | 进度弧颜色 | #4CAF50 |
| `circleProgress_bgColor` | 背景环颜色 | #E0E0E0 |
| `circleProgress_showText` | 显示百分比文字 | true |

### AwSkeletonView

```kotlin
skeletonView.startShimmer()
skeletonView.stopShimmer()
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `skeleton_baseColor` | 基础颜色 | #E0E0E0 |
| `skeleton_highlightColor` | 高光颜色 | #F5F5F5 |
| `skeleton_cornerRadius` | 圆角半径 | 4dp |
| `skeleton_duration` | 动画周期（ms） | 1000 |

### AwBannerView

```kotlin
bannerView.setAdapter(myAdapter)
bannerView.setOnPageClickListener { position -> openDetail(position) }
bannerView.startAutoScroll()
bannerView.stopAutoScroll()
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `banner_interval` | 自动轮播间隔（ms） | 3000 |
| `banner_indicatorColor` | 未选中指示点颜色 | #80FFFFFF |
| `banner_indicatorSelectedColor` | 选中指示点颜色 | #FFFFFF |

### AwRoundImageView

```kotlin
roundImageView.isCircle = true
roundImageView.radius = 16f
roundImageView.borderWidth = 2f
roundImageView.borderColor = Color.WHITE
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `roundImg_radius` | 圆角半径 | 0 |
| `roundImg_isCircle` | 是否圆形裁切 | false |
| `roundImg_borderWidth` | 边框宽度 | 0 |
| `roundImg_borderColor` | 边框颜色 | #FFFFFF |

### AwExpandableLayout

```kotlin
expandableLayout.expand()
expandableLayout.collapse()
expandableLayout.toggle()
expandableLayout.onExpandChange = { isExpanded -> updateArrow(isExpanded) }
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `expandable_expanded` | 初始是否展开 | false |
| `expandable_duration` | 动画时长（ms） | 300 |

### AwActionSheetDialog

```kotlin
AwActionSheetDialog(context)
    .setTitle("选择操作")
    .setItems(listOf("拍照", "相册", "删除"))
    .setDestructiveIndex(2)
    .setOnSelect { index -> handleSelection(index) }
    .setOnCancel { /* 取消 */ }
    .show()
```

### AwTooltipView

```kotlin
tooltipView.text = "Hello"
tooltipView.arrowPosition = ArrowPosition.BOTTOM
tooltipView.show(anchorView)
tooltipView.dismiss()
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `tooltip_text` | 提示文字 | "" |
| `tooltip_arrowPosition` | 箭头方向（left/top/right/bottom） | bottom |
| `tooltip_bgColor` | 背景颜色 | #333333 |
| `tooltip_textColor` | 文字颜色 | #FFFFFF |

### AwMarqueeTextView

```kotlin
marqueeView.speed = 2f
marqueeView.direction = AwMarqueeTextView.Direction.RIGHT_TO_LEFT
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `marquee_speed` | 滚动速度（px/帧） | 1 |
| `marquee_pauseDuration` | 到达端点暂停时长（ms） | 1000 |
| `marquee_direction` | 滚动方向（left_to_right / right_to_left） | right_to_left |

### AwSwitchButton

```kotlin
switchButton.isChecked = true
switchButton.onCheckedChangeListener = { checked -> handleToggle(checked) }
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `switch_checked` | 初始选中状态 | false |
| `switch_trackColor` | 未选中轨道颜色 | #CCCCCC |
| `switch_trackCheckedColor` | 选中轨道颜色 | #4CAF50 |
| `switch_thumbColor` | 未选中滑块颜色 | 白色 |
| `switch_thumbCheckedColor` | 选中滑块颜色 | 白色 |

### AwCountDownView

```kotlin
countDownView.formatTime = { seconds -> "${seconds}s remaining" }
countDownView.onFinish = { showToast("Done!") }
countDownView.start(60)
```

XML 属性：

| 属性 | 说明 | 默认值 |
|---|---|---|
| `countdown_seconds` | 初始倒计时秒数 | 60 |
| `countdown_textColor` | 文字颜色 | 当前文字颜色 |
| `countdown_textSize` | 文字大小 | 14sp |

### StateLayout

```kotlin
stateLayout.showLoading()
stateLayout.showContent()
stateLayout.showEmpty()
stateLayout.showError { retryLoad() }

stateLayout.transition = StateTransition.CROSS_FADE
stateLayout.transition = StateTransition.slideFromBottom()
stateLayout.transition = StateTransition { view, duration ->
    view.alpha = 0f
    view.visibility = View.VISIBLE
    view.animate().alpha(1f).setDuration(duration).start()
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
adapter.setOnItemClickListener { user, pos -> openDetail(user) }
adapter.submitList(userList)
```

### MultiTypeAdapter

```kotlin
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
    diffCallback = articleDiffCallback
) { binding, item, _ ->
    binding.tvTitle.text = item.title
}

adapter.setOnLoadMoreListener { fetchNextPage() }
adapter.submitInitialList(firstPageData)
adapter.loadMore(nextPageData)
adapter.noMore()
adapter.loadFailed()
```

### AwDialog

```kotlin
AwDialog.alert(context).setDialogTitle("提示").setDialogMessage("操作成功").showDialog()
AwDialog.showConfirm(context, "提示", "确定删除吗？") { deleteItem() }
```

### LoadingDialog

```kotlin
LoadingDialog.show(context, "提交中…")
```

### AwAnim

```kotlin
view.fadeIn()
view.fadeOut()
view.slideInFromBottom()
view.shake()
view.pulse()
view.bounce()

val anim = view.createShake(amplitude = 20f)
anim.startDelay = 200
anim.start()
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
    DividerDecoration(height = 1.dp, color = Color.LTGRAY)
)
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
```

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
