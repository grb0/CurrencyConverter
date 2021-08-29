package ba.grbo.currencyconverter.util

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

object Trees {
    val Debug: Tree = object : Tree() {
        private var counter = AtomicInteger(1)

        override fun getRealMessage(message: String) = String.format(
            "%d. %s",
            counter.getAndIncrement(),
            super.getRealMessage(message)
        )
    }

    val Release: Tree = object : Tree() {
        override fun log(
            priority: Int,
            tag: String?,
            message: String,
            t: Throwable?
        ) {
            if (priority == Log.ERROR || priority == Log.ASSERT || priority == Log.WARN) {
                val realMessage = getRealMessage(message)
                // Log this message to Firebase
            }
        }
    }
}