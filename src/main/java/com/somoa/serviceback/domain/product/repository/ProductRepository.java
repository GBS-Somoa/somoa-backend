package com.somoa.serviceback.domain.product.repository;

import com.somoa.serviceback.domain.product.entity.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveCrudRepository<Product, Integer> {

    Flux<Product> findAllByBarcode(String barcode);
}
