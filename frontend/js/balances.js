document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadGroupsDropdown();

    document.getElementById('balance-group-select').addEventListener('change', handleGroupChange);
});

async function loadGroupsDropdown() {
    try {
        const response = await fetch(`${API_BASE_URL}/groups`);
        if (!response.ok) throw new Error('Failed to fetch groups');

        const groups = await response.json();
        const selector = document.getElementById('balance-group-select');
        
        selector.innerHTML = '<option value="">Select Group...</option>';
        groups.forEach(g => {
            const option = document.createElement('option');
            option.value = g.id;
            option.textContent = escapeHtml(g.name);
            selector.appendChild(option);
        });
    } catch (error) {
        console.error('Error fetching groups list:', error);
        showToast('Failed to load group listing.', 'danger');
    }
}

async function handleGroupChange() {
    const groupId = document.getElementById('balance-group-select').value;
    const tableBody = document.querySelector('#balances-table tbody');
    const adviceArea = document.getElementById('advice-area');
    const adviceUl = document.getElementById('advice-ul');

    if (!groupId) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align: center; color: var(--text-muted);">Please select a group on the left to view the balance sheet.</td>
            </tr>
        `;
        adviceArea.style.display = 'none';
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/balances/groups/${groupId}`);
        if (!response.ok) throw new Error('Failed to fetch group balances');

        const balances = await response.json();
        tableBody.innerHTML = '';

        if (balances.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align: center; color: var(--text-muted);">No members found in this group.</td>
                </tr>
            `;
            adviceArea.style.display = 'none';
            return;
        }

        balances.forEach(b => {
            const tr = document.createElement('tr');
            
            let balClass = '';
            let balText = '';
            let badgeHtml = '';

            const bal = b.finalBalance;
            if (bal > 0.01) {
                balClass = 'style="color: var(--success); font-weight: 600;"';
                balText = `+Rs. ${bal.toFixed(2)}`;
                badgeHtml = '<span class="badge badge-success"><i class="fa-solid fa-arrow-down-long"></i> Owed Money</span>';
            } else if (bal < -0.01) {
                balClass = 'style="color: var(--warning); font-weight: 600;"';
                balText = `-Rs. ${Math.abs(bal).toFixed(2)}`;
                badgeHtml = '<span class="badge badge-danger"><i class="fa-solid fa-arrow-up-long"></i> Owes Money</span>';
            } else {
                balClass = 'style="color: var(--text-muted); font-weight: 500;"';
                balText = 'Rs. 0.00';
                badgeHtml = '<span class="badge badge-secondary"><i class="fa-solid fa-check"></i> Settled</span>';
            }

            tr.innerHTML = `
                <td><strong>${escapeHtml(b.userName)}</strong><br><span style="color:var(--text-muted); font-size:0.8rem;">${escapeHtml(b.userEmail)}</span></td>
                <td>Rs. ${b.totalContributions.toFixed(2)}</td>
                <td>Rs. ${b.totalShare.toFixed(2)}</td>
                <td ${balClass}>${balText}</td>
                <td>${badgeHtml}</td>
            `;
            tableBody.appendChild(tr);
        });

        // Compute and render Settlement Advice
        const settlements = computeDebtResolution(balances);
        adviceUl.innerHTML = '';

        if (settlements.length === 0) {
            adviceUl.innerHTML = `
                <li style="color: var(--success); font-weight: 500; display:flex; align-items:center; gap:0.5rem;">
                    <i class="fa-solid fa-circle-check"></i> Everyone is settled! No transactions needed.
                </li>
            `;
        } else {
            settlements.forEach((s, index) => {
                const li = document.createElement('li');
                li.style.borderBottom = '1px solid var(--border-color)';
                li.style.paddingBottom = '1rem';
                li.style.marginBottom = '1rem';
                li.style.display = 'flex';
                li.style.flexDirection = 'column';
                li.style.gap = '0.5rem';
                if (index === settlements.length - 1) {
                    li.style.borderBottom = 'none';
                    li.style.paddingBottom = '0';
                    li.style.marginBottom = '0';
                }

                const suggestionText = `<span style="font-weight:600; color:var(--text-main);">${escapeHtml(s.from)}</span> owes <span style="font-weight:600; color:var(--primary);">${escapeHtml(s.to)}</span> <strong style="color: var(--success);">Rs. ${s.amount}</strong>`;

                li.innerHTML = `
                    <div style="font-size: 1rem; margin-bottom: 0.25rem;">
                        <i class="fa-solid fa-arrow-right-arrow-left" style="color: var(--secondary); margin-right: 0.5rem;"></i>
                        ${suggestionText}
                    </div>
                    <div class="settlement-form-container" style="display: flex; flex-direction: column; gap: 0.5rem; background: rgba(255, 255, 255, 0.02); padding: 0.75rem; border-radius: 8px; border: 1px solid var(--border-color); margin-top: 0.25rem;">
                        <div style="display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center;">
                            <div style="flex: 1; min-width: 150px;">
                                <label style="font-size: 0.75rem; color: var(--text-muted); display: block; margin-bottom: 0.25rem;">Amount Paying Now</label>
                                <input type="number" step="1" min="1" max="${s.amount}" class="form-control amount-paying-input" placeholder="Amount" id="amount-paying-${index}">
                            </div>
                            <div style="display: flex; gap: 0.5rem; align-items: flex-end; height: 100%; padding-top: 1.1rem;">
                                <button class="btn btn-secondary btn-sm pay-full-btn" onclick="fillFullAmount(${index}, ${s.amount})">Pay Full Amount</button>
                                <button class="btn btn-primary btn-sm record-settlement-btn" onclick="submitSettlement(${index}, ${s.fromId}, ${s.toId}, ${groupId})">Record Settlement</button>
                            </div>
                        </div>
                        <div>
                            <input type="text" class="form-control note-input" placeholder="Add a note (optional)" id="settlement-note-${index}">
                        </div>
                    </div>
                `;
                adviceUl.appendChild(li);
            });
        }

        adviceArea.style.display = 'block';

    } catch (error) {
        console.error('Error loading balances:', error);
        showToast('Failed to calculate group balance sheet.', 'danger');
        adviceArea.style.display = 'none';
    }
}

function computeDebtResolution(balances) {
    const debtors = [];
    const creditors = [];

    balances.forEach(b => {
        const bal = b.finalBalance;
        if (bal < -1.0) {
            debtors.push({ id: b.userId, name: b.userName, balance: Math.abs(bal) });
        } else if (bal > 1.0) {
            creditors.push({ id: b.userId, name: b.userName, balance: bal });
        }
    });

    const settlements = [];
    let iterations = 0;
    const maxIterations = debtors.length + creditors.length + 10;

    while (debtors.length > 0 && creditors.length > 0 && iterations < maxIterations) {
        iterations++;
        // Sort both descending to match largest debtor to largest creditor
        debtors.sort((a, b) => b.balance - a.balance);
        creditors.sort((a, b) => b.balance - a.balance);

        const d = debtors[0];
        const c = creditors[0];

        const amount = Math.min(d.balance, c.balance);
        const roundedAmount = Math.round(amount);
        if (roundedAmount > 1) {
            settlements.push({
                fromId: d.id,
                from: d.name,
                toId: c.id,
                to: c.name,
                amount: roundedAmount
            });
        }

        d.balance -= amount;
        c.balance -= amount;

        if (d.balance < 1.0) {
            debtors.shift();
        }
        if (c.balance < 1.0) {
            creditors.shift();
        }
    }

    return settlements;
}

window.fillFullAmount = function(index, amount) {
    const input = document.getElementById(`amount-paying-${index}`);
    if (input) {
        input.value = amount;
    }
};

window.submitSettlement = async function(index, payerUserId, receiverUserId, groupId) {
    const amountInput = document.getElementById(`amount-paying-${index}`);
    const noteInput = document.getElementById(`settlement-note-${index}`);
    
    if (!amountInput) return;
    
    const amountVal = parseFloat(amountInput.value);
    const noteVal = noteInput ? noteInput.value.trim() : "";
    
    if (isNaN(amountVal) || amountVal <= 0) {
        showToast("Please enter a valid amount greater than 0.", "danger");
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/settlements`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                groupId: groupId,
                payerUserId: payerUserId,
                receiverUserId: receiverUserId,
                amount: amountVal,
                note: noteVal
            })
        });
        
        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || "Failed to record settlement");
        }
        
        showToast("Settlement recorded successfully!", "success");
        await handleGroupChange();
    } catch (error) {
        console.error("Error recording settlement:", error);
        showToast(error.message, "danger");
    }
};
