# Anim 扩展

`com.answufeng.ui.anim` 包提供 View 常用动画扩展：

```kotlin
view.fadeIn(duration = 300L)
view.fadeOut { view.visibility = View.GONE }
view.slideInFromBottom()
view.slideOutToBottom()
view.fadeSlideIn()
view.shake()
view.pulse()
view.bounce()
```

日常场景优先使用 `fadeIn` / `fadeOut` / `slideInFromBottom`；`shake` / `pulse` / `bounce` 适合轻量交互反馈。
