package com.expensesplitter.controller;

import com.expensesplitter.dto.GroupDto;
import com.expensesplitter.dto.GroupMemberRequest;
import com.expensesplitter.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupDto> createGroup(@Valid @RequestBody GroupDto groupDto) {
        return new ResponseEntity<>(groupService.createGroup(groupDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GroupDto>> getUserGroups() {
        return ResponseEntity.ok(groupService.getUserGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<GroupDto> addMember(@PathVariable Long id, @Valid @RequestBody GroupMemberRequest request) {
        return ResponseEntity.ok(groupService.addMemberByEmail(id, request.getEmail()));
    }
}
