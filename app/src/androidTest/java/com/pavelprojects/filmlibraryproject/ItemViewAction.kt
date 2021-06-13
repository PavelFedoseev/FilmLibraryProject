package com.pavelprojects.filmlibraryproject

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher


class ItemViewAction(val viewId: Int) : ViewAction {
    override fun getDescription(): String {
        return ItemViewAction::class.java.name
    }

    override fun getConstraints(): Matcher<View>? {
        return null
    }

    override fun perform(uiController: UiController?, view: View?) {
        val button: View = view!!.findViewById(viewId)
        button.performClick()
        Thread.sleep(20)
        button.performClick()
    }
}