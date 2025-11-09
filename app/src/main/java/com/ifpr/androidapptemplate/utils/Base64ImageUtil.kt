package com.ifpr.androidapptemplate.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Utilitário para conversão de imagens em Base64
 */
object Base64ImageUtil {
    private const val TAG = "Base64ImageUtil"
    private const val MAX_WIDTH = 1024
    private const val MAX_HEIGHT = 1024
    private const val QUALITY = 70

    /**
     * Converte uma imagem em URI para Base64
     */
    fun toBase64(context: Context, imageUri: Uri): String? {
        var inputStream = context.contentResolver.openInputStream(imageUri)
        var bitmap: Bitmap? = null
        var resizedBitmap: Bitmap? = null
        var outputStream: ByteArrayOutputStream? = null

        return try {
            // Decodifica a imagem
            bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap == null) {
                Log.e(TAG, "Falha ao decodificar imagem: $imageUri")
                return null
            }

            // Redimensiona se necessário
            resizedBitmap = if (bitmap.width > MAX_WIDTH || bitmap.height > MAX_HEIGHT) {
                val scaleWidth = MAX_WIDTH.toFloat() / bitmap.width
                val scaleHeight = MAX_HEIGHT.toFloat() / bitmap.height
                val scale = minOf(scaleWidth, scaleHeight)
                
                val newWidth = (bitmap.width * scale).toInt()
                val newHeight = (bitmap.height * scale).toInt()
                
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            // Converte para JPEG e depois Base64
            outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream)
            val bytes = outputStream.toByteArray()
            
            Base64.encodeToString(bytes, Base64.DEFAULT)

        } catch (e: IOException) {
            Log.e(TAG, "Erro ao processar imagem: ${e.message}", e)
            null
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
                bitmap?.recycle()
                if (resizedBitmap != null && resizedBitmap != bitmap) {
                    resizedBitmap.recycle()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Erro ao liberar recursos: ${e.message}", e)
            }
        }
    }
}