package com.example.medicontrolpro.ui.perfil;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medicontrolpro.R;
import com.example.medicontrolpro.data.UsuarioEntity;
import com.example.medicontrolpro.ui.auth.AuthViewModel;

public class PerfilFragment extends Fragment {

    private AuthViewModel authViewModel;
    private TextView textNombre, textEmail, textTelefono, textDireccion, textTipoSangre,
            textFechaNacimiento, textGenero, textAlergias, textCondicionesMedicas, textMedicamentos;
    private Button btnEditarPerfil;

    private static final String TAG = "PerfilFragment";

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "🔍 onViewCreated - Observando datos del usuario...");
        // Ya no llamamos cargarDatosUsuario() aquí, el observador se encarga
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

            // ✅ ✅ ✅ OBSERVADOR PRINCIPAL - ÚNICA FUENTE DE VERDAD
            authViewModel.getUsuarioActualLiveData().observe(getViewLifecycleOwner(), usuario -> {
                Log.d(TAG, "👀 OBSERVADOR PRINCIPAL ACTIVADO");

                if (usuario != null) {
                    Log.d(TAG, "✅✅✅ USUARIO RECIBIDO EN FRAGMENT:");
                    Log.d(TAG, "   - Email: " + usuario.email);
                    Log.d(TAG, "   - Nombre: " + usuario.nombreCompleto);
                    Log.d(TAG, "   - Teléfono: " + usuario.telefono);
                    Log.d(TAG, "   - Dirección: " + usuario.direccion);
                    Log.d(TAG, "   - Tipo sangre: " + usuario.tipoSangre);

                    mostrarDatosUsuario(usuario);
                } else {
                    Log.e(TAG, "❌ USUARIO ES NULL EN OBSERVADOR");
                    mostrarUsuarioNoDisponible();
                }
            });

            // ✅ OBSERVADOR PARA ACTUALIZACIONES DE PERFIL
            authViewModel.getPerfilActualizado().observe(getViewLifecycleOwner(), exito -> {
                Log.d(TAG, "👀 OBSERVADOR ACTUALIZACIÓN ACTIVADO: " + exito);

                if (exito != null && exito) {
                    Log.d(TAG, "✅✅✅ ACTUALIZACIÓN EXITOSA - RECARGANDO DATOS");
                    Toast.makeText(getContext(), "✅ Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                } else if (exito != null) {
                    Log.e(TAG, "❌ ACTUALIZACIÓN FALLIDA");
                    Toast.makeText(getContext(), "❌ Error al actualizar perfil", Toast.LENGTH_LONG).show();
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
    }

    private void mostrarDatosUsuario(UsuarioEntity usuario) {
        try {
            Log.d(TAG, "📊 Mostrando datos en interfaz:");

            textNombre.setText(validarCampo(usuario.nombreCompleto));
            textEmail.setText(validarCampo(usuario.email));
            textTelefono.setText(validarCampo(usuario.telefono));
            textDireccion.setText(validarCampo(usuario.direccion));
            textTipoSangre.setText(validarCampo(usuario.tipoSangre));
            textFechaNacimiento.setText(validarCampo(usuario.fechaNacimiento));
            textGenero.setText(validarCampo(usuario.genero));
            textAlergias.setText(validarCampo(usuario.alergias));
            textCondicionesMedicas.setText(validarCampo(usuario.condicionesMedicas));
            textMedicamentos.setText(validarCampo(usuario.medicamentosActuales));

            Log.d(TAG, "✅✅✅ INTERFAZ ACTUALIZADA CORRECTAMENTE");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error mostrando datos: " + e.getMessage());
            Toast.makeText(getContext(), "Error al mostrar datos", Toast.LENGTH_SHORT).show();
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
                Log.d(TAG, "   - Nombre: " + usuarioActual.nombreCompleto);
                Log.d(TAG, "   - Email: " + usuarioActual.email);
                Log.d(TAG, "   - Teléfono: " + usuarioActual.telefono);

                EditarPerfilDialogFragment dialog = EditarPerfilDialogFragment.newInstance(usuarioActual);

                dialog.setEditarPerfilListener(usuarioActualizado -> {
                    Log.d(TAG, "🎯 Listener del diálogo activado");
                    if (usuarioActualizado != null) {
                        Log.d(TAG, "📤 Enviando datos actualizados al ViewModel");
                        Log.d(TAG, "   - Nuevo nombre: " + usuarioActualizado.nombreCompleto);
                        Log.d(TAG, "   - Nuevo teléfono: " + usuarioActualizado.telefono);
                        Log.d(TAG, "   - Nueva dirección: " + usuarioActualizado.direccion);

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
        Log.d(TAG, "🔍 onResume - Forzando recarga de datos...");

        if (authViewModel != null) {
            authViewModel.getUsuarioActualLiveData().observe(getViewLifecycleOwner(), usuario -> {
                // El observador principal actualiza la UI automáticamente
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "🔚 onDestroyView - Limpiando recursos...");
    }
}
