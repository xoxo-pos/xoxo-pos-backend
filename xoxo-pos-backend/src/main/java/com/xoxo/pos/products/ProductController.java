package com.xoxo.pos.products;
import org.springframework.web.bind.annotation.*;import java.util.List;import java.math.BigDecimal;
@RestController @RequestMapping("/api/products") public class ProductController{private final ProductRepository repo;public ProductController(ProductRepository repo){this.repo=repo;}
@GetMapping public List<Product> list(){return repo.findByActiveTrueOrderByCategoryAscNameAsc();}
@GetMapping("/all") public List<Product> all(){return repo.findAll();}
@PostMapping public Product create(@RequestBody Product p){validate(p);if(p.getCostPrice()==null)p.setCostPrice(BigDecimal.ZERO);return repo.save(p);}
@PutMapping("/{id}") public Product update(@PathVariable Long id,@RequestBody Product r){validate(r);var p=repo.findById(id).orElseThrow();p.setName(r.getName());p.setCategory(r.getCategory());p.setPrice(r.getPrice());p.setCostPrice(r.getCostPrice()==null?BigDecimal.ZERO:r.getCostPrice());p.setStock(r.getStock());p.setLowStockAlert(r.getLowStockAlert());p.setActive(r.isActive());return repo.save(p);}
@PatchMapping("/{id}/stock") public Product stock(@PathVariable Long id,@RequestParam Integer stock){var p=repo.findById(id).orElseThrow();p.setStock(stock);return repo.save(p);}
@PatchMapping("/{id}/toggle") public Product toggle(@PathVariable Long id){var p=repo.findById(id).orElseThrow();p.setActive(!p.isActive());return repo.save(p);}
private void validate(Product p){if(p.getName()==null||p.getName().isBlank())throw new RuntimeException("Nombre requerido");if(p.getPrice()==null)throw new RuntimeException("Precio requerido");}}
