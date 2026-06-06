package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    // void insert(Category category); // 原 MyBatis 手写 SQL 插入，已被 BaseMapper 接管

    // Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO); // 原 MyBatis 手写 SQL 分页，已被 page() 接管

    // void deleteById(Long id); // 原 MyBatis 手写 SQL 删除，已被 removeById() 接管

    // void update(Category category); // 原 MyBatis 手写 SQL 更新，已被 updateById() 接管

    // List<Category> list(Integer type); // 原 MyBatis 手写 SQL 列表查询，已被 super.list() 接管
}
