# AwTagView

`AwTagView` 是一个标签选择视图，继承自 `AwFlowLayout`，支持单选/多选/不可选模式，自动管理标签视图的创建和选中状态。

## 功能概览

- 三种选择模式：不可选、单选、多选
- 自动创建标签视图，支持自定义样式
- 多选模式下支持最大选择数量
- 选中状态变化回调
- 标签点击回调
- 支持状态保存与恢复
- 可配置文字颜色、背景色、圆角等

## 文件位置

- 组件实现：[AwTagView.kt](../src/main/java/com/answufeng/ui/widget/AwTagView.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwTagView
    android:id="@+id/tagView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:tag_selectionMode="single"
    app:tag_textColor="#333333"
    app:tag_selectedTextColor="#FFFFFF"
    app:tag_bgColor="#F0F0F0"
    app:tag_selectedBgColor="#4F46E5"
    app:tag_cornerRadius="4dp" />
```

### 代码中使用

```kotlin
val tagView = findViewById<AwTagView>(R.id.tagView)

tagView.tags = listOf("Kotlin", "Java", "Python", "Go")
tagView.onSelectionChange = { selectedTags ->
    // 处理选中变化
}
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `tag_textColor` | `color` | `Color.BLACK` | 未选中文字颜色 |
| `tag_selectedTextColor` | `color` | `#FFFFFF` | 选中文字颜色 |
| `tag_bgColor` | `color` | `#F0F0F0` | 未选中背景色 |
| `tag_selectedBgColor` | `color` | `Color.BLUE` | 选中背景色 |
| `tag_textSize` | `dimension` | `14sp` | 文字大小 |
| `tag_paddingH` | `dimension` | `12dp` | 水平内边距 |
| `tag_paddingV` | `dimension` | `6dp` | 垂直内边距 |
| `tag_cornerRadius` | `dimension` | `4dp` | 圆角半径 |
| `tag_selectionMode` | `enum` | `single` | 选择模式：none/single/multi |
| `tag_maxSelectCount` | `integer` | `∞` | 多选模式下最大选择数量 |

## 选择模式

| 模式 | 枚举值 | 说明 |
|---|---|---|
| 不可选 | `NONE` | 标签不可选中，仅展示 |
| 单选 | `SINGLE` | 只能选中一个标签 |
| 多选 | `MULTI` | 可选中多个标签 |

## Kotlin API

### 数据设置

```kotlin
tagView.tags = listOf("标签1", "标签2", "标签3")
```

### 选中控制

```kotlin
tagView.setTagSelected("标签1", true)  // 选中指定标签
tagView.clearSelection()                // 清除所有选中
tagView.selectedTags                    // 当前选中的标签集合
```

### 模式设置

```kotlin
tagView.selectionMode = AwTagView.SelectionMode.SINGLE
tagView.maxSelectCount = 3              // 多选时最多选3个
```

### 回调

```kotlin
tagView.onTagClick = { tag, isSelected -> }
tagView.onSelectionChange = { selectedTags -> }
```

## 使用示例

### 单选标签

```kotlin
tagView.selectionMode = AwTagView.SelectionMode.SINGLE
tagView.tags = listOf("全部", "最新", "热门", "推荐")
tagView.onSelectionChange = { selected ->
    val category = selected.firstOrNull() ?: "全部"
    viewModel.filterByCategory(category)
}
```

### 多选标签（限制数量）

```kotlin
tagView.selectionMode = AwTagView.SelectionMode.MULTI
tagView.maxSelectCount = 3
tagView.tags = listOf("科技", "体育", "娱乐", "财经", "健康")
tagView.onSelectionChange = { selected ->
    viewModel.filterByTags(selected)
}
```

### 仅展示标签

```kotlin
tagView.selectionMode = AwTagView.SelectionMode.NONE
tagView.tags = listOf("已售罄", "新品", "限时")
```

## 注意事项

- 继承自 `AwFlowLayout`，自动换行
- 默认使用 Material 主题色
- 单选模式下再次点击已选中标签会取消选中
- 多选模式下达到 `maxSelectCount` 后不再选中新标签
- 修改 `selectionMode` 为 `NONE` 时会自动清除所有选中
