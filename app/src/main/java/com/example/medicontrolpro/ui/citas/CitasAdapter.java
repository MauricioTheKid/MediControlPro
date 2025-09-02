package com.example.medicontrolpro.ui.citas;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicontrolpro.R;

import java.util.List;

public class CitasAdapter extends RecyclerView.Adapter<CitasAdapter.CitaViewHolder> {

    private List<CitasViewModel.Cita> citas;
    private OnItemClickListener itemClickListener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public CitasAdapter(List<CitasViewModel.Cita> citas, Context context) {
        this.citas = citas;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita, parent, false);
        return new CitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        CitasViewModel.Cita cita = citas.get(position);
        holder.bind(cita, itemClickListener);
    }

    @Override
    public int getItemCount() {
        return citas != null ? citas.size() : 0;
    }

    public void setCitas(List<CitasViewModel.Cita> citas) {
        this.citas = citas;
        notifyDataSetChanged();
    }

    public CitasViewModel.Cita getCitaAtPosition(int position) {
        if (position >= 0 && position < citas.size()) {
            return citas.get(position);
        }
        return null;
    }

    public void removeCita(int position) {
        if (position >= 0 && position < citas.size()) {
            citas.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, citas.size());
        }
    }

    public void updateCita(int position, CitasViewModel.Cita citaActualizada) {
        if (position >= 0 && position < citas.size()) {
            citas.set(position, citaActualizada);
            notifyItemChanged(position);
        }
    }

    public void addCita(CitasViewModel.Cita nuevaCita) {
        citas.add(nuevaCita);
        notifyItemInserted(citas.size() - 1);
    }

    public void clearCitas() {
        int size = citas.size();
        citas.clear();
        notifyItemRangeRemoved(0, size);
    }

    static class CitaViewHolder extends RecyclerView.ViewHolder {
        private TextView textDoctor, textEspecialidad, textFechaHora, textMotivo, textEstado;
        private ImageButton btnEdit, btnDelete;
        private Context context;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            textDoctor = itemView.findViewById(R.id.text_doctor);
            textEspecialidad = itemView.findViewById(R.id.text_especialidad);
            textFechaHora = itemView.findViewById(R.id.text_fecha_hora);
            textMotivo = itemView.findViewById(R.id.text_motivo);
            textEstado = itemView.findViewById(R.id.text_estado);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(CitasViewModel.Cita cita, OnItemClickListener listener) {
            textDoctor.setText(cita.getDoctor());
            textEspecialidad.setText(cita.getEspecialidad());
            textFechaHora.setText(String.format("%s - %s", cita.getFecha(), cita.getHora()));
            textMotivo.setText(cita.getMotivo());
            textEstado.setText(cita.getEstado());

            // Configurar color y drawable según el estado
            configurarEstado(cita.getEstado());

            // Configurar click listeners para toda la tarjeta
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemLongClick(getAdapterPosition());
                    return true;
                }
                return false;
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

            // Agregar animación de elevación al hacer clic
            itemView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        itemView.setElevation(8f);
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        itemView.setElevation(2f);
                        break;
                }
                return false;
            });
        }

        private void configurarEstado(String estado) {
            int colorResId;

            switch (estado.toLowerCase()) {
                case "confirmada":
                case "confirmado":
                    colorResId = R.color.estado_confirmado;
                    break;
                case "pendiente":
                    colorResId = R.color.estado_pendiente;
                    break;
                case "cancelada":
                case "cancelado":
                    colorResId = R.color.estado_cancelado;
                    break;
                case "completada":
                case "completado":
                    colorResId = R.color.estado_completado;
                    break;
                default:
                    colorResId = R.color.textDark;
            }

            int color = ContextCompat.getColor(context, colorResId);
            textEstado.setTextColor(color);

            // Crear drawable circular dinámicamente
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(color);
            circle.setSize(16, 16); // Tamaño del círculo en pixels

            // Establecer el drawable a la izquierda del texto
            textEstado.setCompoundDrawablesWithIntrinsicBounds(circle, null, null, null);
            textEstado.setCompoundDrawablePadding(8); // Espacio entre círculo y texto
        }
    }
}