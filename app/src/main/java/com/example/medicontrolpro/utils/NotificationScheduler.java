package com.example.medicontrolpro.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.medicontrolpro.data.CitaEntity;
import com.example.medicontrolpro.receivers.AlarmReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationScheduler {

    public static void scheduleCitaNotification(Context context, CitaEntity cita) {
        try {
            // Parsear fecha y hora de la cita
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date citaDateTime = sdf.parse(cita.fecha + " " + cita.hora);

            if (citaDateTime != null) {
                Calendar citaCal = Calendar.getInstance();
                citaCal.setTime(citaDateTime);

                // Programar recordatorio 1 hora antes
                Calendar alertCal = (Calendar) citaCal.clone();
                alertCal.add(Calendar.HOUR_OF_DAY, -1);

                // Si la alerta es en el pasado, no programar
                if (alertCal.before(Calendar.getInstance())) {
                    return;
                }

                // Crear intent para la alarma
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra(AlarmReceiver.EXTRA_CITA_ID, cita.id);
                intent.putExtra(AlarmReceiver.EXTRA_DOCTOR, cita.doctor);
                intent.putExtra(AlarmReceiver.EXTRA_ESPECIALIDAD, cita.especialidad);
                intent.putExtra(AlarmReceiver.EXTRA_FECHA, cita.fecha);
                intent.putExtra(AlarmReceiver.EXTRA_HORA, cita.hora);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        cita.id, // ID único por cita
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                // Programar la alarma
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                alertCal.getTimeInMillis(),
                                pendingIntent
                        );
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                alertCal.getTimeInMillis(),
                                pendingIntent
                        );
                    } else {
                        alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                alertCal.getTimeInMillis(),
                                pendingIntent
                        );
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void cancelCitaNotification(Context context, int citaId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                citaId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        // También cancelar la notificación si ya está mostrándose
        NotificationHelper.cancelNotification(context, citaId);
    }

    public static void rescheduleAllNotifications(Context context, List<CitaEntity> citas) {
        // Cancelar todas las notificaciones existentes
        for (CitaEntity cita : citas) {
            cancelCitaNotification(context, cita.id);
        }

        // Reprogramar todas las citas futuras
        for (CitaEntity cita : citas) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date citaDateTime = sdf.parse(cita.fecha + " " + cita.hora);

                if (citaDateTime != null && citaDateTime.after(new Date())) {
                    scheduleCitaNotification(context, cita);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}