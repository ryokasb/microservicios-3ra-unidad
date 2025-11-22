package com.example.product.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.product.model.Dto.ProductoDTO;

import com.example.product.model.Product;
import com.example.product.Service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "productos", description = "Operaciones relacionadas con la gestión de productos")
@RestController
@RequestMapping("/duodeal/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Obtener todos los productos", description = "Devuelve una lista de todos los productos registrados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<?> listarProductos() {
        try {
            List<Product> products = productService.listarProductos();

            if (products.isEmpty()) {
                SuccessResponse response = new SuccessResponse("No hay productos registrados");
                EntityModel<SuccessResponse> model = EntityModel.of(response);
                model.add(linkTo(methodOn(ProductController.class).listarProductos()).withSelfRel());
                return ResponseEntity.ok(model);
            }

            List<EntityModel<Product>> productModels = products.stream()
                .map(product -> {
                    EntityModel<Product> model = EntityModel.of(product);
                    model.add(linkTo(methodOn(ProductController.class).obtenerProducto(product.getId())).withSelfRel());
                    model.add(linkTo(methodOn(ProductController.class).actualizarProducto(product.getId(), null)).withRel("update"));
                    model.add(linkTo(methodOn(ProductController.class).eliminarProducto(product.getId())).withRel("delete"));
                    return model;
                }).collect(Collectors.toList());

            CollectionModel<EntityModel<Product>> collectionModel = CollectionModel.of(productModels);
            collectionModel.add(linkTo(methodOn(ProductController.class).listarProductos()).withSelfRel());
            collectionModel.add(linkTo(ProductController.class).slash("duodeal/products").withRel("create-product"));

            return ResponseEntity.ok(collectionModel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al obtener productos", e.getMessage()));
        }
    }

    @Operation(summary = "Buscar producto por ID", description = "Devuelve los datos del producto solicitado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProducto(@PathVariable Long id) {
        try {
            Product product = productService.getProducto(id);

            EntityModel<Product> model = EntityModel.of(product);
            model.add(linkTo(methodOn(ProductController.class).obtenerProducto(id)).withSelfRel());
            model.add(linkTo(methodOn(ProductController.class).actualizarProducto(id, null)).withRel("update"));
            model.add(linkTo(methodOn(ProductController.class).eliminarProducto(id)).withRel("delete"));
            model.add(linkTo(methodOn(ProductController.class).listarProductos()).withRel("all-products"));

            return ResponseEntity.ok(model);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Producto no encontrado", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno", e.getMessage()));
        }
    }

    @Operation(summary = "Crear un nuevo producto", description = "Permite registrar un nuevo producto en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Error en los datos proporcionados"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
   @PostMapping
   public ResponseEntity<?> crearProducto(@RequestBody ProductoDTO dto) {
    try {
        Product newProduct = productService.crearProducto(
                dto.getUserId(),
                dto.getToken(),
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getStock(),
                dto.getPhotoBytes()
                
        );

        EntityModel<Product> model = EntityModel.of(newProduct);
        model.add(linkTo(methodOn(ProductController.class).obtenerProducto(newProduct.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);

    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ErrorResponse("Error al decodificar la imagen", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(new ErrorResponse("Error interno del servidor", e.getMessage()));
    }
}

    @Operation(summary = "Actualizar producto", description = "Permite actualizar los datos de un producto existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Error en los datos proporcionados"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody Product datosNuevos) {
        try {
            Product updatedProduct = productService.actualizarProducto(id, datosNuevos);

            EntityModel<Product> model = EntityModel.of(updatedProduct);
            model.add(linkTo(methodOn(ProductController.class).obtenerProducto(id)).withSelfRel());
            model.add(linkTo(methodOn(ProductController.class).eliminarProducto(id)).withRel("delete"));
            model.add(linkTo(methodOn(ProductController.class).listarProductos()).withRel("all-products"));

            return ResponseEntity.ok(model);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Error al actualizar producto", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar producto", description = "Elimina un producto del sistema por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        try {
            String mensaje = productService.eliminarProducto(id);
            SuccessResponse response = new SuccessResponse(mensaje);
            EntityModel<SuccessResponse> model = EntityModel.of(response);
            model.add(linkTo(methodOn(ProductController.class).listarProductos()).withRel("all-products"));
            model.add(linkTo(methodOn(ProductController.class).crearProducto((ProductoDTO) null)).withRel("create-product"));
            return ResponseEntity.ok(model);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Error al eliminar producto", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }

    @Schema(description = "Respuesta de error estandarizada")
    public static class ErrorResponse {
        private String error;
        private String mensaje;
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
        private String mensaje;
        private Object data;
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

