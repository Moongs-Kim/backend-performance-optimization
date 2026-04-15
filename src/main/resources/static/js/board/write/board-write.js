const contentArea = document.querySelector('#content');
const textCountSpan = document.querySelector('#current-count');

contentArea.addEventListener('input', () => {
    textCountSpan.innerText = contentArea.value.length;
});