package scan.lucas.com.docscan.Utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import android.util.Log
import scan.lucas.com.docscan.Enum.TipoNotificacao


class NotificationManagerCustom(fileName: String, context: Context, var tipo: TipoNotificacao) {

    init {
        mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        builder = NotificationCompat.Builder(context)
        if (tipo == TipoNotificacao.UPLOAD) {
            builder!!.setContentTitle("Iniciando o Upload...")
                    .setContentText(fileName)
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setProgress(100, 0, false)
                    .setAutoCancel(false)
        } else if (tipo == TipoNotificacao.DOWNLOAD) {
            builder!!.setContentTitle("Iniciando o Download...")
                    .setContentText(fileName)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setProgress(100, 0, false)
                    .setAutoCancel(false)
        }
    }

    fun updateNotification(percent: Int, fileName: String, contentText: String, contentIntent: PendingIntent? = null) {
        try {
            if (tipo == TipoNotificacao.UPLOAD)
                builder!!.setContentText(contentText)
                        .setContentTitle(fileName)
                        .setSmallIcon(android.R.drawable.stat_sys_upload)
                        .setOngoing(true)
                        .setContentInfo("$percent%")
                        .setProgress(100, percent, false)
            else if (tipo == TipoNotificacao.DOWNLOAD)
                builder!!.setContentText(contentText)
                        .setContentTitle(fileName)
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setOngoing(true)
                        .setContentInfo("$percent%")
                        .setProgress(100, percent, false)

            mNotificationManager!!.notify(NOTIFICATION_ID, builder!!.build())
            if (percent == 100 && tipo == TipoNotificacao.UPLOAD)
                deleteNotification()
            else if (percent == 100 && tipo == TipoNotificacao.DOWNLOAD) {
                builder!!.setContentText(contentText)
                        .setContentTitle(fileName)
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .setProgress(0, 0, false)
                        .setContentInfo("Download Concluido")

                if (contentIntent != null)
                    builder!!.setContentIntent(contentIntent)
                mNotificationManager!!.notify(NOTIFICATION_ID, builder!!.build())

            }

        } catch (e: Exception) {
            // TODO Auto-generated catch block
            Log.e("Error...Notification.", e.message + ".....")
            e.printStackTrace()
        }

    }

    fun failUploadNotification(/*int percentage, String fileName*/) {
        Log.e("downloadsize", "failed notification...")

        if (builder != null) {
            /* if (percentage < 100) {*/
            builder!!.setContentText("Uploading Failed")
                    //.setContentTitle(fileName)
                    .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                    .setOngoing(false)
            mNotificationManager!!.notify(NOTIFICATION_ID, builder!!.build())
            /*} else {
            mNotificationManager.cancel(NOTIFICATION_ID);
            builder = null;
        }*/
        } else {
            mNotificationManager!!.cancel(NOTIFICATION_ID)
        }
    }

    fun deleteNotification() {
        mNotificationManager!!.cancel(NOTIFICATION_ID)
        builder = null
    }

    companion object {
        var mNotificationManager: NotificationManager? = null
        var builder: NotificationCompat.Builder? = null
        var context: Context? = null
        var NOTIFICATION_ID = 111
        var fileUploadNotification: NotificationManagerCustom? = null
    }
}