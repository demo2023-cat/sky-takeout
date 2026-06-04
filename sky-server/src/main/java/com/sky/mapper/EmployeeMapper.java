package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    // @Select("select * from employee where username = #{username}")
    // Employee getByUsername(String username); // 已由 Service 中的 getOne + QueryWrapper 接管

    // Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO); // 原 MyBatis 手写 SQL 分页方法，现已被 MBP 接管

    // @AutoFill(value = OperationType.UPDATE)
    // void update(Employee employee); // 原 MyBatis 手写 XML 更新方法，已由 MBP updateById 接管

    // @Select("select * from employee where id = #{id}")
    // Employee getById(Long id); // 已由 BaseMapper.selectById 接管
}
