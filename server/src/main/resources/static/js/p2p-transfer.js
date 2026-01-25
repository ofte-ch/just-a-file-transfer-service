/**
 * P2P File Transfer - Frontend JavaScript with Idempotency and Double-Click Prevention
 */

// Generate UUID v4 for idempotency key
function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

// Upload state management
let isUploading = false;

// Send file form handler
document.getElementById('sendForm')?.addEventListener('submit', async function (e) {
    e.preventDefault();

    // Prevent double-click
    if (isUploading) {
        showStatus('File đang được tải lên, vui lòng đợi...', 'warning');
        return;
    }

    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];

    if (!file) {
        showStatus('Vui lòng chọn file', 'danger');
        return;
    }

    // Disable button and set uploading state
    isUploading = true;
    const submitBtn = this.querySelector('button[type="submit"]');
    const originalBtnContent = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang tải lên...';

    // Generate idempotency key
    const idempotencyKey = generateUUID();
    console.log('Generated idempotency key:', idempotencyKey);

    // Prepare form data
    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/api/p2p/upload', {
            method: 'POST',
            headers: {
                'X-Idempotency-Key': idempotencyKey
            },
            body: formData
        });

        const result = await response.json();

        if (response.ok) {
            showStatus(
                `✅ Upload thành công!<br>
                File ID: <strong>${result.fileId}</strong><br>
                File: ${result.fileName} (${formatFileSize(result.fileSize)})`,
                'success'
            );
            fileInput.value = ''; // Clear input
        } else if (response.status === 409) {
            // Duplicate request
            showStatus('⚠️ Request đang được xử lý, vui lòng đợi...', 'warning');
        } else {
            showStatus(`❌ Lỗi: ${result.error || 'Upload thất bại'}`, 'danger');
        }
    } catch (error) {
        console.error('Upload error:', error);
        showStatus(`❌ Lỗi kết nối: ${error.message}`, 'danger');
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
    showStatus(`🔍 Đang tìm file với key: ${key}...`, 'info');
    // TODO: Implement receive logic
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

console.log('P2P File Transfer initialized with idempotency protection');
