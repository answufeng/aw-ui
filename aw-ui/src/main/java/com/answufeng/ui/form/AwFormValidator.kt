package com.answufeng.ui.form

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView

/** 校验规则接口 */
interface Rule {
    val errorMsg: String

    fun validate(text: String): Boolean
}

/**
 * 链式表单校验器。
 *
 * - [addField]：适用于 [TextView] / [EditText]
 * - [addCustomField]：自定义取值（如 [com.answufeng.ui.widget.AwCodeInputView]）
 */
class AwFormValidator {
    private data class FieldEntry(
        val view: View,
        val rules: List<Rule>,
    )

    private data class CustomFieldEntry(
        val view: View,
        val getter: () -> String,
        val rules: List<Rule>,
        val onError: (View, String?) -> Unit,
    )

    private val fields = mutableListOf<FieldEntry>()
    private val customFields = mutableListOf<CustomFieldEntry>()

    private val errors = mutableMapOf<View, String>()

    private val realtimeWatchers = mutableMapOf<View, TextWatcher>()

    private var validityListener: ((Boolean) -> Unit)? = null

    /**
     * 注册 [TextView] / [EditText] 字段及规则。
     * 非 TextView 请使用 [addCustomField]。
     */
    fun addField(
        view: View,
        vararg rules: Rule,
    ): AwFormValidator {
        if (rules.isEmpty()) return this
        require(view is TextView) {
            "addField 仅支持 TextView/EditText，自定义控件请使用 addCustomField"
        }
        fields.add(FieldEntry(view, rules.toList()))
        return this
    }

    /**
     * 注册自定义控件字段。
     *
     * @param getter 读取待校验文本
     * @param onError 展示/清除错误，默认对 [EditText] 设置 [EditText.error]
     */
    fun addCustomField(
        view: View,
        getter: () -> String,
        vararg rules: Rule,
        onError: ((View, String?) -> Unit)? = null,
    ): AwFormValidator {
        if (rules.isEmpty()) return this
        val displayError =
            onError ?: { v, msg ->
                if (v is EditText) {
                    v.error = msg
                }
            }
        customFields.add(CustomFieldEntry(view, getter, rules.toList(), displayError))
        return this
    }

    fun validate(): Boolean {
        errors.clear()
        var allValid = true

        for (entry in fields) {
            val view = entry.view
            val text = (view as TextView).text.toString()
            val failedRule = entry.rules.firstOrNull { !it.validate(text) }
            if (failedRule != null) {
                allValid = false
                errors[view] = failedRule.errorMsg
                if (view is EditText) {
                    view.error = failedRule.errorMsg
                }
            } else {
                if (view is EditText) {
                    view.error = null
                }
            }
        }

        for (entry in customFields) {
            val text = entry.getter()
            val failedRule = entry.rules.firstOrNull { !it.validate(text) }
            if (failedRule != null) {
                allValid = false
                errors[entry.view] = failedRule.errorMsg
                entry.onError(entry.view, failedRule.errorMsg)
            } else {
                entry.onError(entry.view, null)
            }
        }

        validityListener?.invoke(allValid)
        return allValid
    }

    /**
     * 验证单个字段。
     */
    fun validate(view: View): Boolean {
        val entry = fields.find { it.view === view }
        if (entry != null) {
            val text = (view as TextView).text.toString()
            val failedRule = entry.rules.firstOrNull { !it.validate(text) }
            if (failedRule != null) {
                errors[view] = failedRule.errorMsg
                if (view is EditText) {
                    view.error = failedRule.errorMsg
                }
                return false
            } else {
                errors.remove(view)
                if (view is EditText) {
                    view.error = null
                }
                return true
            }
        }
        val customEntry = customFields.find { it.view === view }
        if (customEntry != null) {
            val text = customEntry.getter()
            val failedRule = customEntry.rules.firstOrNull { !it.validate(text) }
            if (failedRule != null) {
                errors[view] = failedRule.errorMsg
                customEntry.onError(view, failedRule.errorMsg)
                return false
            } else {
                errors.remove(view)
                customEntry.onError(view, null)
                return true
            }
        }
        return true
    }

    fun getErrors(): Map<View, String> = errors.toMap()

    fun clearErrors() {
        errors.clear()
        for (entry in fields) {
            val view = entry.view
            if (view is EditText) {
                view.error = null
            }
        }
        for (entry in customFields) {
            entry.onError(entry.view, null)
        }
    }

    /**
     * 移除已注册的字段。
     */
    fun removeField(view: View): AwFormValidator {
        fields.removeAll { it.view === view }
        customFields.removeAll { it.view === view }
        errors.remove(view)
        realtimeWatchers.remove(view)?.let { watcher ->
            if (view is EditText) {
                view.removeTextChangedListener(watcher)
            }
        }
        return this
    }

    /**
     * 为 [EditText] 字段添加实时校验，用户输入时自动显示/清除错误。
     *
     * @param view  已通过 [addField] 注册的 EditText
     * @param delay 输入后延迟校验的毫秒数，默认 500
     */
    fun addRealtimeValidation(
        view: View,
        delay: Long = 500L,
    ): AwFormValidator {
        if (view !is EditText) return this
        // 移除旧的 watcher
        realtimeWatchers.remove(view)?.let { view.removeTextChangedListener(it) }
        val watcher = object : TextWatcher {
            private val runnable = Runnable { validate(view) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                view.removeCallbacks(runnable)
                view.postDelayed(runnable, delay)
            }
        }
        view.addTextChangedListener(watcher)
        realtimeWatchers[view] = watcher
        return this
    }

    fun setOnValidityChange(listener: (Boolean) -> Unit): AwFormValidator {
        validityListener = listener
        return this
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun required(errorMsg: String = "此字段不能为空"): Rule =
            object : Rule {
                override val errorMsg: String = errorMsg

                override fun validate(text: String): Boolean = text.isNotBlank()
            }

        @JvmStatic
        @JvmOverloads
        fun minLength(
            min: Int,
            errorMsg: String? = null,
        ): Rule =
            object : Rule {
                override val errorMsg: String = errorMsg ?: "长度不能少于${min}个字符"

                override fun validate(text: String): Boolean = text.length >= min
            }

        @JvmStatic
        @JvmOverloads
        fun maxLength(
            max: Int,
            errorMsg: String? = null,
        ): Rule =
            object : Rule {
                override val errorMsg: String = errorMsg ?: "长度不能超过${max}个字符"

                override fun validate(text: String): Boolean = text.length <= max
            }

        @JvmStatic
        @JvmOverloads
        fun email(errorMsg: String = "邮箱格式不正确"): Rule =
            object : Rule {
                override val errorMsg: String = errorMsg

                override fun validate(text: String): Boolean {
                    if (text.isBlank()) return true
                    return android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()
                }
            }

        @JvmStatic
        @JvmOverloads
        fun phone(errorMsg: String = "手机号格式不正确"): Rule =
            object : Rule {
                override val errorMsg: String = errorMsg

                override fun validate(text: String): Boolean {
                    if (text.isBlank()) return true
                    return text.matches(Regex("^1[3-9]\\d{9}$"))
                }
            }

        @JvmStatic
        fun pattern(
            regex: String,
            errorMsg: String,
        ): Rule =
            object : Rule {
                override val errorMsg: String = errorMsg

                override fun validate(text: String): Boolean = text.matches(Regex(regex))
            }

        @JvmStatic
        fun custom(
            predicate: (String) -> Boolean,
            errorMsg: String,
        ): Rule =
            object : Rule {
                override val errorMsg: String = errorMsg

                override fun validate(text: String): Boolean = predicate(text)
            }
    }
}
