
package com.example.casajojo.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.casajojo.feature.admin.AdminScreen
import com.example.casajojo.feature.admin.AdminViewModel
import com.example.casajojo.feature.admin.MenuFormScreen
import com.example.casajojo.feature.auth.AuthViewModel
import com.example.casajojo.feature.auth.CambiarPasswordScreen
import com.example.casajojo.feature.auth.ClienteDatosScreen
import com.example.casajojo.feature.auth.InicioScreen
import com.example.casajojo.feature.auth.LoginScreen
import com.example.casajojo.feature.auth.RolUsuario
import com.example.casajojo.feature.auth.UsuariosScreen
import com.example.casajojo.feature.auth.UsuariosViewModel
import com.example.casajojo.feature.caja.CajaScreen
import com.example.casajojo.feature.client.CarritoScreen
import com.example.casajojo.feature.client.CheckoutScreen
import com.example.casajojo.feature.client.ClienteViewModel
import com.example.casajojo.feature.client.EstadoPedidoScreen
import com.example.casajojo.feature.client.MenuClienteScreen
import com.example.casajojo.feature.cocina.CocinaScreen
import com.example.casajojo.feature.operations.OperationsViewModel

sealed class Screen(val route: String, val title: String) {
    object Inicio : Screen("inicio", "CasaJojo")
    object Login : Screen("login", "Iniciar sesión")
    object CambiarPassword : Screen("cambiar_password", "Seguridad")
    object ClienteDatos : Screen("cliente_datos", "Datos del cliente")

    object Admin : Screen("admin", "Administración")
    object MenuForm : Screen("menu_form", "Formulario")
    object Usuarios : Screen("usuarios", "Gestión de usuarios")

    object ClienteMenu : Screen("cliente_menu", "Menú")
    object ClienteCarrito : Screen("cliente_carrito", "Carrito")
    object ClienteCheckout : Screen("cliente_checkout", "Checkout")
    object ClienteEstado : Screen("cliente_estado", "Estado del pedido")

    object Caja : Screen("caja", "Caja")
    object Cocina : Screen("cocina", "Cocina")
}

