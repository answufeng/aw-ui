package com.answufeng.ui.statelayout

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import com.answufeng.ui.R
import com.answufeng.ui.widget.skeleton.AwSkeleton
import com.answufeng.ui.widget.skeleton.AwSkeletonConfig
import com.answufeng.ui.widget.skeleton.createAwSkeleton

/**
 * 状态切换过渡动画策略。
 *
 * 通过 [transition] 方法自定义视图显示时的动画效果。
 * 可使用伴生对象中提供的预设动画，或通过 Lambda 创建自定义动画。
 *
 * ### 预设动画
 * - [NONE]    — 无动画
 * - [FADE]    — 淡入（alpha 0 → 1）
 * - [CROSS_FADE] — 交叉淡入（alpha + 轻微缩放）
 * - [slideFromBottom] — 从底部滑入
 *
 * ### 自定义动画
 * ```kotlin
 * StateTransition { view, duration ->
 *     view.alpha = 0f
 *     view.animate()
 *         .alpha(1f)
 *         .setDuration(duration)
 *         .start()
 * }
 * ```
 */
fun interface StateTransition {
    /**
     * 对即将显示的视图执行过渡动画。
     *
     * @param view     即将显示的视图
     * @param duration 动画时长（毫秒）
     */
    fun transition(
        view: View,
        duration: Long,
    )

    companion object {
        /** 无动画 */
        @JvmField
        val NONE = StateTransition { _, _ -> }

        /** 淡入动画（alpha 0 → 1） */
        @JvmField
        val FADE =
            StateTransition { view, duration ->
                view.alpha = 0f
                view.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .start()
            }

        /** 交叉淡入动画（alpha 0 → 1 + 轻微缩放 0.92 → 1） */
        @JvmField
        val CROSS_FADE =
            StateTransition { view, duration ->
                view.alpha = 0f
                view.scaleX = 0.92f
                view.scaleY = 0.92f
                view.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duration)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }

        /**
         * 从底部滑入动画（translationY + alpha）。
         *
         * 若切换瞬间目标视图尚未完成布局（高度为 0），会等首次 [android.view.View.layout] 再开启动画；仍无高度时以约 96dp 为位移，避免无位移。
         *
         * @return 从底部滑入的过渡动画实例
         */
        @JvmStatic
        fun slideFromBottom(): StateTransition =
            StateTransition { view, duration ->
                view.alpha = 0f
                val startSlide: () -> Unit = {
                    val h = view.height
                    val offsetY =
                        if (h > 0) {
                            h.toFloat()
                        } else {
                            96f * view.resources.displayMetrics.density
                        }
                    view.translationY = offsetY
                    view.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(duration)
                        .start()
                }
                if (view.height > 0) {
                    startSlide()
                } else {
                    view.doOnLayout { startSlide() }
                }
            }
    }
}

/**
 * 四态页面容器：**内容 / 加载中 / 空数据 / 错误**。
 *
 * XML 中第一个子 View 自动被识别为「内容视图」，其余三种状态视图
 * 通过 XML 属性或代码指定（默认使用内置布局）。
 *
 * ### XML 用法
 * ```xml
 * <com.answufeng.ui.statelayout.AwStateLayout
 *     android:id="@+id/stateLayout"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:loadingLayout="@layout/custom_loading"
 *     app:emptyLayout="@layout/custom_empty"
 *     app:enableAnimation="true"
 *     app:animationDuration="200">
 *
 *     <!-- 第一个子 View 被用作内容视图 -->
 *     <RecyclerView ... />
 * </com.answufeng.ui.statelayout.AwStateLayout>
 * ```
 *
 * ### 代码切换状态
 * ```kotlin
 * stateLayout.showLoading()
 * // 数据加载成功
 * stateLayout.showContent()
 * // 或空数据
 * stateLayout.showEmpty()
 * // 或失败（带重试）
 * stateLayout.showError { loadData() }
 * ```
 *
 * ### 切换动画
 * 默认启用 200ms 淡入淡出动画，可通过 XML 属性或代码控制：
 * ```kotlin
 * stateLayout.enableAnimation = false       // 关闭动画
 * stateLayout.animationDuration = 300L      // 自定义时长
 * stateLayout.transition = StateTransition.CROSS_FADE  // 交叉淡入
 * stateLayout.transition = StateTransition.slideFromBottom() // 从底部滑入
 * ```
 *
 * 状态视图使用懒加载策略——首次切换到某状态时才 inflate 对应布局。
 */
