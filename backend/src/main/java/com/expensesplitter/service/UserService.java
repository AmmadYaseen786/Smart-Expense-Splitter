package com.expensesplitter.service;

import com.expensesplitter.dto.RegisterRequest;
import com.expensesplitter.dto.UserDto;
import com.expensesplitter.model.User;

import java.util.List;

public interface UserService {
    UserDto registerUser(RegisterRequest registerRequest);
    UserDto getCurrentUserDto();
    User getCurrentUser();
    List<UserDto> getAllUsers();
    UserDto getUserByEmail(String email);
}
