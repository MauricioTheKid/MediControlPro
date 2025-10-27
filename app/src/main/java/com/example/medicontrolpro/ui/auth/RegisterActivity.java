package com.example.medicontrolpro.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.medicontrolpro.R;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editNombreCompleto, editEmail, editPassword, editConfirmPassword;
    private Button btnRegistro;
    private TextView textLogin;

    private AuthViewModel authViewModel;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.d(TAG, "📝 Iniciando RegisterActivity...");

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initViews();
        setupObservers();

        Log.d(TAG, "📝 RegisterActivity inicializado correctamente");
    }

    private void initViews() {
        try {
            Log.d(TAG, "👀 Inicializando vistas...");

            // ✅ USANDO LOS IDs DEL NUEVO XML
            editNombreCompleto = findViewById(R.id.edit_nombre_completo);
            editEmail = findViewById(R.id.edit_email);
            editPassword = findViewById(R.id.edit_password);
            editConfirmPassword = findViewById(R.id.edit_confirm_password); // ✅ AHORA SÍ EXISTE
            btnRegistro = findViewById(R.id.btn_registro);
            textLogin = findViewById(R.id.text_login);

            setupClickListeners();

            Log.d(TAG, "👀 Vistas inicializadas correctamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR en initViews: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error al inicializar interfaz", Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        btnRegistro.setOnClickListener(v -> attemptRegister());
        textLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void setupObservers() {
        authViewModel.getAuthResult().observe(this, authResult -> {
            if (authResult != null) {
                if (authResult.isSuccess()) {
                    Log.d(TAG, "✅ Registro exitoso, navegando a Login");
                    showSuccess("Registro exitoso. Ahora puede iniciar sesión.");

                    new android.os.Handler().postDelayed(() -> {
                        navigateToLogin();
                    }, 2000);

                } else {
                    Log.d(TAG, "❌ Error en registro: " + authResult.getMessage());
                    showError(authResult.getMessage());
                }
            }
        });
    }

    private void attemptRegister() {
        String nombreCompleto = editNombreCompleto.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        Log.d(TAG, "📝 Intentando registro para: " + email);

        // ✅ VALIDACIONES SEGÚN RÚBRICA
        if (nombreCompleto.isEmpty()) {
            showError("Ingrese su nombre completo");
            editNombreCompleto.requestFocus();
            return;
        }

        if (nombreCompleto.length() < 7) {
            showError("El nombre debe tener al menos 7 caracteres");
            editNombreCompleto.requestFocus();
            return;
        }

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

        if (confirmPassword.isEmpty()) {
            showError("Confirme su contraseña");
            editConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Las contraseñas no coinciden");
            editConfirmPassword.requestFocus();
            return;
        }

        btnRegistro.setEnabled(false);
        btnRegistro.setText("Registrando...");

        authViewModel.register(nombreCompleto, email, password);
    }

    private void navigateToLogin() {
        try {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            Log.d(TAG, "🔙 Navegando a LoginActivity");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al navegar a LoginActivity: " + e.getMessage());
            Toast.makeText(this, "Error al navegar", Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String message) {
        btnRegistro.setEnabled(true);
        btnRegistro.setText("Registrarse");
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "❌ Error: " + message);
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d(TAG, "✅ Éxito: " + message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔚 RegisterActivity destruido");
    }
}