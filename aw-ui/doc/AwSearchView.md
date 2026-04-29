# AwSearchView

`AwSearchView` 是一个搜索栏视图，包含搜索图标、输入框和清除按钮，支持实时搜索和提交回调。

## 功能概览

- 内置搜索图标、输入框和清除按钮
- 支持实时搜索回调（`onQueryChange`）
- 支持提交搜索回调（`onQuerySubmit`）
- 支持清除按钮回调（`onClearClick`）
- 支持焦点变化回调（`onSearchFocusChange`）
- 可配置背景色、图标色、文字色、圆角半径、高度
- 支持状态保存与恢复

## 文件位置

- 组件实现：[AwSearchView.kt](../src/main/java/com/answufeng/ui/widget/AwSearchView.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwSearchView
    android:id="@+id/searchView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:search_hint="搜索..."
    app:search_bgColor="#F5F5F5"
    app:search_iconColor="#999999"
    app:search_textColor="#333333"
    app:search_hintTextColor="#999999" />
```

### 代码中使用

```kotlin
val searchView = findViewById<AwSearchView>(R.id.searchView)

searchView.onQueryChange = { query -> filterList(query) }
searchView.onQuerySubmit = { query -> doSearch(query) }
searchView.onClearClick = { clearResults() }
searchView.onSearchFocusChange = { hasFocus -> handleFocus(hasFocus) }
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `search_hint` | `string` | `"搜索"` | 提示文本 |
| `search_bgColor` | `color` | `#F5F5F5` | 背景颜色 |
| `search_iconColor` | `color` | `Color.GRAY` | 图标颜色 |
| `search_textColor` | `color` | `Color.BLACK` | 输入文字颜色 |
| `search_hintTextColor` | `color` | `Color.GRAY` | 提示文字颜色 |

## Kotlin API

### 属性设置

```kotlin
searchView.hint = "输入关键词"                    // 提示文本
searchView.query = "初始值"                       // 设置搜索内容
searchView.searchBackgroundColor = Color.WHITE    // 背景色
searchView.searchIconColor = Color.GRAY           // 图标色
searchView.searchTextColor = Color.BLACK          // 文字色
searchView.searchHintColor = Color.LTGRAY         // 提示色
searchView.searchCornerRadius = 20f * density     // 圆角半径
searchView.searchHeight = (40 * density).toInt()  // 搜索栏高度
```

### 回调

```kotlin
searchView.onQueryChange = { query -> }         // 文本变化
searchView.onQuerySubmit = { query -> }         // 提交搜索
searchView.onClearClick = { }                   // 清除按钮
searchView.onSearchFocusChange = { hasFocus -> } // 焦点变化
```

### 焦点控制

```kotlin
searchView.requestSearchFocus()  // 请求焦点
searchView.clearFocus()          // 清除焦点
```

## 使用示例

### 配合列表筛选

```kotlin
searchView.onQueryChange = { query ->
    adapter.filter(query)
}
searchView.onQuerySubmit = { query ->
    viewModel.search(query)
}
```

### 自定义样式

```xml
<com.answufeng.ui.widget.AwSearchView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:search_hint="搜索商品"
    app:search_bgColor="#EEEEEE"
    app:search_iconColor="#888888" />
```

### 动态修改样式

```kotlin
searchView.hint = "请输入商品名称"
searchView.searchBackgroundColor = Color.parseColor("#FFF3E0")
searchView.searchIconColor = Color.parseColor("#FF9800")
```

## 注意事项

- 清除按钮仅在输入内容非空时显示
- 提交搜索通过键盘的搜索按钮触发
- 默认高度为 40dp，可通过 `searchHeight` 修改
- 背景使用 `GradientDrawable` 实现圆角效果
