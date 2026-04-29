package com.answufeng.ui.demo

object DemoData {

    val categories: List<DemoCategory> by lazy {
        listOf(
            DemoCategory(
                title = "导航与标题",
                icon = "🧭",
                colorHex = "#1976D2",
                entries = listOf(
                    DemoEntry(
                        title = "AwTitleBar",
                        desc = "标题栏：主/副标题、返回按钮、右侧操作、沉浸式适配",
                        activity = TitleBarDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "AwBottomTabBar",
                        desc = "底部导航栏：图标+文字、未读角标、ViewPager 联动",
                        activity = AwBottomTabBarDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "AwSegmentedControl",
                        desc = "分段控制：pill/rect/underline/text_tint 样式、ViewPager2 联动",
                        activity = SegmentedControlDemoActivity::class.java
                    )
                )
            ),
            DemoCategory(
                title = "输入与表单",
                icon = "✏️",
                colorHex = "#388E3C",
                entries = listOf(
                    DemoEntry(
                        title = "AwSearchView",
                        desc = "搜索栏：实时搜索、提交回调、清除按钮、自定义样式",
                        activity = SearchDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "AwCodeInputView",
                        desc = "验证码输入：6位/4位、自动跳转、粘贴、完成回调",
                        activity = CodeInputDemoActivity::class.java
                    )
                )
            ),
            DemoCategory(
                title = "布局与容器",
                icon = "📐",
                colorHex = "#F57C00",
                entries = listOf(
                    DemoEntry(
                        title = "AwFlowLayout & AwTagView",
                        desc = "流式布局与标签选择：自动换行、单选/多选/不可选",
                        activity = FlowTagDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "AwExpandableLayout",
                        desc = "展开/收起布局：平滑动画、自定义时长、默认展开",
                        activity = ExpandableDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "AwSkeletonView",
                        desc = "骨架屏：列表项/卡片占位、闪光动画、自定义颜色和圆角",
                        activity = SkeletonDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "AwStateLayout",
                        desc = "状态页：加载/空/错/内容四态切换、过渡动画、重试",
                        activity = StateDemoActivity::class.java
                    )
                )
            ),
            DemoCategory(
                title = "弹窗与提示",
                icon = "💬",
                colorHex = "#7B1FA2",
                entries = listOf(
                    DemoEntry(
                        title = "Dialog 总览",
                        desc = "AwDialog / LoadingDialog / AwActionSheetDialog 综合演示",
                        activity = ShowcaseActivity::class.java
                    )
                )
            ),
            DemoCategory(
                title = "进度与加载",
                icon = "⏳",
                colorHex = "#C62828",
                entries = listOf(
                    DemoEntry(
                        title = "AwCircleProgressBar",
                        desc = "圆形进度条：进度动画、自定义颜色和粗细",
                        activity = CircleProgressDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "AwLoadingView",
                        desc = "加载指示器：圆形/水平条/点跳动/花朵旋转/条形波动",
                        activity = LoadingDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "AwCountDownView",
                        desc = "倒计时视图：秒数/分秒模式、进度环动画、跳过、重置",
                        activity = CountDownDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "AwSwipeRefreshLayout",
                        desc = "下拉刷新 + 列表：上拉分页、下拉回第一页",
                        activity = SwipeRefreshListDemoActivity::class.java
                    )
                )
            ),
            DemoCategory(
                title = "视觉与动效",
                icon = "🎨",
                colorHex = "#00838F",
                entries = listOf(
                    DemoEntry(
                        title = "AwBannerView",
                        desc = "轮播图：自动滚动、指示器、点击回调",
                        activity = BannerDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "圆角与裁切",
                        desc = "AwRoundImageView + AwRoundLayout：圆形头像、圆角图片、独立圆角、描边、图片裁切",
                        activity = RoundImageDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "AwBadgeView",
                        desc = "角标：红点/数字/文字模式、自动处理可见性",
                        activity = BadgeDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "AwSwitchButton",
                        desc = "开关按钮：自定义轨道/滑块颜色、默认选中、状态回调",
                        activity = SwitchDemoActivity::class.java
                    ),
                    DemoEntry(
                        title = "AwMarqueeTextView",
                        desc = "跑马灯文本：左/右方向、速度控制、暂停时长、动态更新",
                        activity = MarqueeDemoActivity::class.java
                    )
                )
            )
        )
    }

    val allEntries: List<DemoEntry> by lazy {
        categories.flatMap { it.entries }
    }

    fun getEntriesForCategory(title: String): List<DemoEntry> {
        return categories.find { it.title == title }?.entries ?: emptyList()
    }
}
