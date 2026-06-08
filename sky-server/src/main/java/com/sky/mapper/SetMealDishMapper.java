package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface SetMealDishMapper extends BaseMapper<SetmealDish> {
    /**
     * 根据菜品id查询对应的套餐id
     *
     * @param dishIds
     * @return
     */

    //List<Long> getSetmealIdsByDishIds(List<Long> dishIds);




}
