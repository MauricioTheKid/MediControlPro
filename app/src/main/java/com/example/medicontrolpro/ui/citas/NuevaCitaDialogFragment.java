package com.example.medicontrolpro.ui.citas;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.medicontrolpro.R;
import com.example.medicontrolpro.data.CitaEntity;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NuevaCitaDialogFragment extends DialogFragment {

    public interface OnCitaGuardadaListener {
        void onCitaGuardada(CitasViewModel.Cita cita);
    }

    private OnCitaGuardadaListener listener;
    private TextInputEditText editDoctor, editEspecialidad, editFecha, editHora, editMotivo;
    private AutoCompleteTextView autoCompleteEstado;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter, timeFormatter;

    public static NuevaCitaDialogFragment newInstance(CitaEntity cita) {
        NuevaCitaDialogFragment fragment = new NuevaCitaDialogFragment();
        Bundle args = new Bundle();
        if (cita != null) {
            args.putString("doctor", cita.doctor);
            args.putString("especialidad", cita.especialidad);
            args.putString("fecha", cita.fecha);
            args.putString("hora", cita.hora);
            args.putString("motivo", cita.motivo);
            args.putString("estado", cita.estado);
            args.putInt("id", cita.id);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnCitaGuardadaListener(OnCitaGuardadaListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_nueva_cita, null);

        inicializarVistas(view);
        configurarListeners();
        cargarDatosExistente();
        configurarEstados();
        configurarBotones(view);

        builder.setView(view);
        return builder.create();
    }

    private void inicializarVistas(View view) {
        editDoctor = view.findViewById(R.id.edit_doctor);
        editEspecialidad = view.findViewById(R.id.edit_especialidad);
        editFecha = view.findViewById(R.id.edit_fecha);
        editHora = view.findViewById(R.id.edit_hora);
        editMotivo = view.findViewById(R.id.edit_motivo);
        autoCompleteEstado = view.findViewById(R.id.edit_estado);
    }

    private void configurarListeners() {
        // Listener para seleccionar fecha
        editFecha.setOnClickListener(v -> mostrarDatePicker());

        // Listener para evitar que el teclado aparezca
        editFecha.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mostrarDatePicker();
            }
        });

        // Listener para seleccionar hora
        editHora.setOnClickListener(v -> mostrarTimePicker());

        // Listener para evitar que el teclado aparezca
        editHora.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mostrarTimePicker();
            }
        });
    }

    private void configurarBotones(View view) {
        Button btnCancelar = view.findViewById(R.id.btn_cancelar);
        Button btnGuardar = view.findViewById(R.id.btn_guardar);

        btnCancelar.setOnClickListener(v -> dismiss());

        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                guardarCita();
                dismiss();
            }
        });
    }

    private void mostrarDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    editFecha.setText(dateFormatter.format(calendar.getTime()));
                    editFecha.setError(null);
                },
                year, month, day
        );

        // Establecer fecha mínima como hoy
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.show();
    }

    private void mostrarTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    calendar.set(Calendar.MINUTE, selectedMinute);
                    editHora.setText(timeFormatter.format(calendar.getTime()));
                    editHora.setError(null);
                },
                hour, minute, true
        );

        timePickerDialog.show();
    }

    private void cargarDatosExistente() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("doctor")) {
            editDoctor.setText(args.getString("doctor", ""));
            editEspecialidad.setText(args.getString("especialidad", ""));
            editFecha.setText(args.getString("fecha", ""));
            editHora.setText(args.getString("hora", ""));
            editMotivo.setText(args.getString("motivo", ""));

            if (autoCompleteEstado != null) {
                String estado = args.getString("estado", "Pendiente");
                autoCompleteEstado.setText(estado);
            }

            // Si estamos editando, actualizar el calendario con la fecha existente
            if (args.containsKey("fecha") && !args.getString("fecha").isEmpty()) {
                try {
                    Calendar tempCalendar = Calendar.getInstance();
                    tempCalendar.setTime(dateFormatter.parse(args.getString("fecha")));
                    calendar = tempCalendar;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void configurarEstados() {
        if (autoCompleteEstado != null) {
            String[] estados = {"Pendiente", "Confirmada", "Cancelada", "Completada"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    estados
            );
            autoCompleteEstado.setAdapter(adapter);

            if (autoCompleteEstado.getText().toString().isEmpty()) {
                autoCompleteEstado.setText("Pendiente");
            }
        }
    }

    private boolean validarCampos() {
        boolean isValid = true;

        if (editDoctor.getText().toString().trim().isEmpty()) {
            editDoctor.setError("Ingrese el nombre del doctor");
            isValid = false;
        } else {
            editDoctor.setError(null);
        }

        if (editEspecialidad.getText().toString().trim().isEmpty()) {
            editEspecialidad.setError("Ingrese la especialidad");
            isValid = false;
        } else {
            editEspecialidad.setError(null);
        }

        if (editFecha.getText().toString().trim().isEmpty()) {
            editFecha.setError("Seleccione una fecha");
            isValid = false;
        } else {
            editFecha.setError(null);
        }

        if (editHora.getText().toString().trim().isEmpty()) {
            editHora.setError("Seleccione una hora");
            isValid = false;
        } else {
            editHora.setError(null);
        }

        return isValid;
    }

    private void guardarCita() {
        String doctor = editDoctor.getText().toString().trim();
        String especialidad = editEspecialidad.getText().toString().trim();
        String fecha = editFecha.getText().toString().trim();
        String hora = editHora.getText().toString().trim();
        String motivo = editMotivo.getText().toString().trim();
        String estado = autoCompleteEstado != null ? autoCompleteEstado.getText().toString().trim() : "Pendiente";

        CitasViewModel.Cita nuevaCita = new CitasViewModel.Cita(doctor, especialidad, fecha, hora, motivo, estado);

        if (listener != null) {
            listener.onCitaGuardada(nuevaCita);
        }
    }
}