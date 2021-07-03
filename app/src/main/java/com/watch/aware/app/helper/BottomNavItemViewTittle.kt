package com.watch.aware.app.helper

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.watch.aware.app.models.CbnMenuItem

/**
 * Created by suson on 10/1/20
 */

class BottomNavItemViewTittle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {
        const val TAG = "BottomNavItemView"
    }

    // the animation progress as the caller might call multiple times even when we
    // might have animation running
    private var isAnimating = false


    fun setMenuItem(itemCbn: CbnMenuItem) {
        setTextSize(12f)
        if(itemCbn.tittle != 0) {
            setText(itemCbn.tittle)

        }
    }

}