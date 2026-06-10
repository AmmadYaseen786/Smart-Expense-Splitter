package com.expensesplitter.service.impl;

import com.expensesplitter.dto.GroupDto;
import com.expensesplitter.dto.UserDto;
import com.expensesplitter.exception.ResourceNotFoundException;
import com.expensesplitter.model.Group;
import com.expensesplitter.model.GroupMember;
import com.expensesplitter.model.User;
import com.expensesplitter.repository.GroupMemberRepository;
import com.expensesplitter.repository.GroupRepository;
import com.expensesplitter.repository.UserRepository;
import com.expensesplitter.service.GroupService;
import com.expensesplitter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public GroupDto createGroup(GroupDto groupDto) {
        User currentUser = userService.getCurrentUser();

        Group group = Group.builder()
                .name(groupDto.getName())
                .description(groupDto.getDescription())
                .createdBy(currentUser)
                .build();

        Group savedGroup = groupRepository.save(group);

        GroupMember creatorMember = GroupMember.builder()
                .group(savedGroup)
                .user(currentUser)
                .build();
        groupMemberRepository.save(creatorMember);

        return mapToDto(savedGroup);
    }

    @Override
    public List<GroupDto> getUserGroups() {
        User currentUser = userService.getCurrentUser();
        List<GroupMember> memberships = groupMemberRepository.findByUserId(currentUser.getId());
        
        return memberships.stream()
                .map(m -> mapToDto(m.getGroup()))
                .collect(Collectors.toList());
    }

    @Override
    public GroupDto getGroupById(Long groupId) {
        User currentUser = userService.getCurrentUser();
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new IllegalArgumentException("Access Denied. You do not belong to this group.");
        }

        return mapToDto(group);
    }

    @Override
    @Transactional
    public GroupDto addMemberByEmail(Long groupId, String email) {
        User currentUser = userService.getCurrentUser();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new IllegalArgumentException("Access Denied. You cannot add members to this group.");
        }

        User targetUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No registered user found with email: " + email));

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUser.getId())) {
            throw new IllegalArgumentException("User is already a member of this group.");
        }

        GroupMember newMember = GroupMember.builder()
                .group(group)
                .user(targetUser)
                .build();
        groupMemberRepository.save(newMember);

        return mapToDto(group);
    }

    private GroupDto mapToDto(Group group) {
        List<GroupMember> memberships = groupMemberRepository.findByGroupId(group.getId());
        List<UserDto> members = memberships.stream()
                .map(m -> UserDto.builder()
                        .id(m.getUser().getId())
                        .name(m.getUser().getName())
                        .email(m.getUser().getEmail())
                        .build())
                .collect(Collectors.toList());

        return GroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdById(group.getCreatedBy().getId())
                .createdByName(group.getCreatedBy().getName())
                .members(members)
                .build();
    }
}
