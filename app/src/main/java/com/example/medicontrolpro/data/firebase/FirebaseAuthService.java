package com.example.medicontrolpro.data.firebase;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FirebaseAuthService {
    private static final String TAG = "FirebaseAuthService";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    public FirebaseAuthService() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public Task<AuthResult> registerUser(String email, String password, String nombreCompleto) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), email, nombreCompleto);
                            Log.d(TAG, "Usuario registrado en Firebase: " + email);
                        }
                    }
                });
    }

    public Task<AuthResult> loginUser(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    private void saveUserToFirestore(String userId, String email, String nombreCompleto) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("nombreCompleto", nombreCompleto);
        user.put("fechaCreacion", System.currentTimeMillis());
        user.put("sincronizado", true);

        firestore.collection("usuarios")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Usuario guardado en Firestore: " + email))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al guardar usuario en Firestore: " + e.getMessage()));
    }

    public void updateUserProfile(String userId, String nuevoNombre) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombreCompleto", nuevoNombre);
        updates.put("fechaActualizacion", System.currentTimeMillis());

        firestore.collection("usuarios")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Perfil actualizado en Firestore"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar perfil: " + e.getMessage()));
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
}