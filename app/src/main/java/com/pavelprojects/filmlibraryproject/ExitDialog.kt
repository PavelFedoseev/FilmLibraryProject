package com.pavelprojects.filmlibraryproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class ExitDialog(private val listener: OnDialogClickListener) : DialogFragment() {
    companion object {
        private const val TAG_EXIT_DIALOG = "ExitDialog"
        fun createDialog(fragmentManager: FragmentManager, listener: OnDialogClickListener) {
            val dialog = ExitDialog(listener)
            dialog.show(fragmentManager, TAG_EXIT_DIALOG)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(requireContext(), R.layout.fragment_exit_dialog, null)
        view.findViewById<View>(R.id.button_exit_accept).setOnClickListener {
            listener.onAcceptButtonCLick()
            dismiss()
        }
        view.findViewById<View>(R.id.button_exit_dismiss).setOnClickListener {
            listener.onDismissButtonClick()
            dismiss()
        }
        return view
    }

    interface OnDialogClickListener {
        fun onAcceptButtonCLick()
        fun onDismissButtonClick()
    }
}