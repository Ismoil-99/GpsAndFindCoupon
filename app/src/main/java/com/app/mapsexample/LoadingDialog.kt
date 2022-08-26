package com.app.mapsexample

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window

class LoadingDialog(context:Context) : Dialog(context) {

    init {
        setCancelable(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_dowload)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
    }
}