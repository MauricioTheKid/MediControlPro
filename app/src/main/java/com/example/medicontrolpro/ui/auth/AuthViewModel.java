package com.example.medicontrolpro.ui.auth;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.medicontrolpro.data.UsuarioEntity;
import com.example.medicontrolpro.data.UsuarioRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuthViewModel extends AndroidViewModel {

    private final UsuarioRepository usuarioRepository;
    private final ExecutorService executorService;

    private MutableLiveData<AuthResult> authResult = new MutableLiveData<>();
    private MutableLiveData<Boolean> perfilActualizado = new MutableLiveData<>();
    private MutableLiveData<UsuarioEntity> usuarioActual = new MutableLiveData<>();
    private MutableLiveData<Boolean> sincronizacionPendiente = new MutableLiveData<>(); // ✅ NUEVO: Para Paso 8

    private static final String TAG = "AuthViewModel";
    private static final String CREDENTIALS_FILE = "UserCredentials";

    public AuthViewModel(Application application) {
        super(application);
        usuarioRepository = new UsuarioRepository(application);
        executorService = Executors.newSingleThreadExecutor();
        Log.d(TAG, "AuthViewModel inicializado");

        // Cargar usuario actual al iniciar
        cargarUsuarioActual();
    }

    // ✅ NUEVO PARA PASO 8: Verificar conexión a internet
    private boolean tieneConexionInternet() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean conectado = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            Log.d(TAG, "📶 Verificación conexión: " + (conectado ? "CONECTADO" : "SIN CONEXIÓN"));
            return conectado;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error verificando conexión: " + e.getMessage());
            return false;
        }
    }

    // 🎯 ACTUALIZAR PERFIL COMPLETO (MODIFICADO PARA PASO 8 - SINCRONIZACIÓN OFFLINE)
    public void actualizarPerfilCompleto(UsuarioEntity usuarioActualizado) {
        Log.d(TAG, "🎯 INICIANDO ACTUALIZACION COMPLETA CON SINCRONIZACIÓN OFFLINE");

        if (usuarioActualizado == null) {
            Log.e(TAG, "❌ usuarioActualizado es NULL");
            perfilActualizado.postValue(false);
            return;
        }

        executorService.execute(() -> {
            try {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getEmail() != null) {
                    String emailFirebase = user.getEmail();
                    Log.d(TAG, "🔥 Email usuario actual: " + emailFirebase);

                    // Asegurar que el email sea el correcto
                    usuarioActualizado.email = emailFirebase;

                    // ✅ VERIFICAR CONEXIÓN (NUEVO PARA PASO 8)
                    boolean tieneInternet = tieneConexionInternet();
                    Log.d(TAG, "📶 Estado conexión: " + (tieneInternet ? "CONECTADO" : "OFFLINE"));

                    // ✅ 1. SIEMPRE ACTUALIZAR EN ROOM PRIMERO (PARA DATOS LOCALES INMEDIATOS)
                    Log.d(TAG, "🔄 Actualizando en Room...");
                    boolean exitoRoom = actualizarPerfilCompletoEnRoom(emailFirebase, usuarioActualizado);

                    if (exitoRoom) {
                        Log.d(TAG, "✅ ACTUALIZACIÓN EXITOSA EN ROOM");

                        if (tieneInternet) {
                            // ✅ 2. SI HAY INTERNET: SINCRONIZAR CON FIRESTORE
                            Log.d(TAG, "🔄 Con conexión - Sincronizando con Firestore...");
                            boolean exitoFirestore = actualizarPerfilCompletoEnFirestore(emailFirebase, usuarioActualizado);

                            if (exitoFirestore) {
                                // ✅ SINCRONIZACIÓN COMPLETA
                                Log.d(TAG, "✅✅✅ SINCRONIZACIÓN COMPLETA");
                                actualizarEstadoSincronizacionRoom(emailFirebase, true);
                                sincronizacionPendiente.postValue(false);

                                // Mostrar Toast de éxito
                                mostrarToastEnUI("✅ Perfil actualizado correctamente");
                            } else {
                                // ❌ ERROR EN FIRESTORE - MARCAR COMO PENDIENTE
                                Log.w(TAG, "⚠️ Error Firestore - Marcando como pendiente");
                                actualizarEstadoSincronizacionRoom(emailFirebase, false);
                                sincronizacionPendiente.postValue(true);

                                // Mostrar Toast informativo
                                mostrarToastEnUI("✅ Guardado local - Error de sincronización");
                            }
                        } else {
                            // 📶 SIN INTERNET - MARCAR COMO PENDIENTE
                            Log.w(TAG, "📶 Sin conexión - Marcando como pendiente de sincronización");
                            actualizarEstadoSincronizacionRoom(emailFirebase, false);
                            sincronizacionPendiente.postValue(true);

                            // Mostrar Toast informativo
                            mostrarToastEnUI("✅ Guardado local - Sincronizará cuando haya conexión");
                        }

                        // ✅ FORZAR ACTUALIZACIÓN INMEDIATA DEL LIVEDATA
                        UsuarioEntity usuarioRecargado = usuarioRepository.getUsuarioByEmailSync(emailFirebase);
                        if (usuarioRecargado != null) {
                            usuarioActual.postValue(usuarioRecargado);
                            Log.d(TAG, "🎯 LiveData actualizado con datos frescos de Room");
                        } else {
                            usuarioActual.postValue(usuarioActualizado);
                        }

                        perfilActualizado.postValue(true);
                    } else {
                        Log.e(TAG, "❌ ERROR EN ACTUALIZACIÓN ROOM");
                        perfilActualizado.postValue(false);
                    }

                } else {
                    Log.e(TAG, "❌ No hay usuario autenticado");
                    perfilActualizado.postValue(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ ERROR actualizando perfil: " + e.getMessage());
                perfilActualizado.postValue(false);
            }
        });
    }

    // ✅ NUEVO PARA PASO 8: Mostrar Toast en el hilo principal
    private void mostrarToastEnUI(String mensaje) {
        if (getApplication() != null && getApplication().getMainExecutor() != null) {
            getApplication().getMainExecutor().execute(() -> {
                android.widget.Toast.makeText(getApplication(), mensaje, android.widget.Toast.LENGTH_LONG).show();
            });
        }
    }

    // ✅ NUEVO PARA PASO 8: Actualizar solo el estado de sincronización en Room
    private void actualizarEstadoSincronizacionRoom(String email, boolean sincronizado) {
        try {
            executorService.execute(() -> {
                UsuarioEntity usuario = usuarioRepository.getUsuarioByEmailSync(email);
                if (usuario != null) {
                    usuario.sincronizado = sincronizado;
                    usuarioRepository.actualizarPerfil(usuario);
                    Log.d(TAG, "📊 Estado sincronización actualizado: " + (sincronizado ? "SINCRONIZADO" : "PENDIENTE"));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Error actualizando estado sincronización: " + e.getMessage());
        }
    }

    // ✅ NUEVO PARA PASO 8: Reintentar sincronización pendiente
    public void reintentarSincronizacionPendiente() {
        executorService.execute(() -> {
            try {
                if (!tieneConexionInternet()) {
                    Log.d(TAG, "📶 Sin conexión - No se puede reintentar sincronización");
                    return;
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getEmail() != null) {
                    String email = user.getEmail();
                    UsuarioEntity usuario = usuarioRepository.getUsuarioByEmailSync(email);

                    if (usuario != null && !usuario.sincronizado) {
                        Log.d(TAG, "🔄 Reintentando sincronización pendiente para: " + email);
                        boolean exitoFirestore = actualizarPerfilCompletoEnFirestore(email, usuario);

                        if (exitoFirestore) {
                            usuario.sincronizado = true;
                            usuarioRepository.actualizarPerfil(usuario);
                            Log.d(TAG, "✅✅✅ SINCRONIZACIÓN PENDIENTE COMPLETADA");

                            // Actualizar LiveData
                            usuarioActual.postValue(usuario);
                            sincronizacionPendiente.postValue(false);

                            // Notificar éxito
                            mostrarToastEnUI("✅ Sincronización completada");
                        } else {
                            Log.w(TAG, "⚠️ Sincronización pendiente falló nuevamente");
                        }
                    } else {
                        Log.d(TAG, "✅ No hay sincronizaciones pendientes");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error en reintento sincronización: " + e.getMessage());
            }
        });
    }

    // 🔄 ACTUALIZAR PERFIL COMPLETO EN ROOM (VERSIÓN ROBUSTA - SIN CAMBIOS DEL PASO 7)
    private boolean actualizarPerfilCompletoEnRoom(String email, UsuarioEntity usuarioActualizado) {
        try {
            Log.d(TAG, "🔄 ROOM: Actualizando para: " + email);

            // Obtener usuario existente
            UsuarioEntity usuarioExistente = usuarioRepository.getUsuarioByEmailSync(email);

            if (usuarioExistente != null) {
                Log.d(TAG, "✅ ROOM: Usuario encontrado, actualizando...");

                // Actualizar todos los campos
                usuarioExistente.nombreCompleto = usuarioActualizado.nombreCompleto != null ? usuarioActualizado.nombreCompleto : usuarioExistente.nombreCompleto;
                usuarioExistente.fechaNacimiento = usuarioActualizado.fechaNacimiento != null ? usuarioActualizado.fechaNacimiento : usuarioExistente.fechaNacimiento;
                usuarioExistente.genero = usuarioActualizado.genero != null ? usuarioActualizado.genero : usuarioExistente.genero;
                usuarioExistente.telefono = usuarioActualizado.telefono != null ? usuarioActualizado.telefono : usuarioExistente.telefono;
                usuarioExistente.direccion = usuarioActualizado.direccion != null ? usuarioActualizado.direccion : usuarioExistente.direccion;
                usuarioExistente.tipoSangre = usuarioActualizado.tipoSangre != null ? usuarioActualizado.tipoSangre : usuarioExistente.tipoSangre;
                usuarioExistente.alergias = usuarioActualizado.alergias != null ? usuarioActualizado.alergias : usuarioExistente.alergias;
                usuarioExistente.condicionesMedicas = usuarioActualizado.condicionesMedicas != null ? usuarioActualizado.condicionesMedicas : usuarioExistente.condicionesMedicas;
                usuarioExistente.medicamentosActuales = usuarioActualizado.medicamentosActuales != null ? usuarioActualizado.medicamentosActuales : usuarioExistente.medicamentosActuales;
                // sincronizado se actualiza después según el resultado

                usuarioRepository.actualizarPerfil(usuarioExistente);

                Log.d(TAG, "✅ ROOM: Actualización exitosa");
                return true;

            } else {
                Log.d(TAG, "🆕 ROOM: Creando nuevo usuario...");

                // Crear nuevo usuario
                UsuarioEntity nuevoUsuario = new UsuarioEntity();
                nuevoUsuario.email = email;
                nuevoUsuario.nombreCompleto = usuarioActualizado.nombreCompleto;
                nuevoUsuario.fechaNacimiento = usuarioActualizado.fechaNacimiento;
                nuevoUsuario.genero = usuarioActualizado.genero;
                nuevoUsuario.telefono = usuarioActualizado.telefono;
                nuevoUsuario.direccion = usuarioActualizado.direccion;
                nuevoUsuario.tipoSangre = usuarioActualizado.tipoSangre;
                nuevoUsuario.alergias = usuarioActualizado.alergias;
                nuevoUsuario.condicionesMedicas = usuarioActualizado.condicionesMedicas;
                nuevoUsuario.medicamentosActuales = usuarioActualizado.medicamentosActuales;
                // sincronizado se actualiza después según el resultado

                usuarioRepository.insert(nuevoUsuario);

                Log.d(TAG, "✅ ROOM: Nuevo usuario creado");
                return true;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ ROOM: Error: " + e.getMessage());
            return false;
        }
    }

    // 🎯 ACTUALIZAR PERFIL COMPLETO EN FIRESTORE (MEJORADO PARA PASO 8)
    private boolean actualizarPerfilCompletoEnFirestore(String email, UsuarioEntity usuario) {
        try {
            Log.d(TAG, "🔄 FIRESTORE: Actualizando para: " + email);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> updates = new HashMap<>();

            // Actualizar todos los campos
            updates.put("email", usuario.email);
            updates.put("nombreCompleto", usuario.nombreCompleto != null ? usuario.nombreCompleto : "");
            updates.put("telefono", usuario.telefono != null ? usuario.telefono : "");
            updates.put("direccion", usuario.direccion != null ? usuario.direccion : "");
            updates.put("tipoSangre", usuario.tipoSangre != null ? usuario.tipoSangre : "");
            updates.put("fechaNacimiento", usuario.fechaNacimiento != null ? usuario.fechaNacimiento : "");
            updates.put("genero", usuario.genero != null ? usuario.genero : "");
            updates.put("alergias", usuario.alergias != null ? usuario.alergias : "");
            updates.put("condicionesMedicas", usuario.condicionesMedicas != null ? usuario.condicionesMedicas : "");
            updates.put("medicamentosActuales", usuario.medicamentosActuales != null ? usuario.medicamentosActuales : "");
            updates.put("fechaActualizacion", System.currentTimeMillis());
            updates.put("sincronizado", true);

            final boolean[] exito = {false};
            final CountDownLatch latch = new CountDownLatch(1);

            db.collection("usuarios").document(email)
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ FIRESTORE: Actualización exitosa");
                        exito[0] = true;
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ FIRESTORE: Error: " + e.getMessage());
                        exito[0] = false;
                        latch.countDown();
                    });

            try {
                latch.await(10, TimeUnit.SECONDS);
                return exito[0];
            } catch (InterruptedException e) {
                Log.e(TAG, "❌ FIRESTORE: Interrupción: " + e.getMessage());
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ FIRESTORE: Error general: " + e.getMessage());
            return false;
        }
    }

    // 🔄 ACTUALIZAR DISPLAY NAME EN FIREBASE AUTH (SIN CAMBIOS)
    private void actualizarDisplayNameFirebaseAuth(String nuevoNombre) {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && nuevoNombre != null) {
                com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                        new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(nuevoNombre)
                                .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "✅ AUTH: Display name actualizado: " + nuevoNombre);
                            } else {
                                Log.e(TAG, "❌ AUTH: Error actualizando display name");
                            }
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ AUTH: Error: " + e.getMessage());
        }
    }

    // 📥 CARGAR USUARIO ACTUAL (MEJORADO PARA PASO 8)
    private void cargarUsuarioActual() {
        executorService.execute(() -> {
            try {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null && firebaseUser.getEmail() != null) {
                    String email = firebaseUser.getEmail();

                    // FORZAR CARGA DESDE ROOM
                    UsuarioEntity usuario = usuarioRepository.getUsuarioByEmailSync(email);
                    if (usuario != null) {
                        Log.d(TAG, "✅✅✅ USUARIO CARGADO DESDE ROOM:");
                        Log.d(TAG, "   - Nombre: " + usuario.nombreCompleto);
                        Log.d(TAG, "   - Teléfono: " + usuario.telefono);
                        Log.d(TAG, "   - Dirección: " + usuario.direccion);
                        Log.d(TAG, "   - Tipo sangre: " + usuario.tipoSangre);
                        Log.d(TAG, "   - Sincronizado: " + usuario.sincronizado);

                        usuarioActual.postValue(usuario);
                        sincronizacionPendiente.postValue(!usuario.sincronizado); // ✅ NUEVO PARA PASO 8
                    } else {
                        Log.w(TAG, "⚠️ Usuario no encontrado en Room, creando básico...");
                        UsuarioEntity nuevoUsuario = new UsuarioEntity();
                        nuevoUsuario.email = email;
                        nuevoUsuario.nombreCompleto = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Usuario";
                        nuevoUsuario.sincronizado = true;
                        usuarioRepository.insert(nuevoUsuario);
                        usuarioActual.postValue(nuevoUsuario);
                        sincronizacionPendiente.postValue(false); // ✅ NUEVO PARA PASO 8
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error cargando usuario actual: " + e.getMessage());
            }
        });
    }

    // 🎯 OBTENER USUARIO ACTUAL (Sincrónico) - VERSIÓN DEFINITIVA (SIN CAMBIOS)
    public UsuarioEntity getUsuarioActual() {
        try {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null && firebaseUser.getEmail() != null) {
                String email = firebaseUser.getEmail();

                // OBTENER SIEMPRE DE ROOM
                UsuarioEntity usuarioRoom = usuarioRepository.getUsuarioByEmailSync(email);
                if (usuarioRoom != null) {
                    Log.d(TAG, "✅ getUsuarioActual(): Datos COMPLETOS desde Room");
                    return usuarioRoom;
                }

                // Solo si no existe en Room, crear básico
                Log.w(TAG, "⚠️ getUsuarioActual(): Usuario no encontrado en Room");
                UsuarioEntity nuevoUsuario = new UsuarioEntity();
                nuevoUsuario.email = email;
                nuevoUsuario.nombreCompleto = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Usuario";
                return nuevoUsuario;
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error en getUsuarioActual(): " + e.getMessage());
            return null;
        }
    }

    // 🔐 LOGIN (SIN CAMBIOS)
    public void login(String email, String password) {
        executorService.execute(() -> {
            try {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "✅ Login exitoso");
                                cargarUsuarioActual();
                                authResult.postValue(new AuthResult(true, "Login exitoso"));
                            } else {
                                authResult.postValue(new AuthResult(false, "Credenciales incorrectas"));
                            }
                        });
            } catch (Exception e) {
                authResult.postValue(new AuthResult(false, "Error interno"));
            }
        });
    }

    // 📝 REGISTRO (SIN CAMBIOS)
    public void register(String nombreCompleto, String email, String password) {
        executorService.execute(() -> {
            try {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                guardarUsuarioEnFirestore(nombreCompleto, email);
                                guardarUsuarioEnRoom(nombreCompleto, email, password);
                                cargarUsuarioActual();
                                authResult.postValue(new AuthResult(true, "Registro exitoso"));
                            } else {
                                authResult.postValue(new AuthResult(false, "Error de registro"));
                            }
                        });
            } catch (Exception e) {
                authResult.postValue(new AuthResult(false, "Error interno"));
            }
        });
    }

    // 💾 GUARDAR USUARIO EN ROOM (SIN CAMBIOS)
    private void guardarUsuarioEnRoom(String nombreCompleto, String email, String password) {
        executorService.execute(() -> {
            try {
                UsuarioEntity usuario = new UsuarioEntity();
                usuario.email = email;
                usuario.nombreCompleto = nombreCompleto;
                usuario.password = password;
                usuario.sincronizado = true;
                usuarioRepository.insert(usuario);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error guardando en Room: " + e.getMessage());
            }
        });
    }

    // ☁️ GUARDAR USUARIO EN FIRESTORE (SIN CAMBIOS)
    private void guardarUsuarioEnFirestore(String nombreCompleto, String email) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("nombreCompleto", nombreCompleto);
            usuario.put("email", email);
            usuario.put("fechaRegistro", System.currentTimeMillis());
            db.collection("usuarios").document(email).set(usuario);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error en Firestore: " + e.getMessage());
        }
    }

    // 💾 GUARDAR CREDENCIALES (SIN CAMBIOS)
    public void saveCredentials(String email, String password) {
        executorService.execute(() -> {
            try {
                android.content.SharedPreferences prefs = getApplication()
                        .getSharedPreferences(CREDENTIALS_FILE, android.content.Context.MODE_PRIVATE);
                android.content.SharedPreferences.Editor editor = prefs.edit();
                editor.putString("email", email);
                editor.putString("password", password);
                editor.apply();
            } catch (Exception e) {
                Log.e(TAG, "❌ Error guardando credenciales: " + e.getMessage());
            }
        });
    }

    // 📥 CARGAR CREDENCIALES (SIN CAMBIOS)
    public String[] getSavedCredentials() {
        android.content.SharedPreferences prefs = getApplication()
                .getSharedPreferences(CREDENTIALS_FILE, android.content.Context.MODE_PRIVATE);
        String email = prefs.getString("email", null);
        String password = prefs.getString("password", null);
        return new String[]{email, password};
    }

    // 🗑️ LIMPIAR CREDENCIALES (SIN CAMBIOS)
    public void clearCredentials() {
        executorService.execute(() -> {
            try {
                android.content.SharedPreferences prefs = getApplication()
                        .getSharedPreferences(CREDENTIALS_FILE, android.content.Context.MODE_PRIVATE);
                prefs.edit().clear().apply();
            } catch (Exception e) {
                Log.e(TAG, "❌ Error limpiando credenciales: " + e.getMessage());
            }
        });
    }

    // 🚪 LOGOUT (ACTUALIZADO PARA PASO 8)
    public void logout() {
        executorService.execute(() -> {
            try {
                FirebaseAuth.getInstance().signOut();
                clearCredentials();
                usuarioActual.postValue(null);
                sincronizacionPendiente.postValue(false); // ✅ NUEVO PARA PASO 8
            } catch (Exception e) {
                Log.e(TAG, "❌ Error en logout: " + e.getMessage());
            }
        });
    }

    // 🎯 VERIFICAR SI HAY USUARIO LOGUEADO (SIN CAMBIOS)
    public boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    // 📊 GETTERS PARA LIVEDATA (ACTUALIZADO PARA PASO 8)
    public MutableLiveData<AuthResult> getAuthResult() {
        return authResult;
    }

    public MutableLiveData<Boolean> getPerfilActualizado() {
        return perfilActualizado;
    }

    public MutableLiveData<UsuarioEntity> getUsuarioActualLiveData() {
        return usuarioActual;
    }

    // ✅ NUEVO GETTER PARA PASO 8
    public MutableLiveData<Boolean> getSincronizacionPendiente() {
        return sincronizacionPendiente;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    // 🔹 CLASE INTERNA AuthResult (SIN CAMBIOS)
    public static class AuthResult {
        private final boolean success;
        private final String message;

        public AuthResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}