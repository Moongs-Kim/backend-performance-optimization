const homeBtn = document.querySelector('.home-btn');
const backBtn = document.querySelector('.back-btn');

if (!document.referrer) {
    backBtn.style.display="none";
}

backBtn.addEventListener('click', () => {
    if (document.referrer) {
        history.back();
    } else {
        location.href = "/";
    }
});

homeBtn.addEventListener('click', () => {
    location.href = '/';
});