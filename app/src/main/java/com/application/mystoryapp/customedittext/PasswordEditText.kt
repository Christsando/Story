package com.application.mystoryapp.customedittext

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.application.mystoryapp.R

class PasswordEditText: AppCompatEditText {

    constructor(context: Context) : super(context) { checkPassword() }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { checkPassword() }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { checkPassword() }

    private fun checkPassword() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (!charSequence.isNullOrEmpty() && charSequence.length < 8) {
                    error = context.getString(R.string.password_error_message)
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }
}