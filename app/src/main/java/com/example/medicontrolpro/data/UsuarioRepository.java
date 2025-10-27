package com.example.medicontrolpro.data;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsuarioRepository {

    private UsuarioDao usuarioDao;
    private LiveData<List<UsuarioEntity>> allUsuarios;
    private ExecutorService executorService;

    private static final String TAG = "UsuarioRepository";

    public UsuarioRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        usuarioDao = database.usuarioDao();
        allUsuarios = usuarioDao.getAllUsuarios();
        executorService = Executors.newSingleThreadExecutor();
        Log.d(TAG, "UsuarioRepository inicializado correctamente");
    }

    public LiveData<List<UsuarioEntity>> getAllUsuarios() {
        return allUsuarios;
    }

    public LiveData<UsuarioEntity> getUsuarioByEmail(String email) {
        Log.d(TAG, "🔍 Buscando usuario por email (async): " + email);

        MutableLiveData<UsuarioEntity> result = new MutableLiveData<>();

        executorService.execute(() -> {
            try {
                UsuarioEntity usuario = usuarioDao.getUsuarioByEmail(email);

                if (usuario != null) {
                    Log.d(TAG, "✅ Usuario encontrado (async): " + usuario.email);
                } else {
                    Log.w(TAG, "⚠️ Usuario NO encontrado (async), creando: " + email);

                    // Crear usuario básico si no existe
                    UsuarioEntity nuevoUsuario = new UsuarioEntity();
                    nuevoUsuario.email = email;
                    nuevoUsuario.nombreCompleto = "Usuario";
                    nuevoUsuario.sincronizado = false;

                    usuarioDao.insert(nuevoUsuario);
                    Log.d(TAG, "✅ Nuevo usuario creado (async): " + email);

                    // Obtener el usuario recién creado
                    usuario = usuarioDao.getUsuarioByEmail(email);
                }

                result.postValue(usuario);

            } catch (Exception e) {
                Log.e(TAG, "❌ ERROR en getUsuarioByEmail (async): " + e.getMessage());
                result.postValue(null);
            }
        });

        return result;
    }

    // ✅✅✅ MÉTODO CRÍTICO CORREGIDO - SÍNCRONO CON CREACIÓN DE USUARIO
    public UsuarioEntity getUsuarioByEmailSync(String email) {
        try {
            Log.d(TAG, "🔍 Búsqueda síncrona para: " + email);

            UsuarioEntity usuario = usuarioDao.getUsuarioByEmail(email);

            if (usuario != null) {
                Log.d(TAG, "✅ Usuario encontrado (sync): " + usuario.email);
                return usuario;
            } else {
                Log.w(TAG, "⚠️ Usuario NO encontrado (sync), creando: " + email);

                // ✅✅✅ CREAR USUARIO SI NO EXISTE - ESTO FALTABA
                UsuarioEntity nuevoUsuario = new UsuarioEntity();
                nuevoUsuario.email = email;
                nuevoUsuario.nombreCompleto = "Usuario";
                nuevoUsuario.sincronizado = false;

                usuarioDao.insert(nuevoUsuario);
                Log.d(TAG, "✅ Nuevo usuario creado (sync): " + email);

                // ✅ Obtener el usuario recién creado
                return usuarioDao.getUsuarioByEmail(email);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR en búsqueda síncrona: " + e.getMessage());
            return null;
        }
    }

    public void insert(UsuarioEntity usuario) {
        executorService.execute(() -> {
            try {
                usuarioDao.insert(usuario);
                Log.d(TAG, "✅ Usuario insertado: " + usuario.email);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error insertando usuario: " + e.getMessage());
            }
        });
    }

    public void actualizarPerfil(UsuarioEntity usuario) {
        executorService.execute(() -> {
            try {
                usuarioDao.actualizarPerfil(
                        usuario.email,
                        usuario.nombreCompleto,
                        usuario.telefono,
                        usuario.direccion,
                        usuario.tipoSangre,
                        usuario.fechaNacimiento,
                        usuario.genero,
                        usuario.alergias,
                        usuario.condicionesMedicas,
                        usuario.medicamentosActuales,
                        usuario.sincronizado
                );
                Log.d(TAG, "✅ Perfil actualizado: " + usuario.email);
                Log.d(TAG, "   - Nombre: " + usuario.nombreCompleto);
                Log.d(TAG, "   - Teléfono: " + usuario.telefono);
                Log.d(TAG, "   - Dirección: " + usuario.direccion);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error actualizando perfil: " + e.getMessage());
            }
        });
    }

    // Método delete corregido
    public void delete(UsuarioEntity usuario) {
        executorService.execute(() -> {
            try {
                usuarioDao.delete(usuario);
                Log.d(TAG, "✅ Usuario eliminado: " + usuario.email);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error eliminando usuario: " + e.getMessage());
            }
        });
    }

    public void deleteAll() {
        executorService.execute(() -> {
            try {
                usuarioDao.deleteAll();
                Log.d(TAG, "✅ Todos los usuarios eliminados");
            } catch (Exception e) {
                Log.e(TAG, "❌ Error eliminando todos los usuarios: " + e.getMessage());
            }
        });
    }
}