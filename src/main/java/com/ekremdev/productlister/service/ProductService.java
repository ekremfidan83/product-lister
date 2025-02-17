package com.ekremdev.productlister.service;

import com.ekremdev.productlister.entity.Product;
import com.ekremdev.productlister.repository.ProductRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @SneakyThrows
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new Exception("Ürün bulunamadı: " + id));
    }

    public Product createProduct(Product product) {
        log.info("Yeni ürün veritabanına ekleniyor: {}", product.getName());
        Product savedProduct = productRepository.save(product);
        try {
            log.info("Ürün cache'e eklendi, ID: {}", savedProduct.getId());
        } catch (Exception e) {
            log.error("Cache güncelleme hatası! Redis sunucusuna erişilemiyor olabilir. Hata: {}", e.getMessage());
        }
        return savedProduct;
    }

    public void deleteProduct(Long id) {
        log.info("Ürün siliniyor: {}", id);
        productRepository.deleteById(id);
    }

    public Page<Product> getProductsPaginated(int page, int size) {
        log.warn("Sayfalı veriler veritabanından getiriliyor - Sayfa: {}, Boyut: {}", page, size);
        return productRepository.findAll(PageRequest.of(page, size));
    }
}