# AwFormValidator

链式表单校验，内置 required、minLength、email、phone、pattern、custom 等规则。

## TextView / EditText

```kotlin
val validator = AwFormValidator()
    .addField(etEmail, AwFormValidator.required(), AwFormValidator.email())
    .addField(etPhone, AwFormValidator.phone())

if (validator.validate()) {
    submit()
}
```

## 自定义控件（如 AwCodeInputView）

```kotlin
validator.addCustomField(codeInput, getter = { codeInput.code }) {
    AwFormValidator.required("请输入验证码"),
    AwFormValidator.minLength(6, "请输入 6 位验证码"),
}
```

## 注意

- `addField` 仅支持 `TextView` / `EditText`
- 其它控件请使用 `addCustomField`

## Demo

`ShowcaseActivity` 表单区域
