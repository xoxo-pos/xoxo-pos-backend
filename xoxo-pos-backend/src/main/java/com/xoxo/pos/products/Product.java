package com.xoxo.pos.products;
import jakarta.persistence.*;import java.math.BigDecimal;
@Entity @Table(name="products") public class Product{
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@Column(nullable=false) private String name; private String category;
@Column(nullable=false,precision=10,scale=2) private BigDecimal price;
@Column(precision=10,scale=2) private BigDecimal costPrice = BigDecimal.ZERO;
private Integer stock; private Integer lowStockAlert; private boolean active=true;
public Long getId(){return id;} public String getName(){return name;} public String getCategory(){return category;} public BigDecimal getPrice(){return price;} public BigDecimal getCostPrice(){return costPrice;} public Integer getStock(){return stock;} public Integer getLowStockAlert(){return lowStockAlert;} public boolean isActive(){return active;}
public void setId(Long id){this.id=id;} public void setName(String name){this.name=name;} public void setCategory(String category){this.category=category;} public void setPrice(BigDecimal price){this.price=price;} public void setCostPrice(BigDecimal costPrice){this.costPrice=costPrice;} public void setStock(Integer stock){this.stock=stock;} public void setLowStockAlert(Integer lowStockAlert){this.lowStockAlert=lowStockAlert;} public void setActive(boolean active){this.active=active;}}
