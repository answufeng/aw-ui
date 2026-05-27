# AwNoticeBar

顶部通知/公告条，可关闭、可点击整栏。

```xml
<com.answufeng.ui.widget.AwNoticeBar
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:notice_text="系统将于今晚维护"
    app:notice_closable="true" />
```

```kotlin
noticeBar.showMessage("新版本已发布")
noticeBar.onBarClick = { openDetail() }
noticeBar.onCloseClick = { /* 用户关闭 */ }
```
