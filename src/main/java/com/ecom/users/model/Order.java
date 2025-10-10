package com.ecom.users.model;


import com.ecom.users.enums.OrderStatus;
import lombok.*;
@Data
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private Long id;
    private OrderStatus orderStatus;
    private Long amount;
    private Long userId;

}