package com.example.medicontrolpro.ui.citas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicontrolpro.data.CitaEntity;
import com.example.medicontrolpro.databinding.FragmentCitasBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CitasFragment extends Fragment {

    private FragmentCitasBinding binding;
    private CitasViewModel citasViewModel;
    private CitasAdapter citasAdapter;
    private List<CitaEntity> citasActuales = new ArrayList<>();
    private List<CitaEntity> citasFiltradas = new ArrayList<>();

    private String filtroEstado = null;
    private String filtroFecha = null;
    private String textoBusqueda = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        citasViewModel = new ViewModelProvider(this).get(CitasViewModel.class);

        binding = FragmentCitasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final RecyclerView recyclerView = binding.recyclerCitas;
        final TextView textSinCitas = binding.textSinCitas;
        final Button btnNuevaCita = binding.btnNuevaCita;
        final TextInputEditText searchEditText = binding.searchEditText;
        final Button btnFilterEstado = binding.btnFilterEstado;
        final Button btnFilterFecha = binding.btnFilterFecha;
        final ChipGroup chipGroupFilters = binding.chipGroupFilters;

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        citasAdapter = new CitasAdapter(new ArrayList<>(), getContext());
        recyclerView.setAdapter(citasAdapter);

        // Configurar click listener para items del RecyclerView
        citasAdapter.setOnItemClickListener(new CitasAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position < citasFiltradas.size()) {
                    CitaEntity cita = citasFiltradas.get(position);
                    Toast.makeText(getContext(), "Cita: " + cita.doctor, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemLongClick(int position) {
                Toast.makeText(getContext(), "Mantén presionado para opciones", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditClick(int position) {
                if (position < citasFiltradas.size()) {
                    CitaEntity cita = citasFiltradas.get(position);
                    mostrarDialogoEditarCita(cita);
                }
            }

            @Override
            public void onDeleteClick(int position) {
                if (position < citasFiltradas.size()) {
                    CitaEntity cita = citasFiltradas.get(position);
                    mostrarDialogoConfirmacionEliminacion(cita, position);
                }
            }
        });

        // Observar cambios en las citas
        citasViewModel.getAllCitas().observe(getViewLifecycleOwner(), citas -> {
            if (citas != null && !citas.isEmpty()) {
                citasActuales = citas;
                aplicarFiltros();
            } else {
                recyclerView.setVisibility(View.GONE);
                textSinCitas.setVisibility(View.VISIBLE);
                citasActuales.clear();
                citasFiltradas.clear();
                citasAdapter.setCitas(new ArrayList<>());
            }
        });

        // Configurar búsqueda
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBusqueda = s.toString().trim();
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Configurar filtro por estado
        btnFilterEstado.setOnClickListener(v -> mostrarDialogoFiltroEstado());

        // Configurar filtro por fecha
        btnFilterFecha.setOnClickListener(v -> mostrarDialogoFiltroFecha());

        // Configurar botón de nueva cita
        btnNuevaCita.setOnClickListener(v -> {
            mostrarDialogoNuevaCita();
        });

        return root;
    }

    private void aplicarFiltros() {
        List<CitaEntity> citasFiltradasTemp = new ArrayList<>(citasActuales);

        // Aplicar filtro de búsqueda
        if (!textoBusqueda.isEmpty()) {
            List<CitaEntity> citasBusqueda = new ArrayList<>();
            for (CitaEntity cita : citasFiltradasTemp) {
                if (cita.doctor.toLowerCase().contains(textoBusqueda.toLowerCase()) ||
                        cita.especialidad.toLowerCase().contains(textoBusqueda.toLowerCase()) ||
                        cita.motivo.toLowerCase().contains(textoBusqueda.toLowerCase()) ||
                        cita.estado.toLowerCase().contains(textoBusqueda.toLowerCase())) {
                    citasBusqueda.add(cita);
                }
            }
            citasFiltradasTemp = citasBusqueda;
        }

        // Aplicar filtro de estado (puede ser múltiple)
        if (filtroEstado != null && !filtroEstado.isEmpty()) {
            List<CitaEntity> citasEstado = new ArrayList<>();
            String[] estadosFiltro = filtroEstado.split(",");

            for (CitaEntity cita : citasFiltradasTemp) {
                for (String estado : estadosFiltro) {
                    if (cita.estado.equalsIgnoreCase(estado.trim())) {
                        citasEstado.add(cita);
                        break;
                    }
                }
            }
            citasFiltradasTemp = citasEstado;
        }

        // Aplicar filtro de fecha
        if (filtroFecha != null && !filtroFecha.isEmpty()) {
            List<CitaEntity> citasFecha = new ArrayList<>();
            for (CitaEntity cita : citasFiltradasTemp) {
                if (cita.fecha.equals(filtroFecha)) {
                    citasFecha.add(cita);
                }
            }
            citasFiltradasTemp = citasFecha;
        }

        citasFiltradas = citasFiltradasTemp;
        actualizarVistaCitas();
        actualizarChipsFiltros();
    }

    private void actualizarVistaCitas() {
        if (citasFiltradas.isEmpty()) {
            binding.recyclerCitas.setVisibility(View.GONE);
            binding.textSinCitas.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerCitas.setVisibility(View.VISIBLE);
            binding.textSinCitas.setVisibility(View.GONE);
            List<CitasViewModel.Cita> citasParaAdapter = convertirCitas(citasFiltradas);
            citasAdapter.setCitas(citasParaAdapter);
        }
    }

    private void actualizarChipsFiltros() {
        binding.chipGroupFilters.removeAllViews();

        boolean tieneFiltros = false;

        if (filtroEstado != null) {
            String[] estados = filtroEstado.split(",");
            for (String estado : estados) {
                if (!estado.trim().isEmpty()) {
                    agregarChipFiltro("Estado: " + estado.trim(), () -> {
                        // Remover solo este estado específico
                        String nuevosEstados = filtroEstado.replace(estado.trim(), "")
                                .replace(",,", ",")
                                .replaceAll("^,|,$", "")
                                .trim();

                        if (nuevosEstados.isEmpty()) {
                            filtroEstado = null;
                        } else {
                            filtroEstado = nuevosEstados;
                        }
                        aplicarFiltros();
                    });
                    tieneFiltros = true;
                }
            }
        }

        if (filtroFecha != null) {
            agregarChipFiltro("Fecha: " + filtroFecha, () -> {
                filtroFecha = null;
                aplicarFiltros();
            });
            tieneFiltros = true;
        }

        binding.chipGroupFilters.setVisibility(tieneFiltros ? View.VISIBLE : View.GONE);
    }

    private void agregarChipFiltro(String texto, Runnable onCloseClick) {
        Chip chip = new Chip(requireContext());
        chip.setText(texto);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> onCloseClick.run());

        // Configuración básica del chip
        chip.setChipBackgroundColorResource(android.R.color.transparent);
        chip.setChipStrokeWidth(2f);

        binding.chipGroupFilters.addView(chip);
    }

    private void mostrarDialogoFiltroEstado() {
        String[] estados = {"Pendiente", "Confirmada", "Cancelada", "Completada"};
        boolean[] estadosSeleccionados = new boolean[estados.length];

        // Marcar el estado actual si existe
        if (filtroEstado != null) {
            String[] estadosActuales = filtroEstado.split(",");
            for (int i = 0; i < estados.length; i++) {
                for (String estadoActual : estadosActuales) {
                    if (estados[i].equalsIgnoreCase(estadoActual.trim())) {
                        estadosSeleccionados[i] = true;
                        break;
                    }
                }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar estado para filtrar")
                .setMultiChoiceItems(estados, estadosSeleccionados, (dialog, which, isChecked) -> {
                    estadosSeleccionados[which] = isChecked;
                })
                .setPositiveButton("Aplicar", (dialog, which) -> {
                    // Obtener los estados seleccionados
                    StringBuilder estadosFiltro = new StringBuilder();
                    for (int i = 0; i < estados.length; i++) {
                        if (estadosSeleccionados[i]) {
                            if (estadosFiltro.length() > 0) {
                                estadosFiltro.append(",");
                            }
                            estadosFiltro.append(estados[i]);
                        }
                    }

                    if (estadosFiltro.length() > 0) {
                        filtroEstado = estadosFiltro.toString();
                    } else {
                        filtroEstado = null;
                    }
                    aplicarFiltros();
                })
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Limpiar todos", (dialog, which) -> {
                    filtroEstado = null;
                    aplicarFiltros();
                })
                .show();
    }

    private void mostrarDialogoFiltroFecha() {
        // Crear el diálogo programáticamente en lugar de usar XML
        TextInputEditText editFecha = new TextInputEditText(requireContext());
        editFecha.setHint("Seleccionar fecha");
        editFecha.setFocusable(false);

        // Configurar el selector de fecha
        editFecha.setOnClickListener(v -> mostrarDatePicker(editFecha));

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 30, 50, 30);
        container.addView(editFecha);

        new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar fecha para filtrar")
                .setView(container)
                .setPositiveButton("Aplicar filtro", (dialog, which) -> {
                    String fechaSeleccionada = editFecha.getText().toString().trim();
                    if (!fechaSeleccionada.isEmpty()) {
                        filtroFecha = fechaSeleccionada;
                        aplicarFiltros();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Limpiar filtro", (dialog, which) -> {
                    filtroFecha = null;
                    aplicarFiltros();
                })
                .show();
    }

    private void mostrarDatePicker(TextInputEditText editFecha) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String fechaFormateada = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    editFecha.setText(fechaFormateada);
                },
                year, month, day
        );
        datePickerDialog.show();
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
            // Verificar si el estado cambió a "Completada"
            boolean seCompleto = "Completada".equalsIgnoreCase(citaEditada.getEstado()) &&
                    !"Completada".equalsIgnoreCase(cita.estado);

            cita.doctor = citaEditada.getDoctor();
            cita.especialidad = citaEditada.getEspecialidad();
            cita.fecha = citaEditada.getFecha();
            cita.hora = citaEditada.getHora();
            cita.motivo = citaEditada.getMotivo();
            cita.estado = citaEditada.getEstado();

            citasViewModel.update(cita);

            // Crear expediente automáticamente si se completó la cita
            if (seCompleto) {
                citasViewModel.crearExpedienteDesdeCita(cita);
                Toast.makeText(getContext(), "Cita completada y expediente creado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Cita actualizada exitosamente", Toast.LENGTH_SHORT).show();
            }
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
        Toast.makeText(getContext(), "Cita eliminada", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}