package com.dashingqi.applicationa

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Button
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btnA).setOnClickListener {
            Intent().apply {
                component = ComponentName(
                    "com.dashingqi.applicationb", "com.dashingqi.applicationb.ActionActivity"
                )
                startActivity(this)
            }
        }


        findViewById<Button>(R.id.btnPermission).setOnClickListener {
            requestPermission()
        }

        findViewById<Button>(R.id.btnB).setOnClickListener {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                // 指定类型
                type = "application/pdf"
                startActivityForResult(this, 10001)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                // saf
                10001 -> {
                    kotlin.runCatching {
                        val dataUri = intent?.data ?: return
                        Log.d(TAG, "onActivityResult: dataUri is $dataUri")
                        val isFile = getFileInput(this, dataUri)
                        if (isFile != null) {
                            Log.d(TAG, "isFile is not null ")
                        } else {
                            Log.d(TAG, "isFile is null ")
                        }

                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            }
        }
    }

    private fun getFileInput(@NonNull context: Context, @NonNull uri: Uri): InputStream? {
        val resolver = context.contentResolver
        kotlin.runCatching {
            val openAssetFileDescriptor = resolver.openAssetFileDescriptor(uri, "r") ?: return null
            val parcelFileDescriptor = openAssetFileDescriptor.parcelFileDescriptor
            if (parcelFileDescriptor != null) {
                return FileInputStream(parcelFileDescriptor.fileDescriptor)
            }
        }

        return null
    }


    /**
     * uri 转 文件路径
     * @param uri Uri
     * @return String
     */
    private fun uri2Path(@NonNull uri: Uri): String {
        // content 开头
        val schemeContent = ContentResolver.SCHEME_CONTENT
        // file 开头
        val schemeFile = ContentResolver.SCHEME_FILE
        return when (uri.scheme) {
            schemeContent -> {
                contentUri2Path(uri)
            }
            schemeFile -> {
                ""
            }

            else -> {
                ""
            }
        }
    }

    /**
     * content开头的uri转文件path
     * @return String
     */
    private fun contentUri2Path(@NonNull uri: Uri): String {
        val documentUri = DocumentsContract.isDocumentUri(this, uri)
        if (documentUri) {
            when {
                isExternalStorageDocument(uri) -> {
                    val documentId = DocumentsContract.getDocumentId(uri)
                    Log.d(TAG, "external storage document documentId is $documentId")
                    val split = documentId.split(":")
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return "${Environment.getExternalStorageDirectory()}${File.separator}${split[1]}"
                    }
                    return ""
                }

                isDownloadsDocument(uri) -> {
                    val documentId = DocumentsContract.getDocumentId(uri)
                    Log.d(TAG, "download document documentId is $documentId")
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), documentId.toLong()
                    )
                    return getDataColumn(this, contentUri, null, null)
                }

                isMediaDocument(uri) -> {
                    val documentId = DocumentsContract.getDocumentId(uri)
                    Log.d(TAG, "media documentId is $documentId")
                    val split = documentId.split(":")
                    val type = split[0]
                    Log.d(TAG, "media type is $type")
                    when (type) {
                        "document" -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val findDocumentPath =
                                    DocumentsContract.findDocumentPath(contentResolver, uri) ?: return ""
                                findDocumentPath.path.forEach {
                                    Log.d(TAG, "document path is i")
                                }
                            } else {

                            }
                            return ""
                        }
                    }
                }

                else -> {
                    return ""
                }
            }
        }

        return ""
    }

    private fun isExternalStorageDocument(@NonNull uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(@NonNull uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(@NonNull uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String {
        var cursor: Cursor? = null
        val column = "_data"
        val projections = arrayOf(column)
        kotlin.runCatching {
            cursor = context.contentResolver.query(uri, projections, selection, selectionArgs, null)
            if (cursor?.moveToFirst() == true) {
                val columnIndex = cursor?.getColumnIndexOrThrow(column) ?: return ""
                return cursor?.getString(columnIndex) ?: ""
            }
        }.getOrDefault {
            cursor?.close()
        }
        return ""
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.MANAGE_DOCUMENTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.MANAGE_DOCUMENTS), 1000
            )
        }
    }
}