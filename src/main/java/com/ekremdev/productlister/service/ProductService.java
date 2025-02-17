package com.ekremdev.productlister.service;

import com.ekremdev.productlister.entity.Product;
import com.ekremdev.productlister.repository.ProductRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        log.warn("Cache kullanılamıyor veya boş! Veriler veritabanından getiriliyor...");
        return productRepository.findAll();
    }

    @SneakyThrows
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        log.warn("Cache kullanılamıyor veya bulunamadı! ID: {} için veri veritabanından getiriliyor", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new Exception("Ürün bulunamadı: " + id));
    }

    @CachePut(value = "products", key = "#result.id")
    @CacheEvict(value = "products", key = "'all'")
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

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Ürün siliniyor: {}", id);
        productRepository.deleteById(id);
    }

    @Cacheable(value = "products", key = "'page_' + #page + '_' + #size")
    public Page<Product> getProductsPaginated(int page, int size) {
        log.warn("Cache kullanılamıyor veya bulunamadı! Sayfalı veriler veritabanından getiriliyor - Sayfa: {}, Boyut: {}", page, size);
        return productRepository.findAll(PageRequest.of(page, size));
    }
}