package com.answufeng.ui.demo

object DemoData {

    val categories: List<DemoCategory> by lazy {
        listOf(
            DemoCategory(
                title = "导航与标题",
                desc = "页面导航、标题栏与分段切换等顶部结构组件。",
                icon = "🧭",
                colorHex = "#4F46E5",
                entries = navEntries(),
            ),
            DemoCategory(
                title = "输入与表单",
                desc = "搜索、验证码输入与表单校验等数据录入场景。",
                icon = "✏️",
                colorHex = "#059669",
                entries = formEntries(),
            ),
            DemoCategory(
                title = "输入与选择",
                desc = "带清除/密码切换的输入框、数量加减、下拉选择与复选框/单选按钮组。",
                icon = "✅",
                colorHex = "#0EA5E9",
                entries = inputChoiceEntries(),
            ),
            DemoCategory(
                title = "列表与数据",
                desc = "RecyclerView 适配器、分割线、动画、下拉刷新与吸顶索引。",
                icon = "📋",
                colorHex = "#2563EB",
                entries = listEntries(),
            ),
            DemoCategory(
                title = "布局与状态",
                desc = "流式布局、展开收起、骨架屏与多状态页面容器。",
                icon = "📐",
                colorHex = "#D97706",
                entries = layoutEntries(),
            ),
            DemoCategory(
                title = "弹窗与反馈",
                desc = "对话框、底部面板、通知条、步骤条与评分等用户反馈组件。",
                icon = "💬",
                colorHex = "#7C3AED",
                entries = dialogEntries(),
            ),
            DemoCategory(
                title = "进度与加载",
                desc = "圆形进度、水平进度条、加载动画与倒计时等异步等待场景。",
                icon = "⏳",
                colorHex = "#DC2626",
                entries = progressEntries(),
            ),
            DemoCategory(
                title = "底部面板与滚轮选择",
                desc = "底部弹出面板、滚轮选择器、日期选择与时间选择器。",
                icon = "📅",
                colorHex = "#F43F5E",
                entries = pickerSheetEntries(),
            ),
            DemoCategory(
                title = "视觉与动效",
                desc = "轮播、圆角裁切、角标、开关、跑马灯与 View 动画扩展。",
                icon = "🎨",
                colorHex = "#0891B2",
                entries = visualEntries(),
            ),
        )
    }

    val allEntries: List<DemoEntry> by lazy {
        categories.flatMap { category ->
            category.entries.map { it.copy(category = category.title) }
        }
    }

    val totalComponentCount: Int get() = allEntries.size

    fun getCategory(title: String): DemoCategory? = categories.find { it.title == title }

    fun getEntriesForCategory(title: String): List<DemoEntry> {
        return getCategory(title)?.entries?.map { it.copy(category = title) } ?: emptyList()
    }

    private fun navEntries() = listOf(
        DemoEntry(
            title = "AwTitleBar",
            desc = "主/副标题、返回按钮、右侧操作与沉浸式状态栏适配",
            activity = TitleBarDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwBottomTabBar",
            desc = "底部导航：图标+文字、未读角标、ViewPager 联动",
            activity = AwBottomTabBarDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwSegmentedControl",
            desc = "pill / rect / underline / text_tint 样式与 ViewPager2 联动",
            activity = SegmentedControlDemoActivity::class.java,
        ),
    )

    private fun formEntries() = listOf(
        DemoEntry(
            title = "AwSearchView",
            desc = "实时搜索、提交回调、清除按钮与自定义样式",
            activity = SearchDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwCodeInputView",
            desc = "6 位 / 4 位验证码、自动跳转、粘贴与完成回调",
            activity = CodeInputDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwFormValidator",
            desc = "EditText 规则链 + AwCodeInputView 自定义字段校验",
            activity = FormDemoActivity::class.java,
        ),
    )

    private fun inputChoiceEntries() = listOf(
        DemoEntry(
            title = "输入增强",
            desc = "AwClearEditText 输入清除、AwPasswordEditText 密码可见切换、AwStepper 数量加减、AwDropDownMenu 下拉选择",
            activity = InputEnhanceDemoActivity::class.java,
        ),
        DemoEntry(
            title = "选择控件",
            desc = "AwCheckBox 复选框、AwRadioButton / AwRadioGroup 单选按钮组（竖/横排列）",
            activity = ChoiceDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwRangeSeekBar",
            desc = "双滑块范围选择：价格区间、步长控制、自定义颜色与标签",
            activity = RangeSeekBarDemoActivity::class.java,
        ),
    )

    private fun listEntries() = listOf(
        DemoEntry(
            title = "RecyclerView 工具集",
            desc = "AwSimpleAdapter、AwMultiTypeAdapter、AwDividerDecoration、AwItemAnimator",
            activity = RecyclerViewDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwSwipeRefreshLayout",
            desc = "下拉刷新 + 列表：多种 Header 样式、上拉分页、下拉回第一页",
            activity = SwipeRefreshListDemoActivity::class.java,
        ),
        DemoEntry(
            title = "吸顶 Header + 索引",
            desc = "AwStickyHeaderDecoration 分组吸顶与 AwIndexBar 侧边索引",
            activity = StickyHeaderDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwSwipeMenuLayout",
            desc = "左滑菜单：滑动露出编辑/删除按钮、平滑动画、打开/关闭回调",
            activity = SwipeMenuDemoActivity::class.java,
        ),
    )

