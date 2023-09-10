package com.shankar.videoPlayer

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.shankar.videoPlayer.databinding.ActivityMainBinding
import com.shankar.videoPlayer.pdfService.AppPermission
import com.shankar.videoPlayer.pdfService.AppPermission.Companion.permissionGranted
import com.shankar.videoPlayer.pdfService.AppPermission.Companion.requestPermission


class MainActivity : AppCompatActivity() {
    private var position = 0
    private val viewDataBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

//    val videoUrl = "http://192.168.126.193:8626/videoplayer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewDataBinding.root)

        if (!permissionGranted(this@MainActivity))
            requestPermission(this)
        playVideo()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppPermission.REQUEST_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                requestPermission(this)
                toastErrorMessage("Permission should be allowed")
            }
        }
    }

    private fun toastErrorMessage(s: String) {
        Toast.makeText(this@MainActivity, s, Toast.LENGTH_SHORT).show()
    }

    private fun playVideo() {
        try {
            val videoUrl = "http://192.168.126.193:8626/videoplayer"
            val mediaController = MediaController(this)
            mediaController.setAnchorView(viewDataBinding.videoView)
            val video = Uri.parse(videoUrl)

            viewDataBinding.videoView.setMediaController(mediaController)
            viewDataBinding.videoView.setVideoURI(video)
            viewDataBinding.videoView.requestFocus()
            viewDataBinding.videoView.setOnErrorListener { _, what, _ ->
                val errorMessage: String = when (what) {
                    MediaPlayer.MEDIA_ERROR_UNKNOWN -> "Unknown error occurred."
                    MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "Media server died unexpectedly."
                    else -> "An error occurred while playing the video."
                }
                toastErrorMessage(errorMessage)
                return@setOnErrorListener true
            }
            viewDataBinding.videoView.setOnInfoListener { _, what, _ ->
                when (what) {
                    MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                        viewDataBinding.progressBar.visibility = ProgressBar.VISIBLE
                    }
                    MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                        viewDataBinding.progressBar.visibility = ProgressBar.INVISIBLE
                    }
                }
                true
            }
            viewDataBinding.videoView.setOnPreparedListener {
                viewDataBinding.progressBar.visibility = ProgressBar.INVISIBLE
                viewDataBinding.videoView.seekTo(position)

                if (position == 0) {
                    viewDataBinding.videoView.start()
                }
                it.setOnVideoSizeChangedListener { mp, width, height ->
                    mediaController.setAnchorView(viewDataBinding.videoView)
                }

            }
        } catch (e: Exception) {
            println("Video Play Error :$e")
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outPersistentState.putInt("CurrentPosition", viewDataBinding.videoView.currentPosition);
        viewDataBinding.videoView.pause()
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
        position = savedInstanceState!!.getInt("CurrentPosition")
        viewDataBinding.videoView.seekTo(position)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        super.onConfigurationChanged(newConfig)
        if (viewDataBinding.videoView.isPlaying) {
            viewDataBinding.videoView.pause()
            viewDataBinding.videoView.postDelayed(
                { viewDataBinding.videoView.start() },
                100
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewDataBinding.videoView.seekTo(position)
        viewDataBinding.videoView.start()
    }

    override fun onPause() {
        super.onPause()
        position = viewDataBinding.videoView.currentPosition
        viewDataBinding.videoView.pause()
    }

}