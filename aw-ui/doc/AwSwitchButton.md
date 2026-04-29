# AwSwitchButton

`AwSwitchButton` 是一个带滑块动画的自定义开关按钮，支持自定义颜色、阴影和状态保存。

## 功能概览

- 圆角轨道 + 圆形滑块，滑块与轨道之间有合理间距
- 选中/未选中状态之间平滑过渡动画
- 轨道和滑块颜色均支持渐变过渡
- 滑块带阴影效果，增强立体感
- 支持状态保存与恢复
- 无障碍支持

## 文件位置

- 组件实现：[AwSwitchButton.kt](../src/main/java/com/answufeng/ui/widget/AwSwitchButton.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwSwitchButton
    android:layout_width="52dp"
    android:layout_height="28dp"
    app:switch_checked="false"
    app:switch_trackColor="#CCCCCC"
    app:switch_trackCheckedColor="#4CAF50"
    app:switch_thumbColor="#FFFFFF"
    app:switch_thumbCheckedColor="#FFFFFF" />
```

### 代码中使用

```kotlin
switchButton.isChecked = true
switchButton.onCheckedChangeListener = { checked ->
    // 处理状态变化
}
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `switch_checked` | `boolean` | `false` | 初始选中状态 |
| `switch_trackColor` | `color` | `#CCCCCC` | 未选中时轨道颜色 |
| `switch_trackCheckedColor` | `color` | `#4CAF50` | 选中时轨道颜色 |
| `switch_thumbColor` | `color` | `#FFFFFF` | 未选中时滑块颜色 |
| `switch_thumbCheckedColor` | `color` | `#FFFFFF` | 选中时滑块颜色 |
| `switch_thumbShadowEnabled` | `boolean` | `true` | 是否显示滑块阴影 |

## Kotlin API

```kotlin
switchButton.isChecked = true                       // 设置选中状态
switchButton.trackColor = Color.GRAY                // 未选中轨道颜色
switchButton.trackCheckedColor = Color.GREEN        // 选中轨道颜色
switchButton.thumbColor = Color.WHITE               // 未选中滑块颜色
switchButton.thumbCheckedColor = Color.WHITE        // 选中滑块颜色
switchButton.thumbShadowEnabled = true              // 是否显示滑块阴影
switchButton.onCheckedChangeListener = { checked -> }  // 状态变化回调
```

## 注意事项

- 推荐尺寸为 52dp × 28dp
- 滑块与轨道之间有合理间距，不会贴边
- 滑块默认带轻微阴影，可通过 `thumbShadowEnabled = false` 关闭
- 状态变化时滑块有 250ms 的加速减速动画
- 组件自动保存和恢复选中状态
