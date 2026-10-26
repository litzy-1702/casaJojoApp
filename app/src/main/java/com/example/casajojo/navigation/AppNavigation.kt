package com.example.casajojo.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.casajojo.data.repository.MenuRepository
import com.example.casajojo.feature.admin.AdminScreen
import com.example.casajojo.feature.admin.AdminViewModel
import com.example.casajojo.feature.admin.MenuFormScreen
import com.example.casajojo.feature.caja.CajaScreen
import com.example.casajojo.feature.cocina.CocinaScreen
import com.example.casajojo.feature.operations.OperationsViewModel

sealed class Screen(val route: String, val title: String) {
    object Admin : Screen("admin", "Admin")
    object MenuForm : Screen("menu_form", "Formulario")
    object Cliente : Screen("cliente", "Cliente")
    object Caja : Screen("caja", "Caja")
    object Cocina : Screen("cocina", "Cocina")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    adminViewModel: AdminViewModel,
    operationsViewModel: OperationsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(Screen.Admin, Screen.Cliente, Screen.Caja, Screen.Cocina).forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
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
            modifier = Modifier.padding(innerPadding)
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

            composable(Screen.Cliente.route) {
                // pantalla momentanea para la vista de cliente
                Surface(modifier = Modifier.padding(paddingValues = innerPadding)) {
                    Text("Vista Cliente: Próximamente integración del Integrante 2")
                }
            }

            composable(Screen.Caja.route) {
                CajaScreen(viewModel = operationsViewModel)
            }

            composable(Screen.Cocina.route) {
                CocinaScreen(viewModel = operationsViewModel)
            }
        }
    }
}