// Theme switcher
function setTheme(isDark) {
    document.documentElement.setAttribute('data-bs-theme', isDark ? 'dark' : 'light');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
    updateTheme(isDark);
}

function updateTheme(isDark) {
    const navbar = document.querySelector('.navbar');
    const body = document.body;
    if (isDark) {
        navbar.classList.add('navbar-dark', 'bg-dark');
        navbar.classList.remove('navbar-light', 'bg-white');
        body.classList.add('bg-dark', 'text-light');
        body.classList.remove('bg-light', 'text-dark');
    } else {
        body.classList.add('bg-light', 'text-dark');
        body.classList.remove('bg-dark', 'text-light');
        navbar.classList.add('navbar-light', 'bg-white');
        navbar.classList.remove('navbar-dark', 'bg-dark');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const themeSwitch = document.getElementById('themeSwitch');
    const savedTheme = localStorage.getItem('theme') || 'light';
    const isDark = savedTheme === 'dark';
    setTheme(isDark);
    themeSwitch.checked = isDark;

    themeSwitch.addEventListener('change', (e) => setTheme(e.target.checked));
}); 