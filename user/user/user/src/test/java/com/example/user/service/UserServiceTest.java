package com.example.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.user.model.*;
import com.example.user.model.Dto.ChangeusernameRequest;
import com.example.user.model.Dto.UserUpdateResponse;
import com.example.user.repository.PasswordRecoveryRepository;
import com.example.user.repository.RoleRepository;
import com.example.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository usuarioRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordRecoveryService passwordRecoveryService;
    @Mock
    private PasswordRecoveryRepository passwordRecoveryRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private JwtPasswordReset jwtPasswordReset;
    @Mock
    private EmailService emailService;

    private User user;
    private Rol rol;

    @BeforeEach
    void setup() {
        rol = new Rol();
        rol.setId(1L);
        rol.setNombre("ROLE_USER");

        user = new User();
        user.setId(1L);
        user.setUsername("matias");
        user.setCorreo("matias@test.com");
        user.setPassword("encodedPass");
        user.setRol(rol);
    }

    // ================= LOGIN =================

    @Test
    void autenticarUsuario_ok() {
        when(usuarioRepository.findByCorreo("matias@test.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encodedPass"))
                .thenReturn(true);
        when(jwtUtil.generarToken("matias", "ROLE_USER"))
                .thenReturn("token123");

        AuthResponse response = userService.autenticarUsuario(
                "matias@test.com", "123456");

        assertEquals("matias", response.getUsername());
        assertEquals("ROLE_USER", response.getRol());
        assertEquals("token123", response.getToken());
    }

    @Test
    void autenticarUsuario_passwordIncorrecta() {
        when(usuarioRepository.findByCorreo(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.autenticarUsuario("matias@test.com", "bad"));

        assertEquals("Credenciales inválidas", ex.getMessage());
    }

    // ================= CREAR USUARIO =================

    @Test
    void crearUsuario_ok() {
        when(usuarioRepository.existsByUsername("matias")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("matias@test.com")).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode("123456")).thenReturn("encoded");
        when(usuarioRepository.save(any(User.class)))
                .thenAnswer(i -> i.getArgument(0));

        User creado = userService.crearUsuario(
                "matias", "123456", "matias@test.com", 1L);

        assertEquals("matias", creado.getUsername());
        assertEquals("encoded", creado.getPassword());
        assertEquals("matias@test.com", creado.getCorreo());
    }

    @Test
    void crearUsuario_usernameDuplicado() {
        when(usuarioRepository.existsByUsername("matias")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.crearUsuario(
                        "matias", "123456", "test@test.com", 1L));

        assertTrue(ex.getMessage().contains("ya está en uso"));
    }

    // ================= CAMBIAR CONTRASEÑA =================

    @Test
    void cambiarContrasena_ok() {
        CambioContrasena dto = new CambioContrasena();
        dto.setUsername("matias");
        dto.setContrasenaActual("123456");
        dto.setContrasenaNueva("nueva123");
        dto.setConfirmarContrasena("nueva123");

        when(usuarioRepository.findByUsername("matias"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encodedPass"))
                .thenReturn(true);
        when(passwordEncoder.encode("nueva123"))
                .thenReturn("encodedNueva");

        String result = userService.cambiarContrasena(dto);

        assertEquals("Contraseña actualizada exitosamente", result);
        verify(usuarioRepository).save(user);
    }

    // ================= ELIMINAR =================

    @Test
    void eliminarUsuario_ok() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        String msg = userService.eliminarusuarioporid(1L);

        assertTrue(msg.contains("eliminado"));
        verify(usuarioRepository).deleteById(1L);
    }

    // ================= CAMBIAR NOMBRE =================

    @Test
    void cambiarNombreUsuario_ok() {
        ChangeusernameRequest req = new ChangeusernameRequest();
        req.setNuevoNombre("nuevoNombre");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(usuarioRepository.existsByUsername("nuevoNombre"))
                .thenReturn(false);
        when(usuarioRepository.save(any(User.class)))
                .thenReturn(user);

        UserUpdateResponse response =
                userService.cambiarNombreUsuario(1L, req);

        assertEquals("nuevoNombre", response.getUsername());
    }

    // ================= BUSCAR =================

    @Test
    void buscarUsuarios_ok() {
        when(usuarioRepository.findAll())
                .thenReturn(List.of(user));

        List<User> users = userService.buscarUsuarios();

        assertEquals(1, users.size());
    }

    @Test
    void getUsuario_ok() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User encontrado = userService.getUsuario(1L);

        assertEquals("matias", encontrado.getUsername());
    }
}
