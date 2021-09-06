package com.pavelprojects.filmlibraryproject.ui.about

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.WindowManager.*
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.pavelprojects.filmlibraryproject.App.Companion.LINK_GITHUB
import com.pavelprojects.filmlibraryproject.App.Companion.LINK_PROFILE
import com.pavelprojects.filmlibraryproject.R


class AboutDialogFragment private constructor() : DialogFragment() {
    companion object {
        const val TAG = "AboutDialogFragment"

        fun newInstance(): AboutDialogFragment {
            return AboutDialogFragment()
        }
    }

    private lateinit var textViewVersion: TextView
    private lateinit var cardImageGithub: CardView
    private lateinit var cardImageProfile: CardView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        initViews(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = resources.getDimensionPixelSize(R.dimen.cardview_about_width)
        params?.height = LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = params
    }

    private fun initViews(view: View) {
        cardImageGithub = view.findViewById(R.id.imageView_github)
        cardImageProfile = view.findViewById(R.id.imageView_profile)
        textViewVersion = view.findViewById(R.id.tv_about_version)
        cardImageGithub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(LINK_GITHUB))
            startActivity(intent)
        }
        cardImageProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(LINK_PROFILE))
            startActivity(intent)
        }
        val pInfo = requireContext().packageManager.getPackageInfo(
            requireContext().packageName, 0
        )
        val version = resources.getString(R.string.about_version) + " ${pInfo.versionName}"
        textViewVersion.text = version
    }
}