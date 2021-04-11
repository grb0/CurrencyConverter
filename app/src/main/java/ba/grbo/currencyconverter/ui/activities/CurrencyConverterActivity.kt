package ba.grbo.currencyconverter.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ba.grbo.currencyconverter.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrencyConverterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_converter)
    }
}