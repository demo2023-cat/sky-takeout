package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into dish (name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) values (#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    @Select("select status from dish where id = #{id}")
    int getStatusById(Long id);

    void delete(List<Long> ids);

    @Update("update dish set status = #{status} where id = #{id}")
    void startOrStop(Integer status, Long id);

    @Select("select * from dish where id = #{id}")
    DishVO getByIdWithFlavors(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void update(DishDTO dishDTO);

    List<Dish> list(Dish dish);
}
