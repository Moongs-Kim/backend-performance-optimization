const fileInput = document.querySelector('#profileImage');
const fileBtn = document.querySelector('#fileSelectBtn');
const fileNameInput = document.querySelector('.file-name');

fileBtn.addEventListener('click', () => {
    fileInput.click();
});

fileInput.addEventListener('change', () => {
    if (fileInput.files.length > 0) {
        fileNameInput.value = fileInput.files[0].name;
    } else {
        fileNameInput.value = '';
    }
});