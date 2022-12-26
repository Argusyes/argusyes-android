package com.android.argusyes.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

class FlipUtils {
    companion object {
        fun flipAnimation(one: View?, two: View?) {
            if (one == null || two == null) {
                return
            }

            val v : View
            val i : View

            //逻辑判断
            if (one.visibility == View.GONE) {
                i = one
                v = two
            } else {
                i = two
                v = one
            }
            //LinearInterpolator()     其变化速率恒定
            val visibleToInvisible = ObjectAnimator.ofFloat(v, "rotationY", 0f, 90f)
            visibleToInvisible.duration = 200
            //AccelerateInterpolator()    其变化开始速率较慢，后面加速
            visibleToInvisible.interpolator = AccelerateInterpolator()
            val invisibleToVisible = ObjectAnimator.ofFloat(i, "rotationY", -90f, 0f)
            invisibleToVisible.duration = 200
            //DecelerateInterpolator()   其变化开始速率较快，后面减速
            invisibleToVisible.interpolator = DecelerateInterpolator()
            visibleToInvisible.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(anim: Animator) {
                    v.visibility = View.GONE
                    invisibleToVisible.start()
                    i.visibility = View.VISIBLE
                }
            })
            visibleToInvisible.start()
        }
    }
}