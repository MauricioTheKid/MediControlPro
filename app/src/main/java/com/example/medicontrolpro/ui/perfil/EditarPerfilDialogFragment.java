package com.example.medicontrolpro.ui.perfil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.medicontrolpro.R;
import com.example.medicontrolpro.data.UsuarioEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditarPerfilDialogFragment extends DialogFragment {

    private EditText editNombre, editEmail, editTelefono, editDireccion, editTipoSangre,
            editFechaNacimiento, editGenero, editAlergias, editCondicionesMedicas, editMedicamentos;
    private Button btnGuardar, btnCancelar;

    private UsuarioEntity usuario;
    private EditarPerfilListener listener;

    private static final String TAG = "EditarPerfilDialog";

    public interface EditarPerfilListener {
        void onPerfilActualizado(UsuarioEntity usuarioActualizado);
    }

    public static EditarPerfilDialogFragment newInstance(UsuarioEntity usuario) {
        EditarPerfilDialogFragment fragment = new EditarPerfilDialogFragment();
        Bundle args = new Bundle();
        // Usar putParcelable en lugar de putSerializable
        args.putParcelable("usuario", usuario);
        fragment.setArguments(args);
        return fragment;
    }

    public void setEditarPerfilListener(EditarPerfilListener listener) {
        this.listener = listener;
        Log.d(TAG, "🎯 Listener configurado: " + (listener != null));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "🎯 Diálogo creado, inicializando vistas...");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_editar_perfil, null);

        inicializarVistas(view);
        cargarDatosUsuario();
        configurarBotones();

        builder.setView(view)
                .setTitle("Editar Perfil Completo");

        Log.d(TAG, "✅ Vistas inicializadas correctamente");

        return builder.create();
    }

    private void inicializarVistas(View view) {
        try {
            editNombre = view.findViewById(R.id.edit_nombre);
            editEmail = view.findViewById(R.id.edit_email);
            editTelefono = view.findViewById(R.id.edit_telefono);
            editDireccion = view.findViewById(R.id.edit_direccion);
            editTipoSangre = view.findViewById(R.id.edit_tipo_sangre);
            editFechaNacimiento = view.findViewById(R.id.edit_fecha_nacimiento);
            editGenero = view.findViewById(R.id.edit_genero);
            editAlergias = view.findViewById(R.id.edit_alergias);
            editCondicionesMedicas = view.findViewById(R.id.edit_condiciones_medicas);
            editMedicamentos = view.findViewById(R.id.edit_medicamentos);
            btnGuardar = view.findViewById(R.id.btn_guardar);
            btnCancelar = view.findViewById(R.id.btn_cancelar);

            // Hacer el campo de email no editable
            editEmail.setEnabled(false);

            Log.d(TAG, "✅ Vistas inicializadas correctamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando vistas: " + e.getMessage());
        }
    }

    private void cargarDatosUsuario() {
        Log.d(TAG, "📥 Cargando datos desde arguments...");

        try {
            if (getArguments() != null) {
                // Usar getParcelable en lugar de getSerializable
                usuario = getArguments().getParcelable("usuario");
                if (usuario != null) {
                    editNombre.setText(usuario.nombreCompleto != null ? usuario.nombreCompleto : "");
                    editEmail.setText(usuario.email != null ? usuario.email : "");
                    editTelefono.setText(usuario.telefono != null ? usuario.telefono : "");
                    editDireccion.setText(usuario.direccion != null ? usuario.direccion : "");
                    editTipoSangre.setText(usuario.tipoSangre != null ? usuario.tipoSangre : "");
                    editFechaNacimiento.setText(usuario.fechaNacimiento != null ? usuario.fechaNacimiento : "");
                    editGenero.setText(usuario.genero != null ? usuario.genero : "");
                    editAlergias.setText(usuario.alergias != null ? usuario.alergias : "");
                    editCondicionesMedicas.setText(usuario.condicionesMedicas != null ? usuario.condicionesMedicas : "");
                    editMedicamentos.setText(usuario.medicamentosActuales != null ? usuario.medicamentosActuales : "");

                    Log.d(TAG, "✅ Datos cargados correctamente");
                    Log.d(TAG, "   - Nombre cargado: " + usuario.nombreCompleto);
                    Log.d(TAG, "   - Email cargado: " + usuario.email);
                } else {
                    Log.e(TAG, "❌ Usuario es NULL en arguments");
                }
            } else {
                Log.e(TAG, "❌ Arguments es NULL");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error cargando datos: " + e.getMessage());
        }
    }

    private void configurarBotones() {
        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnCancelar.setOnClickListener(v -> dismiss());
    }

    private void guardarCambios() {
        try {
            Log.d(TAG, "💾 Usuario hizo clic en Guardar");

            // ✅ USAR SIEMPRE EL EMAIL DEL USUARIO AUTENTICADO, NO EL DEL FORMULARIO
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String emailUsuarioActual = currentUser != null ? currentUser.getEmail() : null;

            if (emailUsuarioActual == null) {
                Log.e(TAG, "❌ No hay usuario autenticado");
                Toast.makeText(getContext(), "Error: No hay usuario autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear usuario actualizado con el email CORRECTO
            UsuarioEntity usuarioActualizado = new UsuarioEntity();
            usuarioActualizado.email = emailUsuarioActual; // ✅ EMAIL CORRECTO
            usuarioActualizado.nombreCompleto = editNombre.getText().toString().trim();
            usuarioActualizado.telefono = editTelefono.getText().toString().trim();
            usuarioActualizado.direccion = editDireccion.getText().toString().trim();
            usuarioActualizado.tipoSangre = editTipoSangre.getText().toString().trim();
            usuarioActualizado.fechaNacimiento = editFechaNacimiento.getText().toString().trim();
            usuarioActualizado.genero = editGenero.getText().toString().trim();
            usuarioActualizado.alergias = editAlergias.getText().toString().trim();
            usuarioActualizado.condicionesMedicas = editCondicionesMedicas.getText().toString().trim();
            usuarioActualizado.medicamentosActuales = editMedicamentos.getText().toString().trim();
            usuarioActualizado.sincronizado = false;

            Log.d(TAG, "✅ DATOS PREPARADOS CON EMAIL CORRECTO:");
            Log.d(TAG, "   - Email: " + usuarioActualizado.email);
            Log.d(TAG, "   - Nombre: " + usuarioActualizado.nombreCompleto);
            Log.d(TAG, "   - Teléfono: " + usuarioActualizado.telefono);

            // Validaciones básicas
            if (usuarioActualizado.nombreCompleto.isEmpty()) {
                Toast.makeText(getContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onPerfilActualizado(usuarioActualizado);
                dismiss();
            } else {
                Log.e(TAG, "❌ Listener es null");
                Toast.makeText(getContext(), "Error interno", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error guardando cambios: " + e.getMessage());
            Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "🔚 Diálogo destruido");
    }
}