package com.example.user.service;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.user.model.AuthResponse;
import com.example.user.model.CambioContrasena;
import com.example.user.model.Rol;
import com.example.user.model.User;
import com.example.user.repository.RoleRepository;
import com.example.user.repository.UserRepository;

import jakarta.transaction.Transactional;


@Service
@Transactional
public class UserService {
  @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    // Método para autenticar usuario (login) - CON TOKEN
    public AuthResponse autenticarUsuario(String mail, String password) {
        // Validaciones de entrada
        if (mail == null || mail.trim().isEmpty()) {
            throw new RuntimeException("El mail de usuario no puede estar vacío");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("La contraseña no puede estar vacía");
        }

        // Buscar usuario por username
        User user = usuarioRepository.findByCorreo(mail.trim())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // Validar contraseña encriptada
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // Validar que el usuario tenga un rol asignado
        if (user.getRol() == null) {
            throw new RuntimeException("El usuario no tiene un rol asignado");
        }

        // Generar token JWT
        String token = jwtUtil.generarToken(user.getUsername(), user.getRol().getNombre());

        // Retornar respuesta con datos del usuario, su rol y el token
        return new AuthResponse(
            user.getUsername(),
            user.getRol().getNombre(),
            "Inicio de sesión exitoso",
            token
        );
    }

    // Método para cambiar contraseña
    public String cambiarContrasena(CambioContrasena cambioContrasena) {
        // Validaciones
        if (!cambioContrasena.getContrasenaNueva().equals(cambioContrasena.getConfirmarContrasena())) {
            throw new RuntimeException("Las contraseñas nuevas no coinciden");
        }

        if (cambioContrasena.getContrasenaNueva().length() < 6) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        // Buscar usuario
        User user = usuarioRepository.findByUsername(cambioContrasena.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(cambioContrasena.getContrasenaActual(), user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(cambioContrasena.getContrasenaNueva()));
        usuarioRepository.save(user);

        return "Contraseña actualizada exitosamente";
    }

    //metodo para obtener un usuario mediante su id
    public User getUsuario(Long id){
        if (id == null || id <= 0) {
            throw new RuntimeException("ID de usuario inválido");
        }
        return usuarioRepository.findById(id)
        .orElseThrow(()-> new RuntimeException("Usuario no encontrado"));
    }
      public Long getUsuarioIdByUsername(String username) {
          return usuarioRepository.findByUsername(username)
            .map(User::getId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
     }
    //metodo para buscar todos los usuarios
    public List<User> buscarUsuarios(){
        return usuarioRepository.findAll();
    }

    //metodo para crear un nuevo usuario
    public User crearUsuario(String username, String password, String correo, Long roleid){
        // Validaciones de entrada
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("El nombre de usuario no puede estar vacío");
        }
        if (password == null || password.length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        }
        if (correo == null || !correo.contains("@")) {
            throw new RuntimeException("El correo electrónico no es válido");
        }
        if (roleid == null || roleid <= 0) {
            throw new RuntimeException("El ID del rol no es válido");
        }

        // Normalizar datos
        username = username.trim().toLowerCase();
        correo = correo.trim().toLowerCase();

        //verificar si el usuario existe
        if (usuarioRepository.existsByUsername(username)){
            throw new RuntimeException("El nombre de usuario " + username + " ya está en uso");
        }
        //verificar si el correo ya esta en uso
        if(usuarioRepository.existsByCorreo(correo)){
            throw new RuntimeException("El correo " + correo + " ya está en uso");
        }

        //verificar si el rol existe para poder crear el usuario
        Rol role = roleRepository.findById(roleid)
        .orElseThrow(()-> new RuntimeException("Rol no encontrado con ID: " + roleid));
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setCorreo(correo);
        user.setRol(role);
        
        return usuarioRepository.save(user);
    }

    //eliminar usuario por id
    public String eliminarusuarioporid(Long id){
        if (id == null || id <= 0) {
            throw new RuntimeException("ID de usuario inválido");
        }
        if(!usuarioRepository.existsById(id)){
            throw new RuntimeException("El usuario con ID " + id + " no existe");
        }
        usuarioRepository.deleteById(id);

        return "El usuario con ID " + id + " se ha eliminado exitosamente";
    }

    //actualizar datos usuario
    public User actualizarUsuario(Long id, User datosnuevos) {
        if (id == null || id <= 0) {
            throw new RuntimeException("ID de usuario inválido");
        }
        
        User usuarioExistente = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Validaciones
        if (datosnuevos.getUsername() == null || datosnuevos.getUsername().trim().isEmpty()) {
            throw new RuntimeException("El nombre de usuario no puede estar vacío");
        }
        if (datosnuevos.getCorreo() == null || !datosnuevos.getCorreo().contains("@")) {
            throw new RuntimeException("El correo electrónico no es válido");
        }

        // Normalizar datos
        String nuevoUsername = datosnuevos.getUsername().trim().toLowerCase();
        String nuevoCorreo = datosnuevos.getCorreo().trim().toLowerCase();

        // Validar correo no esté en uso 
        if (!usuarioExistente.getCorreo().equals(nuevoCorreo) && usuarioRepository.existsByCorreo(nuevoCorreo)) {
            throw new RuntimeException("El correo " + nuevoCorreo + " ya está en uso");
        }

        // Validar username no esté en uso
        if (!usuarioExistente.getUsername().equals(nuevoUsername) && usuarioRepository.existsByUsername(nuevoUsername)) {
            throw new RuntimeException("El nombre de usuario " + nuevoUsername + " ya está en uso");
        }

        // Validar rol
        if (datosnuevos.getRol() == null || datosnuevos.getRol().getId() <= 0) {
            throw new RuntimeException("El rol no es válido");
        }

        Rol rol = roleRepository.findById(datosnuevos.getRol().getId())
            .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + datosnuevos.getRol().getId()));

        usuarioExistente.setUsername(nuevoUsername);
        usuarioExistente.setCorreo(nuevoCorreo);
        
        // Solo actualizar contraseña si se proporciona una nueva
        if (datosnuevos.getPassword() != null && !datosnuevos.getPassword().trim().isEmpty()) {
            if (datosnuevos.getPassword().length() < 6) {
                throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
            }
            usuarioExistente.setPassword(passwordEncoder.encode(datosnuevos.getPassword()));
        }
        
        usuarioExistente.setRol(rol);

        return usuarioRepository.save(usuarioExistente);
    }
}
