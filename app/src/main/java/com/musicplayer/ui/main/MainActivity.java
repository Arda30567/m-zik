package com.musicplayer.ui.main;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.musicplayer.R;
import com.musicplayer.databinding.ActivityMainBinding;
import com.musicplayer.service.MusicPlayerService;
import com.musicplayer.ui.player.PlayerViewModel;
import com.musicplayer.ui.player.mini.MiniPlayerFragment;

/**
 * Main activity for the Music Player application.
 * Hosts the navigation and mini player components.
 */
public class MainActivity extends AppCompatActivity implements ServiceConnection {
    
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private ActivityMainBinding binding;
    private NavController navController;
    private PlayerViewModel playerViewModel;
    
    // Service binding
    private MusicPlayerService musicPlayerService;
    private boolean isServiceBound = false;
    private Intent serviceIntent;
    
    // Permission launcher
    private ActivityResultLauncher<String[]> permissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize ViewModels
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        
        // Setup navigation
        setupNavigation();
        
        // Setup mini player
        setupMiniPlayer();
        
        // Setup permissions
        setupPermissions();
        
        // Bind to music service
        bindToMusicService();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        if (!isServiceBound) {
            bindToMusicService();
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (isServiceBound) {
            unbindService(this);
            isServiceBound = false;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
    
    // ServiceConnection methods
    
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected: Service connected");
        MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
        musicPlayerService = binder.getService();
        isServiceBound = true;
        
        // Notify ViewModel about service connection
        playerViewModel.setMusicPlayerService(musicPlayerService);
    }
    
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected: Service disconnected");
        musicPlayerService = null;
        isServiceBound = false;
        
        // Notify ViewModel about service disconnection
        playerViewModel.setMusicPlayerService(null);
    }
    
    // Navigation setup
    
    private void setupNavigation() {
        BottomNavigationView bottomNavigationView = binding.bottomNavigation;
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        
        // Handle navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                navController.navigate(R.id.homeFragment);
                return true;
            } else if (itemId == R.id.nav_library) {
                navController.navigate(R.id.libraryFragment);
                return true;
            } else if (itemId == R.id.nav_playlists) {
                navController.navigate(R.id.playlistsFragment);
                return true;
            } else if (itemId == R.id.nav_search) {
                navController.navigate(R.id.searchFragment);
                return true;
            } else if (itemId == R.id.nav_settings) {
                navController.navigate(R.id.settingsFragment);
                return true;
            }
            
            return false;
        });
        
        // Handle navigation destination changes
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            updateMiniPlayerVisibility(destination.getId());
        });
    }
    
    private void setupMiniPlayer() {
        // Add mini player fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mini_player_container, new MiniPlayerFragment())
                .commit();
    }
    
    private void updateMiniPlayerVisibility(int destinationId) {
        // Show mini player on all screens except full player
        boolean showMiniPlayer = destinationId != R.id.playerFragment;
        binding.miniPlayerContainer.setVisibility(showMiniPlayer ? View.VISIBLE : View.GONE);
    }
    
    // Permission handling
    
    private void setupPermissions() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Boolean granted : result.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }
                    
                    if (allGranted) {
                        onPermissionsGranted();
                    } else {
                        onPermissionsDenied();
                    }
                }
        );
        
        checkPermissions();
    }
    
    private void checkPermissions() {
        String[] permissions = getRequiredPermissions();
        boolean allGranted = true;
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        
        if (allGranted) {
            onPermissionsGranted();
        } else {
            requestPermissions();
        }
    }
    
    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            return new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
    }
    
    private void requestPermissions() {
        permissionLauncher.launch(getRequiredPermissions());
    }
    
    private void onPermissionsGranted() {
        Log.d(TAG, "All permissions granted");
        // Initialize app components that require permissions
        initializeApp();
    }
    
    private void onPermissionsDenied() {
        Log.w(TAG, "Some permissions denied");
        // Show permission rationale or disable features
        showPermissionRationale();
    }
    
    private void showPermissionRationale() {
        // Show a dialog explaining why permissions are needed
        // This is a simplified version - implement proper UI
        Log.w(TAG, "Showing permission rationale");
    }
    
    private void initializeApp() {
        // Initialize app components that require permissions
        // For example, scan for music files, load library, etc.
        Log.d(TAG, "Initializing app components");
    }
    
    // Service binding
    
    private void bindToMusicService() {
        serviceIntent = new Intent(this, MusicPlayerService.class);
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        
        // Start service to keep it running
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
    
    // Utility methods
    
    public boolean isServiceBound() {
        return isServiceBound;
    }
    
    public MusicPlayerService getMusicPlayerService() {
        return musicPlayerService;
    }
    
    // Navigation helpers
    
    public void navigateToPlayer() {
        if (navController != null) {
            navController.navigate(R.id.playerFragment);
        }
    }
    
    public void navigateToLibrary() {
        if (navController != null) {
            navController.navigate(R.id.libraryFragment);
        }
    }
    
    public void navigateToPlaylists() {
        if (navController != null) {
            navController.navigate(R.id.playlistsFragment);
        }
    }
    
    // Handle back button
    
    @Override
    public void onBackPressed() {
        if (navController != null) {
            if (!navController.popBackStack()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}