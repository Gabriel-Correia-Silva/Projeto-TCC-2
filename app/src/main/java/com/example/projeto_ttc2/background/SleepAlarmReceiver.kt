package com.example.projeto_ttc2.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.projeto_ttc2.R
import com.example.projeto_ttc2.presentation.MainActivity

class SleepAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 2
        val channelId = "ExcessiveSleepAlarmChannel"
        val channelName = "Alarme de Sono Excessivo"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Canal para o alarme de sono excessivo."
            enableVibration(true)
            vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
        }
        notificationManager.createNotificationChannel(channel)

        val resultIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val resultPendingIntent = PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("É hora de acordar!")
            .setContentText("Atingiu o tempo limite de sono. Toque para abrir a aplicação.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(resultPendingIntent)
            .setFullScreenIntent(resultPendingIntent, true)

        try {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            notificationBuilder.setSound(alarmSound)
        } catch (e: Exception) {
            Log.e("SleepAlarmReceiver", "Não foi possível obter o som do alarme.", e)
            e.printStackTrace()
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d("SleepAlarmReceiver", "Alarme e notificação de sono excessivo disparados.")
    }
}