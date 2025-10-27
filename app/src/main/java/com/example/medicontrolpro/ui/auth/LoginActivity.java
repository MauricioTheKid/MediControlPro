package com.example.medicontrolpro.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.medicontrolpro.R;
import com.example.medicontrolpro.MainActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editPassword;
    private CheckBox checkRecordarme;
    private Button btnLogin;
    private TextView textRegister;

    private AuthViewModel authViewModel;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d(TAG, "🏁 Iniciando LoginActivity...");

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initViews();
        setupObservers();

        Log.d(TAG, "🏁 LoginActivity inicializado correctamente");
    }

    private void initViews() {
        try {
            Log.d(TAG, "👀 Inicializando vistas...");

            // ✅ USANDO LOS IDs DE TU XML
            editEmail = findViewById(R.id.edit_email);
            editPassword = findViewById(R.id.edit_password);
            checkRecordarme = findViewById(R.id.chip_remember_me); // CheckBox normal
            btnLogin = findViewById(R.id.btn_login);
            textRegister = findViewById(R.id.text_register);

            // ✅ VERIFICAR QUE SE ENCONTRARON TODAS LAS VISTAS
            if (editEmail == null) Log.e(TAG, "❌ editEmail NO encontrado");
            if (editPassword == null) Log.e(TAG, "❌ editPassword NO encontrado");
            if (checkRecordarme == null) Log.e(TAG, "❌ chip_remember_me NO encontrado");
            if (btnLogin == null) Log.e(TAG, "❌ btnLogin NO encontrado");
            if (textRegister == null) Log.e(TAG, "❌ text_register NO encontrado");

            setupClickListeners();

            Log.d(TAG, "👀 Vistas inicializadas correctamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR en initViews: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error al inicializar interfaz", Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        // Botón Login
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> attemptLogin());
        }

        // Texto de Registro
        if (textRegister != null) {
            textRegister.setOnClickListener(v -> navigateToRegister());
        }
    }

    private void setupObservers() {
        authViewModel.getAuthResult().observe(this, authResult -> {
            if (authResult != null) {
                if (authResult.isSuccess()) {
                    Log.d(TAG, "✅ Login exitoso, navegando a MainActivity");
                    navigateToMain();
                } else {
                    Log.d(TAG, "❌ Error en login: " + authResult.getMessage());
                    showError(authResult.getMessage());

                    // Re-enable login button on error
                    if (btnLogin != null) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Iniciar Sesión");
                    }
                }
            }
        });

        loadSavedCredentials();
    }

    private void attemptLogin() {
        if (editEmail == null || editPassword == null) {
            showError("Error en la interfaz de usuario");
            return;
        }

        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        Log.d(TAG, "🔐 Intentando login para: " + email);

        // ✅ VALIDACIONES SEGÚN RÚBRICA
        if (email.isEmpty()) {
            showError("Ingrese su email");
            editEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Ingrese un email válido");
            editEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Ingrese su contraseña");
            editPassword.requestFocus();
            return;
        }

        if (password.length() < 8) { // ✅ 8 CARACTERES SEGÚN RÚBRICA
            showError("La contraseña debe tener al menos 8 caracteres");
            editPassword.requestFocus();
            return;
        }

        if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            showError("La contraseña debe ser alfanumérica (letras y números)");
            editPassword.requestFocus();
            return;
        }

        // Disable login button during attempt
        if (btnLogin != null) {
            btnLogin.setEnabled(false);
            btnLogin.setText("Iniciando sesión...");
        }

        // ✅ CHIP PARA RECORDAR CREDENCIALES (SharedPreferences)
        if (checkRecordarme != null && checkRecordarme.isChecked()) {
            authViewModel.saveCredentials(email, password);
            Log.d(TAG, "💾 Credenciales guardadas para: " + email);
        } else {
            authViewModel.clearCredentials();
            Log.d(TAG, "🗑️ Credenciales limpiadas");
        }

        authViewModel.login(email, password);
    }

    private void loadSavedCredentials() {
        Log.d(TAG, "🔍 Buscando credenciales guardadas...");

        String[] credentials = authViewModel.getSavedCredentials();
        String savedEmail = credentials[0];
        String savedPassword = credentials[1];

        if (savedEmail != null && savedPassword != null) {
            if (editEmail != null) editEmail.setText(savedEmail);
            if (editPassword != null) editPassword.setText(savedPassword);
            if (checkRecordarme != null) checkRecordarme.setChecked(true);
            Log.d(TAG, "📝 Credenciales cargadas: " + savedEmail);
        } else {
            Log.d(TAG, "ℹ️ No hay credenciales guardadas previas");
        }
    }

    private void navigateToMain() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Log.d(TAG, "✅ Navegación a MainActivity exitosa");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al navegar a MainActivity: " + e.getMessage());
            Toast.makeText(this, "Error al iniciar aplicación", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToRegister() {
        try {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            Log.d(TAG, "📝 Navegando a RegisterActivity");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al navegar a RegisterActivity: " + e.getMessage());
            Toast.makeText(this, "Error al abrir registro", Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "❌ Error: " + message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔚 LoginActivity destruido");
    }
}