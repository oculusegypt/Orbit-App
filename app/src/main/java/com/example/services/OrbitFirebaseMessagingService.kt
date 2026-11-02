package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class OrbitFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "OrbitFCM"
        const val CHANNEL_LIVE_CLASSES_ID = "channel_orbit_live_classes"
        const val CHANNEL_LIVE_CLASSES_NAME = "تنبيهات الفصول الحية والمباشرة 🔴"

        const val CHANNEL_MODULE_REMINDERS_ID = "channel_orbit_module_reminders"
        const val CHANNEL_MODULE_REMINDERS_NAME = "تذكيرات إكمال الدروس والموديولات 📚"

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val liveChannel = NotificationChannel(
                    CHANNEL_LIVE_CLASSES_ID,
                    CHANNEL_LIVE_CLASSES_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تنبيهات فورية عند بدء البث المباشر والميتينج التفاعلي مع المعلمة"
                    enableLights(true)
                    enableVibration(true)
                }

                val reminderChannel = NotificationChannel(
                    CHANNEL_MODULE_REMINDERS_ID,
                    CHANNEL_MODULE_REMINDERS_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "تذكيرات بمتابعة الدروس المعلقة والواجبات المنزلية"
                    enableVibration(true)
                }

                notificationManager.createNotificationChannel(liveChannel)
                notificationManager.createNotificationChannel(reminderChannel)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token received: $token")
        // Broadcast or save token for server delivery
        OrbitNotificationManager.onNewTokenReceived(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val alertType = data["type"] ?: "live_class"
        val title = remoteMessage.notification?.title ?: data["title"] ?: "تنبيه من منصة أوربت ليرن 🌟"
        val body = remoteMessage.notification?.body ?: data["body"] ?: "لديك إشعار جديد في حصص الحساب الذهني"
        val targetId = data["targetId"] ?: ""

        sendNotification(title, body, alertType, targetId)
    }

    private fun sendNotification(title: String, messageBody: String, alertType: String, targetId: String) {
        createNotificationChannels(this)

        val channelId = if (alertType == "module_reminder") {
            CHANNEL_MODULE_REMINDERS_ID
        } else {
            CHANNEL_LIVE_CLASSES_ID
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_type", alertType)
            putExtra("target_id", targetId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(
                if (alertType == "live_class") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        OrbitNotificationManager.recordReceivedAlert(title, messageBody, alertType)
    }
}
