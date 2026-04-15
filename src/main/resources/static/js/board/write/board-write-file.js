const fileBtn = document.querySelector('#fileSelectBtn');
const fileInput = document.querySelector('#attachFiles');
const fileNameInput = document.querySelector('#fileNameInput');

fileBtn.addEventListener('click', () => {
    fileInput.click();
});

fileInput.addEventListener('change', () => {
    const files = fileInput.files;
    if (files.length > 0) {
        const fileNames = [];
        for (const file of files) {
            fileNames.push(file.name);
        }
        fileNameInput.value = fileNames.join(', ');
    } else {
      fileNameInput.value = '';
  }
});