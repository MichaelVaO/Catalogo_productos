package com.legalSuite.Catalogo_productos.controllers;

import com.legalSuite.Catalogo_productos.models.ProductoEntity;
import com.legalSuite.Catalogo_productos.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/productos")
public class CatalogoControllers {

    @Autowired
    private ProductoService productoService;


    @PostMapping()
    public CompletableFuture<ResponseEntity<?>> saveProducto(
            @Valid @RequestBody ProductoEntity producto,
            BindingResult result) {


        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(404).body(errors)
            );
        }



        if (producto.getNombre().trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(
                            Map.of("nombre", "El nombre no puede estar vacío")
                    )
            );
        }

        if (producto.getPrecio() <= 0) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(404).body(
                            Map.of("precio", "El precio debe ser mayor a 0")
                    )
            );
        }

        return CompletableFuture.completedFuture(
                ResponseEntity.ok().body("Producto guardado")
        );
    }




    @GetMapping()
    public CompletableFuture<ResponseEntity<?>> getProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Validaciones de paginación y precios
        if (page < 0 || size <= 0) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(Map.of("error", "Parámetros de paginación inválidos"))
            );
        }
        if ((precioMin != null && precioMin <= 0) || (precioMax != null && precioMax <= 0)) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(Map.of("error", "El precio no puede ser negativo o igual a 0"))
            );
        }

        Pageable pageable = PageRequest.of(page, size);


        return productoService.getProductosFilter(nombre, precioMin, precioMax, pageable)
                .thenApply(productos -> {
                    if (productos.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("mensaje", "No se encontraron productos con los filtros aplicados"));
                    }
                    return ResponseEntity.ok(productos);
                })
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Error interno al buscar los productos: " + ex.getMessage())));
    }


    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<?>> getProductobyId(@PathVariable Long id) {
        if (id <= 0) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(404).body(Map.of("error", "El ID debe ser un número entero positivo."))
            );
        }

        return productoService.getProductoById(id)
                .thenApply(productOpt -> {
                    if (productOpt.isPresent()) {
                        return ResponseEntity.ok().body(productOpt.get());
                    } else {
                        return ResponseEntity.status(404)
                                .body(Map.of("error", "Producto con el ID " + id + " no encontrado."));
                    }
                })
                .exceptionally(ex -> ResponseEntity.status(500)
                        .body(Map.of("error", "Error interno al buscar el producto: " + ex.getMessage())));
    }

    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<?>> updateByID(@RequestBody ProductoEntity producto, @PathVariable("id") Long id) {
        if (id <= 0) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(Map.of("error", "El ID debe ser un número entero positivo."))
            );
        }

        return productoService.getProductoById(id)
                .thenCompose(productoExistenteOpt -> {
                    if (productoExistenteOpt.isEmpty()) {
                        return CompletableFuture.completedFuture(
                                ResponseEntity.status(400)
                                        .body(Map.of("error", "Producto con ID " + id + " no encontrado."))
                        );
                    }

                    ProductoEntity productoActualizado = productoExistenteOpt.get();

                    if (producto.getNombre() != null) {
                        productoActualizado.setNombre(producto.getNombre());
                    }
                    if (producto.getDescripcion() != null) {
                        productoActualizado.setDescripcion(producto.getDescripcion());
                    }


                    return productoService.saveProducto(productoActualizado)
                            .thenApply(productoGuardado -> ResponseEntity.ok(productoGuardado));
                })
                .exceptionally(ex -> ResponseEntity.status(500)
                        .body(Map.of("error", "Error interno al actualizar el producto: " + ex.getMessage()))
                );
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<?>> deleteById(@PathVariable Long id) {
        return productoService.deleteById(id)
                .thenApply(eliminado -> {
                    if (Boolean.TRUE.equals(eliminado)) {
                        return (ResponseEntity<?>) ResponseEntity.ok(Map.of("mensaje", "Producto eliminado correctamente"));
                    } else {
                        return (ResponseEntity<?>) ResponseEntity.status(404)
                                .body(Map.of("error", "No se encontró el producto con el ID: " + id));
                    }
                })
                .exceptionally(ex -> {
                    return (ResponseEntity<?>) ResponseEntity.status(500)
                            .body(Map.of("error", "Error interno al eliminar el producto: " + ex.getMessage()));
                });
    }
}
