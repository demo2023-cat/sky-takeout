package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "C端-购物车接口")
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @ApiOperation("添加购物车")
    @PostMapping("add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车{}", shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    @ApiOperation("查看购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        Long userId = BaseContext.getCurrentId();
        log.info("查看购物车",userId);
        return Result.success(shoppingCartService.list(userId));
    }

    @ApiOperation("删除购物车中的数据")
    @DeleteMapping("/clean")
    public Result clean() {
        log.info("清空购物车");
        return Result.success(shoppingCartService.clean());
    }

    @ApiOperation("减少购物车中的商品数量1")
    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("减少购物车中的商品数量1");
        return Result.success(shoppingCartService.sub(shoppingCartDTO));
    }
}
