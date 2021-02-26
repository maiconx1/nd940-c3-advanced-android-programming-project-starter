package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.Constants.Companion.EXTRA_NAME
import com.udacity.Constants.Companion.EXTRA_STATUS
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val downloadName = intent.getStringExtra(EXTRA_NAME)
        file_name.text = downloadName ?: ""

        val statusValue: Pair<String, Int> = when (intent.getIntExtra(EXTRA_STATUS, 0)) {
            1 -> Pair(getString(R.string.success), getColor(R.color.colorPrimaryDark))
            else -> Pair(getString(R.string.fail), getColor(R.color.red))
        }
        status.text = statusValue.first
        status.setTextColor(statusValue.second)

        button_ok.setOnClickListener {
            onBackPressed()
        }
    }
}
