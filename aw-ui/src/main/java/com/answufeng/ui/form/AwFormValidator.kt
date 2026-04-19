package com.answufeng.ui.form

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.answufeng.ui.widget.AwSmartEditText

/**
 * 验证规则接口，检查字段文本输入的单个约束条件。
 */
interface Rule {

    /** 验证失败时显示的错误信息 */
    val errorMsg: String

    /**
     * 验证给定文本是否符合此规则。
     *
     * @param text 要验证的输入文本
     * @return 文本通过验证返回 `true`，否则返回 `false`
     */
    fun validate(text: String): Boolean
}

/**
 * 统一的表单验证框架，支持链式调用 API。
 *
 * 支持验证任何 [TextView]（包括 [EditText]）和 [AwSmartEditText]。
 * 对于普通 [TextView]，提取文本并在此验证器中评估规则。
 * 对于 [AwSmartEditText]，验证委托给其自身的 [AwSmartEditText.validate] 方法，
 * 通过 [addField] 添加的规则会注册为该视图的命名验证器。
 *
 * ### 用法
 * ```kotlin
 * val validator = AwFormValidator()
 *     .addField(usernameInput, AwFormValidator.required(), AwFormValidator.minLength(3))
 *     .addField(emailInput, AwFormValidator.required(), AwFormValidator.email())
 *     .addField(phoneInput, AwFormValidator.phone())
 *
 * if (validator.validate()) {
 *     submitForm()
 * } else {
 *     validator.getErrors().values.forEach { println(it) }
 * }
 * ```
 */
class AwFormValidator {

    private data class FieldEntry(
        val view: View,
        val rules: List<Rule>
    )

    private val fields = mutableListOf<FieldEntry>()

    private val errors = mutableMapOf<View, String>()

    private var validityListener: ((Boolean) -> Unit)? = null

    /**
     * 添加一个字段及其验证规则。
     *
     * 如果视图是 [AwSmartEditText]，规则会注册为该视图的命名验证器，
     * 验证委托给 [AwSmartEditText.validate]。
     * 对于其他 [TextView] 子类（如 [EditText]），提取文本并由此验证器评估规则。
     *
     * @param view  要验证的视图
     * @param rules 应用于此字段的一个或多个 [Rule] 实例
     * @return 当前验证器实例，用于链式调用
     */
    fun addField(view: View, vararg rules: Rule): AwFormValidator {
        if (rules.isEmpty()) return this

        if (view is AwSmartEditText) {
            rules.forEachIndexed { index, rule ->
                view.addValidator(
                    name = "aw_form_rule_$index",
                    predicate = { input -> rule.validate(input) },
                    errorMsg = rule.errorMsg
                )
            }
        }

        fields.add(FieldEntry(view, rules.toList()))
        return this
    }

