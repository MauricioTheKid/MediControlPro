package com.example.medicontrolpro.ui.expediente;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicontrolpro.R;

import java.util.List;

public class ExpedienteAdapter extends RecyclerView.Adapter<ExpedienteAdapter.ExpedienteViewHolder> {

    private List<ExpedienteViewModel.Expediente> expedientes;
    private OnItemClickListener itemClickListener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public ExpedienteAdapter(List<ExpedienteViewModel.Expediente> expedientes, Context context) {
        this.expedientes = expedientes;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public ExpedienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expediente, parent, false);
        return new ExpedienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpedienteViewHolder holder, int position) {
        ExpedienteViewModel.Expediente expediente = expedientes.get(position);
        holder.bind(expediente, itemClickListener);
    }

    @Override
    public int getItemCount() {
        return expedientes != null ? expedientes.size() : 0;
    }

    public void setExpedientes(List<ExpedienteViewModel.Expediente> expedientes) {
        this.expedientes = expedientes;
        notifyDataSetChanged();
    }

    public ExpedienteViewModel.Expediente getExpedienteAtPosition(int position) {
        if (position >= 0 && position < expedientes.size()) {
            return expedientes.get(position);
        }
        return null;
    }

    public void removeExpediente(int position) {
        if (position >= 0 && position < expedientes.size()) {
            expedientes.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, expedientes.size());
        }
    }

    public void updateExpediente(int position, ExpedienteViewModel.Expediente expedienteActualizado) {
        if (position >= 0 && position < expedientes.size()) {
            expedientes.set(position, expedienteActualizado);
            notifyItemChanged(position);
        }
    }

    public void addExpediente(ExpedienteViewModel.Expediente nuevoExpediente) {
        expedientes.add(nuevoExpediente);
        notifyItemInserted(expedientes.size() - 1);
    }

    static class ExpedienteViewHolder extends RecyclerView.ViewHolder {
        private TextView textDoctor, textEspecialidad, textFechaCita, textDiagnostico, textTratamiento;
        private TextView textMedicamentos, textNotas, textFechaCreacion;
        private ImageButton btnEdit, btnDelete;

        public ExpedienteViewHolder(@NonNull View itemView) {
            super(itemView);
            textDoctor = itemView.findViewById(R.id.text_doctor);
            textEspecialidad = itemView.findViewById(R.id.text_especialidad);
            textFechaCita = itemView.findViewById(R.id.text_fecha_cita);
            textDiagnostico = itemView.findViewById(R.id.text_diagnostico);
            textTratamiento = itemView.findViewById(R.id.text_tratamiento);
            textMedicamentos = itemView.findViewById(R.id.text_medicamentos);
            textNotas = itemView.findViewById(R.id.text_notas);
            textFechaCreacion = itemView.findViewById(R.id.text_fecha_creacion);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(ExpedienteViewModel.Expediente expediente, OnItemClickListener listener) {
            textDoctor.setText("Dr. " + expediente.getDoctor());
            textEspecialidad.setText(expediente.getEspecialidad());
            textFechaCita.setText("Cita: " + expediente.getFechaCita());
            textDiagnostico.setText("Diagnóstico: " + (expediente.getDiagnostico().isEmpty() ? "No especificado" : expediente.getDiagnostico()));
            textTratamiento.setText("Tratamiento: " + (expediente.getTratamiento().isEmpty() ? "No especificado" : expediente.getTratamiento()));
            textMedicamentos.setText("Medicamentos: " + (expediente.getMedicamentos().isEmpty() ? "No especificados" : expediente.getMedicamentos()));
            textNotas.setText("Notas: " + expediente.getNotas());
            textFechaCreacion.setText("Creado: " + expediente.getFechaCreacion());

            // Configurar click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getAdapterPosition());
                }
            });

            // Configurar botones de acción
            btnEdit.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEditClick(getAdapterPosition());
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getAdapterPosition());
                }
            });
        }
    }
}