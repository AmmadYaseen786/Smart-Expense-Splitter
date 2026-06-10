const API_BASE_URL = window.location.origin.includes('localhost:8080')
    ? '/api'
    : 'http://localhost:8080/api';

// Globally intercept unauthorized fetch requests
const originalFetch = window.fetch;
window.fetch = async function(...args) {
    const response = await originalFetch.apply(this, args);
    if (response.status === 401 && !window.location.pathname.includes('login.html') && !window.location.pathname.includes('register.html')) {
        console.warn('Unauthorized. Redirecting to login page...');
        localStorage.removeItem('currentUser');
        window.location.href = 'login.html';
    }
    return response;
};

// Check authentication
async function checkAuth() {
    const isAuthPage = window.location.pathname.includes('login.html') || window.location.pathname.includes('register.html');
    
    try {
        const res = await fetch(`${API_BASE_URL}/auth/me`);
        if (res.ok) {
            const user = await res.json();
            localStorage.setItem('currentUser', JSON.stringify(user));
            
            const avatar = document.getElementById('user-avatar-initial');
            const nameLbl = document.getElementById('user-profile-name');
            if (avatar && nameLbl) {
                avatar.textContent = user.name.charAt(0).toUpperCase();
                nameLbl.textContent = user.name;
            }

            if (isAuthPage) {
                window.location.href = 'dashboard.html';
            }
        } else {
            if (!isAuthPage) {
                window.location.href = 'login.html';
            }
        }
    } catch (error) {
        console.error('Auth verification failed:', error);
        if (!isAuthPage) {
            window.location.href = 'login.html';
        }
    }
}

// Programmatic Logout
async function logout() {
    try {
        await fetch(`${API_BASE_URL}/auth/logout`, { method: 'POST' });
    } catch (error) {
        console.error('Logout request failed:', error);
    } finally {
        localStorage.removeItem('currentUser');
        window.location.href = 'login.html';
    }
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let iconClass = 'fa-circle-check';
    if (type === 'danger') iconClass = 'fa-circle-exclamation';
    if (type === 'warning') iconClass = 'fa-triangle-exclamation';

    toast.innerHTML = `
        <i class="fa-solid ${iconClass}"></i>
        <div>${escapeHtml(message)}</div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('fade-out');
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 4000);
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;")
              .replace(/</g, "&lt;")
              .replace(/>/g, "&gt;")
              .replace(/"/g, "&quot;")
              .replace(/'/g, "&#039;");
}
