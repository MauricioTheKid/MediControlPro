package com.example.medicontrolpro.ui.expediente;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicontrolpro.data.ExpedienteEntity;
import com.example.medicontrolpro.databinding.FragmentExpedienteBinding;

import java.util.ArrayList;
import java.util.List;

public class ExpedienteFragment extends Fragment {

    private FragmentExpedienteBinding binding;
    private ExpedienteViewModel expedienteViewModel;
    private ExpedienteAdapter expedienteAdapter;
    private List<ExpedienteEntity> expedientesActuales = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        expedienteViewModel = new ViewModelProvider(this).get(ExpedienteViewModel.class);

        binding = FragmentExpedienteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textExpediente;
        final RecyclerView recyclerView = binding.recyclerExpedientes;
        final TextView textSinExpedientes = binding.textSinExpedientes;

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expedienteAdapter = new ExpedienteAdapter(new ArrayList<>(), getContext());
        recyclerView.setAdapter(expedienteAdapter);

        // Configurar click listener para items del RecyclerView
        expedienteAdapter.setOnItemClickListener(new ExpedienteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position < expedientesActuales.size()) {
                    ExpedienteEntity expediente = expedientesActuales.get(position);
                    mostrarDialogoDetalleExpediente(expediente);
                }
            }

            @Override
            public void onEditClick(int position) {
                if (position < expedientesActuales.size()) {
                    ExpedienteEntity expediente = expedientesActuales.get(position);
                    mostrarDialogoEditarExpediente(expediente);
                }
            }

            @Override
            public void onDeleteClick(int position) {
                if (position < expedientesActuales.size()) {
                    ExpedienteEntity expediente = expedientesActuales.get(position);
                    mostrarDialogoConfirmacionEliminacion(expediente, position);
                }
            }
        });

        // Observar cambios en los expedientes
        expedienteViewModel.getAllExpedientes().observe(getViewLifecycleOwner(), expedientes -> {
            if (expedientes != null && !expedientes.isEmpty()) {
                expedientesActuales = expedientes;
                recyclerView.setVisibility(View.VISIBLE);
                textSinExpedientes.setVisibility(View.GONE);
                List<ExpedienteViewModel.Expediente> expedientesParaAdapter = convertirExpedientes(expedientes);
                expedienteAdapter.setExpedientes(expedientesParaAdapter);
            } else {
                recyclerView.setVisibility(View.GONE);
                textSinExpedientes.setVisibility(View.VISIBLE);
                expedientesActuales.clear();
            }
        });

        // Observar el texto del ViewModel
        expedienteViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    private List<ExpedienteViewModel.Expediente> convertirExpedientes(List<ExpedienteEntity> expedientesEntity) {
        List<ExpedienteViewModel.Expediente> expedientes = new ArrayList<>();
        for (ExpedienteEntity entity : expedientesEntity) {
            ExpedienteViewModel.Expediente expediente = new ExpedienteViewModel.Expediente(
                    entity.id,
                    entity.citaId,
                    entity.doctor,
                    entity.especialidad,
                    entity.fechaCita,
                    entity.diagnostico,
                    entity.tratamiento,
                    entity.medicamentos,
                    entity.notas,
                    entity.fechaCreacion,
                    entity.fechaModificacion
            );
            expedientes.add(expediente);
        }
        return expedientes;
    }

    private void mostrarDialogoDetalleExpediente(ExpedienteEntity expediente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Detalles del Expediente")
                .setMessage("Doctor: " + expediente.doctor + "\n" +
                        "Especialidad: " + expediente.especialidad + "\n" +
                        "Fecha de Cita: " + expediente.fechaCita + "\n" +
                        "Diagnóstico: " + (expediente.diagnostico.isEmpty() ? "No especificado" : expediente.diagnostico) + "\n" +
                        "Tratamiento: " + (expediente.tratamiento.isEmpty() ? "No especificado" : expediente.tratamiento) + "\n" +
                        "Medicamentos: " + (expediente.medicamentos.isEmpty() ? "No especificados" : expediente.medicamentos) + "\n" +
                        "Notas: " + expediente.notas + "\n" +
                        "Creado: " + expediente.fechaCreacion + "\n" +
                        "Modificado: " + expediente.fechaModificacion)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void mostrarDialogoEditarExpediente(ExpedienteEntity expediente) {
        EditarExpedienteDialogFragment dialog = EditarExpedienteDialogFragment.newInstance(expediente);
        dialog.setOnExpedienteGuardadoListener(expedienteEditado -> {
            // Actualizar el expediente existente
            expediente.diagnostico = expedienteEditado.getDiagnostico();
            expediente.tratamiento = expedienteEditado.getTratamiento();
            expediente.medicamentos = expedienteEditado.getMedicamentos();
            expediente.notas = expedienteEditado.getNotas();
            expediente.fechaModificacion = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

            expedienteViewModel.update(expediente);
            Toast.makeText(getContext(), "Expediente actualizado exitosamente", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "EditarExpedienteDialog");
    }

    private void mostrarDialogoConfirmacionEliminacion(ExpedienteEntity expediente, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar el expediente del Dr. " + expediente.doctor + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarExpediente(expediente, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarExpediente(ExpedienteEntity expediente, int position) {
        expedienteViewModel.delete(expediente);
        Toast.makeText(getContext(), "Expediente eliminado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}