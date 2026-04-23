# aw-ui

[![JitPack](https://jitpack.io/v/answufeng/aw-ui.svg)](https://jitpack.io/#answufeng/aw-ui)

**Android 通用 UI 库**（传统 View / XML），覆盖状态页、列表适配、弹窗、表单与输入、动效、标题栏、横幅等日常场景。与组织内其他 `aw-*` 基础库同栈（minSdk 24、JDK 17 工程基线）。

| 项目 | 链接 |
|------|------|
| 演示能力矩阵 | [demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md) |
| demo 内总览 | 启动 App 主列表 → **「演示清单」** |

---

## 环境要求

| 项 | 要求 |
|----|------|
| minSdk | 24+ |
| compileSdk / targetSdk（demo） | 35 |
| Kotlin | 2.0+ |
| AGP | 8.0+ |
| Java | **17** |
| ViewBinding | 需启用（demo 已开） |

**发版前建议本地执行**（与 CI 一致）：

`./gradlew :aw-ui:assembleRelease :aw-ui:ktlintCheck :demo:ktlintCheck :aw-ui:lintRelease :demo:assembleRelease`

CI 见 [`.github/workflows/ci.yml`](.github/workflows/ci.yml)。上线前在真机过一遍状态页、列表、对话框与低内存；关键控件在业务侧补全 **TalkBack** / `contentDescription`。

---

## 引入（JitPack）

`settings.gradle.kts` 中已包含 `maven { url = uri("https://jitpack.io") }` 即可。

`build.gradle.kts`（版本以 tag 为准，**首个正式版 `1.0.0`**）：

```kotlin
dependencies {
    implementation("com.github.answufeng:aw-ui:1.0.0")
}
```

**传递依赖**（`api` 已带出，宿主一般不必重复写）：`appcompat`、`swiperefreshlayout`、`recyclerview`、`viewpager2` 等；若你 `exclude` 了某条，需自行补回兼容版本。详见下节 FAQ。

---

## 模块一览

按场景分组；具体 API 以源码 **KDoc** 为准（此处仅作索引）。

| 分类 | 组件 |
|------|------|
| 状态与布局 | `AwStateLayout`，`AwTitleBar`，`AwFlowLayout`，`AwSearchView`，`AwTagView` |
| 列表与刷新 | `AwSimpleAdapter`，`AwMultiTypeAdapter`，`AwLoadMoreAdapter`，`AwSwipeRefreshLayout`，`AwDividerDecoration`，`AwItemAnimator` |
| 输入与表单 | `AwSmartEditText`，`AwCodeInputView`，`AwPasswordInputView`，`AwFormValidator` |
| 分段与底栏 | `AwSegmentedControl`，`AwBottomTabBar` |
| 弹窗与提示 | `AwDialog`，`LoadingDialog`，`AwActionSheetDialog`，`AwBottomSheet`，`AwTooltipView` |
| 进度与加载 | `AwCircleProgressBar`，`AwSkeletonView`，`AwCountDownView`，`AwLoadingView` 等 |
| 视觉与动效 | `AwBannerView`，`AwRoundImageView`，`AwRoundLayout`，`AwExpandableLayout`，`AwBadgeView`，`Anim` |
| 其他 | `AwMarqueeTextView`，`AwSwitchButton`，`ViewBindingDelegate`，`DimenExt`，`DiffCallbacks` |

---

## 代码片段（节选）

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
stateLayout.transition = StateTransition.CROSS_FADE
```

</details>

<details>
<summary><strong>SwipeRefresh + stopRefreshing</strong></summary>

```kotlin
import com.answufeng.ui.widget.setOnRefreshWithStop
swipeRefresh.setOnRefreshWithStop { stopRefreshing ->
    loadData { stopRefreshing() }
}
```

与 `AwLoadMoreAdapter` 同页时注意与「下拉刷新中刷新首屏」的竞态，见 demo `SwipeRefreshListDemoActivity`。

</details>

<details>
<summary><strong>ViewBinding / 尺寸</strong></summary>

```kotlin
private val binding by viewBinding(ActivityMainBinding::bind)
val x = 16.dp
```

</details>

更多组件（`AwSegmentedControl`、`AwFormValidator`、底栏、圆角图等）请直接查看 **KDoc** 与 **demo 专项页**。

---

## R8 / ProGuard

`aw-ui` 已带 `consumer-rules.pro`；**宿主一般无需再写**自定义 View 相关 keep，除非你在业务里做了非常规反射。

---

## 常见问题

1. **无法解析 `SwipeRefreshLayout` / 刷新 API**  
   库对 `swiperefreshlayout` 为 `api` 传递；若宿主 `exclude` 或版本冲突，请显式依赖兼容的 `androidx.swiperefreshlayout:swiperefreshlayout`。

2. **ViewBinding 委托不工作**  
   确认已在 `onCreate` / `onViewCreated` 中 `setContentView` 或已 inflate，且 Binding 与布局名一致。

3. **Banner 不自动播**  
   调用 `startAutoScroll()`，且条数 ≥ 2。

4. **StateLayout 切换无动效**  
   内容视图已就绪后再 `showXxx()`，并检查 `transition` 配置。

5. **FlowLayout RTL**  
   `flow_gravity` 使用 `start`/`end` 等，随系统 RTL 行为。

更细的联调说明见 [demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md) 与 CI 配置。

---

## 许可证

Apache License 2.0，见 [LICENSE](LICENSE)。
