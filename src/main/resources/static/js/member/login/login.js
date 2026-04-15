const loginBtn = document.querySelector('#login-btn');
const errorMessageDiv = document.querySelectorAll('.password-not-match')[0];

loginBtn.addEventListener('click', () => {
    if (errorMessageDiv != null) {
        errorMessageDiv.remove();
    }
});