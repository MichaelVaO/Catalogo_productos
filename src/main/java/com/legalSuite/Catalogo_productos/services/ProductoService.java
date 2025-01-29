package com.legalSuite.Catalogo_productos.services;

import com.legalSuite.Catalogo_productos.models.ProductoEntity;
import com.legalSuite.Catalogo_productos.repository.IProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductoService {

    @Autowired
    private IProductoService productoRepo;

    @Async
    public CompletableFuture<ProductoEntity> saveProducto(ProductoEntity producto) {
        return CompletableFuture.supplyAsync(() -> productoRepo.save(producto));
    }

    @Async
    public Optional<List<ProductoEntity>> getProcto(){
        List<ProductoEntity> product = productoRepo.findAll();
        return Optional.of(product);
    }
    @Async
    public CompletableFuture<Optional<ProductoEntity>> getProductoById(Long id) {
        return CompletableFuture.completedFuture(productoRepo.findById(id));
    }

    @Async
    public CompletableFuture<Page<ProductoEntity>> getProductosFilter(String nombre, Double precioMin, Double precioMax, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> productoRepo.findByFilters(nombre, precioMin, precioMax, pageable));
    }

    @Async
    public CompletableFuture<Boolean> deleteById(Long id) {
        try {
            Optional<ProductoEntity> producto = productoRepo.findById(id);
            if (producto.isPresent()) {
                productoRepo.deleteById(id);
                return CompletableFuture.completedFuture(Boolean.TRUE);
            } else {
                return CompletableFuture.completedFuture(Boolean.FALSE);
            }
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
