package com.legalSuite.Catalogo_productos.services;

import com.legalSuite.Catalogo_productos.models.ProductoEntity;
import com.legalSuite.Catalogo_productos.repository.IProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private IProductoService productoRepo;


    public ProductoEntity saveProducto(ProductoEntity producto) {
        return productoRepo.save(producto);
    }


    public Optional<List<ProductoEntity>> getProcto(){
        List<ProductoEntity> product = productoRepo.findAll();
        return Optional.of(product);
    }

    public Optional<ProductoEntity> getProductoById(Long id){
        return productoRepo.findById(id);
    }

    public Page<ProductoEntity> getProductosFilter(String nombre, Double precioMin, Double precioMax, Pageable pageable) {
        return productoRepo.findByFilters(nombre, precioMin, precioMax, pageable);
    }

    public Boolean deleteById(Long id){
        try {
            productoRepo.deleteById(id);
            return Boolean.TRUE;
        }catch (Exception e){
            return Boolean.FALSE;
        }

    }
}
