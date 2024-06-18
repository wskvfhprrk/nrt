package com.jc.entity;

import lombok.Data;

/**
 * 订单实体类
 * 包含所选菜谱、价格、香料、是否加香菜和是否加洋葱等属性
 */
@Data
public class Order {
    // 所选菜谱
    private String selectedRecipe;
    // 价格
    private int selectedPrice;
    // 香料
    private String selectedSpice;
    // 是否加香菜
    private boolean addCilantro;
    // 是否加洋葱
    private boolean addOnion;
}
