package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    @Transactional
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty())
            {   long dishId = dish.getId();
                flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
                dishFlavorMapper.insert(flavors);}

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
        for (Long id : ids) {
            Integer dish_status = dishMapper.getStatusById(id);
            if(dish_status.equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断dish是否被setmeal关联
        for (Long id : ids){
            int count =setmealMapper.countByDishId(id);
            if(count != 0){
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        }
        //删除dish及flavor
//        for (Long id : ids){
//            dishMapper.delete(id);
//            dishFlavorMapper.deleteByDishId(id);
//        }
        dishMapper.delete(ids);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        dishMapper.startOrStop(status, id);
    }

    @Override
    public DishVO getByIdWithFlavors(Long id) {
        DishVO dishVO = dishMapper.getByIdWithFlavors(id);
        dishVO.setFlavors(dishFlavorMapper.getByDishId(id));
        return dishVO;
    }

    @Override
    public void update(DishDTO dishDTO) {
        long dishId = dishDTO.getId();
        List<Long> ids = Collections.singletonList(dishId);
        dishFlavorMapper.deleteByDishIds(ids);
        dishFlavorMapper.insert(dishDTO.getFlavors());
        dishMapper.update(dishDTO);
    }

}
