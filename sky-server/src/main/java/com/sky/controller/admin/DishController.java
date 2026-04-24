package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/admin/dish")
@ApiOperation("菜品管理")
@RestController
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result<DishDTO> saveDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品{}", dishDTO);
        dishService.save(dishDTO);
        redisTemplate.delete("dish_" + dishDTO.getCategoryId());
        return Result.success();
    }

    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult> dishPageQuery(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @ApiOperation("菜品删除")
    @DeleteMapping
    public Result delete(@RequestParam List< Long> ids){
        log.info("菜品删除{}", ids);
        dishService.delete(ids);
        redisTemplate.delete(redisTemplate.keys("dish_*"));
        return Result.success();
    }
    @ApiOperation("菜品起售停售")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status,@RequestParam Long id){
        log.info("菜品起售停售{}，{}", id, status);
        dishService.startOrStop(status,id);
        redisTemplate.delete(redisTemplate.keys("dish_*"));
        return Result.success();
    }

    @ApiOperation("id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("id查询菜品{}", id);
        DishVO dishVO = dishService.getByIdWithFlavors(id);
        return Result.success(dishVO);
    }

    @ApiOperation("修改菜品")
    @PutMapping
    public Result<DishDTO> update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品{}", dishDTO);
        dishDTO.getFlavors().forEach(flavor -> flavor.setDishId(dishDTO.getId()));
        log.info("修改口味{}", dishDTO.getFlavors());
        dishService.update(dishDTO);
        redisTemplate.delete("dish_" + dishDTO.getCategoryId());
        return Result.success();
    }
    @ApiOperation("按照分类id查询菜品")
    @GetMapping("/list")
    public Result<List<DishVO>> getDishByCategoryId(@RequestParam Integer categoryId){
        log.info("按照分类id查询菜品{}", categoryId);
        List<DishVO> list = dishService.getDishByCategoryId(categoryId);
        return Result.success(list);
    }
}
