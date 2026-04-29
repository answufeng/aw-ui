# aw-ui Demo 功能矩阵

与库内公开组件对照，用于检查演示是否覆盖核心路径。主页 **「演示清单」** 可速览本表摘要。

## 主入口（MainActivity）

| 入口 | Activity | 建议验证 |
|------|----------|----------|
| 组件总览 | `ShowcaseActivity` | 输入类、进度、Dialog、分割线、Banner 导航等聚合演示 |
| 状态页 | `StateDemoActivity` | `AwStateLayout` 四态与重试；**点按单键循环** `StateTransition`（无 / 淡入 / 交叉 / **自底** `slideFromBottom`） |
| 下拉刷新 + 列表 | `SwipeRefreshListDemoActivity` | `AwSwipeRefreshLayout` + `AwLoadMoreAdapter`，布局跟随下拉、上拉分页 / 无更多、下拉回第一页 |
| 轮播 | `BannerDemoActivity` | `AwBannerView` 自动滚动、指示器、点击 |
| 分段控制 | `SegmentedControlDemoActivity` | `AwSegmentedControl` 文字/图标/下划线/圆点、`bindViewPager2` 与仅回调两种 |

## Showcase 内常见映射（摘录）

| 区域 | 组件 / 能力 | 说明 |
|------|-------------|------|
| 输入 | `AwCodeInputView` | 校验、格式化 |
| 分段 | `AwSegmentedControl` | 滑动与选中 |
| 表单 | `AwFormValidator` | 链式规则 |
| 列表 | `AwSimpleAdapter`、`AwMultiTypeAdapter`、`AwLoadMoreAdapter`、`AwDividerDecoration`、`AwSwipeRefreshLayout` | Diff、Payload、加载更多、下拉刷新 |
| 对话框 | `AwDialog`、`LoadingDialog`、`AwActionSheetDialog` | 与 Material 主题一致 |
| 进度 / 骨架 | `AwCircleProgressBar`、`AwSkeletonView` | 动画与生命周期 |
| 视觉 | `AwBannerView`、`AwRoundImageView`、`AwRoundLayout`、`AwExpandableLayout` | 轮播、圆角、展开 |
| 其他 | `AwBadgeView`、`AwItemAnimator` | 角标、列表动画 |

## 推荐手测（边界与极端场景）

| 场景 | 建议操作 |
|------|----------|
| 下拉刷新 | 快速连续下拉、刷新中途退出 Activity，确认无泄漏且 `isRefreshing` 能关掉 |
| 列表 | 长列表惯性滚动 + 系统「减少动画」开关 |
| 无障碍 | TalkBack 走查 Showcase 主要按钮与状态页 |
| 低内存 | 开发者选项不保留活动 + 反复打开 Banner / 列表页 |

## 质量与发版

- CI：`assembleRelease`、`ktlintCheck`、`lintRelease`、demo R8（见 README）。
- 真机：低内存、TalkBack、系统「减少动画」建议各扫一遍。

更新组件时请同步维护本表与 `ShowcaseActivity` / `MainActivity` 入口。
