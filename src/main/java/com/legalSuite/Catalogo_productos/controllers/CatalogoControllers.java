package com.legalSuite.Catalogo_productos.controllers;

import com.legalSuite.Catalogo_productos.models.ProductoEntity;
import com.legalSuite.Catalogo_productos.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<?> saveProducto(@Valid @RequestBody ProductoEntity producto, BindingResult result) {
        try{
            if (result.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(errors);
            }
            if (producto.getPrecio() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "El precio debe ser mayor a 0"));
            }
            ProductoEntity savedProducto = productoService.saveProducto(producto);
            return ResponseEntity.ok(savedProducto);
        }catch (Exception e){
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al buscar el producto"));
        }

    }


    @GetMapping()
    public ResponseEntity<?> getProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size <= 0) {
            return ResponseEntity.status(400).body("Parámetros de paginación inválidos");
        }
        if ((precioMin != null && precioMin <= 0) || (precioMax != null && precioMax <= 0)) {
            return ResponseEntity.status(400).body(Map.of("ERROR", "El precio no puede ser negativo o igual a 0"));
        }
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductoEntity> productos = productoService.getProductosFilter(nombre, precioMin, precioMax, pageable);

            if (productos.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("Mensaje", "No se encontraron productos con los filtros aplicadoz"));
            }

            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al buscar el producto"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductobyId(@PathVariable Long id){
        try {
            if (!(id instanceof Long) ){
                return ResponseEntity.status(400).body(Map.of("error", "El ID debe ser un número entero positivo."));
            }

            Optional<ProductoEntity> product = productoService.getProductoById(id);
            if (product.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("Eror", "Producto con el ID " + id + " no ubicado"));
            }

            return ResponseEntity.ok(product.get());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al buscar el producto."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateByID(@RequestBody ProductoEntity producto,@PathVariable("id") Long id ){
        try {
            if (!(id instanceof Long) ){
                return ResponseEntity.badRequest().body(Map.of("error", "El ID debe ser un número entero positivo."));
            }

            Optional<ProductoEntity> productoExistente = productoService.getProductoById(id);

            if (productoExistente.isEmpty()) {
                return ResponseEntity.status(404).body("Error: Producto con ID " + id + " no encontrado.");
            }

            ProductoEntity productoActualizado = productoExistente.get();

            if (producto.getNombre() != null) {
                productoActualizado.setNombre(producto.getNombre());
            }
            if (producto.getDescripcion() != null) {
                productoActualizado.setDescripcion(producto.getDescripcion());
            }
            if (producto.getPrecio() > 0) {
                productoActualizado.setPrecio(producto.getPrecio());
            }
            ProductoEntity productoGuardado = productoService.saveProducto(productoActualizado);

            return ResponseEntity.ok(productoGuardado);
        }catch (Exception e){
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al buscar el producto."));
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id){
        try{
            if (!(id instanceof Long) ){
                return ResponseEntity.status(400).body(Map.of("error", "El ID debe ser un número entero positivo."));
            }

            Optional<ProductoEntity> productoExixt = productoService.getProductoById(id);

            if(productoExixt.isEmpty()){
                return ResponseEntity.status(404).body("Error: Producto con ID " + id + " no encontrado.");
            }

            ProductoEntity productoEliminado = productoExixt.get();
            boolean eliminado = productoService.deleteById(id);

            if (!eliminado) {
                return ResponseEntity.status(405).body("No se pudo eliminar el producto con el ID: " + id);
            }

            return ResponseEntity.ok(productoEliminado);
        }catch (Exception e){
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al buscar el producto."));
        }

    }

}
