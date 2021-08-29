package ba.grbo.currencyconverter.util

import timber.log.Timber

open class Tree : Timber.DebugTree() {
    private lateinit var element: StackTraceElement

    private val StackTraceElement.info: String
        get() = String.format(
            "%s.%s (%s:%s)",
            className.substring(className.lastIndexOf(".") + 1),
            methodName,
            fileName,
            lineNumber
        )

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, getRealMessage(message), t)
    }

    override fun createStackElementTag(element: StackTraceElement): String {
        this.element = element
        return "ba.grbo"
    }

    open fun getRealMessage(message: String) = String.format(
        "%s -> %s -> %s",
        Thread.currentThread().name,
        element.info,
        message
    )
}