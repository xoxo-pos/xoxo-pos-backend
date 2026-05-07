package com.xoxo.pos.accounts;

import com.xoxo.pos.products.ProductRepository;
import com.xoxo.pos.tables.BarTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AccountService {
    private final AccountRepository accounts;
    private final BarTableRepository tables;
    private final ProductRepository products;
    private final AccountItemRepository accountItems;

    public AccountService(AccountRepository accounts, BarTableRepository tables, ProductRepository products, AccountItemRepository accountItems) {
        this.accounts = accounts;
        this.tables = tables;
        this.products = products;
        this.accountItems = accountItems;
    }

    @Transactional
    public Account create(CreateAccountRequest r) {
        var t = tables.findById(r.tableId()).orElseThrow();
        var a = new Account();
        a.setBarTable(t);
        a.setCustomerName((r.customerName() == null || r.customerName().isBlank()) ? "Cliente" : r.customerName());
        a.setNotes(r.notes());
        a.setStatus(AccountStatus.OPEN);
        a.setOpenedAt(LocalDateTime.now());
        a.setTotal(BigDecimal.ZERO);
        a.setTotalCost(BigDecimal.ZERO);
        a.setGrossProfit(BigDecimal.ZERO);
        return accounts.save(a);
    }

    @Transactional
    public Account addItem(Long accountId, AddAccountItemRequest r) {
        if (r.quantity() == null || r.quantity() <= 0) throw new RuntimeException("Cantidad inválida");
        var a = accounts.findById(accountId).orElseThrow();
        if (a.getStatus() != AccountStatus.OPEN) throw new RuntimeException("La cuenta no está abierta");
        var p = products.findById(r.productId()).orElseThrow();
        if (!p.isActive()) throw new RuntimeException("Producto inactivo");
        if (p.getStock() != null && p.getStock() < r.quantity())
            throw new RuntimeException("No hay stock suficiente de " + p.getName());
        var cost = p.getCostPrice() == null ? BigDecimal.ZERO : p.getCostPrice();
        var item = new AccountItem();
        item.setAccount(a);
        item.setProduct(p);
        item.setQuantity(r.quantity());
        item.setUnitPrice(p.getPrice());
        item.setUnitCost(cost);
        item.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(r.quantity())));
        item.setCostSubtotal(cost.multiply(BigDecimal.valueOf(r.quantity())));
        item.setGrossProfit(item.getSubtotal().subtract(item.getCostSubtotal()));
        accountItems.save(item);
        a.getItems().add(item);
        recalc(a);
        if (p.getStock() != null) {
            p.setStock(p.getStock() - r.quantity());
            products.save(p);
        }
        return accounts.save(a);
    }

    @Transactional
    public Account removeItem(Long accountId, Long itemId) {
        var a = accounts.findById(accountId).orElseThrow();
        if (a.getStatus() != AccountStatus.OPEN) throw new RuntimeException("La cuenta no está abierta");
        var item = a.getItems().stream().filter(i -> i.getId().equals(itemId)).findFirst().orElseThrow(() -> new RuntimeException("Item no encontrado"));
        var p = item.getProduct();
        if (p.getStock() != null) {
            p.setStock(p.getStock() + item.getQuantity());
            products.save(p);
        }
        a.getItems().remove(item);
        recalc(a);
        return accounts.save(a);
    }

    @Transactional
    public Account pay(Long id, PayAccountRequest r) {
        var a = accounts.findById(id).orElseThrow();
        if (a.getStatus() != AccountStatus.OPEN) throw new RuntimeException("La cuenta no está abierta");
        if (a.getItems().isEmpty()) throw new RuntimeException("La cuenta no tiene productos");
        a.setStatus(AccountStatus.PAID);
        a.setClosedAt(LocalDateTime.now());
        a.setPaymentMethod(r.paymentMethod() == null ? PaymentMethod.CASH : r.paymentMethod());
        return accounts.save(a);
    }

    @Transactional
    public Account cancel(Long id) {
        var a = accounts.findById(id).orElseThrow();
        if (a.getStatus() != AccountStatus.OPEN) throw new RuntimeException("Solo cuentas abiertas");
        for (var i : a.getItems()) {
            var p = i.getProduct();
            if (p.getStock() != null) {
                p.setStock(p.getStock() + i.getQuantity());
                products.save(p);
            }
        }
        a.setStatus(AccountStatus.CANCELLED);
        a.setClosedAt(LocalDateTime.now());
        return accounts.save(a);
    }

    @Transactional
    public Account merge(MergeAccountsRequest r) {
        if (r.accountIds() == null || r.accountIds().size() < 2)
            throw new RuntimeException("Selecciona mínimo 2 cuentas");
        var list = r.accountIds().stream().map(id -> accounts.findById(id).orElseThrow()).toList();
        var table = list.get(0).getBarTable();
        for (var a : list) {
            if (a.getStatus() != AccountStatus.OPEN) throw new RuntimeException("Solo puedes juntar cuentas abiertas");
            if (!a.getBarTable().getId().equals(table.getId()))
                throw new RuntimeException("Solo puedes juntar cuentas de la misma mesa");
        }
        var merged = new Account();
        merged.setBarTable(table);
        merged.setCustomerName((r.customerName() == null || r.customerName().isBlank()) ? "Cuenta junta" : r.customerName());
        merged.setOpenedAt(LocalDateTime.now());
        merged.setStatus(AccountStatus.OPEN);
        merged = accounts.save(merged);
        for (var old : list) {
            for (var oldItem : old.getItems()) {
                var item = new AccountItem();
                item.setAccount(merged);
                item.setProduct(oldItem.getProduct());
                item.setQuantity(oldItem.getQuantity());
                item.setUnitPrice(oldItem.getUnitPrice());
                item.setUnitCost(oldItem.getUnitCost());
                item.setSubtotal(oldItem.getSubtotal());
                item.setCostSubtotal(oldItem.getCostSubtotal());
                item.setGrossProfit(oldItem.getGrossProfit());
                accountItems.save(item);
                merged.getItems().add(item);           }
            old.getItems().clear();
            old.setTotal(BigDecimal.ZERO);
            old.setTotalCost(BigDecimal.ZERO);
            old.setGrossProfit(BigDecimal.ZERO);
            old.setStatus(AccountStatus.CANCELLED);
            old.setClosedAt(LocalDateTime.now());
            old.setNotes("Juntada en cuenta #" + merged.getId());
            accounts.save(old);
        }
        recalc(merged);
        return accounts.save(merged);
    }

    private void recalc(Account a) {
        var sales = a.getItems().stream().map(AccountItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        var cost = a.getItems().stream().map(AccountItem::getCostSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        a.setTotal(sales);
        a.setTotalCost(cost);
        a.setGrossProfit(sales.subtract(cost));
    }
}
