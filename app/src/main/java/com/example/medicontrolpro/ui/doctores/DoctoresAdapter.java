package com.example.medicontrolpro.ui.doctores;

import de.hdodenhof.circleimageview.CircleImageView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicontrolpro.R;
import java.util.List;
import java.io.File;

public class DoctoresAdapter extends RecyclerView.Adapter<DoctoresAdapter.DoctorViewHolder> {

    private List<DoctoresViewModel.Doctor> doctores;
    private OnItemClickListener itemClickListener;
    private Context context;

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
        private CircleImageView fotoDoctor; // CAMBIADO A CIRCLEIMAGEVIEW

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
            fotoDoctor = itemView.findViewById(R.id.foto_doctor); // NUEVA REFERENCIA
        }

        public void bind(DoctoresViewModel.Doctor doctor, OnItemClickListener listener, Context context) {
            textNombre.setText(doctor.getNombre());
            textEspecialidad.setText(doctor.getEspecialidad());
            textTelefono.setText(doctor.getTelefono().isEmpty() ? "Sin teléfono" : doctor.getTelefono());
            textHorarios.setText(doctor.getHorarios().isEmpty() ? "Horario no especificado" : doctor.getHorarios());

            // Configurar favorito
            btnFavorito.setImageResource(doctor.isEsFavorito() ?
                    R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

            // Cargar foto del doctor si existe
            cargarFotoDoctor(doctor);

            // Listeners
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getAdapterPosition());
                }
            });

            btnFavorito.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onFavoritoClick(getAdapterPosition(), !doctor.isEsFavorito());
                }
            });

            btnLlamar.setOnClickListener(v -> {
                if (doctor.getTelefono() != null && !doctor.getTelefono().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + doctor.getTelefono()));
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "No hay número de teléfono", Toast.LENGTH_SHORT).show();
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

        private void cargarFotoDoctor(DoctoresViewModel.Doctor doctor) {
            // Extraer la ruta de la foto de las notas (formato temporal)
            String fotoPath = extraerFotoPathDeNotas(doctor.getNotasPaciente());

            if (fotoPath != null && !fotoPath.isEmpty()) {
                File imgFile = new File(fotoPath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    fotoDoctor.setImageBitmap(bitmap);
                    return;
                }
            }

            // Si no hay foto, usar la imagen por defecto
            fotoDoctor.setImageResource(R.drawable.ic_medico_default);
        }

        private String extraerFotoPathDeNotas(String notas) {
            if (notas != null && notas.contains("|FOTO_PATH:")) {
                String[] partes = notas.split("\\|FOTO_PATH:");
                if (partes.length > 1) {
                    return partes[1];
                }
            }
            return null;
        }
    }
}