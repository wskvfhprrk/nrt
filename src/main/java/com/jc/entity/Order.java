package com.jc.entity;

import lombok.Data;

@Data
public class Order {
    private String selectedRecipe;
    private int selectedPrice;
    private String selectedSpice;
    private boolean addCilantro;
    private boolean addOnion;
}
