package com.example.medicontrolpro.ui.maps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.medicontrolpro.R;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapaActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private MaterialToolbar toolbar;
    private CardView cardInfo;
    private TextView textNombreLugar, textDireccion, textTelefono;
    private Button btnDirecciones;
    private FloatingActionButton fabMyLocation;

    // ✅ COORDENADAS EXACTAS DE PLAZA MUNDO APOPA (Ubicación muy reconocible)
    private final LatLng UBICACION_CONSULTORIO = new LatLng(13.8008, -89.1853);
    private final String NOMBRE_CONSULTORIO = "MediControlPro - Plaza Mundo Apopa";
    private final String DIRECCION_CONSULTORIO = "Plaza Mundo Apopa, Carretera Troncal del Norte, Apopa";
    private final String TELEFONO_CONSULTORIO = "Tel: 2222-2222";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        Log.d(TAG, "🎯 Iniciando MapaActivity...");

        inicializarVistas();
        configurarToolbar();
        inicializarMapa();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void inicializarVistas() {
        try {
            toolbar = findViewById(R.id.toolbar);
            cardInfo = findViewById(R.id.card_info);
            textNombreLugar = findViewById(R.id.text_nombre_lugar);
            textDireccion = findViewById(R.id.text_direccion);
            textTelefono = findViewById(R.id.text_telefono);
            btnDirecciones = findViewById(R.id.btn_direcciones);
            fabMyLocation = findViewById(R.id.fab_my_location);

            // Configurar información del consultorio
            textNombreLugar.setText(NOMBRE_CONSULTORIO);
            textDireccion.setText(DIRECCION_CONSULTORIO);
            textTelefono.setText(TELEFONO_CONSULTORIO);

            // Configurar botones
            btnDirecciones.setOnClickListener(v -> abrirGoogleMapsDirecciones());
            fabMyLocation.setOnClickListener(v -> centrarEnMiUbicacion());

            Log.d(TAG, "✅ Vistas inicializadas correctamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando vistas: " + e.getMessage());
            Toast.makeText(this, "Error al cargar el mapa", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarToolbar() {
        toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "← Regresando...");
            finish();
        });

        toolbar.setTitle("Ubicación MediControlPro");

        Log.d(TAG, "✅ Toolbar configurado correctamente");
    }

    private void inicializarMapa() {
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.main);

            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
                Log.d(TAG, "🗺️ Mapa inicializado, esperando callback...");
            } else {
                Log.e(TAG, "❌ No se pudo encontrar el fragmento del mapa");
                Toast.makeText(this, "Error: No se pudo cargar el mapa", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando mapa: " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "✅✅✅ onMapReady - Mapa listo para usar");

        mMap = googleMap;

        if (mMap == null) {
            Log.e(TAG, "❌ ERROR: GoogleMap es null");
            Toast.makeText(this, "Error: Mapa no disponible", Toast.LENGTH_LONG).show();
            return;
        }

        configurarControlesMapa();
        agregarMarcadorConsultorio();

        if (cardInfo != null) {
            cardInfo.setVisibility(View.VISIBLE);
            Log.d(TAG, "✅ Card info visible");
        } else {
            Log.e(TAG, "❌ Card info es null");
        }

        verificarPermisosUbicacion();
    }

    private void configurarControlesMapa() {
        try {
            if (mMap != null) {
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                Log.d(TAG, "🎛️ Controles del mapa configurados");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error configurando controles del mapa: " + e.getMessage());
        }
    }

    private void agregarMarcadorConsultorio() {
        try {
            Log.d(TAG, "📍 Agregando marcador en Plaza Mundo Apopa: " + UBICACION_CONSULTORIO);

            mMap.addMarker(new MarkerOptions()
                    .position(UBICACION_CONSULTORIO)
                    .title(NOMBRE_CONSULTORIO)
                    .snippet("Centro comercial Plaza Mundo Apopa")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // Zoom más cercano para mejor visualización
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UBICACION_CONSULTORIO, 17f));

            Log.d(TAG, "✅ Marcador ROJO agregado exitosamente en Plaza Mundo Apopa");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error agregando marcador: " + e.getMessage());
            Toast.makeText(this, "Error al agregar marcador en el mapa", Toast.LENGTH_SHORT).show();
        }
    }

    private void verificarPermisosUbicacion() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Permisos de ubicación concedidos");
                habilitarMiUbicacion();
            } else {
                Log.d(TAG, "📋 Solicitando permisos de ubicación...");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error verificando permisos: " + e.getMessage());
        }
    }

    private void habilitarMiUbicacion() {
        try {
            if (mMap != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                Log.d(TAG, "📍 Funcionalidad de mi ubicación habilitada");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error habilitando mi ubicación: " + e.getMessage());
        }
    }

    private void centrarEnMiUbicacion() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                fusedLocationClient.getLastLocation()
                        .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    Location location = task.getResult();
                                    LatLng miUbicacion = new LatLng(location.getLatitude(), location.getLongitude());

                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 15f));
                                    Log.d(TAG, "📍 Centrado en mi ubicación: " + miUbicacion);

                                } else {
                                    Log.w(TAG, "⚠️ No se pudo obtener la ubicación actual");
                                    Toast.makeText(MapaActivity.this,
                                            "No se pudo obtener tu ubicación", Toast.LENGTH_SHORT).show();

                                    // Volver a Plaza Mundo Apopa como fallback
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UBICACION_CONSULTORIO, 17f));
                                }
                            }
                        });

            } else {
                Toast.makeText(this, "Se necesitan permisos de ubicación", Toast.LENGTH_SHORT).show();
                verificarPermisosUbicacion();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error centrando en mi ubicación: " + e.getMessage());
        }
    }

    private void abrirGoogleMapsDirecciones() {
        try {
            Log.d(TAG, "🚗 Abriendo Google Maps para direcciones a Plaza Mundo Apopa");

            // ✅ FORMATO OPTIMIZADO PARA PLAZA MUNDO APOPA
            String uri = String.format("https://www.google.com/maps/search/?api=1&query=Plaza+Mundo+Apopa+El+Salvador&query_place_id=%s",
                    Uri.encode("Plaza Mundo Apopa"));

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");

            if (intent.resolveActivity(getPackageManager()) != null) {
                Log.d(TAG, "📍 Abriendo Google Maps App con Plaza Mundo Apopa...");
                startActivity(intent);
            } else {
                // Fallback: usar coordenadas exactas
                String webUri = String.format("https://www.google.com/maps/@%f,%f,17z",
                        UBICACION_CONSULTORIO.latitude,
                        UBICACION_CONSULTORIO.longitude);

                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
                startActivity(webIntent);
                Log.d(TAG, "🌐 Abierto en navegador web con coordenadas");
            }

            Log.d(TAG, "✅ Google Maps abierto exitosamente para Plaza Mundo Apopa");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error abriendo Google Maps: " + e.getMessage());

            // ✅ FALLBACK FINAL MÁS ROBUSTO
            try {
                String fallbackUri = String.format("https://www.google.com/maps/search/Plaza+Mundo+Apopa");
                Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUri));
                startActivity(fallbackIntent);
                Log.d(TAG, "✅ Abierto con fallback de búsqueda directa");

            } catch (Exception ex) {
                Log.e(TAG, "❌ Error crítico en fallback: " + ex.getMessage());
                Toast.makeText(this,
                        "No se pudo abrir Google Maps. Busca 'Plaza Mundo Apopa' manualmente.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Permisos de ubicación concedidos por el usuario");
                habilitarMiUbicacion();
            } else {
                Log.w(TAG, "⚠️ Permisos de ubicación denegados por el usuario");
                Toast.makeText(this,
                        "La funcionalidad de ubicación está limitada sin permisos",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🔍 onResume - Reanudando actividad de mapa");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔚 onDestroy - Finalizando actividad de mapa");
    }
}