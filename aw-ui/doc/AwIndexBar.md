# AwIndexBar

侧边字母索引条，适用于联系人、城市列表等，可与 [AwStickyHeaderDecoration](../src/main/java/com/answufeng/ui/recyclerview/AwStickyHeaderDecoration.kt) 联动。

```xml
<com.answufeng.ui.widget.AwIndexBar
    android:layout_width="wrap_content"
    android:layout_height="match_parent"
    android:layout_gravity="end|center_vertical" />
```

```kotlin
indexBar.onLetterSelected = { letter, _ ->
    val position = sectionFirstPositions[letter] ?: return@onLetterSelected
    layoutManager.scrollToPositionWithOffset(position, 0)
}
```

## Demo

`StickyHeaderDemoActivity`（索引 + 吸顶 Header）
