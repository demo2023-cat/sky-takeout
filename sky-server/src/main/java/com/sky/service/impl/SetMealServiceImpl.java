package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SetMealServiceImpl implements SetMealService {
    @Autowired
    private SetMealMapper setmealMapper;
    @Autowired
    private SetMealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setMeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setMeal);
        setmealMapper.save(setMeal);
        setmealDTO.getSetmealDishes().forEach(setmealDish -> setmealDish.setSetmealId(setMeal.getId()));
        setmealDishMapper.insertBatch(setmealDTO.getSetmealDishes());
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<Setmeal> result = setmealMapper.pageQuery(setmealPageQueryDTO);
        long total = result.getTotal();
        List<Setmeal> pageResult = result.getResult();
        return new PageResult(total,pageResult);
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        setmealMapper.deleteBatch(ids);
        ids.forEach(id->setmealDishMapper.deleteBySetmealId(id));
    }

    @Override
    public SetmealVO getByIdWithDish(Long id) {
        return setmealMapper.getByIdWithDish(id);
    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        setmealDTO.getSetmealDishes().forEach(setmealDish->setmealDish.setSetmealId(setmeal.getId()));
        setmealDishMapper.insertBatch(setmealDTO.getSetmealDishes());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        setmealMapper.startOrStop(status, id);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

}
