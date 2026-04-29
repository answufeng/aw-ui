# AwRoundImageView

`AwRoundImageView` 是一个圆角/圆形 `ImageView`，使用 `BitmapShader` 将图片裁剪为圆角矩形或圆形，可选绘制边框。

## 功能概览

- 支持圆角矩形裁剪
- 支持圆形裁剪
- 可选绘制边框（颜色和宽度）
- 自动处理 `BitmapDrawable` 和 `VectorDrawable`
- 图片居中裁剪（Crop Center）

## 文件位置

- 组件实现：[AwRoundImageView.kt](../src/main/java/com/answufeng/ui/widget/AwRoundImageView.kt)
- 自定义属性：[attrs.xml](../src/main/res/values/attrs.xml)

## 快速开始

### XML 中使用

```xml
<com.answufeng.ui.widget.AwRoundImageView
    android:layout_width="80dp"
    android:layout_height="80dp"
    android:src="@mipmap/avatar"
    app:roundImg_radius="12dp"
    app:roundImg_isCircle="false"
    app:roundImg_borderWidth="2dp"
    app:roundImg_borderColor="#FFFFFF" />
```

### 代码中使用

```kotlin
val roundImage = findViewById<AwRoundImageView>(R.id.roundImage)

roundImage.isCircle = true
roundImage.radius = 16f.dp()
roundImage.borderWidth = 2f.dp()
roundImage.borderColor = Color.WHITE
```

## XML 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `roundImg_radius` | `dimension` | `0` | 圆角半径（`isCircle` 为 true 时忽略） |
| `roundImg_isCircle` | `boolean` | `false` | 是否裁剪为圆形 |
| `roundImg_borderWidth` | `dimension` | `0` | 边框宽度 |
| `roundImg_borderColor` | `color` | `#FFFFFF` | 边框颜色 |

## Kotlin API

### 属性设置

```kotlin
roundImage.radius = 12f.dp()          // 圆角半径
roundImage.isCircle = true            // 圆形模式
roundImage.borderWidth = 2f.dp()      // 边框宽度
roundImage.borderColor = Color.WHITE  // 边框颜色
```

## 使用示例

### 圆形头像

```xml
<com.answufeng.ui.widget.AwRoundImageView
    android:layout_width="64dp"
    android:layout_height="64dp"
    android:src="@mipmap/avatar"
    app:roundImg_isCircle="true"
    app:roundImg_borderWidth="2dp"
    app:roundImg_borderColor="#FFFFFF" />
```

### 圆角图片

```xml
<com.answufeng.ui.widget.AwRoundImageView
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:src="@drawable/cover"
    app:roundImg_radius="16dp" />
```

## 注意事项

- `isCircle` 为 true 时忽略 `radius`，裁剪为完美圆形
- 图片使用 Crop Center 方式缩放，确保填满视图
- 边框绘制在图片之上
- 支持 `BitmapDrawable` 和 `VectorDrawable`
- 组件在 `onDetachedFromWindow` 时自动回收临时 Bitmap