private fun rutaParaRol(rol: String): String {
    return when (rol) {
        "ADMIN" -> Screen.Admin.route
        "CAJERA" -> Screen.Caja.route
        "COCINA" -> Screen.Cocina.route
        else -> Screen.Inicio.route
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    usuariosViewModel: UsuariosViewModel,
    adminViewModel: AdminViewModel,
    operationsViewModel: OperationsViewModel,
    clienteViewModel: ClienteViewModel
) {
    val navController = rememberNavController()

    val authState by authViewModel.uiState.collectAsState()

    val navBackStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry?.destination?.route

    var rolSeleccionadoNombre by rememberSaveable {
        mutableStateOf(RolUsuario.ADMIN.name)
    }

    val rolSeleccionado = RolUsuario.valueOf(
        rolSeleccionadoNombre
    )

    var clienteNombre by rememberSaveable {
        mutableStateOf("")
    }

    var clienteTelefono by rememberSaveable {
        mutableStateOf("")
    }

    val rutasCliente = setOf(
        Screen.ClienteMenu.route,
        Screen.ClienteCarrito.route,
        Screen.ClienteCheckout.route,
        Screen.ClienteEstado.route
    )

    val rutasInternas = setOf(
        Screen.Admin.route,
        Screen.MenuForm.route,
        Screen.Usuarios.route,
        Screen.Caja.route,
        Screen.Cocina.route
    )

    val mostrarBarraInterna =
        authState.usuarioActual != null &&
                currentRoute in rutasInternas

    val mostrarBarraCliente =
        authState.usuarioActual == null &&
                currentRoute in rutasCliente &&
                clienteNombre.isNotBlank()

    val salir = {
        clienteNombre = ""
        clienteTelefono = ""

        clienteViewModel.onNombreChange("")

        clienteViewModel.carrito.value.forEach { detalle ->
            clienteViewModel.quitarPlato(detalle.menuItemId)
        }

        authViewModel.cerrarSesion()

        navController.navigate(Screen.Inicio.route) {
            popUpTo(Screen.Inicio.route) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    // Después de autenticar, dirigir al cambio de contraseña
    // o a la interfaz correspondiente al rol.
    LaunchedEffect(
        authState.usuarioActual?.id,
        authState.debeCambiarPassword,
        currentRoute
    ) {
        val usuario = authState.usuarioActual
            ?: return@LaunchedEffect

        val destino = when {
            currentRoute == Screen.Login.route &&
                    authState.debeCambiarPassword ->
                Screen.CambiarPassword.route

            currentRoute == Screen.Login.route ->
                rutaParaRol(usuario.rol)

            currentRoute == Screen.CambiarPassword.route &&
                    !authState.debeCambiarPassword ->
                rutaParaRol(usuario.rol)

            else -> null
        }

        if (destino != null && destino != currentRoute) {
            navController.navigate(destino) {
                popUpTo(Screen.Inicio.route) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
    }

    // Comprobar los permisos para acceder a las pantallas internas.
    LaunchedEffect(
        currentRoute,
        authState.usuarioActual?.id,
        authState.usuarioActual?.rol,
        authState.debeCambiarPassword,
        clienteNombre,
        clienteTelefono
    ) {
        val rolRequerido = when (currentRoute) {
            Screen.Admin.route,
            Screen.MenuForm.route,
            Screen.Usuarios.route -> "ADMIN"

            Screen.Caja.route -> "CAJERA"

            Screen.Cocina.route -> "COCINA"

            else -> null
        }

        if (rolRequerido != null) {
            val usuario = authState.usuarioActual

            val destino = when {
                usuario == null ->
                    Screen.Inicio.route

                authState.debeCambiarPassword ->
                    Screen.CambiarPassword.route

                usuario.rol != rolRequerido ->
                    rutaParaRol(usuario.rol)

                else -> null
            }

            if (destino != null && destino != currentRoute) {
                navController.navigate(destino) {
                    popUpTo(Screen.Inicio.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
        }

        // El cliente debe ingresar sus datos antes de entrar al menú.
        if (
            currentRoute in rutasCliente &&
            (
                    clienteNombre.isBlank() ||
                            clienteTelefono.isBlank()
                    )
        ) {
            navController.navigate(Screen.ClienteDatos.route) {
                popUpTo(Screen.Inicio.route) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }

        // No permitir cambiar contraseñas sin una sesión activa.
        if (
            currentRoute == Screen.CambiarPassword.route &&
            authState.usuarioActual == null
        ) {
            navController.navigate(Screen.Inicio.route) {
                popUpTo(Screen.Inicio.route) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
    }

    // Evitar volver a pantallas internas mediante el botón Atrás
    // mientras exista una sesión interna activa.
    BackHandler(
        enabled = authState.usuarioActual != null &&
                (
                        currentRoute in rutasInternas ||
                                currentRoute == Screen.CambiarPassword.route
                        )
    ) {
        // El usuario debe cerrar sesión mediante el botón disponible.
    }

    Scaffold(
        topBar = {
            if (mostrarBarraInterna) {
                TopAppBar(
                    title = {
                        Text(
                            "CasaJojo · " +
                                    authState.usuarioActual?.rol.orEmpty()
                        )
                    },
                    actions = {
                        TextButton(onClick = salir) {
                            Text("Cerrar sesión")
                        }
                    }
                )
            } else if (mostrarBarraCliente) {
                TopAppBar(
                    title = {
                        Text("CasaJojo · $clienteNombre")
                    },
                    actions = {
                        TextButton(onClick = salir) {
                            Text("Salir")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Inicio.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            // Pantalla inicial
            composable(Screen.Inicio.route) {
                InicioScreen(
                    onCliente = {
                        navController.navigate(
                            Screen.ClienteDatos.route
                        )
                    },
                    onRolInterno = { rol ->
                        rolSeleccionadoNombre = rol.name

                        navController.navigate(
                            Screen.Login.route
                        )
                    }
                )
            }

            // Inicio de sesión de los usuarios internos
            composable(Screen.Login.route) {
                LoginScreen(
                    rol = rolSeleccionado,
                    inicializando = authState.inicializando,
                    cargando = authState.cargando,
                    error = authState.error,
                    onIniciarSesion = { username, password ->
                        authViewModel.iniciarSesion(
                            username = username,
                            password = password,
                            rolEsperado = rolSeleccionado
                        )
                    },
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }

            // Cambio obligatorio de contraseña
            composable(Screen.CambiarPassword.route) {
                val usuario = authState.usuarioActual

                if (usuario != null) {
                    CambiarPasswordScreen(
                        username = usuario.username,
                        cargando = authState.cargando,
                        error = authState.error,
                        onGuardar = { nueva, confirmacion ->
                            authViewModel.cambiarPassword(
                                nuevaPassword = nueva,
                                confirmacion = confirmacion
                            )
                        },
                        onCerrarSesion = salir
                    )
                }
            }

            // Identificación básica del cliente
            composable(Screen.ClienteDatos.route) {
                ClienteDatosScreen(
                    onContinuar = { nombre, telefono ->
                        clienteNombre = nombre
                        clienteTelefono = telefono

                        clienteViewModel.onNombreChange(nombre)

                        navController.navigate(
                            Screen.ClienteMenu.route
                        ) {
                            popUpTo(Screen.Inicio.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }

            // Administración del menú
            composable(Screen.Admin.route) {
                AdminScreen(
                    viewModel = adminViewModel,
                    onAgregarClick = {
                        navController.navigate(
                            Screen.MenuForm.route
                        )
                    },
                    onEditarClick = { _ ->
                        navController.navigate(
                            Screen.MenuForm.route
                        )
                    },
                    onGestionUsuariosClick = {
                        navController.navigate(
                            Screen.Usuarios.route
                        )
                    }
                )
            }

            // Formulario para agregar o editar productos del menú
            composable(Screen.MenuForm.route) {
                MenuFormScreen(
                    viewModel = adminViewModel,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }

            // Gestión de usuarios internos
            composable(Screen.Usuarios.route) {
                UsuariosScreen(
                    viewModel = usuariosViewModel,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }

            // Menú del cliente
            composable(Screen.ClienteMenu.route) {
                MenuClienteScreen(
                    viewModel = clienteViewModel,
                    onIrAlCarrito = {
                        navController.navigate(
                            Screen.ClienteCarrito.route
                        )
                    },
                    onVerMisPedidos = {
                        navController.navigate(
                            Screen.ClienteEstado.route
                        )
                    }
                )
            }

            // Carrito del cliente
            composable(Screen.ClienteCarrito.route) {
                CarritoScreen(
                    viewModel = clienteViewModel,
                    onSeguirEligiendo = {
                        navController.popBackStack()
                    },
                    onContinuar = {
                        navController.navigate(
                            Screen.ClienteCheckout.route
                        )
                    }
                )
            }

            // Confirmación del pedido
            composable(Screen.ClienteCheckout.route) {
                CheckoutScreen(
                    viewModel = clienteViewModel,
                    onVolverAlCarrito = {
                        navController.popBackStack()
                    },
                    onPedidoConfirmado = {
                        navController.navigate(
                            Screen.ClienteEstado.route
                        ) {
                            popUpTo(Screen.ClienteMenu.route)
                        }
                    }
                )
            }

            // Estado e historial de pedidos del cliente
            composable(Screen.ClienteEstado.route) {
                EstadoPedidoScreen(
                    viewModel = clienteViewModel,
                    onVolverAlMenu = {
                        navController.navigate(
                            Screen.ClienteMenu.route
                        ) {
                            popUpTo(Screen.ClienteMenu.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // Módulo de caja
            composable(Screen.Caja.route) {
                CajaScreen(
                    viewModel = operationsViewModel
                )
            }

            // Módulo de cocina
            composable(Screen.Cocina.route) {
                CocinaScreen(
                    viewModel = operationsViewModel
                )
            }
        }
    }
}
