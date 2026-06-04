# aw-ui

[![JitPack](https://jitpack.io/v/answufeng/aw-ui.svg)](https://jitpack.io/#answufeng/aw-ui)

基于 **Android View / XML** 的通用 UI 组件库，覆盖状态页、列表适配、弹窗、表单与输入、动效、标题栏、横幅等日常场景。如果你只想最快跑起来，直接看下面的「5 分钟上手」即可；其它内容都可以后置按需查阅。

---

## 5 分钟上手（最小接入）

### 1) 添加依赖（JitPack）

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.answufeng:aw-ui:1.1.1")
}
```

`implementation` 中的 **版本号与 Git / JitPack 的 tag 一致**（当前为 `1.1.1`）。

### 2) 打开 ViewBinding（推荐）

```kotlin
// app/build.gradle.kts
android {
    buildFeatures {
        viewBinding = true
    }
}
```

### 3) 放一个 `AwStateLayout`（XML + Kotlin）

```xml
<com.answufeng.ui.statelayout.AwStateLayout
    android:id="@+id/stateLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- 你的内容视图 -->
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</com.answufeng.ui.statelayout.AwStateLayout>
```

```kotlin
stateLayout.showLoading()
stateLayout.showError { loadData() }
stateLayout.showContent()
```

更完整示例见 demo 的 `StateDemoActivity`（入口矩阵见下方「本仓库与 Demo」）。

---

## 目录（按常见需求跳转）

| 想做什么 | 跳转到 |
|----------|--------|
| 最短时间接入并看到效果 | [5 分钟上手（最小接入）](#5-分钟上手最小接入) · [环境要求](#环境要求) |
| 想快速知道有哪些组件 | [功能概览](#功能概览) |
| 复制可用的代码片段 | [常用片段](#常用片段) |
| 混淆 / 依赖冲突 / 常见坑 | [R8 / ProGuard](#r8--proguard) · [常见问题](#常见问题) |
| 本地构建与 Demo | [本仓库与 Demo](#本仓库与-demo) |

---

## 环境要求

| 项目 | 最低版本 |
|------|----------|
| Android minSdk | 24 |
| Android compileSdk | 35 |
| Demo targetSdk（验证用） | 35 |
| Kotlin | 2.0+ |
| JDK | 17 |
| AGP | 8.0+ |

---

## 功能概览

按场景分组；具体 API 以源码 **KDoc** 为准（此处仅作索引）。

### 组件文档

| 分类 | 组件 | 文档 |
|------|------|------|
| 标题栏 | `AwTitleBar` | [文档](aw-ui/doc/AwTitleBar.md) |
| 搜索栏 | `AwSearchView` | [文档](aw-ui/doc/AwSearchView.md) |
| 底部导航 | `AwBottomTabBar` | [文档](aw-ui/doc/AwBottomTabBar.md) |
| 分段控制 | `AwSegmentedControl` | [文档](aw-ui/doc/AwSegmentedControl.md) |
| 标签选择 | `AwTagView` | [文档](aw-ui/doc/AwTagView.md) |
| 流式布局 | `AwFlowLayout` | [文档](aw-ui/doc/AwFlowLayout.md) |
| 轮播图 | `AwBannerView` | [文档](aw-ui/doc/AwBannerView.md) |
| 角标 | `AwBadgeView` | [文档](aw-ui/doc/AwBadgeView.md) |
| 开关按钮 | `AwSwitchButton` | [文档](aw-ui/doc/AwSwitchButton.md) |
| 骨架屏 | `AwSkeletonLayout` / `AwSkeletonView` | [Layout](aw-ui/doc/AwSkeletonLayout.md) · [View](aw-ui/doc/AwSkeletonView.md) |
| 圆形进度 | `AwCircleProgressBar` | [文档](aw-ui/doc/AwCircleProgressBar.md) |
| 加载指示 | `AwLoadingView` | [文档](aw-ui/doc/AwLoadingView.md) |
| 圆角图片 | `AwRoundImageView` | [文档](aw-ui/doc/AwRoundImageView.md) |
| 圆角容器 | `AwRoundLayout` | [文档](aw-ui/doc/AwRoundLayout.md) |
| 展开/收起 | `AwExpandableLayout` | [文档](aw-ui/doc/AwExpandableLayout.md) |
| 跑马灯 | `AwMarqueeTextView` | [文档](aw-ui/doc/AwMarqueeTextView.md) |
| 验证码输入 | `AwCodeInputView` | [文档](aw-ui/doc/AwCodeInputView.md) |
| 倒计时 | `AwCountDownView` | [文档](aw-ui/doc/AwCountDownView.md) |
| 下拉刷新 | `AwSwipeRefreshLayout` | [文档](aw-ui/doc/AwSwipeRefreshLayout.md) |

### 核心模块文档

| 分类 | 文档 |
|------|------|
| 状态页 | [AwStateLayout](aw-ui/doc/AwStateLayout.md) |
| 弹窗 | [Dialog](aw-ui/doc/Dialog.md) |
| 列表 | [RecyclerView](aw-ui/doc/RecyclerView.md) |
| 表单 | [AwFormValidator](aw-ui/doc/AwFormValidator.md) |
| ViewBinding | [ViewBinding](aw-ui/doc/ViewBinding.md) |
| 动效 | [Anim](aw-ui/doc/Anim.md) |

### 其他组件

| 分类 | 组件 | 文档 |
|------|------|------|
| 步骤条 | `AwStepView` | [文档](aw-ui/doc/AwStepView.md) |
| 评分 | `AwRatingBar` | [文档](aw-ui/doc/AwRatingBar.md) |
| 通知条 | `AwNoticeBar` | [文档](aw-ui/doc/AwNoticeBar.md) |
| 侧边索引 | `AwIndexBar` | [文档](aw-ui/doc/AwIndexBar.md) |
| 列表吸顶 | `AwStickyHeaderDecoration` | 见 [RecyclerView](aw-ui/doc/RecyclerView.md) |
| 底栏预设样式 | `Widget.AwBottomTabBar.*` | 见 [AwBottomTabBar](aw-ui/doc/AwBottomTabBar.md) |
| 带清除输入 | `AwClearEditText` | — |
| 密码切换输入 | `AwPasswordEditText` | — |
| 数量加减 | `AwStepper` | — |
| 水平进度条 | `AwHorizontalProgressBar` | — |
| 复选框 | `AwCheckBox` | — |
| 单选按钮组 | `AwRadioButton` / `AwRadioGroup` | — |
| 下拉菜单 | `AwDropDownMenu` | — |
| 滚轮选择器 | `AwPickerView` | — |
| 日期/时间选择 | `AwDatePickerPanel` / `AwTimePickerPanel` | — |
| 底部面板 | `AwBottomSheetDialog` | — |
| 左滑菜单 | `AwSwipeMenuLayout` | — |
| 范围选择 | `AwRangeSeekBar` | — |
| 圆点指示器 | `AwDotIndicator` | — |
| 日历选择 | `AwCalendarView` | — |
| 垂直跑马灯 | `AwVerticalMarqueeView` | — |
| 进度按钮 | `AwProgressButton` | — |
| 九宫格图片 | `AwNineGridImageView` | — |
| 粘性头部 | `AwStickyHeaderLayout` | — |

---

## 骨架屏（Skeleton）

基于 **mask 模式** 的骨架屏方案：包裹真实 content layout，按子 View bounds 自动遮罩并统一 shimmer 动画，API 收敛为 `showSkeleton()` / `showContent()`。底层 shimmer 与 `AwSkeletonView` 共享 `AwSkeletonShimmer` 引擎。

| 场景 | 推荐方式 |
|------|----------|
| 常规业务页、卡片 | `AwSkeletonLayout`（XML 包裹） |
| RecyclerView 列表 | `RecyclerView.applyAwSkeleton` |
| ViewPager2 | `ViewPager2.applyAwSkeleton` |
| 任意 ViewGroup | `ViewGroup.createAwSkeleton` |
| 营销页、精细手拼占位块 | `AwSkeletonView` |

### XML 用法 — AwSkeletonLayout

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

### 代码用法 — createAwSkeleton / applyAwSkeleton

**RecyclerView 列表骨架：**

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

**ViewPager2 骨架：**

```kotlin
viewPager.applyAwSkeleton(R.layout.page_item, itemCount = 3).showSkeleton()
```

**任意 ViewGroup 骨架：**

```kotlin
val skeleton = contentRoot.createAwSkeleton()
skeleton.showSkeleton()
// ...
skeleton.showContent(animate = true)
```

### AwSkeletonConfig 配置

```kotlin
data class AwSkeletonConfig(
    val maskColor: Int,          // 遮罩基础色
    val shimmerColor: Int,       // shimmer 高亮色
    val maskCornerRadiusPx: Float, // 遮罩圆角（px）
    val shimmerDurationMs: Long, // shimmer 周期（毫秒）
    val showShimmer: Boolean,    // 是否启用 shimmer
    val itemCount: Int = 6,      // 列表占位条数
)
```

获取默认配置：

```kotlin
val config = AwSkeletonConfig.default(context)
```

### XML 属性（AwSkeletonLayout）

| 属性 | 类型 | 说明 |
|------|------|------|
| `skeleton_maskColor` | color | 遮罩基础色，默认 `aw_color_skeleton_base` |
| `skeleton_shimmerColor` | color | shimmer 高亮色 |
| `skeleton_maskCornerRadius` | dimension | 遮罩圆角 |
| `skeleton_shimmerDuration` | integer | shimmer 周期（毫秒） |
| `skeleton_showShimmer` | boolean | 是否启用 shimmer |
| `skeleton_autoShow` | boolean | attach 后自动 `showSkeleton()` |

### 子 View 精细控制

| 属性 | 说明 |
|------|------|
| `app:skeleton_mask="true"` | 强制参与遮罩（即使无尺寸） |
| `app:skeleton_ignore="true"` | 排除不参与遮罩 |

### AwStateLayout 集成

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

> 更详细的骨架屏文档见 [AwSkeletonLayout](aw-ui/doc/AwSkeletonLayout.md) · [AwSkeletonView](aw-ui/doc/AwSkeletonView.md)

---

## 常用片段

<details><summary><strong>Dialog / Loading</strong></summary>

```kotlin
AwDialog.Builder(context)
    .title("提示")
    .message("完成")
    .positiveButton("确定")
    .show()

// 或扩展函数
context.showAwConfirm("确认", "确定提交吗？") { submit() }

AwLoadingDialog.show(context, "提交中…")
```

</details>

<details><summary><strong>Banner</strong></summary>

```kotlin
bannerView.setData(items) { container, item, _ -> /* 填充子 View */ }
bannerView.setOnPageClickListener { /* ... */ }
bannerView.startAutoScroll()
```

</details>

<details><summary><strong>StateLayout</strong></summary>

```kotlin
stateLayout.showLoading()
stateLayout.showError { retry() }
```

</details>

<details><summary><strong>SwipeRefresh</strong></summary>

```kotlin
swipeRefresh.refreshListener = {
    loadData {
        swipeRefresh.finishRefresh()
    }
}
```

</details>

<details><summary><strong>ViewBinding / 尺寸</strong></summary>

```kotlin
private val binding by viewBinding(ActivityMainBinding::class)
// onCreate 中 setContentView(R.layout.activity_main) 后使用 binding

val x = 16.dp
```

</details>

<details><summary><strong>SwipeMenu / RangeSeek / DotIndicator</strong></summary>

```kotlin
// 左滑菜单
swipeMenuLayout.onMenuOpenListener = { /* 菜单打开 */ }
swipeMenuLayout.onMenuCloseListener = { /* 菜单关闭 */ }
swipeMenuLayout.openMenu()
swipeMenuLayout.closeMenu()

// 范围选择
rangeSeekBar.setRange(0f, 1000f, step = 50f)
rangeSeekBar.onRangeChangeListener = { left, right -> /* 范围变化 */ }

// 圆点指示器
dotIndicator.bindViewPager2(viewPager2)
```

</details>

<details><summary><strong>Calendar / Marquee / ProgressButton</strong></summary>

```kotlin
// 日历
calendarView.setOnDateSelectedListener { year, month, day -> /* 选中日期 */ }
calendarView.nextMonth()
calendarView.goToToday()

// 垂直跑马灯
marqueeView.items = listOf("公告1", "公告2", "公告3")
marqueeView.start()
marqueeView.setOnItemClickListener { index -> /* 点击 */ }

// 进度按钮
progressButton.progress = 75f
progressButton.isIndeterminate = true
progressButton.setOnProgressCompleteListener { /* 完成 */ }
```

</details>

<details><summary><strong>NineGrid / StickyHeader</strong></summary>

```kotlin
// 九宫格图片
nineGridImageView.imageLoader = { iv, url -> Glide.with(iv).load(url).into(iv) }
nineGridImageView.setImageUrls(urls)
nineGridImageView.setOnImageClickListener { index -> /* 点击 */ }

// 粘性头部
stickyHeaderLayout.onHeaderStickListener = { stuck -> /* 头部粘住/取消 */ }
```

</details>

---

## R8 / ProGuard

`aw-ui` 已通过 `consumer-rules.pro` 随 AAR 下发，**多数应用无需再写**额外 keep（除非你在业务里做了非常规反射/序列化）。

---

## 本仓库与 Demo

| 项 | 说明 |
|----|------|
| 演示能力矩阵 | [demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md) |
| demo 内总览 | 启动 App 主列表 → **「清单」** |
| CI | [`.github/workflows/ci.yml`](.github/workflows/ci.yml)：assemble、ktlint、Lint |
| 本地建议命令 | `./gradlew :aw-ui:assembleRelease :aw-ui:ktlintCheck :demo:ktlintCheck :aw-ui:lintRelease :demo:assembleRelease`（需 **JDK 17**） |

建议在真机过一遍：状态页、列表、对话框与低内存；关键控件在业务侧补齐 **TalkBack** / `contentDescription`。

---

## 常见问题

1. **无法解析 `SwipeRefreshLayout` / 刷新 API**
   库对 `androidx.swiperefreshlayout:swiperefreshlayout` 为 `api` 传递；若宿主 `exclude` 或版本冲突，请显式依赖兼容版本。

2. **ViewBinding 委托不工作**
   确认已在 `onCreate` / `onViewCreated` 中 `setContentView` 或已 inflate，且 Binding 与布局名一致。

3. **Banner 不自动播**
   调用 `startAutoScroll()`，且条数 ≥ 2。

4. **StateLayout 切换无动效**
   内容视图就绪后再 `showXxx()`，并检查 `transition` 配置。

5. **FlowLayout RTL**
   `flow_gravity` 使用 `start` / `end` 等，随系统 RTL 行为。

---

## 许可证

Apache License 2.0，见 [LICENSE](LICENSE)。
