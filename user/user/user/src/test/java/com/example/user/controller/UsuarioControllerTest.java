package com.example.user.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.example.user.model.*;
import com.example.user.model.Dto.ChangeusernameRequest;
import com.example.user.model.Dto.UserUpdateResponse;
import com.example.user.service.*;
import com.example.user.webclient.ProductClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService usuarioService;
    @MockBean
    private RoleService roleService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private ProductClient productClient;
    @MockBean
    private JwtPasswordReset jwtPasswordReset;

    // ================= LOGIN =================

    @Test
    void iniciarSesion_ok() throws Exception {
        AuthResponse auth = new AuthResponse(
                "matias",
                "USER",
                "Inicio de sesión exitoso",
                "token123"
        );

        when(usuarioService.autenticarUsuario(anyString(), anyString()))
                .thenReturn(auth);

        mockMvc.perform(post("/duodeal/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "mail": "test@correo.com",
                      "password": "123456"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("matias"))
                .andExpect(jsonPath("$.token").value("token123"));
    }

    @Test
    void iniciarSesion_credencialesInvalidas() throws Exception {
        when(usuarioService.autenticarUsuario(anyString(), anyString()))
                .thenThrow(new RuntimeException("Credenciales inválidas"));

        mockMvc.perform(post("/duodeal/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "mail": "test@correo.com",
                      "password": "bad"
                    }
                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Error de autenticación"));
    }

    // ================= OBTENER USUARIOS =================

    @Test
    void obtenerUsuarios_ok() throws Exception {
        when(jwtUtil.extraerTokenDelHeader(anyString())).thenReturn("token");
        when(jwtUtil.esTokenValido("token")).thenReturn(true);

        User user = new User();
        user.setId(1L);
        user.setUsername("matias");

        when(usuarioService.buscarUsuarios()).thenReturn(List.of(user));

        mockMvc.perform(get("/duodeal/users")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists());
    }

    @Test
    void obtenerUsuarios_tokenInvalido() throws Exception {
        when(jwtUtil.extraerTokenDelHeader(anyString())).thenReturn(null);

        mockMvc.perform(get("/duodeal/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Acceso no autorizado"));
    }

    // ================= OBTENER USUARIO POR ID =================

    @Test
    void obtenerUsuario_ok() throws Exception {
        when(jwtUtil.extraerTokenDelHeader(anyString())).thenReturn("token");
        when(jwtUtil.esTokenValido("token")).thenReturn(true);

        User user = new User();
        user.setId(1L);
        user.setUsername("matias");

        when(usuarioService.getUsuario(1L)).thenReturn(user);

        mockMvc.perform(get("/duodeal/users/1")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("matias"));
    }

    // ================= CREAR USUARIO =================

    @Test
    void crearUsuario_ok() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("nuevo");
        user.setCorreo("nuevo@test.com");

        when(usuarioService.crearUsuario(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(user);

        mockMvc.perform(post("/duodeal/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "nuevo",
                      "password": "123456",
                      "correo": "nuevo@test.com",
                      "rol": { "id": 1 }
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("nuevo"));
    }

    // ================= ELIMINAR =================

    @Test
    void eliminarUsuario_ok_admin() throws Exception {
        when(jwtUtil.extraerTokenDelHeader(anyString())).thenReturn("token");
        when(jwtUtil.esTokenValido("token")).thenReturn(true);
        when(jwtUtil.obtenerRol("token")).thenReturn("ADMIN");

        when(usuarioService.eliminarusuarioporid(1L))
                .thenReturn("Usuario eliminado");

        mockMvc.perform(delete("/duodeal/users/1")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    // ================= CAMBIAR NOMBRE =================

    @Test
    void cambiarNombreUsuario_ok() throws Exception {
        when(jwtUtil.extraerTokenDelHeader(anyString())).thenReturn("token");
        when(jwtUtil.esTokenValido("token")).thenReturn(true);

        UserUpdateResponse response =
                new UserUpdateResponse(1L, "nuevoNombre", "test@test.com");

        when(usuarioService.cambiarNombreUsuario(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/duodeal/users/1/change-username")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "nuevoNombre": "nuevoNombre" }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("nuevoNombre"));
    }
}
