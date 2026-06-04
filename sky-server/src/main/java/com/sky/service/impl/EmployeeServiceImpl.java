package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.StringUtils;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {


    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        // 1、根据用户名查询数据库中的数据 (使用 MP 的 getOne 方法，免去依赖 Mapper 自定义方法)
        Employee employee = this.getOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getUsername, username));

        // 2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            // 账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            // 密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            // 账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        this.save(employee); // MyBatis-Plus 方式
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 1. 创建 MyBatis-Plus 的 Page 分页对象
        Page<Employee> pageParam = new Page<>(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        // 2. 构造查询条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(employeePageQueryDTO.getName()), Employee::getName, employeePageQueryDTO.getName())
                    .orderByDesc(Employee::getCreateTime);

        // 3. 调用 Service 的 page 方法执行分页查询
        this.page(pageParam, queryWrapper);

        // 4. 获取结果并封装为 PageResult 返回
        return new PageResult(pageParam.getTotal(), pageParam.getRecords());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        this.updateById(employee);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = super.getById(id);
        if (employee != null) {
            employee.setPassword("****");
        }
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        this.updateById(employee);
    }

}
