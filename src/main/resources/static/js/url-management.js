// URL management functions
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        const toast = new bootstrap.Toast(document.getElementById('copyToast'));
        toast.show();
    });
}

function openEditModal(urlId) {
    document.getElementById('editUrlId').value = urlId;
    new bootstrap.Modal(document.getElementById('editUrlModal')).show();
}

function confirmDelete(urlId) {
    document.getElementById('deleteUrlId').value = urlId;
    new bootstrap.Modal(document.getElementById('deleteConfirmModal')).show();
}

function submitEditForm() {
    const form = document.getElementById('editUrlForm');
    const formData = new FormData(form);
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    
    fetch('/url/update', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            [csrfHeader]: csrfToken
        },
        body: new URLSearchParams(formData)
    }).then(response => {
        if (response.ok) {
            window.location.reload();
        } else {
            alert('Произошла ошибка при обновлении');
        }
    });
}

// URL expiration check
function checkExpiringUrls() {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch('/api/url-expiration/check', {
        headers: {
            [csrfHeader]: csrfToken,
            'Accept': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        if (data && data.hasExpiring && data.urls) {
            // Получаем правильный массив URL
            let urlsToProcess = data.urls;
            if (data.urls[0] === "java.util.ArrayList") {
                urlsToProcess = data.urls[1];
            }
            showExpirationNotifications(urlsToProcess);
        }
    })
    .catch(error => console.error('Error checking expiring URLs:', error));
}

function showExpirationNotifications(urls) {
    const container = document.getElementById('notificationContainer');
    
    urls.forEach(url => {
        const shortUrl = url[0]?.shortUrl || url.shortUrl;
        const hoursLeft = url[0]?.hoursLeft || url.hoursLeft;
        const expiresAt = url[0]?.expiresAt || url.expiresAt;

        if (!shortUrl || hoursLeft === undefined || !expiresAt) {
            console.error('Invalid URL data:', url);
            return;
        }

        const toastHtml = `
            <div class="toast" role="alert">
                <div class="toast-header">
                    <strong class="me-auto">Срок действия истекает</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
                </div>
                <div class="toast-body">
                    Ссылка ${shortUrl} истекает через ${hoursLeft} ч. (${formatDateTime(expiresAt)})
                </div>
            </div>
        `;

        try {
            container.insertAdjacentHTML('beforeend', toastHtml);
            const toastElement = container.lastElementChild;
            
            if (!toastElement) {
                console.error('Failed to create toast element');
                return;
            }

            const toast = new bootstrap.Toast(toastElement, {
                autohide: false
            });
            
            toast.show();
            console.log('Notification shown successfully');
        } catch (error) {
            console.error('Error showing notification:', error);
        }
    });
}

// Initialize URL expiration checks
document.addEventListener('DOMContentLoaded', function() {
    console.log('Starting URL expiration checks...');
    // First check 2 seconds after page load
    setTimeout(checkExpiringUrls, 2000);
    // Subsequent checks every 5 minutes
    setInterval(checkExpiringUrls, 5 * 60 * 1000);
}); 