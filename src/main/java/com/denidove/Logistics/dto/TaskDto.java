package com.denidove.trading.dto;

import com.denidove.trading.entities.Product;
import com.denidove.trading.entities.User;
import com.denidove.trading.enums.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Getter
@Setter
@NoArgsConstructor
public class CartItemDto {

    private Long id;

    private User user;

    private Product product;

    private Integer quantity;

    private ProductStatus status;

    private static Long idIncrement= 0L; // данное статическое поле сохраняет свое значение внезависимости от сессии

    public CartItemDto(Product product, Integer quantity) {
        this.id = idIncrement++;
        this.product = product;
        this.quantity = quantity;
    }
}

