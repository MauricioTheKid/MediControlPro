package com.example.medicontrolpro.ui.doctores;

import de.hdodenhof.circleimageview.CircleImageView;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medicontrolpro.R;

import java.io.File;
import java.util.List;

public class DoctoresAdapter extends RecyclerView.Adapter<DoctoresAdapter.DoctorViewHolder> {

    private List<DoctoresViewModel.Doctor> doctores;
    private OnItemClickListener itemClickListener;
    private Context context;

    private static final String TAG = "DoctoresAdapter";

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onFavoritoClick(int position, boolean esFavorito);
    }

    public DoctoresAdapter(List<DoctoresViewModel.Doctor> doctores, Context context) {
        this.doctores = doctores;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        DoctoresViewModel.Doctor doctor = doctores.get(position);
        holder.bind(doctor, itemClickListener, context);
    }

    @Override
    public int getItemCount() {
        return doctores != null ? doctores.size() : 0;
    }

    public void setDoctores(List<DoctoresViewModel.Doctor> doctores) {
        this.doctores = doctores;
        notifyDataSetChanged();
        Log.d(TAG, "✅ Doctores actualizados en adapter: " + doctores.size());
    }

    public DoctoresViewModel.Doctor getDoctorAtPosition(int position) {
        if (position >= 0 && position < doctores.size()) {
            return doctores.get(position);
        }
        return null;
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        private TextView textNombre, textEspecialidad, textTelefono, textHorarios;
        private ImageButton btnFavorito, btnLlamar;
        private Button btnEditar, btnEliminar;
        private CircleImageView fotoDoctor;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.text_nombre);
            textEspecialidad = itemView.findViewById(R.id.text_especialidad);
            textTelefono = itemView.findViewById(R.id.text_telefono);
            textHorarios = itemView.findViewById(R.id.text_horarios);
            btnFavorito = itemView.findViewById(R.id.btn_favorito);
            btnLlamar = itemView.findViewById(R.id.btn_llamar);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
            fotoDoctor = itemView.findViewById(R.id.foto_doctor);
        }

        public void bind(DoctoresViewModel.Doctor doctor, OnItemClickListener listener, Context context) {
            textNombre.setText(doctor.getNombre());
            textEspecialidad.setText(doctor.getEspecialidad());
            textTelefono.setText(doctor.getTelefono().isEmpty() ? "Sin teléfono" : doctor.getTelefono());
            textHorarios.setText(doctor.getHorarios().isEmpty() ? "Horario no especificado" : doctor.getHorarios());

            // Ícono de favorito
            btnFavorito.setImageResource(doctor.isEsFavorito() ?
                    R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

            // ✅✅✅ CARGAR FOTO DEL DOCTOR CORRECTAMENTE
            cargarFotoDoctor(doctor);

            // Eventos
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getAdapterPosition());
                }
            });

            btnFavorito.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    boolean nuevoEstado = !doctor.isEsFavorito();
                    listener.onFavoritoClick(getAdapterPosition(), nuevoEstado);

                    // Actualizar inmediatamente la UI
                    if (nuevoEstado) {
                        btnFavorito.setImageResource(R.drawable.ic_favorite_filled);
                    } else {
                        btnFavorito.setImageResource(R.drawable.ic_favorite_border);
                    }
                }
            });

            btnLlamar.setOnClickListener(v -> {
                if (doctor.getTelefono() != null && !doctor.getTelefono().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + doctor.getTelefono()));
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "No hay número de teléfono disponible", Toast.LENGTH_SHORT).show();
                }
            });

            btnEditar.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEditClick(getAdapterPosition());
                }
            });

            btnEliminar.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getAdapterPosition());
                }
            });
        }

        // ✅✅✅ MÉTODO MEJORADO PARA CARGAR FOTOS
        private void cargarFotoDoctor(DoctoresViewModel.Doctor doctor) {
            try {
                String fotoPath = doctor.getFotoPath();

                Log.d(TAG, "🖼️ Intentando cargar foto para doctor: " + doctor.getNombre());
                Log.d(TAG, "   - Foto path: " + fotoPath);

                if (fotoPath != null && !fotoPath.isEmpty()) {
                    File imgFile = new File(fotoPath);
                    if (imgFile.exists()) {
                        // Usar Glide para cargar la imagen de forma eficiente
                        Glide.with(itemView.getContext())
                                .load(imgFile)
                                .placeholder(R.drawable.ic_medico_default)
                                .error(R.drawable.ic_medico_default)
                                .circleCrop()
                                .into(fotoDoctor);
                        Log.d(TAG, "✅ Foto cargada exitosamente: " + fotoPath);
                    } else {
                        // Si el archivo no existe, usar placeholder
                        fotoDoctor.setImageResource(R.drawable.ic_medico_default);
                        Log.w(TAG, "⚠️ Archivo de foto no existe: " + fotoPath);
                    }
                } else {
                    // Si no hay foto, usar placeholder
                    fotoDoctor.setImageResource(R.drawable.ic_medico_default);
                    Log.d(TAG, "📸 Sin foto, usando placeholder");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error cargando foto del doctor: " + e.getMessage());
                fotoDoctor.setImageResource(R.drawable.ic_medico_default);
            }
        }
    }
}