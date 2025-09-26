package com.example.medicontrolpro.ui.doctores;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.medicontrolpro.R;
import com.example.medicontrolpro.data.DoctorEntity;
import com.google.android.material.textfield.TextInputEditText;

public class DoctorDialogFragment extends DialogFragment {

    public interface OnDoctorGuardadaListener {
        void onDoctorGuardada(DoctoresViewModel.Doctor doctor);
    }

    private OnDoctorGuardadaListener listener;
    private TextInputEditText editNombre, editEspecialidad, editTelefono, editEmail, editDireccion, editHorarios, editNotas;

    public static DoctorDialogFragment newInstance(DoctorEntity doctor) {
        DoctorDialogFragment fragment = new DoctorDialogFragment();
        Bundle args = new Bundle();
        if (doctor != null) {
            args.putString("nombre", doctor.nombre);
            args.putString("especialidad", doctor.especialidad);
            args.putString("telefono", doctor.telefono);
            args.putString("email", doctor.email);
            args.putString("direccion", doctor.direccion);
            args.putString("horarios", doctor.horarios);
            args.putString("notas", doctor.notasPaciente);
            args.putInt("id", doctor.id);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnDoctorGuardadaListener(OnDoctorGuardadaListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_doctor, null);

        inicializarVistas(view);
        cargarDatosExistente();
        configurarBotones(view);

        builder.setView(view)
                .setTitle(getArguments() != null && getArguments().containsKey("id") ?
                        "Editar Doctor" : "Nuevo Doctor");

        return builder.create();
    }

    private void inicializarVistas(View view) {
        editNombre = view.findViewById(R.id.edit_nombre);
        editEspecialidad = view.findViewById(R.id.edit_especialidad);
        editTelefono = view.findViewById(R.id.edit_telefono);
        editEmail = view.findViewById(R.id.edit_email);
        editDireccion = view.findViewById(R.id.edit_direccion);
        editHorarios = view.findViewById(R.id.edit_horarios);
        editNotas = view.findViewById(R.id.edit_notas);
    }

    private void cargarDatosExistente() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("nombre")) {
            editNombre.setText(args.getString("nombre", ""));
            editEspecialidad.setText(args.getString("especialidad", ""));
            editTelefono.setText(args.getString("telefono", ""));
            editEmail.setText(args.getString("email", ""));
            editDireccion.setText(args.getString("direccion", ""));
            editHorarios.setText(args.getString("horarios", ""));
            editNotas.setText(args.getString("notas", ""));
        }
    }

    private void configurarBotones(View view) {
        Button btnCancelar = view.findViewById(R.id.btn_cancelar);
        Button btnGuardar = view.findViewById(R.id.btn_guardar);

        btnCancelar.setOnClickListener(v -> dismiss());

        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                guardarDoctor();
                dismiss();
            }
        });
    }

    private boolean validarCampos() {
        boolean isValid = true;

        if (editNombre.getText().toString().trim().isEmpty()) {
            editNombre.setError("Ingrese el nombre del doctor");
            isValid = false;
        } else {
            editNombre.setError(null);
        }

        if (editEspecialidad.getText().toString().trim().isEmpty()) {
            editEspecialidad.setError("Ingrese la especialidad");
            isValid = false;
        } else {
            editEspecialidad.setError(null);
        }

        return isValid;
    }

    private void guardarDoctor() {
        String nombre = editNombre.getText().toString().trim();
        String especialidad = editEspecialidad.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String direccion = editDireccion.getText().toString().trim();
        String horarios = editHorarios.getText().toString().trim();
        String notas = editNotas.getText().toString().trim();

        Bundle args = getArguments();
        int id = args != null ? args.getInt("id", 0) : 0;

        DoctoresViewModel.Doctor nuevoDoctor = new DoctoresViewModel.Doctor(
                id, nombre, especialidad, telefono, email, direccion, horarios, false, 0f, notas
        );

        if (listener != null) {
            listener.onDoctorGuardada(nuevoDoctor);
        }
    }
}