package com.example.product.Controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.product.Service.ProductService;
import com.example.product.model.Product;
import com.example.product.model.Dto.DeleteByiduserDto;
import com.example.product.model.Dto.ProductoDTO;
import com.example.product.model.Dto.UpdateStockDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------- LISTAR PRODUCTOS ----------------
    //ok
    @Test
    void listarProductos_ok() throws Exception {
        when(productService.listarProductos())
                .thenReturn(List.of(new Product()));

        mockMvc.perform(get("/duodeal/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded").exists());
    }
    //error
    @Test
    void listarProductos_error() throws Exception {
        when(productService.listarProductos())
                .thenReturn(null);

        mockMvc.perform(get("/duodeal/products"))
                .andExpect(status().isInternalServerError());
    }
    // ---------------- OBTENER PRODUCTO ----------------
    //ok
    @Test
    void obtenerProducto_ok() throws Exception {
        Product product = new Product();
        product.setId(1L);

        when(productService.getProducto(1L)).thenReturn(product);

        mockMvc.perform(get("/duodeal/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L));
    }
    //error
    @Test
    void obtenerProducto_error() throws Exception {

    when(productService.getProducto(1L))
            .thenThrow(new RuntimeException("Producto no encontrado"));

    mockMvc.perform(get("/duodeal/products/{id}", 1L))
            .andExpect(status().isNotFound());
}
    // ---------------- CREAR PRODUCTO ----------------

    @Test
    void crearProducto_ok() throws Exception {
        ProductoDTO dto = new ProductoDTO();
        dto.setUserId(1L);
        dto.setToken("token123");
        dto.setName("Producto Test");
        dto.setDescription("Desc");
        dto.setPrice(1000);
        dto.setStock(5);

        Product product = new Product();
        product.setId(1L);
        product.setName("Producto Test");

        when(productService.crearProducto(
                anyLong(), anyString(), anyString(),
                anyString(), anyDouble(), anyInt(), any()))
                .thenReturn(product);

        mockMvc.perform(post("/duodeal/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("name").value("Producto Test"));
    }

    // ---------------- ACTUALIZAR PRODUCTO ----------------

    @Test
    void actualizarProducto_ok() throws Exception {
        Product actualizado = new Product();
        actualizado.setId(1L);
        actualizado.setName("Nuevo");

        when(productService.actualizarProducto(eq(1L), any(Product.class)))
                .thenReturn(actualizado);

        mockMvc.perform(put("/duodeal/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("Nuevo"));
    }

    // ---------------- RESTAR STOCK ----------------

    @Test
    void restarStock_ok() throws Exception {
        UpdateStockDto dto = new UpdateStockDto("ACTUALIZADO", new Product());

        when(productService.restarStockProducto(1L, 2))
                .thenReturn(dto);

        mockMvc.perform(put("/duodeal/products/{id}/restarstock/{cantidad}", 1L, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("ACTUALIZADO"));
    }

    // ---------------- ELIMINAR PRODUCTO ----------------

    @Test
    void eliminarProducto_ok() throws Exception {
        when(productService.eliminarProducto(1L))
                .thenReturn("Producto eliminado");

        mockMvc.perform(delete("/duodeal/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("mensaje").value("Producto eliminado"));
    }

    // ---------------- ELIMINAR PRODUCTOS POR USUARIO ----------------

    @Test
    void eliminarProductosPorUsuario_ok() throws Exception {
        DeleteByiduserDto dto = new DeleteByiduserDto();
        dto.setToken("token123");

        when(productService.eliminarporUserid(1L, "token123"))
                .thenReturn("Productos eliminados correctamente");

        mockMvc.perform(delete("/duodeal/products/user/{idusuario}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("mensaje")
                        .value("Productos eliminados correctamente"));
    }
}
