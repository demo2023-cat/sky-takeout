package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

@Service
@Slf4j
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    /**
     * 条件查询
     */
    public List<AddressBook> list(AddressBook addressBook) {
        return super.list(new QueryWrapper<>(addressBook));
    }

    /**
     * 新增地址
     */
    @Override
    public boolean save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        return super.save(addressBook);
    }

    /**
     * 根据id查询
     */
    public AddressBook getById(Long id) {
        return super.getById(id);
    }

    /**
     * 根据id修改地址
     */
    public void update(AddressBook addressBook) {
        super.updateById(addressBook);
    }

    /**
     * 设置默认地址
     */
    @Transactional
    public void setDefault(AddressBook addressBook) {
        // 1、将当前用户的所有地址修改为非默认地址
        this.update(new LambdaUpdateWrapper<AddressBook>()
                .set(AddressBook::getIsDefault, 0)
                .eq(AddressBook::getUserId, BaseContext.getCurrentId()));

        // 2、将当前地址改为默认地址
        addressBook.setIsDefault(1);
        super.updateById(addressBook);
    }

    /**
     * 根据id删除地址
     */
    public void deleteById(Long id) {
        super.removeById(id);
    }
}
