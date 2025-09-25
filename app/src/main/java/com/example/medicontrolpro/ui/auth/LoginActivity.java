package com.example.medicontrolpro.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.medicontrolpro.MainActivity;
import com.example.medicontrolpro.R;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private EditText editUsername, editPassword;
    private CheckBox checkRememberMe;
    private Button btnLogin;
    private TextView textRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Verificar si ya está logueado
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        if (authViewModel.isLoggedIn()) {
            startMainActivity();
            return;
        }

        initViews();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        checkRememberMe = findViewById(R.id.check_remember_me);
        btnLogin = findViewById(R.id.btn_login);
        textRegister = findViewById(R.id.text_register);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        textRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        authViewModel.getAuthResult().observe(this, result -> {
            if (result != null) {
                if (result.isSuccess()) {
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    startMainActivity();
                } else {
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void attemptLogin() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty()) {
            editUsername.setError("Ingrese su nombre de usuario");
            return;
        }

        if (password.isEmpty()) {
            editPassword.setError("Ingrese su contraseña");
            return;
        }

        authViewModel.login(username, password);
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}