package com.example.medicontrolpro.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.medicontrolpro.R;

public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private EditText editUsername, editPassword, editFirstName, editLastName;
    private EditText editDui, editEmail, editPhoneNumber;
    private Button btnRegister;
    private TextView textLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        initViews();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        editFirstName = findViewById(R.id.edit_first_name);
        editLastName = findViewById(R.id.edit_last_name);
        editDui = findViewById(R.id.edit_dui);
        editEmail = findViewById(R.id.edit_email);
        editPhoneNumber = findViewById(R.id.edit_phone_number);
        btnRegister = findViewById(R.id.btn_register);
        textLogin = findViewById(R.id.text_login);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        textLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void observeViewModel() {
        authViewModel.getAuthResult().observe(this, result -> {
            if (result != null) {
                if (result.isSuccess()) {
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    finish(); // Volver al login
                } else {
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void attemptRegister() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String dui = editDui.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phoneNumber = editPhoneNumber.getText().toString().trim();

        if (!validateInputs(username, password, firstName, lastName, dui, email, phoneNumber)) {
            return;
        }

        User user = new User(username, password, firstName, lastName, dui, email, phoneNumber);
        authViewModel.register(user);
    }

    private boolean validateInputs(String username, String password, String firstName,
                                   String lastName, String dui, String email, String phoneNumber) {
        boolean isValid = true;

        if (username.isEmpty()) {
            editUsername.setError("Ingrese un nombre de usuario");
            isValid = false;
        }

        if (password.isEmpty()) {
            editPassword.setError("Ingrese una contraseña");
            isValid = false;
        } else if (password.length() < 6) {
            editPassword.setError("La contraseña debe tener al menos 6 caracteres");
            isValid = false;
        }

        if (firstName.isEmpty()) {
            editFirstName.setError("Ingrese su nombre");
            isValid = false;
        }

        if (lastName.isEmpty()) {
            editLastName.setError("Ingrese su apellido");
            isValid = false;
        }

        if (dui.isEmpty()) {
            editDui.setError("Ingrese su DUI");
            isValid = false;
        } else if (dui.length() != 9) {
            editDui.setError("El DUI debe tener 9 dígitos");
            isValid = false;
        }

        if (email.isEmpty()) {
            editEmail.setError("Ingrese su email");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Ingrese un email válido");
            isValid = false;
        }

        if (phoneNumber.isEmpty()) {
            editPhoneNumber.setError("Ingrese su número de teléfono");
            isValid = false;
        }

        return isValid;
    }
}