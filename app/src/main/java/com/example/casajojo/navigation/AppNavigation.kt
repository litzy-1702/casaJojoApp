package com.example.casajojo.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.casajojo.feature.admin.AdminScreen
import com.example.casajojo.feature.admin.AdminViewModel
import com.example.casajojo.feature.admin.MenuFormScreen
import com.example.casajojo.feature.caja.CajaScreen
import com.example.casajojo.feature.client.CarritoScreen
import com.example.casajojo.feature.client.CheckoutScreen
import com.example.casajojo.feature.client.ClienteViewModel
import com.example.casajojo.feature.client.EstadoPedidoScreen
import com.example.casajojo.feature.client.MenuClienteScreen
import com.example.casajojo.feature.cocina.CocinaScreen
import com.example.casajojo.feature.operations.OperationsViewModel

sealed class Screen(val route: String, val title: String) {
    object Admin : Screen("admin", "Admin")
    object MenuForm : Screen("menu_form", "Formulario")
    object ClienteMenu : Screen("cliente_menu", "Cliente")
    object ClienteCarrito : Screen("cliente_carrito", "Carrito")
    object ClienteCheckout : Screen("cliente_checkout", "Checkout")
    object ClienteEstado : Screen("cliente_estado", "Estado Pedido")
    object Caja : Screen("caja", "Caja")
    object Cocina : Screen("cocina", "Cocina")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    adminViewModel: AdminViewModel,
    operationsViewModel: OperationsViewModel,
    clienteViewModel: ClienteViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    Screen.Admin,
                    Screen.ClienteMenu,
                    Screen.Caja,
                    Screen.Cocina
                ).forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute?.startsWith("cliente") == true && screen == Screen.ClienteMenu
                                || currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        label = { Text(screen.title) },
                        icon = { Text("📱") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Caja.route,
            modifier = Modifier.padding(innerPadding) // Corrección del parámetro de padding
        ) {
            composable(Screen.Admin.route) {
                AdminScreen(
                    viewModel = adminViewModel,
                    onAgregarClick = { navController.navigate(Screen.MenuForm.route) },
                    onEditarClick = { navController.navigate(Screen.MenuForm.route) }
                )
            }

            composable(Screen.MenuForm.route) {
                MenuFormScreen(
                    viewModel = adminViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }

            // --- RUTAS DEL MÓDULO CLIENTE ---
            composable(Screen.ClienteMenu.route) {
                MenuClienteScreen(
                    viewModel = clienteViewModel,
                    onIrAlCarrito = { navController.navigate(Screen.ClienteCarrito.route) },
                    onVerMisPedidos = { navController.navigate(Screen.ClienteEstado.route) }
                )
            }

            composable(Screen.ClienteCarrito.route) {
                CarritoScreen(
                    viewModel = clienteViewModel,
                    onSeguirEligiendo = { navController.popBackStack() },
                    onContinuar = { navController.navigate(Screen.ClienteCheckout.route) }
                )
            }

            composable(Screen.ClienteCheckout.route) {
                CheckoutScreen(
                    viewModel = clienteViewModel,
                    onVolverAlCarrito = { navController.popBackStack() },
                    onPedidoConfirmado = {
                        navController.navigate(Screen.ClienteEstado.route) {
                            popUpTo(Screen.ClienteMenu.route)
                        }
                    }
                )
            }

            composable(Screen.ClienteEstado.route) {
                EstadoPedidoScreen(
                    viewModel = clienteViewModel,
                    onVolverAlMenu = {
                        navController.navigate(Screen.ClienteMenu.route) {
                            popUpTo(Screen.ClienteMenu.route) { inclusive = true }
                        }
                    }
                )
            }

            // --- RUTAS DE OPERACIONES ---
            composable(Screen.Caja.route) {
                CajaScreen(viewModel = operationsViewModel)
            }

            composable(Screen.Cocina.route) {
                CocinaScreen(viewModel = operationsViewModel)
            }
        }
    }
}