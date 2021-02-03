package com.example.meme_share

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    var currentImageUrl: String?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)




        loadMeme()
    }
    private fun loadMeme(){
        progressBar.visibility=View.VISIBLE
        shareMeme.visibility = View.GONE
        nextMeme.visibility = View.GONE
        val url = "  https://meme-api.herokuapp.com/gimme"

        val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
            { response ->
                currentImageUrl= response.getString("url")

                Glide.with(this).asBitmap().load(currentImageUrl).listener(object:
                    RequestListener<Bitmap>{

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean):
                            Boolean {

                        progressBar.visibility = View.GONE
                        shareMeme.visibility = View.GONE
                        nextMeme.visibility = View.VISIBLE
                        return false

                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean):
                            Boolean {
                        progressBar.visibility = View.GONE
                        shareMeme.visibility = View.VISIBLE
                        nextMeme.visibility = View.VISIBLE

                        shareMeme.setOnClickListener {
                            downloadThenShare(resource)
                        }
                        return false
                    }

                }).into(imageView)

            },
            { error ->
                Toast.makeText(this,"something went wrong", Toast.LENGTH_LONG).show()
            }
        )

// Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

//    fun shareMeme(view: View) {
//        val intent = Intent(Intent.ACTION_SEND)
//        intent.type = "text/plain"
//        intent.putExtra(
//            Intent.EXTRA_TEXT,
//            "Hey, checkout through this cool meme ! I got from Reddit $currentImageUrl"
//        )
//
//        val chooser = Intent.createChooser(intent, "share this meme")
//        startActivity(chooser)
//    }









    fun nextMeme(view: View) {
        loadMeme()

    }

    private fun downloadThenShare(bitmap: Bitmap) {
        val fileName = "shareMeme-${System.currentTimeMillis()}.jpeg"
        val filePath = "${this.cacheDir}/$fileName"
        download(bitmap, filePath) {
            shareImage(this, File(filePath))
        }
    }
    private fun download(bitmap: Bitmap, path: String, finishDownload: () -> Unit) {
        val file = File(path)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
            finishDownload.invoke()
        }
    }

    private fun shareImage(context: Context, file: File) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "image/*"
        val uri = FileProvider.getUriForFile(context, "$packageName.provider", file)
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)

        val intentChooser = Intent.createChooser(sharingIntent, "Share via")

        val resInfoList =
            packageManager.queryIntentActivities(intentChooser, PackageManager.MATCH_DEFAULT_ONLY)

        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            grantUriPermission(packageName, uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intentChooser)
    }
}