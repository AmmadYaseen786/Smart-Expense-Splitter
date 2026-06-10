package com.expensesplitter.service;

import com.expensesplitter.dto.GroupDto;

import java.util.List;

public interface GroupService {
    GroupDto createGroup(GroupDto groupDto);
    List<GroupDto> getUserGroups();
    GroupDto getGroupById(Long groupId);
    GroupDto addMemberByEmail(Long groupId, String email);
}