    /**
     * 验证所有已注册字段，全部通过则返回 `true`。
     *
     * 对于 [EditText] 字段，第一个失败规则的错误信息通过 [EditText.setError] 设置。
     * 对于 [AwSmartEditText] 字段，验证委托给 [AwSmartEditText.validate]。
     *
     * @return 所有字段通过验证返回 `true`，否则返回 `false`
     */
    fun validate(): Boolean {
        errors.clear()
        var allValid = true

        for (entry in fields) {
            val view = entry.view

            if (view is AwSmartEditText) {
                val result = view.validate()
                if (!result) {
                    allValid = false
                    errors[view] = findFirstErrorMessage(view, entry.rules)
                }
            } else if (view is TextView) {
                val text = view.text.toString()
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
        }

        validityListener?.invoke(allValid)
        return allValid
    }

    /**
     * 返回所有验证失败字段及其对应错误信息的映射。
     * 此映射在调用 [validate] 后填充。
     *
     * @return 映射，键为视图，值为错误信息
     */
    fun getErrors(): Map<View, String> = errors.toMap()

    /**
     * 清除所有验证错误。
     *
     * 对于 [EditText] 字段，通过 [EditText.setError] 移除错误。
     * 对于 [AwSmartEditText] 字段，清除显示的错误信息。
     */
    fun clearErrors() {
        errors.clear()
        for (entry in fields) {
            val view = entry.view
            if (view is EditText) {
                view.error = null
            }
            if (view is AwSmartEditText) {
                view.clearValidators()
            }
        }
    }

    /**
     * 注册回调，在每次调用 [validate] 时报告所有字段是否有效。
     *
     * @param listener 接收验证结果的 lambda，全部通过为 `true`，否则为 `false`
     * @return 当前验证器实例，用于链式调用
     */
    fun setOnValidityChange(listener: (Boolean) -> Unit): AwFormValidator {
        validityListener = listener
        return this
    }

    private fun findFirstErrorMessage(view: AwSmartEditText, rules: List<Rule>): String {
        val text = view.text.toString()
        return rules.firstOrNull { !it.validate(text) }?.errorMsg ?: ""
    }

    companion object {

        /**
         * 创建要求字段非空的规则。
         *
         * @param errorMsg 字段为空时的错误信息
         * @return 拒绝空白输入的 [Rule]
         */
        @JvmStatic
        @JvmOverloads
        fun required(errorMsg: String = "此字段不能为空"): Rule = object : Rule {
            override val errorMsg: String = errorMsg
            override fun validate(text: String): Boolean = text.isNotBlank()
        }

        /**
         * 创建要求字段文本长度至少为 [min] 的规则。
         *
         * @param min      最小允许长度
         * @param errorMsg 文本过短时的错误信息，默认 "长度不能少于{min}个字符"
         * @return 拒绝短于 [min] 个字符输入的 [Rule]
         */
        @JvmStatic
        @JvmOverloads
        fun minLength(min: Int, errorMsg: String? = null): Rule = object : Rule {
            override val errorMsg: String = errorMsg ?: "长度不能少于${min}个字符"
            override fun validate(text: String): Boolean = text.length >= min
        }

        /**
         * 创建要求字段文本长度最多为 [max] 的规则。
         *
         * @param max      最大允许长度
         * @param errorMsg 文本过长时的错误信息，默认 "长度不能超过{max}个字符"
         * @return 拒绝超过 [max] 个字符输入的 [Rule]
         */
        @JvmStatic
        @JvmOverloads
        fun maxLength(max: Int, errorMsg: String? = null): Rule = object : Rule {
            override val errorMsg: String = errorMsg ?: "长度不能超过${max}个字符"
            override fun validate(text: String): Boolean = text.length <= max
        }

        /**
         * 创建验证邮箱地址格式的规则。
         *
         * @param errorMsg 邮箱格式无效时的错误信息
         * @return 拒绝不匹配标准邮箱格式输入的 [Rule]
         */
        @JvmStatic
        @JvmOverloads
        fun email(errorMsg: String = "邮箱格式不正确"): Rule = object : Rule {
            override val errorMsg: String = errorMsg
            override fun validate(text: String): Boolean {
                if (text.isBlank()) return true
                return android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()
            }
        }

        /**
         * 创建验证中国大陆手机号（11 位）的规则。
         *
         * @param errorMsg 手机号格式无效时的错误信息
         * @return 拒绝不匹配手机号格式输入的 [Rule]
         */
        @JvmStatic
        @JvmOverloads
        fun phone(errorMsg: String = "手机号格式不正确"): Rule = object : Rule {
            override val errorMsg: String = errorMsg
            override fun validate(text: String): Boolean {
                if (text.isBlank()) return true
                return text.matches(Regex("^1[3-9]\\d{9}$"))
            }
        }

        /**
         * 创建基于正则表达式的验证规则。
         *
         * @param regex    要匹配的正则表达式模式
         * @param errorMsg 不匹配时的错误信息
         * @return 拒绝不匹配 [regex] 输入的 [Rule]
         */
        @JvmStatic
        fun pattern(regex: String, errorMsg: String): Rule = object : Rule {
            override val errorMsg: String = errorMsg
            override fun validate(text: String): Boolean = text.matches(Regex(regex))
        }

        /**
         * 创建自定义验证谓词的规则。
         *
         * @param predicate 输入有效时返回 `true` 的函数
         * @param errorMsg  验证失败时的错误信息
         * @return 委托验证给 [predicate] 的 [Rule]
         */
        @JvmStatic
        fun custom(predicate: (String) -> Boolean, errorMsg: String): Rule = object : Rule {
            override val errorMsg: String = errorMsg
            override fun validate(text: String): Boolean = predicate(text)
        }
    }
}
