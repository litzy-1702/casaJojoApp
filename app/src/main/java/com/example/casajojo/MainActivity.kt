
package com.example.casajojo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.casajojo.core.notifications.OrderNotificationManager
import com.example.casajojo.data.local.AppDatabase
import com.example.casajojo.data.repository.MenuRepository
import com.example.casajojo.data.repository.PedidoRepository
import com.example.casajojo.feature.admin.AdminViewModel
import com.example.casajojo.feature.admin.AdminViewModelFactory
import com.example.casajojo.feature.auth.AuthRepository
import com.example.casajojo.feature.auth.AuthViewModel
import com.example.casajojo.feature.auth.AuthViewModelFactory
import com.example.casajojo.feature.auth.UsuariosViewModel
import com.example.casajojo.feature.auth.UsuariosViewModelFactory
import com.example.casajojo.feature.client.ClienteViewModel
import com.example.casajojo.feature.client.ClienteViewModelFactory
import com.example.casajojo.feature.operations.OperationsViewModel
import com.example.casajojo.feature.operations.OperationsViewModelFactory
import com.example.casajojo.navigation.AppNavigation
import com.example.casajojo.ui.theme.CasaJojoTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }

        val database = AppDatabase.getInstance(applicationContext)

        val notificationManager =
            OrderNotificationManager(applicationContext)

        val menuRepository = MenuRepository(database.menuDao())
        val pedidoRepository = PedidoRepository(database.pedidoDao())

        // Autenticación y usuarios internos
        val authRepository = AuthRepository(database.usuarioDao())

        val authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(authRepository)
        )[AuthViewModel::class.java]

        // Gestión de usuarios desde administración
        val usuariosViewModel = ViewModelProvider(
            this,
            UsuariosViewModelFactory(authRepository)
        )[UsuariosViewModel::class.java]

        // Administración del menú
        val adminViewModel = ViewModelProvider(
            this,
            AdminViewModelFactory(menuRepository)
        )[AdminViewModel::class.java]

        // Operaciones de caja y cocina
        val operationsViewModel = ViewModelProvider(
            this,
            OperationsViewModelFactory(
                database.pedidoDao(),
                notificationManager
            )
        )[OperationsViewModel::class.java]

        // Cliente y pedidos
        val clienteViewModel = ViewModelProvider(
            this,
            ClienteViewModelFactory(
                menuRepository,
                pedidoRepository
            )
        )[ClienteViewModel::class.java]

        setContent {
            CasaJojoTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    usuariosViewModel = usuariosViewModel,
                    adminViewModel = adminViewModel,
                    operationsViewModel = operationsViewModel,
                    clienteViewModel = clienteViewModel
                )
            }
        }
    }
}
