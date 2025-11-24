package com.example.product.Service;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import com.example.product.webclient.usuarioclient;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
     @Autowired
    private usuarioclient usuarioClient;

    // Crear un nuevo producto
    public Product crearProducto(Long idusuario, String token, String name, String description, double price, int stock, byte[] photo) {

        try {
    // validar si el usuario existe antes de agregar el producto, enviando token
    Map<String, Object> usuario = usuarioClient.obtenerUsuarioPorId(idusuario, token);

    // Si llegó aquí, el usuario existe
    System.out.println("Usuario encontrado: " + usuario);

} catch (RuntimeException e) {
    // Aquí entra si el WebClient devolvió 4xx o 5xx
    throw new RuntimeException("Usuario no encontrado, no se puede agregar el producto", e);
}

        // Validaciones básicas
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("El nombre del producto no puede estar vacío");
        }
        if (price < 0) {
            throw new RuntimeException("El precio no puede ser negativo");
        }
        if (stock < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        Product product = new Product();
        product.setIduser(idusuario);
        product.setName(name.trim());
        product.setDescription(description != null ? description.trim() : null);
        product.setPrice(price);
        product.setStock(stock);
        product.setPhoto(photo);

        return productRepository.save(product);
    }

    // Crear un nuevo producto solo para precarga
    public Product crearProductoPrecarga(Long idusuario, String name, String description, double price, int stock, byte[] photo, String token) {

        // Validaciones básicas
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("El nombre del producto no puede estar vacío");
        }
        if (price < 0) {
            throw new RuntimeException("El precio no puede ser negativo");
        }
        if (stock < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        Product product = new Product();
        product.setIduser(idusuario);
        product.setName(name.trim());
        product.setDescription(description != null ? description.trim() : null);
        product.setPrice(price);
        product.setStock(stock);
        product.setPhoto(photo);

        return productRepository.save(product);
    }


    // Obtener un producto por ID
    public Product getProducto(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("ID de producto inválido");
        }
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    // Obtener todos los productos
    public List<Product> listarProductos() {
        return productRepository.findAll();
    }

    // Actualizar un producto
    public Product actualizarProducto(Long id, Product datosNuevos) {
        if (id == null || id <= 0) {
            throw new RuntimeException("ID de producto inválido");
        }

        Product productoExistente = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // Validaciones
        if (datosNuevos.getName() == null || datosNuevos.getName().trim().isEmpty()) {
            throw new RuntimeException("El nombre del producto no puede estar vacío");
        }
        if (datosNuevos.getPrice() < 0) {
            throw new RuntimeException("El precio no puede ser negativo");
        }
        if (datosNuevos.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        // Actualizar campos
        productoExistente.setName(datosNuevos.getName().trim());
        productoExistente.setDescription(datosNuevos.getDescription() != null ? datosNuevos.getDescription().trim() : null);
        productoExistente.setPrice(datosNuevos.getPrice());
        productoExistente.setStock(datosNuevos.getStock());

        // Actualizar foto solo si se proporciona
        if (datosNuevos.getPhoto() != null && datosNuevos.getPhoto().length > 0) {
            productoExistente.setPhoto(datosNuevos.getPhoto());
        }

        return productRepository.save(productoExistente);
    }

    // Eliminar un producto por ID
    public String eliminarProducto(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("ID de producto inválido");
        }
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Producto con ID " + id + " no existe");
        }
        productRepository.deleteById(id);
        return "Producto con ID " + id + " eliminado exitosamente";
    }

    public String obtenerFotoBase64(Product product) {
        if (product.getPhoto() == null) return null;
        return Base64.getEncoder().encodeToString(product.getPhoto());
    }
}
