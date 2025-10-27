package com.example.medicontrolpro;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.medicontrolpro.databinding.ActivityMainBinding;
import com.example.medicontrolpro.utils.NotificationHelper;
import com.example.medicontrolpro.ui.auth.AuthViewModel;
import com.example.medicontrolpro.ui.auth.LoginActivity;
import com.example.medicontrolpro.ui.maps.MapaActivity; // ✅ IMPORT AGREGADO
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ VERIFICACIÓN CORREGIDA - Usando FirebaseAuth directamente
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startLoginActivity();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar ViewModel después de verificar autenticación
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Crear canal de notificaciones
        NotificationHelper.createNotificationChannel(this);

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Función de ayuda pronto disponible", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Passing each menu ID as set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_citas, R.id.nav_expediente, R.id.nav_doctores, R.id.nav_perfil)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Configuraciones
            Snackbar.make(binding.getRoot(), "Configuraciones - Próximamente", Snackbar.LENGTH_SHORT).show();
            return true;

        } else if (id == R.id.action_mapa) {
            // ✅ NUEVO: Navegar al mapa
            try {
                Intent mapaIntent = new Intent(this, MapaActivity.class);
                startActivity(mapaIntent);
            } catch (Exception e) {
                Snackbar.make(binding.getRoot(), "Error al abrir el mapa", Snackbar.LENGTH_SHORT).show();
            }
            return true;

        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // ✅ MÉTODO LOGOUT CORREGIDO
    private void logout() {
        // Cerrar sesión en Firebase Auth directamente
        FirebaseAuth.getInstance().signOut();
        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}