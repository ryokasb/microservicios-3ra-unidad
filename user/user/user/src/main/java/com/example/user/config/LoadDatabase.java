package com.example.user.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.user.model.Rol;
import com.example.user.repository.RoleRepository;
import com.example.user.service.UserService;

@Configuration
public class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepo, UserService usuarioService) {
        return args -> {
            if (roleRepo.count() == 0) {
                // Crear roles
                Rol admin = new Rol();
                admin.setNombre("ADMIN");
                roleRepo.save(admin);

                Rol user = new Rol();
                user.setNombre("USER");
                roleRepo.save(user);

                Rol dealer = new Rol();
                dealer.setNombre("DEALER");
                roleRepo.save(dealer);
            }

            if (usuarioService.buscarUsuarios().isEmpty()) {
                // Obtener IDs de roles
                Long adminRoleId = roleRepo.findByNombre("ADMIN")
                        .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"))
                        .getId();
                Long userRoleId = roleRepo.findByNombre("USER")
                        .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"))
                        .getId();
                Long dealerRoleId = roleRepo.findByNombre("DEALER")
                        .orElseThrow(() -> new RuntimeException("Rol DEALER no encontrado"))
                        .getId();

                // Crear usuarios usando el servicio (con contraseña cifrada, etc)
                usuarioService.crearUsuario("admin", "admin123", "admin@gmail.com", adminRoleId);
                usuarioService.crearUsuario("cliente", "cliente123", "cliente@gmail.com", userRoleId);
                usuarioService.crearUsuario("vendedor", "vendedor123", "vendedor@gmail.com", dealerRoleId);
            }
        };
    }
}
