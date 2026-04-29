# AwCountDownView

`AwCountDownView` 是一个倒计时视图组件，支持两种显示模式：圆形进度环和文字按钮，覆盖验证码倒计时、广告跳过等常见场景。

## 功能概览

- **两种显示模式**：圆形进度环（CIRCLE）和文字按钮（TEXT）
- 圆形模式：轨道 + 进度弧分离绘制，支持多种时间格式
- 文字模式：验证码倒计时场景，倒计时期间自动禁用，结束后恢复可点击
- 两种时间显示模式：秒数、分:秒
- 支持自定义文字格式化
- 倒计时结束后显示自定义文字（如"跳过"、"重新获取"）
- 倒计时结束后可点击触发跳过
- 秒数后缀文字（如 "5s"）
- 支持开始、重置、跳过操作
- 进度、完成、跳过回调
- 可配置颜色、描边宽度、文字大小
- 默认灰色，适合各种场景

## 文件位置

- 组件实现：[AwCountDownView.kt](../src/main/java/com/answufeng/ui/widget/AwCountDownView.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### 验证码倒计时（最常用场景）

```xml
<com.answufeng.ui.widget.AwCountDownView
    android:id="@+id/countDownSms"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:clickable="true"
    app:countDown_displayMode="text"
    app:countDown_initialText="获取验证码"
    app:countDown_finishText="重新获取"
    app:countDown_runningTextFormat="%d%s后重试"
    app:countDown_suffixText="s"
    app:countDown_autoStartOnClick="true"
    app:countDownTextColor="#4F46E5"
    app:countDown_disabledTextColor="#BBBBBB"
    app:countDown_textBorderColor="#4F46E5"
    app:countDown_textCornerRadius="4dp"
    app:countDown_textPaddingH="12dp"
    app:countDown_textPaddingV="6dp"
    app:countDownTextSize="13sp"
    app:countdown_seconds="60" />
```

```kotlin
// 方式一：autoStartOnClick = true，点击控件自动开始倒计时
countDownSms.onStartClick = {
    // 发送验证码请求，返回 true 开始倒计时，返回 false 取消
    sendSmsCode()
    true
}

// 方式二：手动调用 startSeconds
countDownSms.startSeconds(60)  // 显示 "60s后重试" → "59s后重试" → ... → "重新获取"
```

文字模式下，倒计时期间组件自动 `isEnabled = false`，文字和边框变灰，结束后自动恢复。

### 跳过广告（圆形模式）

```xml
<com.answufeng.ui.widget.AwCountDownView
    android:id="@+id/countDownView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:countdown_seconds="5"
    app:countDown_finishText="跳过"
    app:countDownStrokeColor="#FFFFFF"
    app:countDownTextColor="#FFFFFF"
    app:countDownTrackColor="#33FFFFFF"
    app:countDownStrokeWidth="2dp"
    app:countDownTextSize="12sp" />
```

```kotlin
countDownView.startSeconds(5)
countDownView.setCountDownListener(object : AwCountDownView.CountDownListener {
    override fun onFinish() { /* 倒计时完成，显示"跳过"文字 */ }
    override fun onSkip() { /* 用户点击跳过 */ }
})
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `countdown_seconds` | `integer` | `0` | 默认倒计时时长（秒），>0 时作为默认时长 |
| `countDown_displayMode` | `enum` | `circle` | 显示模式：circle（圆形进度环）/ text（文字按钮） |
| `countDownStrokeColor` | `color` | `#999999` | 进度弧颜色 |
| `countDownProgressColor` | `color` | `#999999` | 进度弧颜色（优先于 StrokeColor） |
| `countDownTrackColor` | `color` | `#1A999999` | 轨道圆环颜色 |
| `countDownTextColor` | `color` | `#999999` | 文字颜色 |
| `countDownStrokeWidth` | `dimension` | `3dp` | 圆环描边宽度 |
| `countDownTextSize` | `dimension` | `14sp` | 文字大小 |
| `countDown_finishText` | `string` | `null` | 倒计时结束后显示的文字（如"跳过"） |
| `countDown_suffixText` | `string` | `""` | 秒数后缀文字（如"s"显示为"5s"） |
| `countDown_initialText` | `string` | `""` | 初始状态显示的文字（文字模式，如"获取验证码"） |
| `countDown_runningTextFormat` | `string` | `%d%s` | 倒计时中文字格式（文字模式，如 `%d%s后重试`） |
| `countDown_timeMode` | `enum` | `seconds` | 时间显示模式：seconds/mmss |
| `countDown_autoStartOnClick` | `boolean` | `false` | 点击控件自动开始倒计时（文字模式推荐开启） |
| `countDown_disabledTextColor` | `color` | `#BBBBBB` | 倒计时运行中文字颜色（灰色表示禁用） |
| `countDown_textBorderColor` | `color` | `#999999` | 文字模式边框颜色 |
| `countDown_textBorderWidth` | `dimension` | `1dp` | 文字模式边框宽度 |
| `countDown_textBgColor` | `color` | `transparent` | 文字模式背景填充色 |
| `countDown_textCornerRadius` | `dimension` | `4dp` | 文字模式圆角半径 |
| `countDown_textPaddingH` | `dimension` | `12dp` | 文字模式水平内边距 |
| `countDown_textPaddingV` | `dimension` | `4dp` | 文字模式垂直内边距 |

## 显示模式

### CIRCLE（圆形进度环）

默认模式，显示圆形进度环 + 中间文字。适用于：
- 跳过广告
- 倒计时展示
- 加载等待

### TEXT（文字按钮）

文字按钮模式，显示带边框的文字。适用于：
- 验证码倒计时（最常见）
- 重新发送

文字模式的状态流转：
1. **初始状态**：显示 `initialText`（如"获取验证码"），可点击
2. **倒计时中**：显示格式化文字（如"60s后重试"），文字和边框变灰，自动禁用
3. **倒计时结束**：显示 `finishText`（如"重新获取"），恢复原色，自动恢复可点击

开启 `autoStartOnClick` 后，点击控件即可自动开始倒计时，通过 `onStartClick` 回调可以拦截（如先发送验证码请求）。

## 时间显示模式

| 模式 | 枚举值 | 说明 |
|---|---|---|
| 秒数 | `SECONDS` | 只显示剩余秒数（进位），可加后缀 |
| 分:秒 | `MM_SS` | 显示为 `01:40` 格式 |

## Kotlin API

### 倒计时控制

```kotlin
countDown.start(durationMs = 3000, maxMs = 3000)  // 毫秒单位开始
countDown.startSeconds(10)                          // 秒单位开始
countDown.reset()                                   // 重置
countDown.skip()                                    // 跳过（触发 onSkip）
countDown.skip(invokeListener = false)              // 跳过（不触发回调）
```

### 属性设置

```kotlin
countDown.setProgressColor(Color.BLUE)         // 进度弧颜色
countDown.setTrackColor(Color.LTGRAY)          // 轨道颜色
countDown.setTextColor(Color.BLUE)             // 文字颜色
countDown.setStrokeWidth(8.dp())               // 描边宽度
countDown.setTextSize(20.sp())                 // 文字大小
countDown.finishText = "跳过"                   // 完成后显示的文字
countDown.suffixText = "s"                     // 秒数后缀
countDown.initialText = "获取验证码"            // 初始文字（文字模式）
countDown.runningTextFormat = "%d%s后重试"      // 倒计时中格式（文字模式）
countDown.displayMode = AwCountDownView.DisplayMode.TEXT  // 切换显示模式
countDown.isClickableWhenFinished = true       // 完成后是否可点击跳过
```

### 文字模式专属属性

```kotlin
countDown.textBorderColor = Color.GRAY         // 边框颜色
countDown.textBorderWidth = 1.dp()             // 边框宽度
countDown.textBgColor = Color.TRANSPARENT      // 背景填充色
countDown.textCornerRadius = 4.dp()            // 圆角半径
countDown.textPaddingH = 12.dp()               // 水平内边距
countDown.textPaddingV = 4.dp()                // 垂直内边距
countDown.disabledTextColor = Color.parseColor("#BBBBBB")  // 倒计时中文字颜色
countDown.autoStartOnClick = true              // 点击自动开始倒计时
countDown.onStartClick = {                     // 点击回调，返回 true 开始倒计时
    sendSmsCode()
    true
}
```

### 自定义文字格式化

```kotlin
countDown.timeTextFormatter = { remainingMs ->
    val sec = remainingMs / 1000
    "${sec}s 剩余"
}
```

### 监听器

```kotlin
countDown.setCountDownListener(object : AwCountDownView.CountDownListener {
    override fun onFinish() { }
    override fun onSkip() { }
    override fun onProgress(progress: Int, remainingMs: Long) { }
})
```

## 注意事项

- `startSeconds` 最小值为 1
- 组件在 `onDetachedFromWindow` 时自动停止倒计时
- `timeTextFormatter` 优先级高于 `timeDisplayMode`
- 进度百分比范围为 0-100
- `finishText` 设置后，倒计时结束会显示该文字，否则不显示文字
- 倒计时结束后点击组件会触发 `skip()`（需 `isClickableWhenFinished = true`）
- 轨道和进度弧使用独立画笔，轨道为浅色完整圆，进度弧为深色扇形
- 默认颜色为灰色 `#999999`，轨道默认为 `#1A999999`（10% 透明度灰色）
- 文字模式下倒计时期间组件自动 `isEnabled = false`，文字和边框变灰，结束后恢复
- `autoStartOnClick` 开启后，点击控件会先调用 `onStartClick` 回调，回调返回 `true` 才开始倒计时
- `disabledTextColor` 仅在文字模式倒计时运行中生效，用于表示禁用状态
