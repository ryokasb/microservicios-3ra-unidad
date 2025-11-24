package com.example.cart.service;

import com.example.cart.model.Cart;
import com.example.cart.model.CartItem;
import com.example.cart.repository.CartItemRepository;
import com.example.cart.repository.CartRepository;
import com.example.cart.webclient.usuarioclient;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Transactional
@Service
@RequiredArgsConstructor
public class CartService {

    private final usuarioclient usuarioclient;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;


  
    //metodo para crear o buscar un carrito
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

  //metodo para agregar un item a un carrito
   public Cart addItem(String token, Long idusuario, Long productId, int quantity) {

// Validar si el usuario existe antes de agregar el producto, enviando token
Map<String, Object> usuario = usuarioclient.obtenerUsuarioPorId(idusuario, token);

if (usuario == null || usuario.isEmpty()) {
    throw new RuntimeException("Usuario no encontrado, no se puede agregar el producto");
}

Cart cart = getOrCreateCart(idusuario);

// Limpiar items corruptos que tengan productId nulo
cart.getItems().removeIf(item -> item.getProductId() == null);

// Buscar si ya existe un item con el mismo productId usando Objects.equals() para evitar NPE
CartItem existingItem = cart.getItems()
        .stream()
        .filter(item -> java.util.Objects.equals(item.getProductId(), productId))
        .findFirst()
        .orElse(null);

if (existingItem != null) {
    // Si existe, aumentar la cantidad
    existingItem.setQuantity(existingItem.getQuantity() + quantity);
    cartItemRepository.save(existingItem);
} else {
    // Si no existe, crear un nuevo item
    CartItem newItem = new CartItem();
    newItem.setProductId(productId);
    newItem.setQuantity(quantity);
    newItem.setCart(cart);

    cart.getItems().add(newItem);
    cartItemRepository.save(newItem);
}

return cartRepository.save(cart);
   }

   public Cart updateItemQuantity(String token, Long idusuario, Long itemId, int quantity) {

    // validar si el usuario existe antes de agregar el producto, enviando token
    Map<String, Object> usuario = usuarioclient.obtenerUsuarioPorId(idusuario, token);

    if (usuario == null || usuario.isEmpty()) {
        throw new RuntimeException("Usuario no encontrado, no se puede actualizar el producto");
    }

    Cart cart = getOrCreateCart(idusuario);

    CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item no encontrado"));

    if (!item.getCart().getId().equals(cart.getId())) {
        throw new RuntimeException("El Item no pertenece a este carrito");
    }

    if (quantity <= 0) {
        // eliminar item si la cantidad es 0 o negativa
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
    } else {
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    return cartRepository.save(cart);
}
  
    public Cart removeItem(String token, Long idusuario, Long productId) {

    // Validar si el usuario existe
    Map<String, Object> usuario = usuarioclient.obtenerUsuarioPorId(idusuario, token);
    if (usuario == null || usuario.isEmpty()) {
        throw new RuntimeException("Usuario no encontrado");
    }

    Cart cart = getOrCreateCart(idusuario);

    // Buscar el item por productId y carrito
    Optional<CartItem> optionalItem = cart.getItems().stream()
        .filter(i -> i.getProductId().equals(productId))
        .findFirst();

    if (optionalItem.isEmpty()) {
        // Devuelve algo más amigable, no 500
        throw new RuntimeException("Item no encontrado en el carrito");
    }

    CartItem item = optionalItem.get();
    cart.getItems().remove(item);
    cartItemRepository.delete(item);

    return cartRepository.save(cart);
}

   
    public void clearCart(String token, Long idusuario) {

           // validar si el usuario existe antes de agregar el producto, enviando token
        Map<String, Object> usuario = usuarioclient.obtenerUsuarioPorId(idusuario, token);
        
         if (usuario == null || usuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado, no se puede agregar el producto");
        }

        Cart cart = getOrCreateCart(idusuario);

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();

        cartRepository.save(cart);
    }
}