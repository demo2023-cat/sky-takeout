package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface SetMealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @Select("select count(id) from setmeal_dish where dish_id = #{id}")
    int countByDishId(Long id);
    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

    @AutoFill(value = OperationType.INSERT)
    @Options(useGeneratedKeys = true , keyColumn = "id", keyProperty = "id" )
    @Insert("insert into setmeal(name, category_id, price, image, description, create_time, update_time, create_user, update_user) values (#{name},#{categoryId},#{price},#{image},#{description},#{createTime},#{updateTime},#{createUser},#{updateUser}) ")
    void save(Setmeal setmeal);


    Page<Setmeal> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    @Update("update setmeal set status = #{status} where id = #{id}")
    void startOrStop(Integer status, Long id);

    @Select("select * from setmeal where id = #{id}")
    SetmealVO getByIdWithDish(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    void deleteBatch(List<Long> ids);

    @Select("select * from setmeal where id = #{setmealId}")
    Setmeal getById(Long setmealId);
}
