package remix.myplayer.misc

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.FlowableSubscriber
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscription
import remix.myplayer.App
import remix.myplayer.R
import remix.myplayer.theme.Theme
import remix.myplayer.util.LogUtil
import remix.myplayer.util.MediaStoreUtil
import remix.myplayer.util.ToastUtil
import java.io.File
import java.util.*

/**
 * Created by Remix on 2017/11/20.
 */

class MediaScanner(private val context: Context) {

    lateinit var connection: MediaScannerConnection
    private var dir: File? = null
    private var subscription: Subscription? = null
    private val toScanFiles = ArrayList<File>()

    init {
        val loadingDialog = Theme.getBaseDialog(context)
                .cancelable(false)
                .title(R.string.please_wait)
                .content(R.string.scaning)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .dismissListener { dialog -> connection.disconnect() }
                .build()

        val client = object : MediaScannerConnection.MediaScannerConnectionClient {
            override fun onMediaScannerConnected() {
                Flowable.create(FlowableOnSubscribe<File> { emitter ->
                    getFileCount(dir!!)
                    for (file in toScanFiles) {
                        emitter.onNext(file)
                    }
                    emitter.onComplete()
                }, BackpressureStrategy.BUFFER)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : FlowableSubscriber<File> {
                            override fun onSubscribe(s: Subscription) {
                                LogUtil.d(TAG, "onSubscribe")
                                loadingDialog.show()
                                subscription = s
                                subscription?.request(1)
                            }

                            override fun onNext(file: File) {
                                LogUtil.d(TAG, "onNext: $file")
                                loadingDialog.setContent(file.absolutePath)
                                connection.scanFile(file.absolutePath, "audio/*")
                            }

                            override fun onError(throwable: Throwable) {
                                LogUtil.d(TAG, "onError: $throwable")
                                loadingDialog.dismiss()
                                ToastUtil.show(context, R.string.scan_failed, throwable.toString())
                            }

                            override fun onComplete() {
                                LogUtil.d(TAG, "onComplete")
                                loadingDialog.dismiss()
                                ToastUtil.show(context, context.getString(R.string.scanned_count, toScanFiles.size))
                                App.getContext().contentResolver.notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null)
                            }
                        })
            }


            override fun onScanCompleted(path: String, uri: Uri) {
                subscription?.request(1)
                LogUtil.d(TAG, "onScanCompleted --- path: $path uri: $uri")
            }
        }

        connection = MediaScannerConnection(context, client)
    }

    fun scanFiles(dir: File) {
        checkNotNull(dir)
        this.dir = dir
        connection.connect()
    }

    private fun getFileCount(file: File) {
        if (file.isFile && file.length() >= MediaStoreUtil.SCAN_SIZE) {
            if (isAudioFile(file))
                toScanFiles.add(file)
        } else {
            val files = file.listFiles() ?: return
            for (temp in files) {
                getFileCount(temp)
            }
        }
    }

    private fun isAudioFile(file: File): Boolean {
        val ext = getFileExtension(file.name)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        LogUtil.d(TAG, "Mime: $mime")
        return !mime.isNullOrEmpty() && mime.startsWith("audio") && !mime.contains("mpegurl")
    }

    private fun getFileExtension(fileName: String): String? {
        val i = fileName.lastIndexOf('.')
        return if (i > 0) {
            fileName.substring(i + 1)
        } else
            null
    }

    companion object {
        private const val TAG = "MediaScanner"

        private val SUPPORT_FORMAT = Arrays.asList("m4a", "aac", "flac", "mp3", "wav", "ogg")
    }
}
