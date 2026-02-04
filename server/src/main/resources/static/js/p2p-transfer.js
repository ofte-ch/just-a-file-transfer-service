/**
 * P2P File Transfer - Frontend JavaScript with CSRF + Session ID Protection
 * Uses Spring Security's CSRF token and Session ID for duplicate prevention
 */

// Get CSRF token from backend (injected via Thymeleaf)
const csrfToken = globalThis.CSRF_TOKEN || '';
const csrfHeader = globalThis.CSRF_HEADER || 'X-XSRF-TOKEN';
const messages = globalThis.MESSAGES || {};

// Upload state management
let isUploading = false;

/**
 * Get CSRF token from cookie (alternative method)
 * Spring Security stores CSRF token in 'XSRF-TOKEN' cookie
 */
function getCsrfTokenFromCookie() {
    const name = 'XSRF-TOKEN';
    const regex = new RegExp('(^| )' + name + '=([^;]+)');
    const match = regex.exec(document.cookie);
    return match ? match[2] : null;
}

// Send file form handler
document.getElementById('sendForm')?.addEventListener('submit', async function (e) {
    e.preventDefault();

    // Prevent double-click
    if (isUploading) {
        showStatus(messages.uploadInProgress, 'warning');
        return;
    }

    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];

    if (!file) {
        showStatus(messages.selectFile, 'danger');
        return;
    }

    // Disable button and set uploading state
    isUploading = true;
    const submitBtn = this.querySelector('button[type="submit"]');
    const originalBtnContent = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>${messages.uploading}`;

    // Get CSRF token (from Thymeleaf or cookie)
    const token = csrfToken || getCsrfTokenFromCookie();
    console.log('Using CSRF token from Spring Security');

    // Prepare form data
    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/api/p2p/upload', {
            method: 'POST',
            headers: {
                [csrfHeader]: token // Send CSRF token in header
            },
            body: formData,
            credentials: 'same-origin' // Send session cookie
        });

        const result = await response.json();

        if (response.ok) {
            const fileIdMsg = messages.fileId.replace('{0}', `<strong>${result.fileId}</strong>`);
            showStatus(
                `${messages.uploadSuccess}<br>
                ${fileIdMsg}<br>
                File: ${result.fileName} (${formatFileSize(result.fileSize)})`,
                'success'
            );

            fileInput.value = ''; // Clear input
        } else if (response.status === 409) {
            // Duplicate request (session is already uploading)
            showStatus(messages.duplicate, 'warning');
        } else {
            const errorMsg = messages.uploadError.replace('{0}', result.error || 'Upload failed');
            showStatus(errorMsg, 'danger');
        }
    } catch (error) {
        console.error('Upload error:', error);
        const errorMsg = messages.connectionError.replace('{0}', error.message);
        showStatus(errorMsg, 'danger');
    } finally {
        // Re-enable button
        isUploading = false;
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalBtnContent;
    }
});

// Receive file form handler (placeholder)
document.getElementById('receiveForm')?.addEventListener('submit', function (e) {
    e.preventDefault();
    const key = document.getElementById('keyInput').value;
    const searchMsg = messages.searching.replace('{0}', key);
    showStatus(searchMsg, 'info');
    // NOTE: File receive logic will be implemented when P2P WebRTC connection is ready
});

// Show status message
function showStatus(message, type = 'info') {
    const statusArea = document.getElementById('statusArea');
    if (!statusArea) return;

    const alertClass = `alert-${type}`;
    statusArea.className = `alert ${alertClass}`;
    statusArea.innerHTML = message;
    statusArea.classList.remove('d-none');

    // Auto-hide after 10 seconds for success/info messages
    if (type === 'success' || type === 'info') {
        setTimeout(() => {
            statusArea.classList.add('d-none');
        }, 10000);
    }
}

// Format file size
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}

// Prevent form double-submission on Enter key
document.querySelectorAll('form').forEach(form => {
    form.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' && isUploading) {
            e.preventDefault();
            return false;
        }
    });
});

console.log('P2P File Transfer initialized with Spring Security CSRF + Session ID protection');
