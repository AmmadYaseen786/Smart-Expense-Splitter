document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadGroupsDropdown();

    document.getElementById('group-form').addEventListener('submit', handleCreateGroup);
    document.getElementById('group-selector').addEventListener('change', handleGroupChange);
    document.getElementById('add-member-form').addEventListener('submit', handleAddMember);
});

let currentGroupId = null;

async function loadGroupsDropdown() {
    try {
        const response = await fetch(`${API_BASE_URL}/groups`);
        if (!response.ok) throw new Error('Failed to fetch groups');

        const groups = await response.json();
        const selector = document.getElementById('group-selector');
        
        // Preserve selected item if exists
        const prevValue = selector.value;
        
        selector.innerHTML = '<option value="">Choose a Group...</option>';
        groups.forEach(g => {
            const option = document.createElement('option');
            option.value = g.id;
            option.textContent = escapeHtml(g.name);
            selector.appendChild(option);
        });

        if (prevValue && groups.some(g => g.id.toString() === prevValue)) {
            selector.value = prevValue;
        } else {
            document.getElementById('group-details-area').style.display = 'none';
        }
    } catch (error) {
        console.error('Error fetching groups list:', error);
        showToast('Failed to load group listing.', 'danger');
    }
}

async function handleCreateGroup(e) {
    e.preventDefault();

    const name = document.getElementById('group-name').value.trim();
    const description = document.getElementById('group-desc').value.trim();

    if (!name) {
        showToast('Please enter a group name.', 'warning');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/groups`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, description })
        });

        const data = await response.json();

        if (response.ok) {
            showToast('Group created successfully!', 'success');
            document.getElementById('group-form').reset();
            
            // Reload dropdown and automatically select the newly created group
            await loadGroupsDropdown();
            document.getElementById('group-selector').value = data.id;
            handleGroupChange();
        } else {
            showToast(data.message || 'Failed to create group.', 'danger');
        }
    } catch (error) {
        console.error('Error creating group:', error);
        showToast('Network error while saving group.', 'danger');
    }
}

async function handleGroupChange() {
    const groupId = document.getElementById('group-selector').value;
    const detailsArea = document.getElementById('group-details-area');

    if (!groupId) {
        detailsArea.style.display = 'none';
        currentGroupId = null;
        return;
    }

    currentGroupId = parseInt(groupId);

    try {
        const response = await fetch(`${API_BASE_URL}/groups/${currentGroupId}`);
        if (!response.ok) throw new Error('Failed to fetch group details');

        const group = await response.json();

        document.getElementById('detail-group-name').textContent = group.name;
        document.getElementById('detail-group-desc').textContent = group.description || 'No description provided.';

        // Populate members table
        const tableBody = document.querySelector('#members-table tbody');
        tableBody.innerHTML = '';

        if (group.members.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="2" style="text-align: center; color: var(--text-muted);">No members rostered.</td>
                </tr>
            `;
        } else {
            group.members.forEach(member => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${escapeHtml(member.name)}</strong></td>
                    <td style="color: var(--text-muted);">${escapeHtml(member.email)}</td>
                `;
                tableBody.appendChild(tr);
            });
        }

        detailsArea.style.display = 'block';

    } catch (error) {
        console.error('Error loading group details:', error);
        showToast('Failed to load group details.', 'danger');
        detailsArea.style.display = 'none';
    }
}

async function handleAddMember(e) {
    e.preventDefault();

    if (!currentGroupId) {
        showToast('Please select a group first.', 'warning');
        return;
    }

    const email = document.getElementById('member-email').value.trim();

    if (!email) {
        showToast('Please enter user email address.', 'warning');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/groups/${currentGroupId}/members`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });

        const data = await response.json();

        if (response.ok) {
            showToast('User added to group successfully!', 'success');
            document.getElementById('member-email').value = '';
            handleGroupChange(); // Refresh details area
        } else {
            showToast(data.message || 'Failed to add user to group.', 'danger');
        }
    } catch (error) {
        console.error('Error adding member to group:', error);
        showToast('Network error while adding user.', 'danger');
    }
}
