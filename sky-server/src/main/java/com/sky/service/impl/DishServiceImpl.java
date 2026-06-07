package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishFlavorService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private SetMealMapper setmealMapper;
    @Autowired
    private SetMealDishMapper setmealDishMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        this.save(dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty())
            {   long dishId = dish.getId();
                flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
                dishFlavorService.saveBatch(flavors);}
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        //判断dish是否启用
        long count = this.count(new LambdaQueryWrapper<Dish>().in(Dish::getId, ids).eq(Dish::getStatus, StatusConstant.ENABLE));
        if(count > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }

        //判断dish是否被setmeal关联
        count = setmealDishMapper.selectCount(new LambdaQueryWrapper<SetmealDish>().in(SetmealDish::getDishId, ids));
        if(count > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        this.removeByIds(ids);
        //连同口味数据一起删除
        dishFlavorService.remove(new LambdaQueryWrapper<DishFlavor>().in(DishFlavor::getDishId, ids));
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        this.updateById(Dish.builder().id(id).status(status).build());
    }

    @Override
    public DishVO getByIdWithFlavors(Long id) {
        Dish dish = this.getById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavorMapper.getByDishId(id));
        return dishVO;
    }

    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        // 1. 根据dishId删除原有的口味数据
        dishFlavorService.remove(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, dishDTO.getId()));
        
        // 2. 重新插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishDTO.getId()));
            dishFlavorService.saveBatch(flavors);
        }
        
        // 3. 更新菜品基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        this.updateById(dish);
    }

    @Override
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    @Override
    public List<DishVO> getDishByCategoryId(Integer categoryId) {
        List<Dish> dishList = this.list(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, categoryId));
        return dishList.stream().map(dish -> DishVO.builder()
                .id(dish.getId())
                .name(dish.getName())
                .price(dish.getPrice())
                .image(dish.getImage())
                .description(dish.getDescription())
                .status(dish.getStatus())
                .build()).toList();
    }

}
