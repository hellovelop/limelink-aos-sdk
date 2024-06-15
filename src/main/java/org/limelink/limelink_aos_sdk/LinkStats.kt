package src.main.java.org.limelink.limelink_aos_sdk

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

object LinkStats {
    private const val PREFS_NAME = "link_first_launch_prefs"
    private const val KEY_FIRST_LAUNCH = "is_first_launch"

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstLaunch(): Boolean {
        val isFirstLaunch = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
        if (isFirstLaunch) {
            sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        }
        return isFirstLaunch
    }
}

fun saveLimeLinkStatus(context: Context, intent: Intent,privateKey:String) {
    if (LinkStats.isFirstLaunch()) {
        onFirstLaunch(context,intent,privateKey)
    } else {
        onSubsequentLaunch(context,intent,privateKey)
    }
}

private fun onFirstLaunch(context: Context,intent:Intent,privateKey:String) {
    // 첫 실행일 때 실행할 코드

}

private fun onSubsequentLaunch(context: Context,intent:Intent,privateKey:String) {
    // 첫 실행이 아닐 때 실행할 코드
}