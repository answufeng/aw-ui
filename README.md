# aw-ui

[![JitPack](https://jitpack.io/v/answufeng/aw-ui.svg)](https://jitpack.io/#answufeng/aw-ui)

基于 **Android View / XML** 的通用 UI 组件库，覆盖状态页、列表适配、弹窗、表单与输入、动效、标题栏、横幅等日常场景。

如果你只想最快跑起来，直接看下面的「5 分钟上手」即可；其它内容都可以后置按需查阅。

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
    implementation("com.github.answufeng:aw-ui:1.0.3")
}
```

`implementation` 中的 **版本号与 Git / JitPack 的 tag 一致**（上例为 `1.0.3`）。

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
| 骨架屏 | `AwSkeletonView` | [文档](aw-ui/doc/AwSkeletonView.md) |
| 圆形进度 | `AwCircleProgressBar` | [文档](aw-ui/doc/AwCircleProgressBar.md) |
| 加载指示 | `AwLoadingView` | [文档](aw-ui/doc/AwLoadingView.md) |
| 圆角图片 | `AwRoundImageView` | [文档](aw-ui/doc/AwRoundImageView.md) |
| 圆角容器 | `AwRoundLayout` | [文档](aw-ui/doc/AwRoundLayout.md) |
| 展开/收起 | `AwExpandableLayout` | [文档](aw-ui/doc/AwExpandableLayout.md) |
| 跑马灯 | `AwMarqueeTextView` | [文档](aw-ui/doc/AwMarqueeTextView.md) |
| 验证码输入 | `AwCodeInputView` | [文档](aw-ui/doc/AwCodeInputView.md) |
| 倒计时 | `AwCountDownView` | [文档](aw-ui/doc/AwCountDownView.md) |
| 下拉刷新 | `AwSwipeRefreshLayout` | [文档](aw-ui/doc/AwSwipeRefreshLayout.md) |

### 其他模块

| 分类 | 组件 |
|------|------|
| 状态与布局 | `AwStateLayout` |
| 列表与刷新 | `AwSimpleAdapter`、`AwMultiTypeAdapter`、`AwLoadMoreAdapter`、`AwDividerDecoration`、`AwItemAnimator` |
| 弹窗与提示 | `AwDialog`、`LoadingDialog`、`AwActionSheetDialog` |
| 工具 | `AwFormValidator`、`ViewBindingDelegate`、`DimenExt`、`DiffCallbacks`、`Anim` |

---

## 常用片段

<details>
<summary><strong>Dialog / Loading</strong></summary>

```kotlin
AwDialog.Builder(context).title("提示").message("完成").positiveButton("确定").show()
LoadingDialog.show(context, "提交中…")
```

</details>

<details>
<summary><strong>Banner</strong></summary>

```kotlin
bannerView.setData(items) { container, item, _ -> /* 填充子 View */ }
bannerView.setOnPageClickListener { /* ... */ }
bannerView.startAutoScroll()
```

</details>

<details>
<summary><strong>StateLayout</strong></summary>

```kotlin
stateLayout.showLoading()
stateLayout.showError { retry() }
```

</details>

<details>
<summary><strong>SwipeRefresh</strong></summary>

```kotlin
swipeRefresh.refreshListener = {
    loadData {
        swipeRefresh.finishRefresh()
    }
}
```

</details>

<details>
<summary><strong>ViewBinding / 尺寸</strong></summary>

```kotlin
private val binding by viewBinding(ActivityMainBinding::bind)
val x = 16.dp
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
| demo 内总览 | 启动 App 主列表 → **「演示清单」** |
| CI | [`.github/workflows/ci.yml`](.github/workflows/ci.yml)：assemble、ktlint、Lint |
| 本地建议命令 | `./gradlew :aw-ui:assembleRelease :aw-ui:ktlintCheck :demo:ktlintCheck :aw-ui:lintRelease :demo:assembleRelease`（需 **JDK 17**） |

上线前建议在真机过一遍：状态页、列表、对话框与低内存；关键控件在业务侧补齐 **TalkBack** / `contentDescription`。

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
