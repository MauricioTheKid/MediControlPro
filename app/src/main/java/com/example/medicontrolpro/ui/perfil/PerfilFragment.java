package com.example.medicontrolpro.ui.perfil;

import de.hdodenhof.circleimageview.CircleImageView; // NUEVA IMPORTACIÓN
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medicontrolpro.data.PacienteEntity;
import com.example.medicontrolpro.databinding.FragmentPerfilBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private PerfilViewModel perfilViewModel;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private String currentPhotoPath;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        perfilViewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        configurarLaunchers();
        configurarObservadores();
        configurarBotones();

        return root;
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
                            guardarRutaFotoEnBD(currentPhotoPath);
                        }
                    }
                });
    }

    private void configurarObservadores() {
        perfilViewModel.getPaciente().observe(getViewLifecycleOwner(), pacienteEntity -> {
            if (pacienteEntity != null) {
                PerfilViewModel.Paciente paciente = new PerfilViewModel.Paciente(pacienteEntity);
                actualizarUI(paciente);
            } else {
                crearPacientePorDefecto();
            }
        });
    }

    private void configurarBotones() {
        binding.imagePerfil.setOnClickListener(v -> {
            mostrarDialogoSeleccionFoto();
        });

        binding.btnEditarPerfil.setOnClickListener(v -> {
            abrirDialogoEdicion();
        });

        binding.btnExportarDatos.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Exportando datos médicos...", Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarDialogoSeleccionFoto() {
        String[] opciones = {"Tomar foto", "Elegir de galería", "Cancelar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccionar foto de perfil");
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
            }

            if (photoFile != null) {
                currentPhotoPath = photoFile.getAbsolutePath();
                Uri photoURI = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    private void elegirDeGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File crearArchivoImagen() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void procesarImagenSeleccionada(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            guardarFotoYActualizar(bitmap);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al cargar imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarFotoDesdeArchivo(String path) {
        File imgFile = new File(path);
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            binding.imagePerfil.setImageBitmap(bitmap);
        }
    }

    private void guardarFotoYActualizar(Bitmap bitmap) {
        try {
            File file = crearArchivoImagen();
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            binding.imagePerfil.setImageBitmap(bitmap);
            guardarRutaFotoEnBD(file.getAbsolutePath());

        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al guardar foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarRutaFotoEnBD(String path) {
        PerfilViewModel.Paciente pacienteActual = null;
        if (perfilViewModel.getPaciente().getValue() != null) {
            pacienteActual = new PerfilViewModel.Paciente(perfilViewModel.getPaciente().getValue());

            PacienteEntity entity = new PacienteEntity();
            entity.id = pacienteActual.getId();
            entity.nombreCompleto = pacienteActual.getNombreCompleto();
            entity.email = pacienteActual.getEmail();
            entity.fechaNacimiento = pacienteActual.getFechaNacimiento();
            entity.genero = pacienteActual.getGenero();
            entity.telefono = pacienteActual.getTelefono();
            entity.direccion = pacienteActual.getDireccion();
            entity.tipoSangre = pacienteActual.getTipoSangre();
            entity.alergias = pacienteActual.getAlergias();
            entity.condicionesMedicas = pacienteActual.getCondicionesMedicas();
            entity.medicamentos = pacienteActual.getMedicamentos();
            entity.fotoPath = path;

            perfilViewModel.actualizarPaciente(entity);
            Toast.makeText(getContext(), "Foto actualizada", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarUI(PerfilViewModel.Paciente paciente) {
        if (paciente != null) {
            binding.textNombreCompleto.setText(paciente.getNombreCompleto());
            binding.textEmail.setText(paciente.getEmail());
            binding.textFechaNacimiento.setText(paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento() : "No especificado");
            binding.textGenero.setText(paciente.getGenero() != null ? paciente.getGenero() : "No especificado");
            binding.textTelefono.setText(paciente.getTelefono() != null ? paciente.getTelefono() : "No especificado");
            binding.textDireccion.setText(paciente.getDireccion() != null ? paciente.getDireccion() : "No especificada");
            binding.textTipoSangre.setText(paciente.getTipoSangre() != null ? paciente.getTipoSangre() : "No especificado");
            binding.textAlergias.setText(paciente.getAlergias() != null ? paciente.getAlergias() : "Ninguna registrada");
            binding.textCondicionesMedicas.setText(paciente.getCondicionesMedicas() != null ? paciente.getCondicionesMedicas() : "Ninguna registrada");
            binding.textMedicamentos.setText(paciente.getMedicamentos() != null ? paciente.getMedicamentos() : "Ninguno registrado");

            if (paciente.getFotoPath() != null && !paciente.getFotoPath().isEmpty()) {
                cargarFotoDesdeArchivo(paciente.getFotoPath());
            }
        }
    }

    private void crearPacientePorDefecto() {
        PacienteEntity paciente = new PacienteEntity();
        paciente.nombreCompleto = "Jose Mauricio Chavarria Gonzalez";
        paciente.email = "mauriciochavarria@gmail.com";
        paciente.fechaNacimiento = "2000-05-19";
        paciente.genero = "Masculino";
        paciente.telefono = "76757575";
        paciente.direccion = "Managua, Nicaragua";
        paciente.tipoSangre = "O+";
        paciente.alergias = "Penicilina, Polvo";
        paciente.condicionesMedicas = "Hipertensión, Diabetes tipo 2";
        paciente.medicamentos = "Losartan 50mg, Metformina 500mg";

        perfilViewModel.insertarPaciente(paciente);
    }

    private void abrirDialogoEdicion() {
        PerfilViewModel.Paciente pacienteActual = null;
        if (perfilViewModel.getPaciente().getValue() != null) {
            pacienteActual = new PerfilViewModel.Paciente(perfilViewModel.getPaciente().getValue());
        }

        EditarPerfilDialogFragment dialog = new EditarPerfilDialogFragment();
        dialog.setPacienteActual(pacienteActual);
        dialog.setOnPerfilActualizadoListener(pacienteActualizado -> {
            PacienteEntity entity = new PacienteEntity();
            entity.id = pacienteActualizado.getId();
            entity.nombreCompleto = pacienteActualizado.getNombreCompleto();
            entity.email = pacienteActualizado.getEmail();
            entity.fechaNacimiento = pacienteActualizado.getFechaNacimiento();
            entity.genero = pacienteActualizado.getGenero();
            entity.telefono = pacienteActualizado.getTelefono();
            entity.direccion = pacienteActualizado.getDireccion();
            entity.tipoSangre = pacienteActualizado.getTipoSangre();
            entity.alergias = pacienteActualizado.getAlergias();
            entity.condicionesMedicas = pacienteActualizado.getCondicionesMedicas();
            entity.medicamentos = pacienteActualizado.getMedicamentos();
            entity.fotoPath = pacienteActualizado.getFotoPath();

            perfilViewModel.actualizarPaciente(entity);
            Toast.makeText(getContext(), "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "EditarPerfilDialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}