// Функция для форматирования даты
function formatDateTime(isoDateString) {
    try {
        const date = new Date(isoDateString);
        if (isNaN(date.getTime())) {
            throw new Error('Invalid date');
        }
        return new Intl.DateTimeFormat('ru-RU', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        }).format(date);
    } catch (error) {
        console.error('Error formatting date:', error);
        return 'Дата недоступна';
    }
} 