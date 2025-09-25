package com.example.medicontrolpro.ui.expediente;

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
import com.example.medicontrolpro.data.ExpedienteEntity;
import com.google.android.material.textfield.TextInputEditText;

public class EditarExpedienteDialogFragment extends DialogFragment {

    public interface OnExpedienteGuardadoListener {
        void onExpedienteGuardado(ExpedienteViewModel.Expediente expediente);
    }

    private OnExpedienteGuardadoListener listener;
    private TextInputEditText editDiagnostico, editTratamiento, editMedicamentos, editNotas;

    public static EditarExpedienteDialogFragment newInstance(ExpedienteEntity expediente) {
        EditarExpedienteDialogFragment fragment = new EditarExpedienteDialogFragment();
        Bundle args = new Bundle();
        if (expediente != null) {
            args.putInt("id", expediente.id);
            args.putString("diagnostico", expediente.diagnostico);
            args.putString("tratamiento", expediente.tratamiento);
            args.putString("medicamentos", expediente.medicamentos);
            args.putString("notas", expediente.notas);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnExpedienteGuardadoListener(OnExpedienteGuardadoListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_editar_expediente, null);

        inicializarVistas(view);
        cargarDatosExistente();
        configurarBotones(view);

        builder.setView(view)
                .setTitle("Editar Expediente Médico");

        return builder.create();
    }

    private void inicializarVistas(View view) {
        editDiagnostico = view.findViewById(R.id.edit_diagnostico);
        editTratamiento = view.findViewById(R.id.edit_tratamiento);
        editMedicamentos = view.findViewById(R.id.edit_medicamentos);
        editNotas = view.findViewById(R.id.edit_notas);
    }

    private void cargarDatosExistente() {
        Bundle args = getArguments();
        if (args != null) {
            editDiagnostico.setText(args.getString("diagnostico", ""));
            editTratamiento.setText(args.getString("tratamiento", ""));
            editMedicamentos.setText(args.getString("medicamentos", ""));
            editNotas.setText(args.getString("notas", ""));
        }
    }

    private void configurarBotones(View view) {
        Button btnCancelar = view.findViewById(R.id.btn_cancelar);
        Button btnGuardar = view.findViewById(R.id.btn_guardar);

        btnCancelar.setOnClickListener(v -> dismiss());

        btnGuardar.setOnClickListener(v -> {
            guardarExpediente();
            dismiss();
        });
    }

    private void guardarExpediente() {
        String diagnostico = editDiagnostico.getText().toString().trim();
        String tratamiento = editTratamiento.getText().toString().trim();
        String medicamentos = editMedicamentos.getText().toString().trim();
        String notas = editNotas.getText().toString().trim();

        Bundle args = getArguments();
        int id = args != null ? args.getInt("id", 0) : 0;

        ExpedienteViewModel.Expediente expedienteEditado = new ExpedienteViewModel.Expediente(
                id, 0, "", "", "", diagnostico, tratamiento, medicamentos, notas, "", ""
        );

        if (listener != null) {
            listener.onExpedienteGuardado(expedienteEditado);
        }
    }
}