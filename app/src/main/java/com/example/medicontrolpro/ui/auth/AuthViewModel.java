package com.example.medicontrolpro.ui.auth;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AuthViewModel extends AndroidViewModel {
    private AuthRepository authRepository;
    private MutableLiveData<AuthResult> authResult = new MutableLiveData<>();
    private MutableLiveData<User> currentUser = new MutableLiveData<>();

    public AuthViewModel(Application application) {
        super(application);
        authRepository = new AuthRepository(application);

        // Cargar usuario actual si existe
        User user = authRepository.getLoggedInUser();
        if (user != null) {
            currentUser.setValue(user);
        }
    }

    public LiveData<AuthResult> getAuthResult() {
        return authResult;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void login(String username, String password) {
        User user = authRepository.loginUser(username, password);
        if (user != null) {
            currentUser.setValue(user);
            authResult.setValue(new AuthResult(true, "Login exitoso"));
        } else {
            authResult.setValue(new AuthResult(false, "Usuario o contraseña incorrectos"));
        }
    }

    public void register(User user) {
        boolean success = authRepository.registerUser(user);
        if (success) {
            authResult.setValue(new AuthResult(true, "Registro exitoso"));
        } else {
            authResult.setValue(new AuthResult(false, "El usuario ya existe"));
        }
    }

    public void logout() {
        authRepository.logout();
        currentUser.setValue(null);
    }

    public boolean isLoggedIn() {
        return authRepository.isUserLoggedIn();
    }

    public static class AuthResult {
        private boolean success;
        private String message;

        public AuthResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}