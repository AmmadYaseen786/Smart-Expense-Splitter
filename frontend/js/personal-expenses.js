document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    fetchPersonalExpenses();

    document.getElementById('personal-form').addEventListener('submit', handleCreatePersonalExpense);
});

async function fetchPersonalExpenses() {
    try {
        const response = await fetch(`${API_BASE_URL}/personal-expenses`);
        if (!response.ok) throw new Error('Failed to fetch personal expenses');

        const expenses = await response.json();
        const tableBody = document.querySelector('#personal-table tbody');
        tableBody.innerHTML = '';

        if (expenses.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align: center; color: var(--text-muted);">No personal expenses logged yet. Add one on the left.</td>
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

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${escapeHtml(expense.title)}</strong></td>
                <td><span class="badge badge-secondary">${escapeHtml(expense.category)}</span></td>
                <td style="color: var(--text-main); font-weight: 500;">Rs. ${expense.amount.toFixed(2)}</td>
                <td style="color: var(--text-muted); font-size: 0.85rem;">${date}</td>
                <td>
                    <button class="btn btn-danger btn-sm delete-pe-btn" data-id="${expense.id}">
                        <i class="fa-solid fa-trash"></i> Delete
                    </button>
                </td>
            `;
            tableBody.appendChild(tr);
        });

        // Bind delete events
        document.querySelectorAll('.delete-pe-btn').forEach(btn => {
            btn.addEventListener('click', handleDeleteClick);
        });

    } catch (error) {
        console.error('Error loading personal expenses:', error);
        showToast('Failed to fetch private expenses log.', 'danger');
    }
}

async function handleCreatePersonalExpense(e) {
    e.preventDefault();

    const title = document.getElementById('pe-title').value.trim();
    const amount = parseFloat(document.getElementById('pe-amount').value);
    const category = document.getElementById('pe-category').value;

    if (!title || isNaN(amount) || amount <= 0) {
        showToast('Please verify all input values.', 'warning');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/personal-expenses`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, amount, category })
        });

        const data = await response.json();

        if (response.ok) {
            showToast('Private expense recorded successfully!', 'success');
            document.getElementById('personal-form').reset();
            fetchPersonalExpenses();
        } else {
            showToast(data.message || 'Failed to record expense.', 'danger');
        }
    } catch (error) {
        console.error('Error creating personal expense:', error);
        showToast('Network error while saving private expense.', 'danger');
    }
}

async function handleDeleteClick(e) {
    const id = e.currentTarget.getAttribute('data-id');
    if (!confirm('Are you sure you want to delete this personal expense record?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/personal-expenses/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showToast('Private expense record deleted!', 'success');
            fetchPersonalExpenses();
        } else {
            showToast('Failed to delete personal expense.', 'danger');
        }
    } catch (error) {
        console.error('Error deleting personal expense:', error);
        showToast('Network error while removing private record.', 'danger');
    }
}
