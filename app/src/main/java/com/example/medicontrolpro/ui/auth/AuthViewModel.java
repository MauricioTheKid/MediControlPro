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
    private MutableLiveData<Boolean> sincronizacionPendiente = new MutableLiveData<>();

    private static final String TAG = "AuthViewModel";
    private static final String CREDENTIALS_FILE = "UserCredentials";

    public AuthViewModel(Application application) {
        super(application);
        usuarioRepository = new UsuarioRepository(application);
        executorService = Executors.newSingleThreadExecutor();
        Log.d(TAG, "AuthViewModel inicializado");

        cargarUsuarioActual();
    }

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

    // ✅ MÉTODO CORREGIDO - MANEJA RUTAS DE ARCHIVO
    public void actualizarFotoPerfil(String filePath) {
        Log.d(TAG, "🔄 INICIANDO ACTUALIZACIÓN DE FOTO - Ruta: " + filePath);

        executorService.execute(() -> {
            try {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getEmail() != null) {
                    String emailFirebase = user.getEmail();
                    Log.d(TAG, "🔥 Actualizando foto para: " + emailFirebase);

                    // ✅ OBTENER USUARIO ACTUAL CON TODOS SUS DATOS
                    UsuarioEntity usuarioExistente = usuarioRepository.getUsuarioByEmailSync(emailFirebase);

                    if (usuarioExistente == null) {
                        Log.w(TAG, "⚠️ Usuario no encontrado en Room, creando nuevo usuario...");

                        // ✅ CREAR NUEVO USUARIO MANTENIENDO LOS DATOS DEL USUARIO FIREBASE
                        usuarioExistente = new UsuarioEntity();
                        usuarioExistente.email = emailFirebase;
                        usuarioExistente.nombreCompleto = user.getDisplayName() != null ? user.getDisplayName() : "Usuario";
                        usuarioExistente.fotoPerfil = filePath; // ✅ GUARDAR RUTA DEL ARCHIVO
                        usuarioExistente.sincronizado = false;

                        usuarioRepository.insert(usuarioExistente);
                        Log.d(TAG, "✅ Nuevo usuario creado en Room con foto");
                    } else {
                        // ✅ MANTENER TODOS LOS DATOS EXISTENTES, SOLO ACTUALIZAR FOTO
                        Log.d(TAG, "📸 Usuario existente encontrado, actualizando SOLO la foto...");
                        Log.d(TAG, "   - Nombre actual: " + usuarioExistente.nombreCompleto);
                        Log.d(TAG, "   - Teléfono actual: " + usuarioExistente.telefono);
                        Log.d(TAG, "   - Dirección actual: " + usuarioExistente.direccion);

                        // ✅ SOLO ACTUALIZAR EL CAMPO DE FOTO, MANTENER EL RESTO
                        String fotoAnterior = usuarioExistente.fotoPerfil;
                        usuarioExistente.fotoPerfil = filePath; // ✅ GUARDAR RUTA DEL ARCHIVO
                        usuarioRepository.actualizarPerfil(usuarioExistente);

                        Log.d(TAG, "✅ Foto actualizada: " + fotoAnterior + " -> " + filePath);
                        Log.d(TAG, "✅ Todos los demás campos preservados");
                    }

                    // ✅ VERIFICAR QUE SE MANTUVIERON LOS DATOS
                    UsuarioEntity usuarioVerificado = usuarioRepository.getUsuarioByEmailSync(emailFirebase);
                    if (usuarioVerificado != null) {
                        Log.d(TAG, "✅✅✅ VERIFICACIÓN POST-ACTUALIZACIÓN:");
                        Log.d(TAG, "   - Nombre: " + usuarioVerificado.nombreCompleto);
                        Log.d(TAG, "   - Teléfono: " + usuarioVerificado.telefono);
                        Log.d(TAG, "   - Dirección: " + usuarioVerificado.direccion);
                        Log.d(TAG, "   - Foto: " + usuarioVerificado.fotoPerfil);

                        if (usuarioVerificado.fotoPerfil != null && usuarioVerificado.fotoPerfil.equals(filePath)) {
                            Log.d(TAG, "✅✅✅ FOTO GUARDADA CORRECTAMENTE EN ROOM");
                        }
                    }

                    // VERIFICAR CONEXIÓN
                    boolean tieneInternet = tieneConexionInternet();
                    Log.d(TAG, "📶 Estado conexión: " + (tieneInternet ? "CONECTADO" : "OFFLINE"));

                    // ✅ ACTUALIZAR LIVEDATA INMEDIATAMENTE
                    UsuarioEntity usuarioRecargado = usuarioRepository.getUsuarioByEmailSync(emailFirebase);
                    if (usuarioRecargado != null) {
                        this.usuarioActual.postValue(usuarioRecargado);
                        Log.d(TAG, "🎯 LiveData actualizado con datos COMPLETOS de Room");
                    }

                    if (tieneInternet) {
                        // ✅ SINCRONIZAR SOLO LA FOTO CON FIRESTORE
                        Log.d(TAG, "🔄 Sincronizando SOLO foto con Firestore...");
                        boolean exitoFirestore = actualizarSoloFotoEnFirestore(emailFirebase, filePath, usuarioExistente);

                        if (exitoFirestore) {
                            Log.d(TAG, "✅✅✅ SINCRONIZACIÓN DE FOTO COMPLETA");
                            actualizarEstadoSincronizacionRoom(emailFirebase, true);
                            sincronizacionPendiente.postValue(false);
                            mostrarToastEnUI("✅ Foto actualizada correctamente");

                            perfilActualizado.postValue(true);
                        } else {
                            Log.w(TAG, "⚠️ Error sincronizando foto - Marcando como pendiente");
                            actualizarEstadoSincronizacionRoom(emailFirebase, false);
                            sincronizacionPendiente.postValue(true);
                            mostrarToastEnUI("✅ Foto guardada local - Error de sincronización");

                            perfilActualizado.postValue(true);
                        }
                    } else {
                        Log.w(TAG, "📶 Sin conexión - Marcando como pendiente");
                        actualizarEstadoSincronizacionRoom(emailFirebase, false);
                        sincronizacionPendiente.postValue(true);
                        mostrarToastEnUI("✅ Foto guardada local - Sincronizará con conexión");

                        perfilActualizado.postValue(true);
                    }

                } else {
                    Log.e(TAG, "❌ No hay usuario autenticado");
                    mostrarToastEnUI("❌ No hay usuario autenticado");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ ERROR actualizando foto: " + e.getMessage());
                mostrarToastEnUI("❌ Error al actualizar foto");
            }
        });
    }

    // ✅ NUEVO MÉTODO: Actualizar solo la foto en Firestore sin afectar otros campos
    private boolean actualizarSoloFotoEnFirestore(String email, String filePath, UsuarioEntity usuarioExistente) {
        try {
            Log.d(TAG, "🔄 FIRESTORE: Actualizando SOLO foto para: " + email);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> updates = new HashMap<>();

            // ✅ SOLO ACTUALIZAR LA FOTO Y CAMPOS DE CONTROL
            updates.put("fotoPerfil", filePath != null ? filePath : "");
            updates.put("fechaActualizacion", System.currentTimeMillis());
            updates.put("sincronizado", true);

            // ✅ PRESERVAR TODOS LOS DEMÁS CAMPOS EXISTENTES
            if (usuarioExistente != null) {
                updates.put("nombreCompleto", usuarioExistente.nombreCompleto != null ? usuarioExistente.nombreCompleto : "");
                updates.put("telefono", usuarioExistente.telefono != null ? usuarioExistente.telefono : "");
                updates.put("direccion", usuarioExistente.direccion != null ? usuarioExistente.direccion : "");
                updates.put("tipoSangre", usuarioExistente.tipoSangre != null ? usuarioExistente.tipoSangre : "");
                updates.put("fechaNacimiento", usuarioExistente.fechaNacimiento != null ? usuarioExistente.fechaNacimiento : "");
                updates.put("genero", usuarioExistente.genero != null ? usuarioExistente.genero : "");
                updates.put("alergias", usuarioExistente.alergias != null ? usuarioExistente.alergias : "");
                updates.put("condicionesMedicas", usuarioExistente.condicionesMedicas != null ? usuarioExistente.condicionesMedicas : "");
                updates.put("medicamentosActuales", usuarioExistente.medicamentosActuales != null ? usuarioExistente.medicamentosActuales : "");
            }

            final boolean[] exito = {false};
            final CountDownLatch latch = new CountDownLatch(1);

            db.collection("usuarios").document(email)
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ FIRESTORE: Solo foto actualizada exitosamente");
                        exito[0] = true;
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ FIRESTORE: Error actualizando solo foto: " + e.getMessage());
                        exito[0] = false;
                        latch.countDown();
                    });

            try {
                latch.await(10, TimeUnit.SECONDS);
                return exito[0];
            } catch (InterruptedException e) {
                Log.e(TAG, "❌ FIRESTORE: Interrupción actualizando solo foto: " + e.getMessage());
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ FIRESTORE: Error general actualizando solo foto: " + e.getMessage());
            return false;
        }
    }

    public void actualizarPerfilCompleto(UsuarioEntity usuarioActualizado) {
        Log.d(TAG, "🎯 INICIANDO ACTUALIZACION COMPLETA DE PERFIL");

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
                    Log.d(TAG, "🔥 Actualizando perfil completo para: " + emailFirebase);

                    usuarioActualizado.email = emailFirebase;

                    boolean tieneInternet = tieneConexionInternet();
                    Log.d(TAG, "📶 Estado conexión: " + (tieneInternet ? "CONECTADO" : "OFFLINE"));

                    Log.d(TAG, "🔄 Actualizando en Room...");
                    boolean exitoRoom = actualizarPerfilCompletoEnRoom(emailFirebase, usuarioActualizado);

                    if (exitoRoom) {
                        Log.d(TAG, "✅ ACTUALIZACIÓN EXITOSA EN ROOM");

                        if (tieneInternet) {
                            Log.d(TAG, "🔄 Sincronizando con Firestore...");
                            boolean exitoFirestore = actualizarPerfilCompletoEnFirestore(emailFirebase, usuarioActualizado);

                            if (exitoFirestore) {
                                Log.d(TAG, "✅✅✅ SINCRONIZACIÓN COMPLETA");
                                actualizarEstadoSincronizacionRoom(emailFirebase, true);
                                sincronizacionPendiente.postValue(false);
                                mostrarToastEnUI("✅ Perfil actualizado correctamente");
                                perfilActualizado.postValue(true);
                            } else {
                                Log.w(TAG, "⚠️ Error Firestore - Marcando como pendiente");
                                actualizarEstadoSincronizacionRoom(emailFirebase, false);
                                sincronizacionPendiente.postValue(true);
                                mostrarToastEnUI("✅ Guardado local - Error de sincronización");
                                perfilActualizado.postValue(true);
                            }
                        } else {
                            Log.w(TAG, "📶 Sin conexión - Marcando como pendiente");
                            actualizarEstadoSincronizacionRoom(emailFirebase, false);
                            sincronizacionPendiente.postValue(true);
                            mostrarToastEnUI("✅ Guardado local - Sincronizará con conexión");
                            perfilActualizado.postValue(true);
                        }

                        UsuarioEntity usuarioRecargado = usuarioRepository.getUsuarioByEmailSync(emailFirebase);
                        if (usuarioRecargado != null) {
                            usuarioActual.postValue(usuarioRecargado);
                            Log.d(TAG, "🎯 LiveData actualizado con datos frescos de Room");
                        }

                    } else {
                        Log.e(TAG, "❌ ERROR EN ACTUALIZACIÓN ROOM");
                        perfilActualizado.postValue(false);
                        mostrarToastEnUI("❌ Error al actualizar perfil");
                    }

                } else {
                    Log.e(TAG, "❌ No hay usuario autenticado");
                    perfilActualizado.postValue(false);
                    mostrarToastEnUI("❌ No hay usuario autenticado");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ ERROR actualizando perfil: " + e.getMessage());
                perfilActualizado.postValue(false);
                mostrarToastEnUI("❌ Error al actualizar perfil");
            }
        });
    }

    private void mostrarToastEnUI(String mensaje) {
        if (getApplication() != null && getApplication().getMainExecutor() != null) {
            getApplication().getMainExecutor().execute(() -> {
                android.widget.Toast.makeText(getApplication(), mensaje, android.widget.Toast.LENGTH_LONG).show();
            });
        }
    }

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

    public void reintentarSincronizacionPendiente() {
        executorService.execute(() -> {
            try {
                if (!tieneConexionInternet()) {
                    Log.d(TAG, "📶 Sin conexión - No se puede reintentar");
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

                            usuarioActual.postValue(usuario);
                            sincronizacionPendiente.postValue(false);
                            mostrarToastEnUI("✅ Sincronización completada");
                        } else {
                            Log.w(TAG, "⚠️ Sincronización pendiente falló");
                        }
                    } else {
                        Log.d(TAG, "✅ No hay sincronizaciones pendientes");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error en reintento: " + e.getMessage());
            }
        });
    }

    private boolean actualizarPerfilCompletoEnRoom(String email, UsuarioEntity usuarioActualizado) {
        try {
            Log.d(TAG, "🔄 ROOM: Actualizando perfil completo para: " + email);

            UsuarioEntity usuarioExistente = usuarioRepository.getUsuarioByEmailSync(email);

            if (usuarioExistente != null) {
                Log.d(TAG, "✅ ROOM: Usuario encontrado, actualizando...");

                // ✅ ACTUALIZAR TODOS LOS CAMPOS MANTENIENDO LA FOTO SI NO SE PROVEE UNA NUEVA
                usuarioExistente.nombreCompleto = usuarioActualizado.nombreCompleto != null ? usuarioActualizado.nombreCompleto : usuarioExistente.nombreCompleto;
                usuarioExistente.fechaNacimiento = usuarioActualizado.fechaNacimiento != null ? usuarioActualizado.fechaNacimiento : usuarioExistente.fechaNacimiento;
                usuarioExistente.genero = usuarioActualizado.genero != null ? usuarioActualizado.genero : usuarioExistente.genero;
                usuarioExistente.telefono = usuarioActualizado.telefono != null ? usuarioActualizado.telefono : usuarioExistente.telefono;
                usuarioExistente.direccion = usuarioActualizado.direccion != null ? usuarioActualizado.direccion : usuarioExistente.direccion;
                usuarioExistente.tipoSangre = usuarioActualizado.tipoSangre != null ? usuarioActualizado.tipoSangre : usuarioExistente.tipoSangre;
                usuarioExistente.alergias = usuarioActualizado.alergias != null ? usuarioActualizado.alergias : usuarioExistente.alergias;
                usuarioExistente.condicionesMedicas = usuarioActualizado.condicionesMedicas != null ? usuarioActualizado.condicionesMedicas : usuarioExistente.condicionesMedicas;
                usuarioExistente.medicamentosActuales = usuarioActualizado.medicamentosActuales != null ? usuarioActualizado.medicamentosActuales : usuarioExistente.medicamentosActuales;

                // ✅ SOLO ACTUALIZAR FOTO SI SE PROVEE UNA NUEVA
                if (usuarioActualizado.fotoPerfil != null && !usuarioActualizado.fotoPerfil.isEmpty()) {
                    usuarioExistente.fotoPerfil = usuarioActualizado.fotoPerfil;
                }

                usuarioRepository.actualizarPerfil(usuarioExistente);

                Log.d(TAG, "✅ ROOM: Perfil completo actualizado exitosamente");
                return true;

            } else {
                Log.d(TAG, "🆕 ROOM: Creando nuevo usuario...");

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
                nuevoUsuario.fotoPerfil = usuarioActualizado.fotoPerfil != null ? usuarioActualizado.fotoPerfil : "";

                usuarioRepository.insert(nuevoUsuario);

                Log.d(TAG, "✅ ROOM: Nuevo usuario creado");
                return true;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ ROOM: Error: " + e.getMessage());
            return false;
        }
    }

    private boolean actualizarPerfilCompletoEnFirestore(String email, UsuarioEntity usuario) {
        try {
            Log.d(TAG, "🔄 FIRESTORE: Actualizando perfil completo para: " + email);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> updates = new HashMap<>();

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
            updates.put("fotoPerfil", usuario.fotoPerfil != null ? usuario.fotoPerfil : "");
            updates.put("fechaActualizacion", System.currentTimeMillis());
            updates.put("sincronizado", true);

            final boolean[] exito = {false};
            final CountDownLatch latch = new CountDownLatch(1);

            db.collection("usuarios").document(email)
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ FIRESTORE: Perfil completo actualizado exitosamente");
                        exito[0] = true;
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ FIRESTORE: Error actualizando perfil completo: " + e.getMessage());
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

    public void cargarUsuarioActual() {
        executorService.execute(() -> {
            try {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null && firebaseUser.getEmail() != null) {
                    String email = firebaseUser.getEmail();

                    UsuarioEntity usuario = usuarioRepository.getUsuarioByEmailSync(email);
                    if (usuario != null) {
                        Log.d(TAG, "✅✅✅ USUARIO CARGADO DESDE ROOM:");
                        Log.d(TAG, "   - Nombre: " + usuario.nombreCompleto);
                        Log.d(TAG, "   - Teléfono: " + usuario.telefono);
                        Log.d(TAG, "   - Dirección: " + usuario.direccion);
                        Log.d(TAG, "   - Foto: " + usuario.fotoPerfil);
                        Log.d(TAG, "   - Sincronizado: " + usuario.sincronizado);

                        usuarioActual.postValue(usuario);
                        sincronizacionPendiente.postValue(!usuario.sincronizado);
                    } else {
                        Log.w(TAG, "⚠️ Usuario no encontrado en Room, creando básico...");
                        UsuarioEntity nuevoUsuario = new UsuarioEntity();
                        nuevoUsuario.email = email;
                        nuevoUsuario.nombreCompleto = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Usuario";
                        nuevoUsuario.sincronizado = true;
                        nuevoUsuario.fotoPerfil = "";
                        usuarioRepository.insert(nuevoUsuario);
                        usuarioActual.postValue(nuevoUsuario);
                        sincronizacionPendiente.postValue(false);
                    }
                } else {
                    Log.w(TAG, "⚠️ No hay usuario autenticado");
                    usuarioActual.postValue(null);
                    sincronizacionPendiente.postValue(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error cargando usuario: " + e.getMessage());
                usuarioActual.postValue(null);
            }
        });
    }

    public UsuarioEntity getUsuarioActual() {
        try {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null && firebaseUser.getEmail() != null) {
                String email = firebaseUser.getEmail();

                UsuarioEntity usuarioRoom = usuarioRepository.getUsuarioByEmailSync(email);
                if (usuarioRoom != null) {
                    Log.d(TAG, "✅ getUsuarioActual(): Datos COMPLETOS desde Room");
                    return usuarioRoom;
                }

                Log.w(TAG, "⚠️ getUsuarioActual(): Usuario no encontrado, creando básico...");
                UsuarioEntity nuevoUsuario = new UsuarioEntity();
                nuevoUsuario.email = email;
                nuevoUsuario.nombreCompleto = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Usuario";
                nuevoUsuario.fotoPerfil = "";

                usuarioRepository.insert(nuevoUsuario);

                return nuevoUsuario;
            }
            Log.w(TAG, "⚠️ getUsuarioActual(): No hay usuario autenticado");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error en getUsuarioActual(): " + e.getMessage());
            return null;
        }
    }

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

    private void guardarUsuarioEnRoom(String nombreCompleto, String email, String password) {
        executorService.execute(() -> {
            try {
                UsuarioEntity usuario = new UsuarioEntity();
                usuario.email = email;
                usuario.nombreCompleto = nombreCompleto;
                usuario.password = password;
                usuario.sincronizado = true;
                usuario.fotoPerfil = "";
                usuarioRepository.insert(usuario);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error guardando en Room: " + e.getMessage());
            }
        });
    }

    private void guardarUsuarioEnFirestore(String nombreCompleto, String email) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("nombreCompleto", nombreCompleto);
            usuario.put("email", email);
            usuario.put("fotoPerfil", "");
            usuario.put("fechaRegistro", System.currentTimeMillis());
            db.collection("usuarios").document(email).set(usuario);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error en Firestore: " + e.getMessage());
        }
    }

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

    public String[] getSavedCredentials() {
        android.content.SharedPreferences prefs = getApplication()
                .getSharedPreferences(CREDENTIALS_FILE, android.content.Context.MODE_PRIVATE);
        String email = prefs.getString("email", null);
        String password = prefs.getString("password", null);
        return new String[]{email, password};
    }

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

    public void logout() {
        executorService.execute(() -> {
            try {
                FirebaseAuth.getInstance().signOut();
                clearCredentials();
                usuarioActual.postValue(null);
                sincronizacionPendiente.postValue(false);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error en logout: " + e.getMessage());
            }
        });
    }

    public boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public MutableLiveData<AuthResult> getAuthResult() {
        return authResult;
    }

    public MutableLiveData<Boolean> getPerfilActualizado() {
        return perfilActualizado;
    }

    public MutableLiveData<UsuarioEntity> getUsuarioActualLiveData() {
        return usuarioActual;
    }

    public MutableLiveData<Boolean> getSincronizacionPendiente() {
        return sincronizacionPendiente;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

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