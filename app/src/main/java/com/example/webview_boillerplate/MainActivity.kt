package com.example.webview_boillerplate

import android.annotation.TargetApi
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import android.webkit.ValueCallback as ValueCallback1
import androidx.core.app.ActivityCompat.startActivityForResult

import android.webkit.WebChromeClient.FileChooserParams

import android.webkit.ValueCallback

import android.webkit.WebView

import android.webkit.WebChromeClient





class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    val IMAGE_SELECTOR_REQ = 1
    private var mFilePathCallback: ValueCallback<*>? = null


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview1)
        
        
        // 사진 업로드 허용
        webView.setWebChromeClient(object : WebChromeClient() { @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
                mFilePathCallback = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT)

                // 여러장의 사진을 선택하는 경우
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(intent, "Select picture"),
                    IMAGE_SELECTOR_REQ
                )
                return true
            }
        })


        webView.apply {
            webViewClient = WebViewClientClass() // new WebViewClient()); //클릭시 새창 안뜨게



            //팝업이나 파일 업로드 등 설정해주기 위해 webView.webChromeClient를 설정
            //웹뷰에서 크롬이 실행가능&& 새창띄우기는 안됨
            //webChromeClient = WebChromeClient()

            //웹뷰에서 팝업창 호출하기 위해
            webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                    val newWebView = WebView(this@MainActivity).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                    }

                    val dialog = Dialog(this@MainActivity).apply {
                        setContentView(newWebView)
                        window!!.attributes.width = ViewGroup.LayoutParams.MATCH_PARENT
                        window!!.attributes.height = ViewGroup.LayoutParams.MATCH_PARENT
                        show()
                    }

                    newWebView.webChromeClient = object : WebChromeClient() {
                        override fun onCloseWindow(window: WebView?) {
                            dialog.dismiss()
                        }
                    }

                    (resultMsg?.obj as WebView.WebViewTransport).webView = newWebView
                    resultMsg.sendToTarget()
                    return true
                }
            }


            settings.javaScriptEnabled = true
            settings.setSupportMultipleWindows(true) // 새창띄우기 허용여부
            settings.javaScriptCanOpenWindowsAutomatically = true // 자바스크립트 새창뛰우기 (멀티뷰) 허용여부
            settings.loadWithOverviewMode = true //메타태크 허용여부
            settings.useWideViewPort = true //화면 사이즈 맞추기 허용여부
            settings.setSupportZoom(true) //화면 줌 허용여부
            settings.builtInZoomControls = true //화면 확대 축소 허용여부
            settings.setSupportMultipleWindows(true);

            // Enable and setup web view cache
            settings.cacheMode =
                WebSettings.LOAD_NO_CACHE //브라우저 캐시 허용여부  // WebSettings.LOAD_DEFAULT

            settings.domStorageEnabled = true //로컬저장소 허용여부
            settings.displayZoomControls = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settings.safeBrowsingEnabled = true  // api 26
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                settings.mediaPlaybackRequiresUserGesture = false
            }

            settings.allowContentAccess = true
            settings.setGeolocationEnabled(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                settings.allowUniversalAccessFromFileURLs = true
            }

            settings.allowFileAccess = true
            //settings.loadsImagesAutomatically = true

            fitsSystemWindows = true
        }

        webview1.loadUrl("http://memento.webview.s3-website.ap-northeast-2.amazonaws.com")
    }

    ////

    inner class WebViewClientClass : WebViewClient() {
        //페이지 이동
//        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//            view.loadUrl(url)
//            return true
//        }
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            if (url.startsWith("intent:")) {
                try {
                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    val existPackage =
                        packageManager.getLaunchIntentForPackage(intent.getPackage()!!)
                    if (existPackage != null) {
                        startActivity(intent)
                    } else {
                        val marketIntent = Intent(Intent.ACTION_VIEW)
                        marketIntent.data = Uri.parse("market://details?id=" + intent.getPackage())
                    }
                    return true
                } catch (e: Exception) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        if (intent.action!!.contains("kakao")) {
                            view.loadUrl(intent.getStringExtra("browser_fallback_url")!!)
                        } else {
                            val marketIntent = Intent(Intent.ACTION_VIEW)
                            marketIntent.data
                            Uri.parse("market://details?id=" + intent.getPackage())
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            } else {
                view.loadUrl(url)
            }
            return true
        }


        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
//            mProgressBar.visibility = ProgressBar.VISIBLE
            webView.visibility = View.INVISIBLE
        }

        override fun onPageCommitVisible(view: WebView, url: String) {
            super.onPageCommitVisible(view, url)
//            mProgressBar.visibility = ProgressBar.GONE
            webView.visibility = View.VISIBLE
        }


        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            var builder: android.app.AlertDialog.Builder =
                android.app.AlertDialog.Builder(this@MainActivity)
            var message = "SSL Certificate error."
            when (error.primaryError) {
                SslError.SSL_UNTRUSTED -> message = "The certificate authority is not trusted."
                SslError.SSL_EXPIRED -> message = "The certificate has expired."
                SslError.SSL_IDMISMATCH -> message = "The certificate Hostname mismatch."
                SslError.SSL_NOTYETVALID -> message = "The certificate is not yet valid."
            }
            message += " Do you want to continue anyway?"
            builder.setTitle("SSL Certificate Error")
            builder.setMessage(message)
            builder.setPositiveButton("continue",
                DialogInterface.OnClickListener { _, _ -> handler.proceed() })
            builder.setNegativeButton("cancel",
                DialogInterface.OnClickListener { dialog, which -> handler.cancel() })
            val dialog: android.app.AlertDialog? = builder.create()
            dialog?.show()
        }
    }

    ////

    override fun onBackPressed() {
        if (webview1.canGoBack())
        {
            webview1.goBack()
        }
        else
        {
            finish()
        }
    }
}