package com.example.medicontrolpro.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.medicontrolpro.data.CitaEntity;
import com.example.medicontrolpro.utils.NotificationHelper;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_CITA_ID = "cita_id";
    public static final String EXTRA_DOCTOR = "doctor";
    public static final String EXTRA_ESPECIALIDAD = "especialidad";
    public static final String EXTRA_FECHA = "fecha";
    public static final String EXTRA_HORA = "hora";

    @Override
    public void onReceive(Context context, Intent intent) {
        int citaId = intent.getIntExtra(EXTRA_CITA_ID, -1);
        String doctor = intent.getStringExtra(EXTRA_DOCTOR);
        String especialidad = intent.getStringExtra(EXTRA_ESPECIALIDAD);
        String fecha = intent.getStringExtra(EXTRA_FECHA);
        String hora = intent.getStringExtra(EXTRA_HORA);

        String title = "⚕️ Recordatorio de Cita";
        String message = "Tienes cita con " + doctor + " (" + especialidad + ") hoy a las " + hora;

        NotificationHelper.showNotification(context, title, message, citaId);
    }
}