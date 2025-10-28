package com.example.medicontrolpro.ui.doctores;

import de.hdodenhof.circleimageview.CircleImageView;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.medicontrolpro.R;
import com.example.medicontrolpro.data.DoctorEntity;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DoctorDialogFragment extends DialogFragment {

    public interface OnDoctorGuardadaListener {
        void onDoctorGuardada(DoctoresViewModel.Doctor doctor);
    }

    private OnDoctorGuardadaListener listener;
    private TextInputEditText editNombre, editEspecialidad, editTelefono, editEmail, editDireccion, editHorarios, editNotas;
    private CircleImageView fotoDoctorDialog;
    private TextView btnCambiarFoto;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private String currentPhotoPath;
    private String fotoPathSeleccionada;

    private static final String TAG = "DoctorDialogFragment";

    public static DoctorDialogFragment newInstance(DoctorEntity doctor) {
        DoctorDialogFragment fragment = new DoctorDialogFragment();
        Bundle args = new Bundle();
        if (doctor != null) {
            args.putString("nombre", doctor.nombre);
            args.putString("especialidad", doctor.especialidad);
            args.putString("telefono", doctor.telefono);
            args.putString("email", doctor.email);
            args.putString("direccion", doctor.direccion);
            args.putString("horarios", doctor.horarios);
            args.putString("notas", doctor.notasPaciente);
            args.putString("fotoPath", doctor.fotoPath); // ✅ GUARDAR FOTO PATH CORRECTAMENTE
            args.putInt("id", doctor.id);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnDoctorGuardadaListener(OnDoctorGuardadaListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarLaunchers();
    }

    private void configurarLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImage = data.getData();
                            procesarImagenSeleccionada(selectedImage);
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        File imgFile = new File(currentPhotoPath);
                        if (imgFile.exists()) {
                            cargarFotoDesdeArchivo(currentPhotoPath);
                            fotoPathSeleccionada = currentPhotoPath;
                            Log.d(TAG, "✅ Foto tomada con cámara: " + currentPhotoPath);
                        }
                    }
                });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_doctor, null);

        inicializarVistas(view);
        cargarDatosExistente();
        configurarBotones(view);

        builder.setView(view)
                .setTitle(getArguments() != null && getArguments().containsKey("id") ?
                        "Editar Doctor" : "Nuevo Doctor");

        return builder.create();
    }

    private void inicializarVistas(View view) {
        editNombre = view.findViewById(R.id.edit_nombre);
        editEspecialidad = view.findViewById(R.id.edit_especialidad);
        editTelefono = view.findViewById(R.id.edit_telefono);
        editEmail = view.findViewById(R.id.edit_email);
        editDireccion = view.findViewById(R.id.edit_direccion);
        editHorarios = view.findViewById(R.id.edit_horarios);
        editNotas = view.findViewById(R.id.edit_notas);
        fotoDoctorDialog = view.findViewById(R.id.foto_doctor_dialog);
        btnCambiarFoto = view.findViewById(R.id.btn_cambiar_foto);
    }

    private void cargarDatosExistente() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("nombre")) {
            editNombre.setText(args.getString("nombre", ""));
            editEspecialidad.setText(args.getString("especialidad", ""));
            editTelefono.setText(args.getString("telefono", ""));
            editEmail.setText(args.getString("email", ""));
            editDireccion.setText(args.getString("direccion", ""));
            editHorarios.setText(args.getString("horarios", ""));
            editNotas.setText(args.getString("notas", ""));

            // ✅ CARGAR FOTO EXISTENTE CORRECTAMENTE
            String fotoPath = args.getString("fotoPath", "");
            if (fotoPath != null && !fotoPath.isEmpty()) {
                cargarFotoDesdeArchivo(fotoPath);
                fotoPathSeleccionada = fotoPath;
                Log.d(TAG, "✅ Foto existente cargada: " + fotoPath);
            }
        }
    }

    private void configurarBotones(View view) {
        Button btnCancelar = view.findViewById(R.id.btn_cancelar);
        Button btnGuardar = view.findViewById(R.id.btn_guardar);

        btnCambiarFoto.setOnClickListener(v -> mostrarDialogoSeleccionFoto());

        btnCancelar.setOnClickListener(v -> dismiss());

        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                guardarDoctor();
                dismiss();
            }
        });
    }

    private void mostrarDialogoSeleccionFoto() {
        String[] opciones = {"Tomar foto", "Elegir de galería", "Cancelar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccionar foto del doctor");
        builder.setItems(opciones, (dialog, which) -> {
            switch (which) {
                case 0:
                    tomarFoto();
                    break;
                case 1:
                    elegirDeGaleria();
                    break;
            }
        });
        builder.show();
    }

    private void tomarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = crearArchivoImagen();
            } catch (IOException ex) {
                Toast.makeText(getContext(), "Error al crear archivo", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "❌ Error al crear archivo de imagen: " + ex.getMessage());
            }

            if (photoFile != null) {
                currentPhotoPath = photoFile.getAbsolutePath();
                Uri photoURI = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
                Log.d(TAG, "📸 Iniciando cámara con ruta: " + currentPhotoPath);
            }
        }
    }

    private void elegirDeGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
        Log.d(TAG, "🖼️ Abriendo galería para seleccionar foto");
    }

    private File crearArchivoImagen() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "DOCTOR_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void procesarImagenSeleccionada(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            guardarFotoYActualizar(bitmap);
            Log.d(TAG, "✅ Imagen de galería procesada correctamente");
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al cargar imagen", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Error al procesar imagen de galería: " + e.getMessage());
        }
    }

    private void cargarFotoDesdeArchivo(String path) {
        File imgFile = new File(path);
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            fotoDoctorDialog.setImageBitmap(bitmap);
            Log.d(TAG, "🖼️ Foto cargada desde archivo: " + path);
        } else {
            Log.w(TAG, "⚠️ Archivo de foto no existe: " + path);
        }
    }

    private void guardarFotoYActualizar(Bitmap bitmap) {
        try {
            File file = crearArchivoImagen();
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            fotoDoctorDialog.setImageBitmap(bitmap);
            fotoPathSeleccionada = file.getAbsolutePath();

            Log.d(TAG, "✅✅✅ FOTO GUARDADA CORRECTAMENTE: " + fotoPathSeleccionada);

        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al guardar foto", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Error al guardar foto: " + e.getMessage());
        }
    }

    private boolean validarCampos() {
        boolean isValid = true;

        if (editNombre.getText().toString().trim().isEmpty()) {
            editNombre.setError("Ingrese el nombre del doctor");
            isValid = false;
        } else {
            editNombre.setError(null);
        }

        if (editEspecialidad.getText().toString().trim().isEmpty()) {
            editEspecialidad.setError("Ingrese la especialidad");
            isValid = false;
        } else {
            editEspecialidad.setError(null);
        }

        return isValid;
    }

    private void guardarDoctor() {
        String nombre = editNombre.getText().toString().trim();
        String especialidad = editEspecialidad.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String direccion = editDireccion.getText().toString().trim();
        String horarios = editHorarios.getText().toString().trim();
        String notas = editNotas.getText().toString().trim();

        Bundle args = getArguments();
        int id = args != null ? args.getInt("id", 0) : 0;

        // ✅✅✅ CORREGIDO: Crear doctor con fotoPath separado
        DoctoresViewModel.Doctor nuevoDoctor = new DoctoresViewModel.Doctor(
                id, nombre, especialidad, telefono, email, direccion, horarios, false, 0f, notas
        );

        // ✅✅✅ CORREGIDO: Asignar fotoPath al campo correcto
        if (fotoPathSeleccionada != null && !fotoPathSeleccionada.isEmpty()) {
            nuevoDoctor.setFotoPath(fotoPathSeleccionada); // ✅ USAR SETTER CORRECTO
            Log.d(TAG, "✅ Foto asignada al doctor: " + fotoPathSeleccionada);
        }

        Log.d(TAG, "✅✅✅ DOCTOR GUARDADO CORRECTAMENTE:");
        Log.d(TAG, "   - Nombre: " + nombre);
        Log.d(TAG, "   - Especialidad: " + especialidad);
        Log.d(TAG, "   - Foto: " + (fotoPathSeleccionada != null ? fotoPathSeleccionada : "Sin foto"));
        Log.d(TAG, "   - Notas: " + notas);

        if (listener != null) {
            listener.onDoctorGuardada(nuevoDoctor);
        }
    }
}