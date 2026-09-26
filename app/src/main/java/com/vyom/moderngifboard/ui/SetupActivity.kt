package com.vyom.moderngifboard.ui
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
class SetupActivity:AppCompatActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b)
  val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(48,48,48,48)}
  box.addView(TextView(this).apply{text="Modern GIF Board";textSize=28f;gravity=Gravity.CENTER})
  box.addView(TextView(this).apply{text="A fast GIF keyboard designed to sit alongside Gboard.\n\n1. Enable Modern GIF Board\n2. Select it from the keyboard switcher\n3. Search and insert a GIF\n4. Tap ABC to return";textSize=16f;setPadding(0,32,0,32)})
  box.addView(MaterialButton(this).apply{text="Enable keyboard";setOnClickListener{startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))}})
  box.addView(MaterialButton(this).apply{text="Choose keyboard";setOnClickListener{(getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).showInputMethodPicker()}})
  setContentView(box)
 }
}