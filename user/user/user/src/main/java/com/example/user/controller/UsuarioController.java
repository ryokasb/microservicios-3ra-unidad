package com.example.user.controller;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.user.model.AuthResponse;
import com.example.user.model.CambioContrasena;
import com.example.user.model.InicioSesion;
import com.example.user.model.RestablecerContrasenaRequest;
import com.example.user.model.Rol;
import com.example.user.model.SolicitarResetRequest;
import com.example.user.model.User;
import com.example.user.model.Dto.ChangeusernameRequest;
import com.example.user.model.Dto.UserUpdateResponse;
import com.example.user.service.JwtPasswordReset;
import com.example.user.service.JwtUtil;
import com.example.user.service.RoleService;
import com.example.user.service.UserService;
import com.example.user.webclient.ProductClient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "usuarios", description = "Operaciones relacionadas con la gestión de usuarios, autenticación y roles")
@RestController
@RequestMapping("/duodeal")
@CrossOrigin(origins = "*")
public class UsuarioController {
    
    @Autowired
    private UserService usuarioService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ProductClient productClient;
    @Autowired
    private JwtPasswordReset JwtUtilPassword;

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de login inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/auth/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody InicioSesion loginRequest) {
        try {
            if (loginRequest.getMail() == null || loginRequest.getPassword() == null) {
                return ResponseEntity.badRequest().body("Username y password son requeridos");
            }

            AuthResponse authResponse = usuarioService.autenticarUsuario(
                loginRequest.getMail(), 
                loginRequest.getPassword()
            );
            
            EntityModel<AuthResponse> authModel = EntityModel.of(authResponse);
            authModel.add(linkTo(methodOn(UsuarioController.class).cambiarContrasena(null)).withRel("change-password"));
            authModel.add(linkTo(methodOn(UsuarioController.class).cerrarSesion(null)).withRel("logout"));
            authModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarios(null)).withRel("users"));
            
            return ResponseEntity.ok(authModel);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Error de autenticación", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor", "Ocurrió un error inesperado"));
        }
    }

    @Operation(summary = "Cambiar contraseña", description = "Permite a un usuario cambiar su contraseña actual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contraseña cambiada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error en los datos proporcionados o contraseña actual incorrecta")
    })
    @PostMapping("/auth/change-password")
    public ResponseEntity<?> cambiarContrasena(@RequestBody CambioContrasena cambioContrasena) {
        try {
            String mensaje = usuarioService.cambiarContrasena(cambioContrasena);
            
            SuccessResponse response = new SuccessResponse(mensaje);
            EntityModel<SuccessResponse> responseModel = EntityModel.of(response);
            responseModel.add(linkTo(methodOn(UsuarioController.class).iniciarSesion(null)).withRel("login"));
            responseModel.add(linkTo(methodOn(UsuarioController.class).cerrarSesion(null)).withRel("logout"));
            
            return ResponseEntity.ok(responseModel);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Error al cambiar contraseña", e.getMessage()));
        }
    }

    @Operation(summary = "Cerrar sesión", description = "Cierra la sesión del usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente")
    })
    @PostMapping("/auth/logout")
    public ResponseEntity<?> cerrarSesion(HttpServletRequest request) {
        SuccessResponse response = new SuccessResponse("Sesión cerrada exitosamente");
        EntityModel<SuccessResponse> responseModel = EntityModel.of(response);
        responseModel.add(linkTo(methodOn(UsuarioController.class).iniciarSesion(null)).withRel("login"));
        
        return ResponseEntity.ok(responseModel);
    }

    @Operation(summary = "Obtener todos los usuarios", description = "Devuelve una lista con todos los usuarios registrados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida correctamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/users")
    public ResponseEntity<?> obtenerUsuarios(HttpServletRequest request) {
        try {
            String token = jwtUtil.extraerTokenDelHeader(request.getHeader("Authorization"));
            if (token == null || !jwtUtil.esTokenValido(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Acceso no autorizado", "Token inválido o faltante"));
            }

            List<User> users = usuarioService.buscarUsuarios();
            
            if(users.isEmpty()){
                // Crear un mapa para la respuesta
                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "No hay usuarios registrados");
                response.put("data", Collections.emptyList());
                
                // Crear el modelo HATEOAS
                EntityModel<Map<String, Object>> model = EntityModel.of(response);
                model.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarios(null)).withSelfRel());
                model.add(linkTo(methodOn(UsuarioController.class).crearUsuario(null)).withRel("create-user"));
                model.add(linkTo(methodOn(UsuarioController.class).obtenerRoles(null)).withRel("roles"));
                
                return ResponseEntity.ok(model);
            }

            List<EntityModel<User>> userModels = users.stream()
                .map(user -> {
                    EntityModel<User> userModel = EntityModel.of(user);
                    userModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuario(user.getId(), null)).withSelfRel());
                    userModel.add(linkTo(methodOn(UsuarioController.class).actualizarUsuario(user.getId(), null, null)).withRel("update"));
                    userModel.add(linkTo(methodOn(UsuarioController.class).eliminarUsuario(user.getId(), null)).withRel("delete"));
                    return userModel;
                })
                .collect(Collectors.toList());

            CollectionModel<EntityModel<User>> collectionModel = CollectionModel.of(userModels);
            collectionModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarios(null)).withSelfRel());
            collectionModel.add(linkTo(methodOn(UsuarioController.class).crearUsuario(null)).withRel("create-user"));
            collectionModel.add(linkTo(methodOn(UsuarioController.class).obtenerRoles(null)).withRel("roles"));

            return ResponseEntity.ok(collectionModel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al obtener usuarios", e.getMessage()));
        }
    }

    @Operation(summary = "Buscar usuario por ID", description = "Devuelve los datos del usuario solicitado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado correctamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = jwtUtil.extraerTokenDelHeader(request.getHeader("Authorization"));
            if (token == null || !jwtUtil.esTokenValido(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Acceso no autorizado", "Token inválido o faltante"));
            }

            User usuario = usuarioService.getUsuario(id);
            
            EntityModel<User> userModel = EntityModel.of(usuario);
            userModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuario(id, null)).withSelfRel());
            userModel.add(linkTo(methodOn(UsuarioController.class).actualizarUsuario(id, null, null)).withRel("update"));
            userModel.add(linkTo(methodOn(UsuarioController.class).eliminarUsuario(id, null)).withRel("delete"));
            userModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarios(null)).withRel("all-users"));
            
            return ResponseEntity.ok(userModel);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Usuario no encontrado", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno", e.getMessage()));
        }
    }

 @Operation(summary = "Buscar usuario por username", description = "Devuelve el ID del usuario por su username")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "ID del usuario encontrado"),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
})
@GetMapping("/users/id-by-username/{username}")
public ResponseEntity<?> obtenerIdUsuarioPorUsername(@PathVariable String username, HttpServletRequest request) {
    try {
        String token = jwtUtil.extraerTokenDelHeader(request.getHeader("Authorization"));
        if (token == null || !jwtUtil.esTokenValido(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Acceso no autorizado", "Token inválido o faltante"));
        }

        
        Long userId = usuarioService.getUsuarioIdByUsername(username);

        
        return ResponseEntity.ok(Map.of("id", userId));

    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Usuario no encontrado", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno", e.getMessage()));
    }
}

    @Operation(summary = "Obtener todos los roles", description = "Devuelve una lista con todos los roles disponibles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de roles obtenida correctamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/roles")
    public ResponseEntity<?> obtenerRoles(HttpServletRequest request) {
        try {
            String token = jwtUtil.extraerTokenDelHeader(request.getHeader("Authorization"));
            if (token == null || !jwtUtil.esTokenValido(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Acceso no autorizado", "Token inválido o faltante"));
            }

            List<Rol> roles = roleService.buscarRoles();
            
            if(roles.isEmpty()){
                SuccessResponse response = new SuccessResponse("No hay roles registrados", roles);
                EntityModel<SuccessResponse> responseModel = EntityModel.of(response);
                responseModel.add(linkTo(methodOn(UsuarioController.class).obtenerRoles(null)).withSelfRel());
                responseModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarios(null)).withRel("users"));
                return ResponseEntity.ok(responseModel);
            }

            List<EntityModel<Rol>> roleModels = roles.stream()
                .map(rol -> {
                    EntityModel<Rol> roleModel = EntityModel.of(rol);
                    roleModel.add(linkTo(methodOn(UsuarioController.class).obtenerRoles(null)).withRel("all-roles"));
                    return roleModel;
                })
                .collect(Collectors.toList());

            CollectionModel<EntityModel<Rol>> collectionModel = CollectionModel.of(roleModels);
            collectionModel.add(linkTo(methodOn(UsuarioController.class).obtenerRoles(null)).withSelfRel());
            collectionModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarios(null)).withRel("users"));

            return ResponseEntity.ok(collectionModel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al obtener roles", e.getMessage()));
        }
    }

  @Operation(summary = "Crear nuevo usuario", description = "Permite registrar un nuevo usuario en el sistema")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
    @ApiResponse(responseCode = "400", description = "Error en los datos proporcionados"),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
})
@PostMapping("/users")
public ResponseEntity<?> crearUsuario(@RequestBody User user) {
    try {
        if (user.getRol() == null || user.getRol().getId() <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Datos inválidos", "El rol es requerido"));
        }

        User newUser = usuarioService.crearUsuario(
            user.getUsername(),
            user.getPassword(),
            user.getCorreo(),
            user.getRol().getId()
        );

        EntityModel<User> userModel = EntityModel.of(newUser);
        userModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuario(newUser.getId(), null)).withSelfRel());
        userModel.add(linkTo(methodOn(UsuarioController.class).actualizarUsuario(newUser.getId(), null, null)).withRel("update"));
        userModel.add(linkTo(methodOn(UsuarioController.class).eliminarUsuario(newUser.getId(), null)).withRel("delete"));
        userModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarios(null)).withRel("all-users"));

        return ResponseEntity.status(HttpStatus.CREATED).body(userModel);

    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Error al crear usuario", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor", e.getMessage()));
    }
}

    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema por su ID")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente"),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
})
@DeleteMapping("/users/{id}")
public ResponseEntity<?> eliminarUsuario(@PathVariable Long id, HttpServletRequest request) {
    try {
        // Validar token
        String token = jwtUtil.extraerTokenDelHeader(request.getHeader("Authorization"));
        if (token == null || !jwtUtil.esTokenValido(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Acceso no autorizado", "Token inválido o faltante"));
        }

        // Validar rol
        String rol = jwtUtil.obtenerRol(token);
        if (!"ADMIN".equals(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Acceso denegado", "Se requiere rol de ADMIN"));
        }

        // 👉 NUEVO: eliminar los productos del usuario ANTES de eliminarlo
        try {
            String respuestaProductos = productClient.eliminarProductosPorUserId(id, token);
            System.out.println("Productos eliminados: " + respuestaProductos);
        } catch (Exception e) {
            System.out.println("Advertencia: No se pudieron eliminar productos del usuario " + id +
                               " -> " + e.getMessage());
        }

        // Eliminar usuario
        String mensaje = usuarioService.eliminarusuarioporid(id);

        // Crear respuesta con HATEOAS
        SuccessResponse response = new SuccessResponse(mensaje);
        EntityModel<SuccessResponse> responseModel = EntityModel.of(response);

        responseModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarios(null)).withRel("all-users"));
        responseModel.add(linkTo(methodOn(UsuarioController.class).crearUsuario(null)).withRel("create-user"));

        return ResponseEntity.ok(responseModel);

    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Error al eliminar usuario", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor", e.getMessage()));
    }
}

  
    @Operation(
    summary = "Solicitar restablecimiento de contraseña",
    description = "Genera y envía un correo con un token de recuperación para restablecer la contraseña"
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Correo enviado exitosamente"),
    @ApiResponse(responseCode = "400", description = "Correo inválido o usuario no encontrado"),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
})
@PostMapping("/auth/request-reset")
public ResponseEntity<?> solicitarRestablecimiento(@RequestBody SolicitarResetRequest request) {
    try {

        usuarioService.solicitarRecuperacionContrasena(request.getCorreo());

        SuccessResponse response = new SuccessResponse("Correo enviado correctamente para restablecer la contraseña");

        EntityModel<SuccessResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(UsuarioController.class).iniciarSesion(null)).withRel("login"));

        return ResponseEntity.ok(model);

    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Error al solicitar recuperación", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor", e.getMessage()));
    }
}



 @Operation(summary = "Restablecer contraseña", description = "Actualiza la contraseña usando el token de recuperación enviado en el header")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente"),
    @ApiResponse(responseCode = "400", description = "Contraseñas no coinciden"),
    @ApiResponse(responseCode = "401", description = "Token inválido o faltante"),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
})
@PostMapping("/auth/reset-password")
public ResponseEntity<?> restablecerContrasena(
        @RequestBody RestablecerContrasenaRequest request) {
    try {

        // Llamar al servicio para confirmar recuperación de contraseña
        usuarioService.confirmarRecuperacionContrasena(
                request.getCorreo(),
                request.getCodigo(),
                request.getNuevaContrasena(),
                request.getConfirmarContrasena()
        );

        SuccessResponse response = new SuccessResponse("Contraseña restablecida correctamente");
        EntityModel<SuccessResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(UsuarioController.class).iniciarSesion(null)).withRel("login"));

        return ResponseEntity.ok(model);

    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Error al restablecer contraseña", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor", e.getMessage()));
    }
}


    @Operation(summary = "Actualizar usuario", description = "Permite actualizar los datos de un usuario existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Error en los datos proporcionados"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/users/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody User datosnuevos, HttpServletRequest request) {
        try {
            String token = jwtUtil.extraerTokenDelHeader(request.getHeader("Authorization"));
            if (token == null || !jwtUtil.esTokenValido(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Acceso no autorizado", "Token inválido o faltante"));
            }

            String rol = jwtUtil.obtenerRol(token);
            if (!"ADMIN".equals(rol)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Acceso denegado", "Se requiere rol de ADMIN"));
            }

            User usuarioModificado = usuarioService.actualizarUsuario(id, datosnuevos);
            
            EntityModel<User> userModel = EntityModel.of(usuarioModificado);
            userModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuario(id, null)).withSelfRel());
            userModel.add(linkTo(methodOn(UsuarioController.class).eliminarUsuario(id, null)).withRel("delete"));
            userModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarios(null)).withRel("all-users"));
            
            return ResponseEntity.ok(userModel);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Error al actualizar usuario", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }

    @Operation(
        summary = "Cambiar nombre de usuario",
        description = "Permite a un usuario cambiar su nombre de usuario"
)
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Nombre de usuario cambiado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre ya en uso"),
        @ApiResponse(responseCode = "401", description = "Acceso no autorizado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
})
@PutMapping("/users/{id}/change-username")
public ResponseEntity<?> cambiarNombreUsuario(
        @PathVariable Long id,
        @RequestBody ChangeusernameRequest nuevoNombre,
        HttpServletRequest request) {

    try {
        // Validar token
        String token = jwtUtil.extraerTokenDelHeader(request.getHeader("Authorization"));
        if (token == null || !jwtUtil.esTokenValido(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Acceso no autorizado", "Token inválido o faltante"));
        }

        // Servicio
        UserUpdateResponse usuarioActualizado =
                usuarioService.cambiarNombreUsuario(id, nuevoNombre);

        EntityModel<UserUpdateResponse> userModel = EntityModel.of(usuarioActualizado);
        userModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuario(id, request)).withSelfRel());
        userModel.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarios(request)).withRel("all-users"));

        return ResponseEntity.ok(userModel);

    } catch (IllegalArgumentException e) {
        // Datos inválidos
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Datos inválidos", e.getMessage()));

    } catch (RuntimeException e) {
        // Usuario no encontrado
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Usuario no encontrado", e.getMessage()));

    } catch (Exception e) {
        // Error inesperado
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor", e.getMessage()));
    }
}



    @Schema(description = "Respuesta de error estandarizada")
    public static class ErrorResponse {
        @Schema(description = "Tipo de error", example = "Error de autenticación")
        private String error;
        @Schema(description = "Mensaje descriptivo del error", example = "Credenciales incorrectas")
        private String mensaje;
        @Schema(description = "Timestamp del error", example = "1640995200000")
        private long timestamp;

        public ErrorResponse(String error, String mensaje) {
            this.error = error;
            this.mensaje = mensaje;
            this.timestamp = System.currentTimeMillis();
        }

        public String getError() { return error; }
        public String getMensaje() { return mensaje; }
        public long getTimestamp() { return timestamp; }
    }

    @Schema(description = "Respuesta de éxito estandarizada")
    public static class SuccessResponse {
        @Schema(description = "Mensaje de éxito", example = "Operación completada exitosamente")
        private String mensaje;
        @Schema(description = "Datos adicionales (opcional)")
        private Object data;
        @Schema(description = "Timestamp de la respuesta", example = "1640995200000")
        private long timestamp;

        public SuccessResponse(String mensaje) {
            this.mensaje = mensaje;
            this.timestamp = System.currentTimeMillis();
        }

        public SuccessResponse(String mensaje, Object data) {
            this.mensaje = mensaje;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMensaje() { return mensaje; }
        public Object getData() { return data; }
        public long getTimestamp() { return timestamp; }
    }
}