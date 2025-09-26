package com.example.medicontrolpro.ui.perfil;

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
import com.google.android.material.textfield.TextInputEditText;

public class EditarPerfilDialogFragment extends DialogFragment {

    public interface OnPerfilActualizadoListener {
        void onPerfilActualizado(PerfilViewModel.Paciente paciente);
    }

    private OnPerfilActualizadoListener listener;
    private PerfilViewModel.Paciente pacienteActual;
    private TextInputEditText editNombre, editEmail, editFechaNacimiento, editGenero,
            editTelefono, editDireccion, editTipoSangre, editAlergias,
            editCondicionesMedicas, editMedicamentos;

    public void setPacienteActual(PerfilViewModel.Paciente paciente) {
        this.pacienteActual = paciente;
    }

    public void setOnPerfilActualizadoListener(OnPerfilActualizadoListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_editar_perfil, null);

        inicializarVistas(view);
        cargarDatosActuales();
        configurarBotones(view);

        builder.setView(view)
                .setTitle("Editar Perfil");

        return builder.create();
    }

    private void inicializarVistas(View view) {
        editNombre = view.findViewById(R.id.edit_nombre);
        editEmail = view.findViewById(R.id.edit_email);
        editFechaNacimiento = view.findViewById(R.id.edit_fecha_nacimiento);
        editGenero = view.findViewById(R.id.edit_genero);
        editTelefono = view.findViewById(R.id.edit_telefono);
        editDireccion = view.findViewById(R.id.edit_direccion);
        editTipoSangre = view.findViewById(R.id.edit_tipo_sangre);
        editAlergias = view.findViewById(R.id.edit_alergias);
        editCondicionesMedicas = view.findViewById(R.id.edit_condiciones_medicas);
        editMedicamentos = view.findViewById(R.id.edit_medicamentos);
    }

    private void cargarDatosActuales() {
        if (pacienteActual != null) {
            editNombre.setText(pacienteActual.getNombreCompleto());
            editEmail.setText(pacienteActual.getEmail());
            editFechaNacimiento.setText(pacienteActual.getFechaNacimiento());
            editGenero.setText(pacienteActual.getGenero());
            editTelefono.setText(pacienteActual.getTelefono());
            editDireccion.setText(pacienteActual.getDireccion());
            editTipoSangre.setText(pacienteActual.getTipoSangre());
            editAlergias.setText(pacienteActual.getAlergias());
            editCondicionesMedicas.setText(pacienteActual.getCondicionesMedicas());
            editMedicamentos.setText(pacienteActual.getMedicamentos());
        }
    }

    private void configurarBotones(View view) {
        Button btnCancelar = view.findViewById(R.id.btn_cancelar);
        Button btnGuardar = view.findViewById(R.id.btn_guardar);

        btnCancelar.setOnClickListener(v -> dismiss());

        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                guardarCambios();
                dismiss();
            }
        });
    }

    private boolean validarCampos() {
        boolean isValid = true;

        if (editNombre.getText().toString().trim().isEmpty()) {
            editNombre.setError("Ingrese su nombre completo");
            isValid = false;
        }

        if (editEmail.getText().toString().trim().isEmpty()) {
            editEmail.setError("Ingrese su email");
            isValid = false;
        }

        return isValid;
    }

    private void guardarCambios() {
        int id = pacienteActual != null ? pacienteActual.getId() : 0;
        String fotoPath = pacienteActual != null ? pacienteActual.getFotoPath() : "";

        PerfilViewModel.Paciente pacienteActualizado = new PerfilViewModel.Paciente(
                id,
                editNombre.getText().toString().trim(),
                editEmail.getText().toString().trim(),
                editFechaNacimiento.getText().toString().trim(),
                editGenero.getText().toString().trim(),
                editTelefono.getText().toString().trim(),
                editDireccion.getText().toString().trim(),
                editTipoSangre.getText().toString().trim(),
                editAlergias.getText().toString().trim(),
                editCondicionesMedicas.getText().toString().trim(),
                editMedicamentos.getText().toString().trim(),
                fotoPath
        );

        if (listener != null) {
            listener.onPerfilActualizado(pacienteActualizado);
        }
    }
}