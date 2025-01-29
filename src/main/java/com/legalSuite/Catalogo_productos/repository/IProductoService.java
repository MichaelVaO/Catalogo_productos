package com.legalSuite.Catalogo_productos.repository;

import com.legalSuite.Catalogo_productos.models.ProductoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface IProductoService extends JpaRepository<ProductoEntity, Long> {

    @Query("SELECT p FROM ProductoEntity  p WHERE " +
            "(:nombre IS NULL OR p.nombre LIKE %:nombre%) AND " +
            "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
            "(:precioMax IS NULL OR p.precio <= :precioMax)")
    Page<ProductoEntity> findByFilters(
            @Param("nombre") String nombre,
            @Param("precioMin") Double precioMin,
            @Param("precioMax") Double precioMax,
            Pageable pageable);

}
