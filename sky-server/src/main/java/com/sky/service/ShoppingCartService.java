package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

public interface ShoppingCartService extends IService<ShoppingCart> {
    void add(ShoppingCartDTO shoppingCartDTO);

    int sub(ShoppingCartDTO shoppingCartDTO);
    
    // clean, list, insertBatch are covered or will be refactored inside Impl. We can keep their original signatures if we want, or remove them and let the controller use MP methods. 
    // Since the controller expects them, I will keep their signature but change the name if needed, or just keep them.
    List<ShoppingCart> list(Long userId);

    int clean(long userId);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}
