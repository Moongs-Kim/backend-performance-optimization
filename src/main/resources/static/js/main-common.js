const loginBtn = document.querySelector('.login-btn');

loginBtn.addEventListener('click', () => {
    const loginToggleData = loginBtn.dataset.loginToggle;
    location.href = (loginToggleData === 'login') ? '/login' : '/logout';
});