package com.example.medicontrolpro.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicontrolpro.MainActivity;
import com.example.medicontrolpro.R;
import com.example.medicontrolpro.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 3000; // 3 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d(TAG, "🚀 SplashActivity iniciado");
        Log.d(TAG, "🎨 Mostrando splash screen con tema: Theme.MediControlPro.NoActionBar");

        // Ocultar la action bar si existe
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Configurar el tiempo de espera y navegación
        new Handler().postDelayed(() -> {
            checkAuthAndNavigate();
        }, SPLASH_DELAY);
    }

    private void checkAuthAndNavigate() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Log.d(TAG, "🔍 Verificando autenticación...");
        Log.d(TAG, "   - Usuario actual: " + (currentUser != null ? currentUser.getEmail() : "null"));

        if (currentUser != null) {
            Log.d(TAG, "✅ Usuario autenticado, navegando a MainActivity");
            navigateToMain();
        } else {
            Log.d(TAG, "❌ Usuario no autenticado, navegando a LoginActivity");
            navigateToLogin();
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
            Log.e(TAG, "❌ Error navegando a MainActivity: " + e.getMessage());
            navigateToLogin(); // Fallback a login si hay error
        }
    }

    private void navigateToLogin() {
        try {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Log.d(TAG, "✅ Navegación a LoginActivity exitosa");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navegando a LoginActivity: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔚 SplashActivity destruido");
    }
}