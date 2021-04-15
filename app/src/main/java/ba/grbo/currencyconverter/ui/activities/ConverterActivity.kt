package ba.grbo.currencyconverter.ui.activities

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import ba.grbo.currencyconverter.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConverterActivity : AppCompatActivity() {
    var onScreenTouched: ((event: MotionEvent) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) onScreenTouched?.invoke(ev)
        return super.dispatchTouchEvent(ev)
    }
}