class AwStateLayout
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        /** 当前页面状态 */
        var currentState: State = State.CONTENT
            private set

        /** 支持的四种页面状态 + 自定义状态 */
        enum class State { CONTENT, LOADING, EMPTY, ERROR, CUSTOM }

        /** Loading 态展示方式 */
        enum class LoadingStyle {
            /** 默认转圈 loading 页 */
            SPINNER,

            /** 对 content 子 View 应用骨架遮罩，不隐藏 content 结构 */
            SKELETON,
        }

        /** 状态变更监听器 */
        fun interface OnStateChangeListener {
            /**
             * 状态发生变更时回调。
             *
             * @param oldState 变更前的状态
             * @param newState 变更后的状态
             */
            fun onStateChanged(
                oldState: State,
                newState: State,
            )
        }

        private var contentView: View? = null
        private var loadingView: View? = null
        private var emptyView: View? = null
        private var errorView: View? = null
        private var stateChangeListener: OnStateChangeListener? = null

        @LayoutRes private var loadingLayoutRes: Int = R.layout.aw_state_loading

        @LayoutRes private var emptyLayoutRes: Int = R.layout.aw_state_empty

        @LayoutRes private var errorLayoutRes: Int = R.layout.aw_state_error

        private var onRetryListener: (() -> Unit)? = null

        /** 是否启用状态切换的淡入淡出动画，默认 true */
        var enableAnimation: Boolean = true

        /** 动画时长（毫秒），默认 200ms */
        var animationDuration: Long = 200L

        /** 状态切换过渡动画策略，默认 [StateTransition.FADE] */
        var transition: StateTransition = StateTransition.FADE

        /** Loading 展示样式，默认 [LoadingStyle.SPINNER] */
        var loadingStyle: LoadingStyle = LoadingStyle.SPINNER

        /** skeleton 模式下使用的配置 */
        var skeletonConfig: AwSkeletonConfig = AwSkeletonConfig.default(context)

        private var contentSkeleton: AwSkeleton? = null

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwStateLayout)
            loadingLayoutRes = ta.getResourceId(R.styleable.AwStateLayout_loadingLayout, R.layout.aw_state_loading)
            emptyLayoutRes = ta.getResourceId(R.styleable.AwStateLayout_emptyLayout, R.layout.aw_state_empty)
            errorLayoutRes = ta.getResourceId(R.styleable.AwStateLayout_errorLayout, R.layout.aw_state_error)
            enableAnimation = ta.getBoolean(R.styleable.AwStateLayout_enableAnimation, true)
            animationDuration = ta.getInt(R.styleable.AwStateLayout_animationDuration, 200).toLong()
            val styleIndex = ta.getInt(R.styleable.AwStateLayout_state_loadingStyle, 0)
            loadingStyle = LoadingStyle.entries.getOrElse(styleIndex) { LoadingStyle.SPINNER }
            val density = resources.displayMetrics.density
            skeletonConfig =
                AwSkeletonConfig(
                    maskColor =
                        ta.getColor(
                            R.styleable.AwStateLayout_skeleton_maskColor,
                            ContextCompat.getColor(context, R.color.aw_color_skeleton_base),
                        ),
                    shimmerColor =
                        ta.getColor(
                            R.styleable.AwStateLayout_skeleton_shimmerColor,
                            ContextCompat.getColor(context, R.color.aw_color_skeleton_highlight),
                        ),
                    maskCornerRadiusPx =
                        ta.getDimension(
                            R.styleable.AwStateLayout_skeleton_maskCornerRadius,
                            4f * density,
                        ),
                    shimmerDurationMs =
                        ta.getInteger(R.styleable.AwStateLayout_skeleton_shimmerDuration, 1500).toLong(),
                    showShimmer = ta.getBoolean(R.styleable.AwStateLayout_skeleton_showShimmer, true),
                )
            ta.recycle()
        }

        override fun onFinishInflate() {
            super.onFinishInflate()
            if (childCount > 0) {
                contentView = getChildAt(0)
            }
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable("superState", super.onSaveInstanceState())
                putString("state", currentState.name)
            }
        }

        override fun onRestoreInstanceState(state: Parcelable?) {
            if (state is Bundle) {
                val superState: Parcelable? =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        state.getParcelable("superState", Parcelable::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        state.getParcelable("superState")
                    }
                super.onRestoreInstanceState(superState)
                val savedName = state.getString("state") ?: State.CONTENT.name
                val restoredState = try { State.valueOf(savedName) } catch (_: Exception) { State.CONTENT }
                if (restoredState != currentState) {
                    switchState(restoredState)
                }
            } else {
                super.onRestoreInstanceState(state)
            }
        }

        /** 切换到内容状态，显示第一个子 View */
        fun showContent(force: Boolean = false) {
            switchState(State.CONTENT, force)
        }

        /** 切换到加载中状态 */
        fun showLoading(force: Boolean = false) {
            switchState(State.LOADING, force)
        }

        /** 切换到空数据状态 */
        fun showEmpty(force: Boolean = false) {
            switchState(State.EMPTY, force)
        }

        /**
         * 切换到错误状态。
         *
         * 错误布局中 id 为 `btnRetry` 的 View 会自动绑定 [onRetry] 回调。
         *
         * @param onRetry 点击重试按钮的回调，null 表示不处理
         */
        fun showError(
            onRetry: (() -> Unit)? = null,
            force: Boolean = false,
        ) {
            this.onRetryListener = onRetry
            switchState(State.ERROR, force)
        }

        // ==================== 自定义状态视图 ====================

        /** 通过布局资源 ID 设置自定义加载中视图（会在下次 showLoading 时重新 inflate） */
        fun setLoadingView(
            @LayoutRes layoutRes: Int,
        ) {
            loadingLayoutRes = layoutRes
            loadingView?.let { removeView(it) }
            loadingView = null
        }

        /** 通过布局资源 ID 设置自定义空数据视图 */
        fun setEmptyView(
            @LayoutRes layoutRes: Int,
        ) {
            emptyLayoutRes = layoutRes
            emptyView?.let { removeView(it) }
            emptyView = null
        }

        /** 通过布局资源 ID 设置自定义错误视图 */
        fun setErrorView(
            @LayoutRes layoutRes: Int,
        ) {
            errorLayoutRes = layoutRes
            errorView?.let { removeView(it) }
            errorView = null
        }

        /** 直接设置加载中视图实例 */
        fun setLoadingView(view: View) {
            loadingView?.let { removeView(it) }
            loadingView = view
        }

        /** 直接设置空数据视图实例 */
        fun setEmptyView(view: View) {
            emptyView?.let { removeView(it) }
            emptyView = view
        }

        /** 直接设置错误视图实例 */
        fun setErrorView(view: View) {
            errorView?.let { removeView(it) }
            errorView = view
        }

        private val customStates = mutableMapOf<String, View>()

        fun registerState(
            stateName: String,
            viewProvider: (Context) -> View,
        ) {
            if (!customStates.containsKey(stateName)) {
                customStates[stateName] = viewProvider(context)
            }
        }

        fun showCustomState(stateName: String) {
            val view = customStates[stateName] ?: return
            hideAllStateViews()
            if (view.parent == null) addView(view)
            view.visibility = VISIBLE
            if (enableAnimation) transition.transition(view, animationDuration)
            val oldState = currentState
            currentState = State.CUSTOM
            stateChangeListener?.onStateChanged(oldState, State.CUSTOM)
        }

        private fun hideAllStateViews() {
            contentView?.visibility = GONE
            loadingView?.visibility = GONE
            emptyView?.visibility = GONE
            errorView?.visibility = GONE
            for (view in customStates.values) {
                view.visibility = GONE
            }
        }

        /** 直接设置内容视图实例 */
        fun setContentView(view: View) {
            contentView?.let { removeView(it) }
            contentView = view
            if (currentState == State.CONTENT) {
                if (view.parent == null) addView(view)
                view.visibility = VISIBLE
            }
        }

        /** 通过布局资源设置内容视图 */
        fun setContentView(@LayoutRes layoutRes: Int) {
            setContentView(inflate(context, layoutRes, this))
        }

        /**
         * 设置状态变更监听器。
         *
         * ```kotlin
         * stateLayout.setOnStateChangeListener { oldState, newState ->
         *     Log.d("AwStateLayout", "$oldState -> $newState")
         * }
         * ```
         *
         * @param listener 状态变更回调，null 表示移除监听
         */
        fun setOnStateChangeListener(listener: OnStateChangeListener?) {
            stateChangeListener = listener
        }

        private fun switchState(
            state: State,
            force: Boolean = false,
        ) {
            if (!force && currentState == state) return
            val oldState = currentState
            currentState = state
            if (state != State.CUSTOM) {
                for (view in customStates.values) {
                    view.visibility = GONE
                }
            }
            stateChangeListener?.onStateChanged(oldState, state)
            announceStateForAccessibility(state)

            if (loadingStyle == LoadingStyle.SKELETON) {
                applySkeletonLoadingState(state, oldState)
            } else {
                dismissContentSkeleton()
                showOrHide(contentView, state == State.CONTENT)
                showOrHide(state, State.LOADING) { ensureLoadingView() }
                showOrHide(state, State.EMPTY) { ensureEmptyView() }
                showOrHide(state, State.ERROR) { ensureErrorView() }
            }
        }

        private fun applySkeletonLoadingState(
            state: State,
            oldState: State,
        ) {
            loadingView?.visibility = GONE
            when (state) {
                State.LOADING -> {
                    showOrHide(contentView, true)
                    emptyView?.visibility = GONE
                    errorView?.visibility = GONE
                    ensureContentSkeleton()?.showSkeleton()
                }
                State.CONTENT -> {
                    ensureContentSkeleton()?.showContent(enableAnimation)
                    showOrHide(contentView, true)
                    emptyView?.visibility = GONE
                    errorView?.visibility = GONE
                }
                State.EMPTY -> {
                    dismissContentSkeleton()
                    showOrHide(contentView, false)
                    showOrHide(state, State.EMPTY) { ensureEmptyView() }
                }
                State.ERROR -> {
                    dismissContentSkeleton()
                    showOrHide(contentView, false)
                    showOrHide(state, State.ERROR) { ensureErrorView() }
                }
                State.CUSTOM -> {
                    dismissContentSkeleton()
                }
            }
        }

        private fun ensureContentSkeleton(): AwSkeleton? {
            val content = contentView as? ViewGroup ?: return null
            if (contentSkeleton == null) {
                contentSkeleton = content.createAwSkeleton(skeletonConfig)
            }
            return contentSkeleton
        }

        private fun dismissContentSkeleton() {
            if (contentSkeleton?.isShowingSkeleton == true) {
                contentSkeleton?.showContent(false)
            }
        }

        private fun showOrHide(
            view: View?,
            show: Boolean,
        ) {
            view ?: return
            if (show) {
                if (view.visibility != VISIBLE) {
                    view.visibility = VISIBLE
                    if (enableAnimation) transition.transition(view, animationDuration)
                }
            } else {
                view.visibility = GONE
            }
        }

        private inline fun showOrHide(
            current: State,
            target: State,
            create: () -> View?,
        ) {
            if (current == target) {
                val view = create() ?: return
                val isNewlyAdded = view.parent == null
                if (isNewlyAdded) addView(view)
                if (isNewlyAdded || view.visibility != VISIBLE) {
                    view.visibility = VISIBLE
                    if (enableAnimation) transition.transition(view, animationDuration)
                }
            } else {
                when (target) {
                    State.LOADING -> loadingView
                    State.EMPTY -> emptyView
                    State.ERROR -> errorView
                    else -> null
                }?.visibility = GONE
            }
        }

        private fun announceStateForAccessibility(state: State) {
            val message =
                when (state) {
                    State.CONTENT -> context.getString(R.string.aw_state_content)
                    State.LOADING -> context.getString(R.string.aw_state_loading)
                    State.EMPTY -> context.getString(R.string.aw_state_empty)
                    State.ERROR -> context.getString(R.string.aw_state_error)
                    State.CUSTOM -> context.getString(R.string.aw_state_custom)
                }
            announceForAccessibility(message)
        }

        private fun ensureLoadingView(): View? {
            if (loadingView == null) {
                loadingView = LayoutInflater.from(context).inflate(loadingLayoutRes, this, false)
            }
            return loadingView
        }

        private fun ensureEmptyView(): View? {
            if (emptyView == null) {
                emptyView = LayoutInflater.from(context).inflate(emptyLayoutRes, this, false)
            }
            return emptyView
        }

        private fun ensureErrorView(): View? {
            if (errorView == null) {
                errorView = LayoutInflater.from(context).inflate(errorLayoutRes, this, false)
                errorView?.findViewById<View>(R.id.btnRetry)?.setOnClickListener {
                    onRetryListener?.invoke()
                }
            }
            return errorView
        }
    }
