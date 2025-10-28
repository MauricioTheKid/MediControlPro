package com.example.medicontrolpro.ui.doctores;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.example.medicontrolpro.data.DoctorEntity;
import com.example.medicontrolpro.databinding.FragmentDoctoresBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class DoctoresFragment extends Fragment {

    private FragmentDoctoresBinding binding;
    private DoctoresViewModel doctoresViewModel;
    private DoctoresAdapter doctoresAdapter;

    private static final String TAG = "DoctoresFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        doctoresViewModel = new ViewModelProvider(requireActivity()).get(DoctoresViewModel.class);

        binding = FragmentDoctoresBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final RecyclerView recyclerView = binding.recyclerDoctores;
        final TextView textSinDoctores = binding.textSinDoctores;
        final TextInputEditText searchEditText = binding.searchEditText;

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        doctoresAdapter = new DoctoresAdapter(new ArrayList<>(), getContext());
        recyclerView.setAdapter(doctoresAdapter);

        // Configurar click listener para items del RecyclerView
        doctoresAdapter.setOnItemClickListener(new DoctoresAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                DoctoresViewModel.Doctor doctor = doctoresAdapter.getDoctorAtPosition(position);
                if (doctor != null) {
                    mostrarDialogoDetalleDoctor(doctor);
                }
            }

            @Override
            public void onEditClick(int position) {
                DoctoresViewModel.Doctor doctor = doctoresAdapter.getDoctorAtPosition(position);
                if (doctor != null) {
                    mostrarDialogoEditarDoctor(doctor);
                }
            }

            @Override
            public void onDeleteClick(int position) {
                DoctoresViewModel.Doctor doctor = doctoresAdapter.getDoctorAtPosition(position);
                if (doctor != null) {
                    mostrarDialogoConfirmacionEliminacion(doctor);
                }
            }

            @Override
            public void onFavoritoClick(int position, boolean esFavorito) {
                DoctoresViewModel.Doctor doctor = doctoresAdapter.getDoctorAtPosition(position);
                if (doctor != null) {
                    // Actualizar el favorito en la base de datos
                    doctoresViewModel.updateFavorito(doctor.getId(), esFavorito);
                    String mensaje = esFavorito ? "Agregado a favoritos" : "Removido de favoritos";
                    Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Configurar búsqueda
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    // Mostrar todos los doctores
                    doctoresViewModel.getAllDoctores().observe(getViewLifecycleOwner(), doctores -> {
                        if (doctores != null) {
                            if (doctores.isEmpty()) {
                                recyclerView.setVisibility(View.GONE);
                                textSinDoctores.setVisibility(View.VISIBLE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                textSinDoctores.setVisibility(View.GONE);
                            }
                            List<DoctoresViewModel.Doctor> doctoresParaAdapter = convertirDoctores(doctores);
                            doctoresAdapter.setDoctores(doctoresParaAdapter);
                        }
                    });
                } else {
                    // Buscar doctores
                    doctoresViewModel.searchDoctores(query).observe(getViewLifecycleOwner(), doctores -> {
                        if (doctores != null) {
                            if (doctores.isEmpty()) {
                                recyclerView.setVisibility(View.GONE);
                                textSinDoctores.setVisibility(View.VISIBLE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                textSinDoctores.setVisibility(View.GONE);
                            }
                            List<DoctoresViewModel.Doctor> doctoresParaAdapter = convertirDoctores(doctores);
                            doctoresAdapter.setDoctores(doctoresParaAdapter);
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Observar cambios en los doctores
        doctoresViewModel.getAllDoctores().observe(getViewLifecycleOwner(), doctores -> {
            if (doctores != null && !doctores.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                textSinDoctores.setVisibility(View.GONE);
                List<DoctoresViewModel.Doctor> doctoresParaAdapter = convertirDoctores(doctores);
                doctoresAdapter.setDoctores(doctoresParaAdapter);
            } else {
                recyclerView.setVisibility(View.GONE);
                textSinDoctores.setVisibility(View.VISIBLE);
                doctoresAdapter.setDoctores(new ArrayList<>());
            }
        });

        // Configurar botón de nuevo doctor
        binding.btnNuevoDoctor.setOnClickListener(v -> {
            mostrarDialogoNuevoDoctor();
        });

        return root;
    }

    private List<DoctoresViewModel.Doctor> convertirDoctores(List<DoctorEntity> doctoresEntity) {
        List<DoctoresViewModel.Doctor> doctores = new ArrayList<>();
        for (DoctorEntity entity : doctoresEntity) {
            DoctoresViewModel.Doctor doctor = new DoctoresViewModel.Doctor(
                    entity.id,
                    entity.nombre,
                    entity.especialidad,
                    entity.telefono,
                    entity.email,
                    entity.direccion,
                    entity.horarios,
                    entity.esFavorito,
                    entity.calificacion,
                    entity.notasPaciente
            );
            // ✅ ASIGNAR FOTO PATH CORRECTAMENTE
            doctor.setFotoPath(entity.fotoPath);
            doctores.add(doctor);
        }
        return doctores;
    }

    private void mostrarDialogoDetalleDoctor(DoctoresViewModel.Doctor doctor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Detalles del Doctor")
                .setMessage("Nombre: " + doctor.getNombre() + "\n" +
                        "Especialidad: " + doctor.getEspecialidad() + "\n" +
                        "Teléfono: " + (doctor.getTelefono().isEmpty() ? "No especificado" : doctor.getTelefono()) + "\n" +
                        "Email: " + (doctor.getEmail().isEmpty() ? "No especificado" : doctor.getEmail()) + "\n" +
                        "Dirección: " + (doctor.getDireccion().isEmpty() ? "No especificada" : doctor.getDireccion()) + "\n" +
                        "Horarios: " + (doctor.getHorarios().isEmpty() ? "No especificados" : doctor.getHorarios()) + "\n" +
                        "Notas: " + (doctor.getNotasPaciente().isEmpty() ? "Sin notas" : doctor.getNotasPaciente()))
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Llamar", (dialog, which) -> {
                    if (doctor.getTelefono() != null && !doctor.getTelefono().isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + doctor.getTelefono()));
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "No hay número de teléfono", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void mostrarDialogoNuevoDoctor() {
        DoctorDialogFragment dialog = new DoctorDialogFragment();
        dialog.setOnDoctorGuardadaListener(nuevoDoctor -> {
            DoctorEntity doctorEntity = new DoctorEntity();
            doctorEntity.nombre = nuevoDoctor.getNombre();
            doctorEntity.especialidad = nuevoDoctor.getEspecialidad();
            doctorEntity.telefono = nuevoDoctor.getTelefono();
            doctorEntity.email = nuevoDoctor.getEmail();
            doctorEntity.direccion = nuevoDoctor.getDireccion();
            doctorEntity.horarios = nuevoDoctor.getHorarios();
            doctorEntity.notasPaciente = nuevoDoctor.getNotasPaciente();
            doctorEntity.fotoPath = nuevoDoctor.getFotoPath(); // ✅ GUARDAR FOTO PATH CORRECTAMENTE

            doctoresViewModel.insert(doctorEntity);
            Log.d(TAG, "✅✅✅ NUEVO DOCTOR GUARDADO:");
            Log.d(TAG, "   - Nombre: " + doctorEntity.nombre);
            Log.d(TAG, "   - Foto: " + doctorEntity.fotoPath);
            Log.d(TAG, "   - Notas: " + doctorEntity.notasPaciente);

            Toast.makeText(getContext(), "Doctor agregado exitosamente", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "NuevoDoctorDialog");
    }

    private void mostrarDialogoEditarDoctor(DoctoresViewModel.Doctor doctor) {
        // Convertir Doctor a DoctorEntity para editar
        DoctorEntity doctorEntity = new DoctorEntity();
        doctorEntity.id = doctor.getId();
        doctorEntity.nombre = doctor.getNombre();
        doctorEntity.especialidad = doctor.getEspecialidad();
        doctorEntity.telefono = doctor.getTelefono();
        doctorEntity.email = doctor.getEmail();
        doctorEntity.direccion = doctor.getDireccion();
        doctorEntity.horarios = doctor.getHorarios();
        doctorEntity.notasPaciente = doctor.getNotasPaciente();
        doctorEntity.esFavorito = doctor.isEsFavorito();
        doctorEntity.fotoPath = doctor.getFotoPath(); // ✅ GUARDAR FOTO PATH CORRECTAMENTE

        DoctorDialogFragment dialog = DoctorDialogFragment.newInstance(doctorEntity);
        dialog.setOnDoctorGuardadaListener(doctorEditado -> {
            // Actualizar la entidad existente
            doctorEntity.nombre = doctorEditado.getNombre();
            doctorEntity.especialidad = doctorEditado.getEspecialidad();
            doctorEntity.telefono = doctorEditado.getTelefono();
            doctorEntity.email = doctorEditado.getEmail();
            doctorEntity.direccion = doctorEditado.getDireccion();
            doctorEntity.horarios = doctorEditado.getHorarios();
            doctorEntity.notasPaciente = doctorEditado.getNotasPaciente();
            doctorEntity.fotoPath = doctorEditado.getFotoPath(); // ✅ ACTUALIZAR FOTO PATH CORRECTAMENTE

            doctoresViewModel.update(doctorEntity);
            Log.d(TAG, "✅✅✅ DOCTOR ACTUALIZADO:");
            Log.d(TAG, "   - Nombre: " + doctorEntity.nombre);
            Log.d(TAG, "   - Foto: " + doctorEntity.fotoPath);
            Log.d(TAG, "   - Notas: " + doctorEntity.notasPaciente);

            Toast.makeText(getContext(), "Doctor actualizado exitosamente", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "EditarDoctorDialog");
    }

    private void mostrarDialogoConfirmacionEliminacion(DoctoresViewModel.Doctor doctor) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar al Dr. " + doctor.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Convertir y eliminar
                    DoctorEntity doctorEntity = new DoctorEntity();
                    doctorEntity.id = doctor.getId();
                    doctoresViewModel.delete(doctorEntity);
                    Toast.makeText(getContext(), "Doctor eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}