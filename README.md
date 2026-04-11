# aw-ui

Android 通用 UI 组件库，提供 StateLayout、TitleBar、RecyclerView 适配器、对话框、动画和自定义布局。

## 引入

```kotlin
dependencies {
    implementation("com.github.answufeng:aw-ui:1.0.0")
}
```

## 功能特性

- StateLayout：4 状态容器（内容/加载中/空/错误），支持懒加载和动画
- TitleBar：通用标题栏，支持沉浸式
- SimpleAdapter / MultiTypeAdapter：RecyclerView 适配器，内置 DiffUtil
- BrickDialog / LoadingDialog：对话框工具
- BrickAnim：视图动画扩展（fadeIn、fadeOut、slideIn、slideOut）
- RoundLayout / FlowLayout / BadgeView：自定义布局
- DividerDecoration：RecyclerView 分割线

## 使用示例

```kotlin
// StateLayout
stateLayout.showLoading()
stateLayout.showContent()
stateLayout.showEmpty()
stateLayout.showError { retryLoad() }

// SimpleAdapter
val adapter = SimpleAdapter(ItemBinding::inflate,
    diffCallback = object : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(old: Item, new: Item) = old.id == new.id
        override fun areContentsTheSame(old: Item, new: Item) = old == new
    }
) { binding, item, _ -> binding.tvName.text = item.name }

// 动画
view.fadeIn()
view.fadeOut()
view.slideInFromBottom()
```

## 许可证

Apache License 2.0，详见 [LICENSE](LICENSE)。
