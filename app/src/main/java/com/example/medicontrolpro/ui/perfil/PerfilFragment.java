package com.example.medicontrolpro.ui.perfil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.medicontrolpro.R;
import com.example.medicontrolpro.data.UsuarioEntity;
import com.example.medicontrolpro.ui.auth.AuthViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilFragment extends Fragment {

    private AuthViewModel authViewModel;
    private TextView textNombre, textEmail, textTelefono, textDireccion, textTipoSangre,
            textFechaNacimiento, textGenero, textAlergias, textCondicionesMedicas, textMedicamentos;
    private CircleImageView imagePerfil;
    private TextView textCambiarFoto;
    private Button btnEditarPerfil, btnExportarDatos;

    private static final String TAG = "PerfilFragment";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private String currentPhotoPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        Log.d(TAG, "🎯 onCreateView - Inicializando vistas...");

        inicializarVistas(view);
        configurarViewModel();
        configurarBotones();

        return view;
    }

    private void inicializarVistas(View view) {
        try {
            textNombre = view.findViewById(R.id.text_nombre_completo);
            textEmail = view.findViewById(R.id.text_email);
            textTelefono = view.findViewById(R.id.text_telefono);
            textDireccion = view.findViewById(R.id.text_direccion);
            textTipoSangre = view.findViewById(R.id.text_tipo_sangre);
            textFechaNacimiento = view.findViewById(R.id.text_fecha_nacimiento);
            textGenero = view.findViewById(R.id.text_genero);
            textAlergias = view.findViewById(R.id.text_alergias);
            textCondicionesMedicas = view.findViewById(R.id.text_condiciones_medicas);
            textMedicamentos = view.findViewById(R.id.text_medicamentos);
            btnEditarPerfil = view.findViewById(R.id.btn_editar_perfil);
            btnExportarDatos = view.findViewById(R.id.btn_exportar_datos);

            imagePerfil = view.findViewById(R.id.image_perfil);
            textCambiarFoto = view.findViewById(R.id.text_cambiar_foto);

            Log.d(TAG, "✅ Vistas inicializadas correctamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando vistas: " + e.getMessage());
            Toast.makeText(getContext(), "Error al cargar interfaz", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarViewModel() {
        try {
            authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
            Log.d(TAG, "✅ ViewModel configurado correctamente");

            // Observador principal para los datos del usuario
            authViewModel.getUsuarioActualLiveData().observe(getViewLifecycleOwner(), usuario -> {
                Log.d(TAG, "👀 OBSERVADOR PRINCIPAL ACTIVADO - Usuario: " + (usuario != null ? "NO NULL" : "NULL"));

                if (usuario != null && usuario.getEmail() != null && !usuario.getEmail().isEmpty()) {
                    Log.d(TAG, "✅✅✅ USUARIO RECIBIDO EN FRAGMENT:");
                    Log.d(TAG, "   - Email: " + usuario.getEmail());
                    Log.d(TAG, "   - Nombre: " + usuario.getNombreCompleto());
                    Log.d(TAG, "   - Foto: " + usuario.getFotoPerfil());

                    mostrarDatosUsuario(usuario);
                } else {
                    Log.e(TAG, "❌ USUARIO ES NULL O SIN EMAIL EN OBSERVADOR");
                    mostrarUsuarioNoDisponible();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error configurando ViewModel: " + e.getMessage());
        }
    }

    private void configurarBotones() {
        btnEditarPerfil.setOnClickListener(v -> {
            Log.d(TAG, "✏️ Botón editar perfil presionado");
            abrirDialogoEditarPerfil();
        });

        imagePerfil.setOnClickListener(v -> {
            Log.d(TAG, "📸 Clic en imagen de perfil");
            seleccionarFoto();
        });

        textCambiarFoto.setOnClickListener(v -> {
            Log.d(TAG, "📸 Clic en texto Cambiar foto");
            seleccionarFoto();
        });

        if (btnExportarDatos != null) {
            btnExportarDatos.setOnClickListener(v -> {
                Log.d(TAG, "📊 Botón exportar datos presionado");
                Toast.makeText(getContext(), "Funcionalidad de exportar en desarrollo", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void mostrarDatosUsuario(UsuarioEntity usuario) {
        try {
            Log.d(TAG, "📊 Mostrando datos en interfaz:");

            textNombre.setText(validarCampo(usuario.getNombreCompleto()));
            textEmail.setText(validarCampo(usuario.getEmail()));
            textTelefono.setText(validarCampo(usuario.getTelefono()));
            textDireccion.setText(validarCampo(usuario.getDireccion()));
            textTipoSangre.setText(validarCampo(usuario.getTipoSangre()));
            textFechaNacimiento.setText(validarCampo(usuario.getFechaNacimiento()));
            textGenero.setText(validarCampo(usuario.getGenero()));
            textAlergias.setText(validarCampo(usuario.getAlergias()));
            textCondicionesMedicas.setText(validarCampo(usuario.getCondicionesMedicas()));
            textMedicamentos.setText(validarCampo(usuario.getMedicamentosActuales()));

            cargarFotoPerfil(usuario.getFotoPerfil());

            Log.d(TAG, "✅✅✅ INTERFAZ ACTUALIZADA CORRECTAMENTE");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error mostrando datos: " + e.getMessage());
            Toast.makeText(getContext(), "Error al mostrar datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarFotoPerfil(String fotoUrl) {
        try {
            if (fotoUrl != null && !fotoUrl.isEmpty() && !fotoUrl.equals("null")) {
                Log.d(TAG, "🖼️ Cargando foto de perfil: " + fotoUrl);

                if (fotoUrl.startsWith("http")) {
                    // Foto de Firebase Storage
                    Glide.with(this)
                            .load(fotoUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(imagePerfil);
                } else if (fotoUrl.startsWith("content://") || fotoUrl.startsWith("file://")) {
                    // Foto local - usar URI directa
                    Glide.with(this)
                            .load(Uri.parse(fotoUrl))
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(imagePerfil);
                } else if (fotoUrl.startsWith("/")) {
                    // Ruta de archivo local
                    File file = new File(fotoUrl);
                    if (file.exists()) {
                        Glide.with(this)
                                .load(file)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(imagePerfil);
                    } else {
                        imagePerfil.setImageResource(R.drawable.ic_profile);
                    }
                } else {
                    imagePerfil.setImageResource(R.drawable.ic_profile);
                }
            } else {
                Log.d(TAG, "🖼️ No hay foto de perfil, usando imagen por defecto");
                imagePerfil.setImageResource(R.drawable.ic_profile);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error cargando foto: " + e.getMessage());
            imagePerfil.setImageResource(R.drawable.ic_profile);
        }
    }

    private void seleccionarFoto() {
        try {
            Log.d(TAG, "📁 Abriendo selector de imágenes...");

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error abriendo selector: " + e.getMessage());
            Toast.makeText(getContext(), "Error al abrir galería", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            Log.d(TAG, "✅ Imagen seleccionada: " + imageUri.toString());

            try {
                // ✅ CONVERTIR URI TEMPORAL A ARCHIVO PERMANENTE
                String filePath = guardarImagenPermanente(imageUri);

                if (filePath != null) {
                    // Mostrar imagen inmediatamente
                    cargarFotoPerfil(filePath);

                    // Actualizar en la base de datos
                    actualizarFotoPerfil(filePath);
                } else {
                    Toast.makeText(getContext(), "Error al procesar imagen", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Error procesando imagen: " + e.getMessage());
                Toast.makeText(getContext(), "Error al procesar imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "❌ Selección de imagen cancelada o fallida");
        }
    }

    // ✅ NUEVO MÉTODO: Guardar imagen permanentemente
    private String guardarImagenPermanente(Uri imageUri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap != null) {
                // Crear directorio si no existe
                File storageDir = new File(getContext().getFilesDir(), "profile_pictures");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }

                // Crear archivo con nombre único
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "profile_" + timeStamp + ".jpg";
                File imageFile = new File(storageDir, imageFileName);

                // Comprimir y guardar bitmap
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                outputStream.flush();
                outputStream.close();

                Log.d(TAG, "✅ Imagen guardada permanentemente: " + imageFile.getAbsolutePath());
                return imageFile.getAbsolutePath();
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Error guardando imagen: " + e.getMessage());
        }
        return null;
    }

    private void actualizarFotoPerfil(String filePath) {
        try {
            Log.d(TAG, "🔄 Actualizando foto de perfil en BD...");

            authViewModel.actualizarFotoPerfil(filePath);

            Log.d(TAG, "✅ Solicitud de actualización de foto enviada al ViewModel");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error actualizando foto: " + e.getMessage());
            Toast.makeText(getContext(), "❌ Error al actualizar foto", Toast.LENGTH_SHORT).show();
        }
    }

    private String validarCampo(String valor) {
        if (valor == null || valor.isEmpty() || valor.equals("null")) {
            return "No disponible";
        }
        return valor;
    }

    private void mostrarUsuarioNoDisponible() {
        try {
            textNombre.setText("No disponible");
            textEmail.setText("No disponible");
            textTelefono.setText("No disponible");
            textDireccion.setText("No disponible");
            textTipoSangre.setText("No disponible");
            textFechaNacimiento.setText("No disponible");
            textGenero.setText("No disponible");
            textAlergias.setText("No disponible");
            textCondicionesMedicas.setText("No disponible");
            textMedicamentos.setText("No disponible");
            imagePerfil.setImageResource(R.drawable.ic_profile);

            Log.d(TAG, "⚠️ Mostrando estado 'No disponible'");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error en mostrarUsuarioNoDisponible: " + e.getMessage());
        }
    }

    private void abrirDialogoEditarPerfil() {
        try {
            Log.d(TAG, "🔄 Abriendo diálogo de edición...");

            UsuarioEntity usuarioActual = authViewModel.getUsuarioActualLiveData().getValue();

            if (usuarioActual != null) {
                Log.d(TAG, "✅ Usuario actual obtenido para edición:");
                Log.d(TAG, "   - Nombre: " + usuarioActual.getNombreCompleto());
                Log.d(TAG, "   - Email: " + usuarioActual.getEmail());
                Log.d(TAG, "   - Teléfono: " + usuarioActual.getTelefono());

                EditarPerfilDialogFragment dialog = EditarPerfilDialogFragment.newInstance(usuarioActual);

                dialog.setEditarPerfilListener(usuarioActualizado -> {
                    Log.d(TAG, "🎯 Listener del diálogo activado");
                    if (usuarioActualizado != null) {
                        Log.d(TAG, "📤 Enviando datos actualizados al ViewModel");
                        Log.d(TAG, "   - Nuevo nombre: " + usuarioActualizado.getNombreCompleto());
                        Log.d(TAG, "   - Nuevo teléfono: " + usuarioActualizado.getTelefono());
                        Log.d(TAG, "   - Nueva dirección: " + usuarioActualizado.getDireccion());

                        authViewModel.actualizarPerfilCompleto(usuarioActualizado);
                    } else {
                        Log.e(TAG, "❌ usuarioActualizado es NULL en el listener");
                        Toast.makeText(getContext(), "Error: Datos inválidos", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show(getParentFragmentManager(), "EditarPerfilDialog");
                Log.d(TAG, "✅ Diálogo mostrado correctamente");

            } else {
                Log.e(TAG, "❌ No se pudo obtener usuario actual para editar");
                Toast.makeText(getContext(), "Error: No se pueden cargar los datos", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error abriendo diálogo: " + e.getMessage());
            Toast.makeText(getContext(), "Error al abrir editor", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔍 onResume - Fragment visible");

        // Forzar recarga de datos
        if (authViewModel != null) {
            authViewModel.cargarUsuarioActual();
        }
    }
}