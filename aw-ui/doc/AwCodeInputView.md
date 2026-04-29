# AwCodeInputView

`AwCodeInputView` 是一个验证码输入视图，每位数字显示在独立的方框中，支持自动跳转、粘贴和完成回调。

## 功能概览

- 通过 `codeLength` 配置验证码位数（默认 6）
- 输入后自动跳转到下一个方框
- 删除后自动回退到上一个方框
- 支持粘贴完整验证码
- 聚焦方框高亮显示
- 所有数字输入完成回调
- 支持状态保存与恢复

## 文件位置

- 组件实现：[AwCodeInputView.kt](../src/main/java/com/answufeng/ui/widget/AwCodeInputView.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwCodeInputView
    android:id="@+id/codeInput"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:code_length="6"
    app:code_boxSize="48dp"
    app:code_boxSpacing="8dp"
    app:code_boxStrokeColor="#CCCCCC"
    app:code_boxStrokeWidth="2dp"
    app:code_textColor="#000000"
    app:code_textSize="18sp" />
```

### 代码中使用

```kotlin
val codeInput = findViewById<AwCodeInputView>(R.id.codeInput)

codeInput.onCodeComplete = { code ->
    verifyCode(code)
}

codeInput.codeLength = 4
val entered = codeInput.code
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `code_length` | `integer` | `6` | 验证码位数 |
| `code_boxSize` | `dimension` | `48dp` | 每个方框尺寸 |
| `code_boxSpacing` | `dimension` | `8dp` | 方框间距 |
| `code_boxStrokeColor` | `color` | `#CCCCCC` | 方框边框颜色 |
| `code_boxStrokeWidth` | `dimension` | `2dp` | 方框边框宽度 |
| `code_textColor` | `color` | `#000000` | 数字文字颜色 |
| `code_textSize` | `dimension` | `18sp` | 数字文字大小 |

## Kotlin API

### 属性设置

```kotlin
codeInput.codeLength = 4           // 设置位数（会重建方框）
codeInput.boxSize = 56.dp()        // 设置方框尺寸
codeInput.boxSpacing = 12.dp()     // 设置方框间距
codeInput.boxStrokeColor = Color.BLUE  // 设置边框颜色
codeInput.boxStrokeWidth = 3f.dp()     // 设置边框宽度
codeInput.codeTextColor = Color.BLACK  // 设置文字颜色
codeInput.codeTextSize = 20f.sp()      // 设置文字大小
```

### 输入控制

```kotlin
codeInput.code = "123456"          // 程序化填充
codeInput.onCodeComplete = { code -> verifyCode(code) }  // 完成回调
```

### 输入类型

```kotlin
codeInput.codeInputType = InputType.TYPE_CLASS_NUMBER  // 仅数字（默认）
codeInput.codeInputType = InputType.TYPE_CLASS_TEXT    // 允许字母
```

## 使用示例

### 4位验证码

```xml
<com.answufeng.ui.widget.AwCodeInputView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="center_horizontal"
    app:code_length="4"
    app:code_boxSize="56dp"
    app:code_boxStrokeColor="#4F46E5" />
```

### 带完成回调

```kotlin
codeInput.onCodeComplete = { code ->
    viewModel.verifyCode(code)
}
```

## 注意事项

- 修改 `codeLength` 会重建所有方框视图
- 粘贴超过位数的文本会被截断
- 输入完成后自动隐藏软键盘
- 聚焦方框会显示半透明高亮背景
