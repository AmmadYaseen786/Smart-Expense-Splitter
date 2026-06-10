document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadSettlementHistory();
});

async function loadSettlementHistory() {
    try {
        const response = await fetch(`${API_BASE_URL}/settlements`);
        if (!response.ok) throw new Error('Failed to fetch settlement logs');

        const settlements = await response.json();
        const tableBody = document.querySelector('#settlements-table tbody');
        tableBody.innerHTML = '';

        if (settlements.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No settlement transactions recorded yet.</td>
                </tr>
            `;
            return;
        }

        settlements.forEach(s => {
            const tr = document.createElement('tr');
            
            const dateFormatted = formatDate(s.settledAt);
            const amountFormatted = `Rs. ${Math.round(s.amount)}`; // suggestion/settlement is whole PKR

            tr.innerHTML = `
                <td style="color: var(--text-muted); font-size: 0.85rem; font-weight: 500;">${dateFormatted}</td>
                <td><strong>${escapeHtml(s.groupName)}</strong></td>
                <td><span style="color: var(--warning); font-weight: 500;">${escapeHtml(s.payerName)}</span></td>
                <td><span style="color: var(--success); font-weight: 500;">${escapeHtml(s.receiverName)}</span></td>
                <td style="font-weight: 600; color: var(--text-main);">${amountFormatted}</td>
                <td style="color: var(--text-muted); font-style: italic;">${escapeHtml(s.note || '-')}</td>
            `;
            tableBody.appendChild(tr);
        });

    } catch (error) {
        console.error('Error fetching settlement history:', error);
        showToast('Failed to load settlement history log.', 'danger');
    }
}

function formatDate(dateStr) {
    const d = new Date(dateStr);
    const day = String(d.getDate()).padStart(2, '0');
    const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    const month = months[d.getMonth()];
    const year = d.getFullYear();
    return `${day}-${month}-${year}`;
}