    private fun layoutEntries() = listOf(
        DemoEntry(
            title = "AwFlowLayout & AwTagView",
            desc = "流式布局与标签选择：自动换行、单选 / 多选 / 不可选",
            activity = FlowTagDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwExpandableLayout",
            desc = "展开 / 收起布局：平滑高度动画、自定义时长",
            activity = ExpandableDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwSkeletonLayout",
            desc = "自动 mask 骨架屏：Layout / 列表 / StateLayout，含 AwSkeletonView 手拼块",
            activity = SkeletonDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwStateLayout",
            desc = "加载 / 空 / 错 / 内容四态切换、过渡动画与重试",
            activity = StateDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwStickyHeaderLayout",
            desc = "粘性头部容器：嵌套滚动中头部固定在顶部、粘住/取消回调",
            activity = StickyHeaderLayoutDemoActivity::class.java,
        ),
    )

    private fun dialogEntries() = listOf(
        DemoEntry(
            title = "AwSnackbar",
            desc = "Snackbar 替代 Toast：预设样式、Builder 自定义、自动查找锚点",
            activity = SnackbarDemoActivity::class.java,
        ),
        DemoEntry(
            title = "Dialog 总览",
            desc = "AwDialog、AwLoadingDialog、AwActionSheetDialog 与 Context 扩展",
            activity = ShowcaseActivity::class.java,
        ),
        DemoEntry(
            title = "AwNoticeBar",
            desc = "顶部通知条：可关闭、点击回调、动态更新文案",
            activity = NoticeBarDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwStepView",
            desc = "步骤条：多步流程指示、当前步骤切换",
            activity = StepViewDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwRatingBar",
            desc = "星级评分：半星步进、最大星数、评分变化回调",
            activity = RatingBarDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwVerticalMarqueeView",
            desc = "垂直翻页公告：自动轮播、点击回调、动态更新内容",
            activity = VerticalMarqueeDemoActivity::class.java,
        ),
    )

    private fun progressEntries() = listOf(
        DemoEntry(
            title = "AwCircleProgressBar",
            desc = "圆形进度条：进度动画、自定义颜色与描边粗细",
            activity = CircleProgressDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwHorizontalProgressBar",
            desc = "水平进度条：确定/不确定模式、辅助进度、百分比文本、动画过渡",
            activity = ProgressBarDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwLoadingView",
            desc = "加载指示器：circular / horizontal / dots / flower / bars",
            activity = LoadingDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwCountDownView",
            desc = "倒计时：秒数 / 分秒模式、进度环动画、跳过与重置",
            activity = CountDownDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwProgressButton",
            desc = "带进度的按钮：确定/不确定进度模式、进度完成回调",
            activity = ProgressButtonDemoActivity::class.java,
        ),
    )

    private fun pickerSheetEntries() = listOf(
        DemoEntry(
            title = "AwBottomSheetDialog",
            desc = "底部弹出面板：拖拽手柄、自定义内容、Builder 链式构建",
            activity = BottomSheetDemoActivity::class.java,
        ),
        DemoEntry(
            title = "滚轮选择器",
            desc = "AwPickerView 单列滚轮、AwDatePickerPanel 日期选择、AwTimePickerPanel 时间选择",
            activity = PickerDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwCalendarView",
            desc = "日历网格选择：日期选中高亮、月份切换、滑动翻月、今日标记",
            activity = CalendarDemoActivity::class.java,
        ),
    )

    private fun visualEntries() = listOf(
        DemoEntry(
            title = "AwBannerView",
            desc = "轮播图：自动滚动、指示器样式、点击回调",
            activity = BannerDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwRoundImageView",
            desc = "圆形头像、圆角图片、独立圆角、描边与图片裁切",
            activity = RoundImageDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwRoundLayout",
            desc = "圆角裁切容器：对子 View 统一圆角裁切",
            activity = RoundLayoutDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwBadgeView",
            desc = "角标：红点 / 数字 / 文字模式、自动处理可见性",
            activity = BadgeDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwSwitchButton",
            desc = "开关按钮：自定义轨道 / 滑块颜色、状态回调",
            activity = SwitchDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwMarqueeTextView",
            desc = "跑马灯文本：方向、速度、暂停时长与动态更新",
            activity = MarqueeDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwDotIndicator",
            desc = "ViewPager2 圆点指示器：选中动画、自定义颜色与尺寸",
            activity = DotIndicatorDemoActivity::class.java,
        ),
        DemoEntry(
            title = "AwNineGridImageView",
            desc = "九宫格图片：1~9 张自适应布局、超出数量覆盖层、点击回调",
            activity = NineGridDemoActivity::class.java,
        ),
        DemoEntry(
            title = "Anim 扩展",
            desc = "fadeIn / fadeOut / slideInFromBottom / fadeSlideIn 等 View 动画",
            activity = AnimDemoActivity::class.java,
        ),
    )
}
