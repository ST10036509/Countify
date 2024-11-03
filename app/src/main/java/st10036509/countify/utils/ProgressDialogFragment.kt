package st10036509.countify.utils

import androidx.fragment.app.DialogFragment
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import st10036509.countify.R
class ProgressDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(R.layout.progress_dialog)
        builder.setCancelable(false) //prevent dialog dismissal when touched outside
        return builder.create()
    }
}