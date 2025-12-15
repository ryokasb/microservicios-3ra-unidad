package com.example.product.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.product.Service.ProductService;
import com.example.product.model.Product;
import com.example.product.model.Dto.UpdateStockDto;
import com.example.product.repository.ProductRepository;
import com.example.product.webclient.usuarioclient;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private usuarioclient usuarioClient;

    @InjectMocks
    private ProductService productService;

    private Product producto;

    @BeforeEach
    void setUp() {
        producto = new Product();
        producto.setId(1L);
        producto.setIduser(10L);
        producto.setName("Producto Test");
        producto.setDescription("Descripcion");
        producto.setPrice(1000);
        producto.setStock(5);
        producto.setPhoto("foto".getBytes());
    }

    // ---------------- CREAR PRODUCTO ----------------

    @Test
    void crearProducto_ok() {
        when(usuarioClient.obtenerUsuarioPorId(10L, "token"))
                .thenReturn(Map.of("id", 10L));

        when(productRepository.save(any(Product.class)))
                .thenReturn(producto);

        Product result = productService.crearProducto(
                10L, "token", "Producto Test", "Descripcion", 1000, 5, "foto".getBytes());

        assertNotNull(result);
        assertEquals("Producto Test", result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void crearProducto_usuarioNoExiste() {
        when(usuarioClient.obtenerUsuarioPorId(anyLong(), anyString()))
                .thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () ->
                productService.crearProducto(
                        10L, "token", "Producto", "Desc", 1000, 5, null));
    }

    // ---------------- PRECARGA ----------------

    @Test
    void crearProductoPrecarga_ok() {
        String base64 = Base64.getEncoder().encodeToString("foto".getBytes());

        when(productRepository.save(any(Product.class)))
                .thenReturn(producto);

        Product result = productService.crearProductoPrecarga(
                10L, "Producto", "Desc", 1000, 5, base64, "token");

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    // ---------------- OBTENER PRODUCTO ----------------

    @Test
    void getProducto_ok() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        Product result = productService.getProducto(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getProducto_noExiste() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                productService.getProducto(1L));
    }

    // ---------------- LISTAR ----------------

    @Test
    void listarProductos_ok() {
        when(productRepository.findAll())
                .thenReturn(List.of(producto));

        List<Product> productos = productService.listarProductos();

        assertEquals(1, productos.size());
    }

    // ---------------- ACTUALIZAR ----------------

    @Test
    void actualizarProducto_ok() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(producto));
        when(productRepository.save(any(Product.class)))
                .thenReturn(producto);

        Product nuevo = new Product();
        nuevo.setName("Nuevo");
        nuevo.setDescription("Nueva desc");
        nuevo.setPrice(2000);
        nuevo.setStock(10);

        Product result = productService.actualizarProducto(1L, nuevo);

        assertEquals("Nuevo", result.getName());
        verify(productRepository).save(producto);
    }

    // ---------------- ELIMINAR ----------------

    @Test
    void eliminarProducto_ok() {
        when(productRepository.existsById(1L)).thenReturn(true);

        String mensaje = productService.eliminarProducto(1L);

        assertTrue(mensaje.contains("eliminado"));
        verify(productRepository).deleteById(1L);
    }

    // ---------------- ELIMINAR POR USUARIO ----------------

    @Test
    void eliminarPorUserId_ok() {
        when(usuarioClient.obtenerUsuarioPorId(10L, "token"))
                .thenReturn(Map.of("id", 10L));
        when(productRepository.countByIduser(10L)).thenReturn(2L);

        String result = productService.eliminarporUserid(10L, "token");

        assertEquals("Productos eliminados correctamente", result);
        verify(productRepository).deleteByIduser(10L);
    }

    // ---------------- RESTAR STOCK ----------------

    @Test
    void restarStock_actualizado() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(producto));
        when(productRepository.save(any(Product.class)))
                .thenReturn(producto);

        UpdateStockDto result = productService.restarStockProducto(1L, 2);

        assertEquals("ACTUALIZADO", result.getStatus());
    }

    @Test
    void restarStock_eliminado() {
        producto.setStock(2);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        UpdateStockDto result = productService.restarStockProducto(1L, 2);

        assertEquals("ELIMINADO", result.getStatus());
        verify(productRepository).deleteById(1L);
    }

    // ---------------- FOTO BASE64 ----------------

    @Test
    void obtenerFotoBase64_ok() {
        String base64 = productService.obtenerFotoBase64(producto);
        assertNotNull(base64);
    }
}
