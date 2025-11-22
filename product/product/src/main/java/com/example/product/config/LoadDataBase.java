package com.example.product.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.product.Service.ProductService;

@Configuration
public class LoadDataBase {

    @Bean
    CommandLineRunner initDatabase(ProductService productService) {
        return args -> {

            if (!productService.listarProductos().isEmpty()) return;

            System.out.println("📌 Base vacía → Insertando productos de precarga...");

            Long idusuario = 3L; // 🟢 Usuario fijo solo para precarga
            System.out.println("✔ ID de usuario asignado: " + idusuario);
 
            productService.crearProductoPrecarga(
                    idusuario,
                    "Minecraft Ps4",
                    "Vendo minecraft de ps4",
                    12000,
                    1,
                    null,
                    null
            );

            productService.crearProductoPrecarga(
                    idusuario,
                    "Pc Gamer",
                    "PC gamer Ryzen 5 5600 y GTX 1070",
                    400000,
                    1,
                    null,
                    null
            );

            productService.crearProductoPrecarga(
                    idusuario,
                    "Ps3",
                    "PS3 en buen estado con 2 controles",
                    50000,
                    2,
                    null,
                    null
            );

            System.out.println("🎉 Productos precargados correctamente");
        };
    }
}