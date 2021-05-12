package ba.grbo.currencyconverter.util

import android.content.DialogInterface
import androidx.annotation.StringRes

open class DialogInfo(
    @StringRes val title: Int,
    @StringRes val message: Int,
    val positiveButton: Pair<Int, (() -> Unit)?>,
    val onDismiss: (() -> Unit)?
)

class ExtendedDialogInfo(
    title: Int,
    message: Int, positiveButton: Pair<Int, (() -> Unit)?>,
    onDismiss: (() -> Unit)?,
    val negativeButton: Pair<Int, (() -> Unit)?>
) : DialogInfo(title, message, positiveButton, onDismiss)
