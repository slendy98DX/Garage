package com.example.garage.worker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.garage.BaseApplication
import com.example.garage.MainActivity
import com.example.garage.R

class CarReminderWorker(
    context: Context,
    workParams : WorkerParameters
) : Worker(context,workParams) {

    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        if(!this.isStopped){
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent: PendingIntent = PendingIntent
                .getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val carName = inputData.getString(nameKey)

            val builder = NotificationCompat.Builder(applicationContext, BaseApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_car)
                .setContentTitle("Time to check your car")
                .setContentText("You have something to do for this car :  $carName")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationId = 17

            with(NotificationManagerCompat.from(applicationContext)) {
                notify(notificationId, builder.build())
            }

            return Result.success()
        }
        else {
            return Result.failure()
        }
    }

    companion object {
        const val nameKey = "NAME"
    }
}