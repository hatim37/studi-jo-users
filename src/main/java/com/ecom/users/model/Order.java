package com.ecom.users.model;


import com.ecom.users.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Order {

    private Long id;
    private OrderStatus orderStatus;
    private Long amount;
    private Long userId;

}