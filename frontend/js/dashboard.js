document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadDashboardStats();
});

async function loadDashboardStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/balances/dashboard`);
        if (!response.ok) throw new Error('Failed to fetch dashboard metrics');

        const stats = await response.json();

        // Render counters
        document.getElementById('stat-personal-total').textContent = `Rs. ${stats.personalExpensesTotal.toFixed(2)}`;
        document.getElementById('stat-group-total').textContent = `Rs. ${stats.groupExpensesTotal.toFixed(2)}`;
        document.getElementById('stat-owe-total').textContent = `Rs. ${stats.outstandingAmount.toFixed(2)}`;
        document.getElementById('stat-owed-total').textContent = `Rs. ${stats.receivableAmount.toFixed(2)}`;
        document.getElementById('stat-pending-settlements-count').textContent = stats.pendingSettlementsCount;
        document.getElementById('stat-completed-settlements-count').textContent = stats.totalSettlementsCompleted;
        document.getElementById('stat-total-amount-settled').textContent = `Rs. ${stats.totalAmountSettled.toFixed(2)}`;

        // Render recent activity table
        const tableBody = document.querySelector('#recent-expenses-table tbody');
        tableBody.innerHTML = '';

        if (stats.recentExpenses.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="4" style="text-align: center; color: var(--text-muted); padding: 2rem;">No recent expenses found. Start by adding a personal or group expense!</td>
                </tr>
            `;
            return;
        }

        stats.recentExpenses.forEach(expense => {
            const tr = document.createElement('tr');
            
            const categoryText = expense.type === 'PERSONAL' 
                ? `<span class="badge badge-secondary"><i class="fa-solid fa-lock"></i> Personal (${escapeHtml(expense.groupName || 'Private')})</span>`
                : `<span class="badge badge-success"><i class="fa-solid fa-users"></i> Group (${escapeHtml(expense.groupName)})</span>`;

            const date = new Date(expense.date).toLocaleDateString('en-US', {
                month: 'short', day: 'numeric', year: 'numeric'
            });

            tr.innerHTML = `
                <td><strong>${escapeHtml(expense.title)}</strong></td>
                <td style="color: var(--text-main); font-weight: 500;">Rs. ${expense.amount.toFixed(2)}</td>
                <td>${categoryText}</td>
                <td style="color: var(--text-muted); font-size: 0.85rem;">${date}</td>
            `;
            tableBody.appendChild(tr);
        });

    } catch (error) {
        console.error('Error fetching dashboard details:', error);
        showToast('Failed to load dashboard statistics.', 'danger');
    }
}
