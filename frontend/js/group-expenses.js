document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadGroupsDropdowns();

    document.getElementById('ge-group').addEventListener('change', handleGroupSelectionChange);
    document.getElementById('filter-group-selector').addEventListener('change', handleFilterGroupChange);
    document.getElementById('group-expense-form').addEventListener('submit', handleFormSubmit);
});

let activeGroupMembers = [];

async function loadGroupsDropdowns() {
    try {
        const response = await fetch(`${API_BASE_URL}/groups`);
        if (!response.ok) throw new Error('Failed to fetch groups');

        const groups = await response.json();
        
        const createSelector = document.getElementById('ge-group');
        const filterSelector = document.getElementById('filter-group-selector');

        createSelector.innerHTML = '<option value="">Select Group...</option>';
        filterSelector.innerHTML = '<option value="">Select Group...</option>';

        groups.forEach(g => {
            const opt1 = document.createElement('option');
            opt1.value = g.id;
            opt1.textContent = escapeHtml(g.name);
            createSelector.appendChild(opt1);

            const opt2 = document.createElement('option');
            opt2.value = g.id;
            opt2.textContent = escapeHtml(g.name);
            filterSelector.appendChild(opt2);
        });

    } catch (error) {
        console.error('Error loading groups for dropdown:', error);
        showToast('Failed to load group dropdown options.', 'danger');
    }
}

async function handleGroupSelectionChange() {
    const groupId = document.getElementById('ge-group').value;
    const inputsArea = document.getElementById('expense-inputs-area');

    if (!groupId) {
        inputsArea.style.display = 'none';
        activeGroupMembers = [];
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/groups/${groupId}`);
        if (!response.ok) throw new Error('Failed to fetch group details');

        const group = await response.json();
        activeGroupMembers = group.members;

        // Render Contributors list with inputs
        const contribList = document.getElementById('contributor-inputs-list');
        contribList.innerHTML = '';

        activeGroupMembers.forEach(member => {
            const row = document.createElement('div');
            row.className = 'contributor-row';
            row.innerHTML = `
                <span class="contributor-name-lbl">${escapeHtml(member.name)}</span>
                <input type="number" step="0.01" min="0" class="form-control contrib-amount-input" data-user-id="${member.id}" value="0" placeholder="Rs. 0">
            `;
            contribList.appendChild(row);
        });

        // Render Participants checkboxes
        const partList = document.getElementById('participant-checkboxes-list');
        partList.innerHTML = '';

        activeGroupMembers.forEach(member => {
            const item = document.createElement('div');
            item.className = 'checkbox-item';
            item.innerHTML = `
                <input type="checkbox" id="part-chk-${member.id}" class="participant-chk" value="${member.id}" checked>
                <label for="part-chk-${member.id}">${escapeHtml(member.name)} (${escapeHtml(member.email)})</label>
            `;
            partList.appendChild(item);
        });

        inputsArea.style.display = 'flex';

    } catch (error) {
        console.error('Error loading group members for inputs:', error);
        showToast('Failed to load group roster details.', 'danger');
        inputsArea.style.display = 'none';
    }
}

async function handleFilterGroupChange() {
    const groupId = document.getElementById('filter-group-selector').value;
    const tableBody = document.querySelector('#group-expenses-table tbody');

    if (!groupId) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align: center; color: var(--text-muted);">Please select a group above to filter historical records.</td>
            </tr>
        `;
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/expenses?groupId=${groupId}`);
        if (!response.ok) throw new Error('Failed to fetch group expenses');

        const expenses = await response.json();
        tableBody.innerHTML = '';

        if (expenses.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No shared expenses logged in this group yet.</td>
                </tr>
            `;
            return;
        }

        // Sort descending by ID (newest first)
        const sortedExpenses = [...expenses].sort((a, b) => b.id - a.id);

        sortedExpenses.forEach(expense => {
            const date = new Date(expense.date).toLocaleDateString('en-US', {
                month: 'short', day: 'numeric', year: 'numeric'
            });

            // Map contributors summaries
            const contribSummary = expense.contributions
                .filter(c => c.amountContributed > 0)
                .map(c => `${escapeHtml(c.userName)}: Rs. ${c.amountContributed.toFixed(2)}`)
                .join('<br>');

            // Map participants splits
            const splitSummary = expense.participants
                .map(p => escapeHtml(p.name))
                .join(', ');

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${escapeHtml(expense.title)}</strong><br><span style="color:var(--text-muted); font-size:0.8rem;">${escapeHtml(expense.description || '')}</span></td>
                <td style="color: var(--text-main); font-weight: 600;">Rs. ${expense.amount.toFixed(2)}</td>
                <td style="font-size:0.85rem; line-height: 1.35; color: var(--text-muted);">${contribSummary || '-'}</td>
                <td style="font-size:0.85rem; color: var(--text-muted); max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${splitSummary}">${splitSummary}</td>
                <td style="color: var(--text-muted); font-size: 0.85rem;">${date}</td>
            `;
            tableBody.appendChild(tr);
        });

    } catch (error) {
        console.error('Error fetching group expenses:', error);
        showToast('Failed to fetch group expenses history.', 'danger');
    }
}

async function handleFormSubmit(e) {
    e.preventDefault();

    const groupId = parseInt(document.getElementById('ge-group').value);
    const title = document.getElementById('ge-title').value.trim();
    const amount = parseFloat(document.getElementById('ge-amount').value);

    if (isNaN(groupId) || !title || isNaN(amount) || amount <= 0) {
        showToast('Please check all header inputs.', 'warning');
        return;
    }

    // Read Contributions
    const contributionInputs = document.querySelectorAll('.contrib-amount-input');
    const contributions = [];
    let sumContributions = 0;

    contributionInputs.forEach(input => {
        const userId = parseInt(input.getAttribute('data-user-id'));
        const contribVal = parseFloat(input.value) || 0;
        if (contribVal > 0) {
            contributions.push({ userId, amountContributed: contribVal });
            sumContributions += contribVal;
        }
    });

    if (Math.abs(sumContributions - amount) > 0.01) {
        showToast(`Sum of contributions (Rs. ${sumContributions.toFixed(2)}) must exactly equal the total bill amount (Rs. ${amount.toFixed(2)}).`, 'danger');
        return;
    }

    // Read Participants
    const participantCheckboxes = document.querySelectorAll('.participant-chk:checked');
    const participantUserIds = Array.from(participantCheckboxes).map(chk => parseInt(chk.value));

    if (participantUserIds.length === 0) {
        showToast('Please select at least one participant to split the bill.', 'warning');
        return;
    }

    const payload = {
        title,
        description: '',
        amount,
        groupId,
        contributions,
        participantUserIds
    };

    try {
        const response = await fetch(`${API_BASE_URL}/expenses`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (response.ok) {
            showToast('Shared group expense recorded successfully!', 'success');
            
            // Reset and refresh
            document.getElementById('ge-title').value = '';
            document.getElementById('ge-amount').value = '';
            handleGroupSelectionChange(); // Reset contributor inputs

            // Refresh history if filter matches
            const filterGroupId = document.getElementById('filter-group-selector').value;
            if (filterGroupId === groupId.toString()) {
                handleFilterGroupChange();
            }
        } else {
            showToast(data.message || 'Failed to record expense.', 'danger');
        }
    } catch (error) {
        console.error('Error posting group expense:', error);
        showToast('Network error while saving shared expense.', 'danger');
    }
}
