package com.legalSuite.Catalogo_productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CatalogoProductosApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogoProductosApplication.class, args);
	}

}
