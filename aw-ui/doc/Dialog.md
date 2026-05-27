# Dialog 模块

包含 `AwDialog`、`AwLoadingDialog`、`AwActionSheetDialog`。

## AwDialog

```kotlin
AwDialog.Builder(context)
    .title("提示")
    .message("确定删除吗？")
    .positiveButton("确定") { delete() }
    .negativeButton("取消")
    .show()

// 快捷
AwDialog.showMessage(context, "提示", "操作成功")
AwDialog.showConfirm(context, "确认", "确定退出吗？") { finish() }

// 扩展函数
context.showAwMessage("提示", "操作成功")
context.showAwConfirm("确认", "确定退出吗？") { finish() }
```

## AwLoadingDialog

```kotlin
val dialog = AwLoadingDialog.show(context, "加载中…")
// 完成后
dialog.dismiss()
```

## AwActionSheetDialog

底部操作表，支持破坏性选项与取消按钮。参见 `ShowcaseActivity`。

## Demo

`ShowcaseActivity`
