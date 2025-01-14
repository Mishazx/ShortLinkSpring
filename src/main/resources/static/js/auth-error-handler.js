// Auth error handling
document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');
    const errorDiv = document.getElementById('error-message');

    if (error) {
        const errorMessages = {
            'Email already exists': 'Этот email уже зарегистрирован',
            'Invalid credentials': 'Неверные учетные данные',
            'User not found': 'Пользователь не найден'
        };

        const decodedError = decodeURIComponent(error);
        errorDiv.textContent = errorMessages[decodedError] || decodedError;
        errorDiv.classList.remove('d-none');
    }
}); 