package com.application.mystoryapp.customedittext

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import com.application.mystoryapp.R
import androidx.appcompat.widget.AppCompatEditText

class EmailEditText: AppCompatEditText {

    constructor(context: Context) : super(context){checkEmail()}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {checkEmail()}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {checkEmail()}

    private fun checkEmail() {
        addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && !isValidEmail(s.toString())) {
                    error = context.getString(R.string.email_error_message)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) { }
        })
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}