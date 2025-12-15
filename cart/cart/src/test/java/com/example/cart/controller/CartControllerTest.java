package com.example.cart.controller;

import com.example.cart.model.Cart;
import com.example.cart.model.dto.AddItemRequest;
import com.example.cart.model.dto.UpdateItemRequest;
import com.example.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    
      //obtener carrito

    @Test
    void obtenerCarrito_ok() throws Exception {
        Cart cart = new Cart();
        Mockito.when(cartService.getOrCreateCart(1L)).thenReturn(cart);

        mockMvc.perform(get("/duodeal/cart/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void obtenerCarrito_errorInterno() throws Exception {
        Mockito.when(cartService.getOrCreateCart(1L))
                .thenThrow(new RuntimeException("Fallo BD"));

        mockMvc.perform(get("/duodeal/cart/{userId}", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error al obtener carrito"));
    }

    
       //AGREGAR ITEM
      
    @Test
    void agregarItem_ok() throws Exception {
        AddItemRequest request = new AddItemRequest();
        request.setUserid(1L);
        request.setProductid(10L);
        request.setQuantity(2);
        request.setToken("token123");

        Cart cart = new Cart();
        Mockito.when(cartService.addItem("token123", 1L, 10L, 2))
                .thenReturn(cart);

        mockMvc.perform(post("/duodeal/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void agregarItem_cantidadInvalida() throws Exception {
        AddItemRequest request = new AddItemRequest();
        request.setUserid(1L);
        request.setProductid(10L);
        request.setQuantity(0);
        request.setToken("token123");

        mockMvc.perform(post("/duodeal/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cantidad inválida"));
    }

       //ACTUALIZAR CANTIDAD
      
    @Test
    void actualizarCantidad_ok() throws Exception {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setQuantity(3);
        request.setToken("token123");

        Cart cart = new Cart();
        Mockito.when(cartService.updateItemQuantity("token123", 1L, 5L, 3))
                .thenReturn(cart);

        mockMvc.perform(put("/duodeal/cart/update/{userid}/{itemid}", 1L, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarCantidad_negativa() throws Exception {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setQuantity(-1);
        request.setToken("token123");

        mockMvc.perform(put("/duodeal/cart/update/{userid}/{itemid}", 1L, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cantidad inválida"));
    }

   
      // ELIMINAR ITEM
      
    @Test
    void eliminarItem_ok() throws Exception {
        Cart cart = new Cart();
        Mockito.when(cartService.removeItem("token123", 1L, 10L))
                .thenReturn(cart);

        mockMvc.perform(delete("/duodeal/cart/remove/{userid}/{productid}/{token}",
                        1L, 10L, "token123"))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarItem_noEncontrado() throws Exception {
        Mockito.when(cartService.removeItem("token123", 1L, 10L))
                .thenThrow(new RuntimeException("Item no existe"));

        mockMvc.perform(delete("/duodeal/cart/remove/{userid}/{productid}/{token}",
                        1L, 10L, "token123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Error al eliminar item"));
    }

  
       // VACIAR CARRITO
       
    @Test
    void vaciarCarrito_ok() throws Exception {

    Mockito.doNothing()
           .when(cartService)
           .clearCart("token123", 1L);

    mockMvc.perform(delete("/duodeal/cart/clear/{userId}/{token}",
                    1L, "token123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje")
                    .value("Carrito vaciado correctamente"));
    }


    @Test
    void vaciarCarrito_error() throws Exception {
        Mockito.doThrow(new RuntimeException("Error"))
                .when(cartService).clearCart("token123", 1L);

        mockMvc.perform(delete("/duodeal/cart/clear/{userId}/{token}",
                        1L, "token123"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error")
                        .value("Error al vaciar carrito"));
    }
}
