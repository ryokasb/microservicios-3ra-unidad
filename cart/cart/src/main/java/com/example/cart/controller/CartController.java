package com.example.cart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.cart.model.Cart;
import com.example.cart.model.CartItem;
import com.example.cart.model.dto.AddItemRequest;
import com.example.cart.model.dto.DeleteItem;
import com.example.cart.model.dto.UpdateItemRequest;
import com.example.cart.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "carrito", description = "Operaciones relacionadas con el carrito de compras")
@RestController
@RequestMapping("/duodeal/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    //obtener carrito
    @Operation(summary = "Obtener carrito del usuario", description = "Devuelve el carrito asociado al usuario; si no existe, lo crea")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito obtenido correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<?> obtenerCarrito(@PathVariable Long userId) {
        try {
            Cart cart = cartService.getOrCreateCart(userId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al obtener carrito", e.getMessage()));
        }
    }

    // Agregar item
    
    @   Operation(summary = "Agregar producto al carrito", description = "Añade un producto al carrito del usuario (se valida usuario via token)")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto agregado al carrito"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
       @PostMapping("/add")
        public ResponseEntity<?> agregarItem(
        @RequestBody AddItemRequest request
         ) {
    try {
        Long userId = request.getUserid();
        Long productId = request.getProductid();
        int quantity = request.getQuantity();
        String token = request.getToken();

        if (quantity <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Cantidad inválida", "La cantidad debe ser mayor que 0"));
        }

        Cart cart = cartService.addItem(token, userId, productId, quantity);
        return ResponseEntity.ok(cart);
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Error al agregar item", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor", e.getMessage()));
    }
      }

   
    // Actualizar cantidad 
    
    @Operation(summary = "Actualizar cantidad de un item", description = "Actualiza la cantidad absoluta de un item (cantidad final). Si es 0, se elimina el item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada"),
            @ApiResponse(responseCode = "400", description = "Cantidad inválida"),
            @ApiResponse(responseCode = "404", description = "Item no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/update/{userid}/{itemid}")
    public ResponseEntity<?> actualizarCantidad(
            @PathVariable Long userid,
            @PathVariable Long itemid,
            @RequestBody UpdateItemRequest request
    ) {
        
        try {
            if (request.getQuantity() < 0) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Cantidad inválida", "La cantidad no puede ser negativa"));
            }

            // Si quantity = 0, el service eliminará el item 
            Cart cart = cartService.updateItemQuantity(request.getToken(), userid, itemid, request.getQuantity());
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Error al actualizar item", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }

    
    // Eliminar item
    
   @Operation(summary = "Eliminar item del carrito", description = "Elimina un item del carrito por su productId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item eliminado"),
        @ApiResponse(responseCode = "404", description = "Item no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
})
@DeleteMapping("/remove/{userid}/{productid}/{token}")
public ResponseEntity<?> eliminarItem(
        @PathVariable Long userid,
        @PathVariable Long productid,
        @PathVariable String token
) {
    try {
        Cart cart = cartService.removeItem(token, userid, productid); // ahora productId
        return ResponseEntity.ok(cart);
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Error al eliminar item", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor", e.getMessage()));
    }
}
    
    // Vaciar carrito
    
    @Operation(summary = "Vaciar carrito completo de un usuario", description = "Elimina todos los items del carrito del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito vaciado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/clear/{userId}/{token}")
    public ResponseEntity<?> vaciarCarrito(
            @PathVariable Long userId,
            @PathVariable String token
    ) {
        try {
            cartService.clearCart(token, userId);
            SuccessResponse response = new SuccessResponse("Carrito vaciado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al vaciar carrito", e.getMessage()));
        }
    }

    
    // Response classes (mismo estilo que UsuarioController)
    
    @Schema(description = "Respuesta de error estandarizada")
    public static class ErrorResponse {
        @Schema(description = "Tipo de error", example = "Error de validación")
        private String error;
        @Schema(description = "Mensaje descriptivo del error", example = "Detalle del error")
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
