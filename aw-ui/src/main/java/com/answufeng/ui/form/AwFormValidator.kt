package com.answufeng.ui.form

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.answufeng.ui.widget.AwSmartEditText

/**
 * A validation rule that checks a single constraint on a field's text input.
 */
interface Rule {

    /**
     * The error message displayed when validation fails.
     */
    val errorMsg: String

    /**
     * Validates the given text against this rule.
     *
     * @param text The input text to validate.
     * @return `true` if the text passes validation, `false` otherwise.
     */
    fun validate(text: String): Boolean
}

/**
 * A unified form validation framework with a chainable API.
 *
 * Supports validating any [TextView] (including [EditText]) and [AwSmartEditText].
 * For regular [TextView]s, text is extracted and rules are evaluated externally.
 * For [AwSmartEditText], validation is delegated to its own [AwSmartEditText.validate] method,
 * and any rules added via [addField] are registered as named validators on that view.
 *
 * ### Usage
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
     * Adds a field with one or more validation rules.
     *
     * If the view is an [AwSmartEditText], the rules are registered as named validators
     * on that view and validation is delegated to [AwSmartEditText.validate].
     * For other [TextView] subclasses (e.g., [EditText]), text is extracted and
     * rules are evaluated by this validator.
     *
     * @param view  The view to validate.
     * @param rules One or more [Rule] instances to apply to this field.
     * @return This validator instance for chaining.
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
     * Validates all registered fields and returns `true` if every field passes.
     *
     * For [EditText] fields, the first failing rule's error message is set via
     * [EditText.setError]. For [AwSmartEditText] fields, validation is delegated
     * to [AwSmartEditText.validate].
     *
     * @return `true` if all fields pass validation, `false` otherwise.
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
     * Returns a map of all fields that failed validation to their corresponding
     * error messages. This map is populated after calling [validate].
     *
     * @return A map where the key is the view and the value is the error message.
     */
    fun getErrors(): Map<View, String> = errors.toMap()

    /**
     * Clears all validation errors.
     *
     * For [EditText] fields, this removes the error via [EditText.setError].
     * For [AwSmartEditText] fields, this clears the displayed error message.
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
     * Registers a callback that is invoked whenever [validate] is called,
     * reporting whether all fields are currently valid.
     *
     * @param listener A lambda that receives `true` if all fields pass, `false` otherwise.
     * @return This validator instance for chaining.
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
         * Creates a rule that requires the field to be non-blank.
         *
         * @param errorMsg The error message when the field is blank.
         * @return A [Rule] that rejects blank input.
         */
        @JvmStatic
        @JvmOverloads
        fun required(errorMsg: String = "此字段不能为空"): Rule = object : Rule {
            override val errorMsg: String = errorMsg
            override fun validate(text: String): Boolean = text.isNotBlank()
        }

        /**
         * Creates a rule that requires the field's text length to be at least [min].
         *
         * @param min      The minimum allowed length.
         * @param errorMsg The error message when the text is too short.
         *                 Defaults to "长度不能少于{min}个字符".
         * @return A [Rule] that rejects input shorter than [min] characters.
         */
        @JvmStatic
        @JvmOverloads
        fun minLength(min: Int, errorMsg: String? = null): Rule = object : Rule {
            override val errorMsg: String = errorMsg ?: "长度不能少于${min}个字符"
            override fun validate(text: String): Boolean = text.length >= min
        }

        /**
         * Creates a rule that requires the field's text length to be at most [max].
         *
         * @param max      The maximum allowed length.
         * @param errorMsg The error message when the text is too long.
         *                 Defaults to "长度不能超过{max}个字符".
         * @return A [Rule] that rejects input longer than [max] characters.
         */
        @JvmStatic
        @JvmOverloads
        fun maxLength(max: Int, errorMsg: String? = null): Rule = object : Rule {
            override val errorMsg: String = errorMsg ?: "长度不能超过${max}个字符"
            override fun validate(text: String): Boolean = text.length <= max
        }

        /**
         * Creates a rule that validates the field as an email address.
         *
         * @param errorMsg The error message when the email format is invalid.
         * @return A [Rule] that rejects input that does not match a standard email pattern.
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
         * Creates a rule that validates the field as a Chinese mobile phone number (11 digits).
         *
         * @param errorMsg The error message when the phone format is invalid.
         * @return A [Rule] that rejects input that does not match the phone number pattern.
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
         * Creates a rule that validates the field against a regular expression.
         *
         * @param regex    The regular expression pattern to match.
         * @param errorMsg The error message when the pattern does not match.
         * @return A [Rule] that rejects input that does not match [regex].
         */
        @JvmStatic
        fun pattern(regex: String, errorMsg: String): Rule = object : Rule {
            override val errorMsg: String = errorMsg
            override fun validate(text: String): Boolean = text.matches(Regex(regex))
        }

        /**
         * Creates a rule with a custom validation predicate.
         *
         * @param predicate A function that returns `true` if the input is valid.
         * @param errorMsg  The error message when validation fails.
         * @return A [Rule] that delegates validation to [predicate].
         */
        @JvmStatic
        fun custom(predicate: (String) -> Boolean, errorMsg: String): Rule = object : Rule {
            override val errorMsg: String = errorMsg
            override fun validate(text: String): Boolean = predicate(text)
        }
    }
}
