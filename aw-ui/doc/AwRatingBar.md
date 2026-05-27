# AwRatingBar

星级评分，支持点击设置、半星步进（`rating_stepSize=0.5`）、只读模式。

```xml
<com.answufeng.ui.widget.AwRatingBar
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:rating_maxStars="5"
    app:rating_value="3.5"
    app:rating_stepSize="0.5" />
```

```kotlin
ratingBar.onRatingChange = { score -> /* ... */ }
```
