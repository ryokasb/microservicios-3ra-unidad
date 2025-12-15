package com.example.cart.service;

import com.example.cart.model.Cart;
import com.example.cart.model.CartItem;
import com.example.cart.repository.CartItemRepository;
import com.example.cart.repository.CartRepository;
import com.example.cart.webclient.usuarioclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class cartServiceTest {

    @Mock
    private usuarioclient usuarioclient;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartService cartService;

    private Cart cart;

    @BeforeEach
    void setup() {
        cart = new Cart();
        cart.setId(1L);
        cart.setUserId(10L);
        cart.setItems(new ArrayList<>());
    }

    // ---------------- getOrCreateCart ----------------

    @Test
    void getOrCreateCart_whenCartExists_returnsExistingCart() {
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(cart));

        Cart result = cartService.getOrCreateCart(10L);

        assertEquals(cart, result);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getOrCreateCart_whenCartDoesNotExist_createsNewCart() {
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.getOrCreateCart(10L);

        assertNotNull(result);
        assertEquals(10L, result.getUserId());
        verify(cartRepository).save(any(Cart.class));
    }

    // ---------------- addItem ----------------

    @Test
    void addItem_newProduct_createsNewItem() {
        when(usuarioclient.obtenerUsuarioPorId(eq(10L), anyString()))
                .thenReturn(Map.of("id", 10L));
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.addItem("token", 10L, 100L, 2);

        assertEquals(1, result.getItems().size());
        CartItem item = result.getItems().get(0);
        assertEquals(100L, item.getProductId());
        assertEquals(2, item.getQuantity());
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItem_existingProduct_increasesQuantity() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setProductId(100L);
        item.setQuantity(1);
        item.setCart(cart);
        cart.getItems().add(item);

        when(usuarioclient.obtenerUsuarioPorId(eq(10L), anyString()))
                .thenReturn(Map.of("id", 10L));
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.addItem("token", 10L, 100L, 3);

        assertEquals(1, result.getItems().size());
        assertEquals(4, result.getItems().get(0).getQuantity());
        verify(cartItemRepository).save(item);
    }

    @Test
    void addItem_userNotFound_throwsException() {
        when(usuarioclient.obtenerUsuarioPorId(eq(10L), anyString()))
                .thenReturn(Collections.emptyMap());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.addItem("token", 10L, 100L, 1));

        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
    }

    // ---------------- updateItemQuantity ----------------

    @Test
    void updateItemQuantity_updatesQuantity() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setProductId(100L);
        item.setQuantity(2);
        item.setCart(cart);
        cart.getItems().add(item);

        when(usuarioclient.obtenerUsuarioPorId(eq(10L), anyString()))
                .thenReturn(Map.of("id", 10L));
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.updateItemQuantity("token", 10L, 1L, 5);

        assertEquals(5, item.getQuantity());
        verify(cartItemRepository).save(item);
        assertEquals(cart, result);
    }

    @Test
    void updateItemQuantity_quantityZero_removesItem() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setProductId(100L);
        item.setQuantity(2);
        item.setCart(cart);
        cart.getItems().add(item);

        when(usuarioclient.obtenerUsuarioPorId(eq(10L), anyString()))
                .thenReturn(Map.of("id", 10L));
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.updateItemQuantity("token", 10L, 1L, 0);

        assertTrue(result.getItems().isEmpty());
        verify(cartItemRepository).delete(item);
    }

    // ---------------- removeItem ----------------

    @Test
    void removeItem_success() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setProductId(100L);
        item.setCart(cart);
        cart.getItems().add(item);

        when(usuarioclient.obtenerUsuarioPorId(eq(10L), anyString()))
                .thenReturn(Map.of("id", 10L));
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.removeItem("token", 10L, 100L);

        assertTrue(result.getItems().isEmpty());
        verify(cartItemRepository).delete(item);
    }

    // ---------------- clearCart ----------------

    @Test
    void clearCart_deletesAllItems() {
        CartItem item1 = new CartItem();
        item1.setProductId(100L);
        item1.setCart(cart);
        cart.getItems().add(item1);

        when(usuarioclient.obtenerUsuarioPorId(eq(10L), anyString()))
                .thenReturn(Map.of("id", 10L));
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(cart));

        cartService.clearCart("token", 10L);

        verify(cartItemRepository).deleteAll(anyList());
        assertTrue(cart.getItems().isEmpty());
    }
}
