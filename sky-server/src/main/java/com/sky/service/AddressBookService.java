package com.sky.service;

import com.sky.entity.AddressBook;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

public interface AddressBookService extends IService<AddressBook> {

    List<AddressBook> list(AddressBook addressBook);

    AddressBook getById(Long id);

    void update(AddressBook addressBook);

    void setDefault(AddressBook addressBook);

    void deleteById(Long id);

}
