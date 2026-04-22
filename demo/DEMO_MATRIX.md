# aw-ui Demo 功能矩阵

与库内公开组件对照，用于检查演示是否覆盖核心路径。主页 **「演示清单」** 可速览本表摘要。

## 主入口（MainActivity）

| 入口 | Activity | 建议验证 |
|------|----------|----------|
| 组件总览 | `ShowcaseActivity` | 输入类、进度、Dialog、分割线、Banner 导航等聚合演示 |
| 状态页 | `StateDemoActivity` | `AwStateLayout` 内容/加载/空/错与重试 |
| 轮播 | `BannerDemoActivity` | `AwBannerView` 自动滚动、指示器、点击 |

## Showcase 内常见映射（摘录）

| 区域 | 组件 / 能力 | 说明 |
|------|-------------|------|
| 输入 | `AwSmartEditText`、`AwCodeInputView`、`AwPasswordInputView` | 校验、格式化、强度 |
| 分段 | `AwSegmentedControl` | 滑动与选中 |
| 表单 | `AwFormValidator` | 链式规则 |
| 列表 | `AwSimpleAdapter`、`AwMultiTypeAdapter`、`AwLoadMoreAdapter`、`AwDividerDecoration` | Diff、Payload、加载更多 |
| 对话框 | `AwDialog`、`LoadingDialog`、`AwActionSheetDialog`、`AwBottomSheet` | 与 Material 主题一致 |
| 进度 / 骨架 | `AwCircleProgressBar`、`AwSkeletonView` | 动画与生命周期 |
| 视觉 | `AwBannerView`、`AwRoundImageView`、`AwRoundLayout`、`AwExpandableLayout` | 轮播、圆角、展开 |
| 其他 | `AwBadgeView`、`AwTooltipView`、`AwItemAnimator` | 角标、气泡、列表动画 |

## 质量与发版

- CI：`assembleRelease`、`ktlintCheck`、`lintRelease`、demo R8（见 README）。
- 真机：低内存、TalkBack、系统「减少动画」建议各扫一遍。

更新组件时请同步维护本表与 `ShowcaseActivity` / `MainActivity` 入口。
