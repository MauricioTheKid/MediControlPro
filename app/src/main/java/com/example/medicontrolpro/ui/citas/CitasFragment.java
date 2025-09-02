package com.example.medicontrolpro.ui.citas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicontrolpro.data.CitaEntity;
import com.example.medicontrolpro.databinding.FragmentCitasBinding;

import java.util.ArrayList;
import java.util.List;

public class CitasFragment extends Fragment {

    private FragmentCitasBinding binding;
    private CitasViewModel citasViewModel;
    private CitasAdapter citasAdapter;
    private List<CitaEntity> citasActuales = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        citasViewModel = new ViewModelProvider(this).get(CitasViewModel.class);

        binding = FragmentCitasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textCitas;
        final RecyclerView recyclerView = binding.recyclerCitas;
        final TextView textSinCitas = binding.textSinCitas;
        final Button btnNuevaCita = binding.btnNuevaCita;

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        citasAdapter = new CitasAdapter(new ArrayList<>(), getContext());
        recyclerView.setAdapter(citasAdapter);

        // Configurar click listener para items del RecyclerView
        citasAdapter.setOnItemClickListener(new CitasAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position < citasActuales.size()) {
                    CitaEntity cita = citasActuales.get(position);
                    Toast.makeText(getContext(), "Cita: " + cita.doctor, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemLongClick(int position) {
                Toast.makeText(getContext(), "Mantén presionado para opciones", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditClick(int position) {
                if (position < citasActuales.size()) {
                    CitaEntity cita = citasActuales.get(position);
                    mostrarDialogoEditarCita(cita);
                }
            }

            @Override
            public void onDeleteClick(int position) {
                if (position < citasActuales.size()) {
                    CitaEntity cita = citasActuales.get(position);
                    mostrarDialogoConfirmacionEliminacion(cita, position);
                }
            }
        });

        // Observar cambios en las citas
        citasViewModel.getAllCitas().observe(getViewLifecycleOwner(), citas -> {
            if (citas != null && !citas.isEmpty()) {
                citasActuales = citas;
                recyclerView.setVisibility(View.VISIBLE);
                textSinCitas.setVisibility(View.GONE);
                List<CitasViewModel.Cita> citasParaAdapter = convertirCitas(citas);
                citasAdapter.setCitas(citasParaAdapter);
            } else {
                recyclerView.setVisibility(View.GONE);
                textSinCitas.setVisibility(View.VISIBLE);
                citasActuales.clear();
            }
        });

        // Observar el texto del ViewModel
        citasViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Configurar botón de nueva cita
        btnNuevaCita.setOnClickListener(v -> {
            mostrarDialogoNuevaCita();
        });

        return root;
    }

    private List<CitasViewModel.Cita> convertirCitas(List<CitaEntity> citasEntity) {
        List<CitasViewModel.Cita> citas = new ArrayList<>();
        for (CitaEntity entity : citasEntity) {
            CitasViewModel.Cita cita = new CitasViewModel.Cita(
                    entity.doctor,
                    entity.especialidad,
                    entity.fecha,
                    entity.hora,
                    entity.motivo,
                    entity.estado
            );
            citas.add(cita);
        }
        return citas;
    }

    private void mostrarDialogoNuevaCita() {
        NuevaCitaDialogFragment dialog = new NuevaCitaDialogFragment();
        dialog.setOnCitaGuardadaListener(nuevaCita -> {
            CitaEntity citaEntity = new CitaEntity();
            citaEntity.doctor = nuevaCita.getDoctor();
            citaEntity.especialidad = nuevaCita.getEspecialidad();
            citaEntity.fecha = nuevaCita.getFecha();
            citaEntity.hora = nuevaCita.getHora();
            citaEntity.motivo = nuevaCita.getMotivo();
            citaEntity.estado = nuevaCita.getEstado();

            citasViewModel.insert(citaEntity);
            Toast.makeText(getContext(), "Cita agregada exitosamente", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "NuevaCitaDialog");
    }

    private void mostrarDialogoEditarCita(CitaEntity cita) {
        NuevaCitaDialogFragment dialog = NuevaCitaDialogFragment.newInstance(cita);
        dialog.setOnCitaGuardadaListener(citaEditada -> {
            // Actualizar la cita existente
            cita.doctor = citaEditada.getDoctor();
            cita.especialidad = citaEditada.getEspecialidad();
            cita.fecha = citaEditada.getFecha();
            cita.hora = citaEditada.getHora();
            cita.motivo = citaEditada.getMotivo();
            cita.estado = citaEditada.getEstado();

            citasViewModel.update(cita);
            Toast.makeText(getContext(), "Cita actualizada exitosamente", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "EditarCitaDialog");
    }

    private void mostrarDialogoConfirmacionEliminacion(CitaEntity cita, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar la cita con el Dr. " + cita.doctor + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarCita(cita, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarCita(CitaEntity cita, int position) {
        citasViewModel.delete(cita);
        // No llamar a citasAdapter.removeCita(position) aquí porque
        // el LiveData se actualizará automáticamente y refrescará el adapter
        Toast.makeText(getContext(), "Cita eliminada", Toast.LENGTH_SHORT).show();
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}