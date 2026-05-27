# AwStepView

水平步骤指示器，适用于注册、下单等多步流程。

```xml
<com.answufeng.ui.widget.AwStepView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:step_count="4"
    app:step_current="1" />
```

```kotlin
stepView.stepCount = 4
stepView.currentStep = 2
stepView.labelTexts = listOf("填写", "确认", "支付", "完成")
```
