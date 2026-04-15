const fileContainer = document.querySelector('.existing-files');

fileContainer.addEventListener('click', (e) => {
    const fileDeleteBtn = e.target.closest('.file-delete-btn');
    if (!fileDeleteBtn) return;

    const wantToDelete = confirm('정말로 삭제하시겠습니까?');
    if (wantToDelete) {
        const boardId = document.querySelector('.main-inner').dataset.boardId;
        const fileItem = fileDeleteBtn.parentElement;
        const fileId = fileItem.dataset.fileId;

        fetch(`/api/board/${boardId}/file/${fileId}`, { method: 'DELETE' })
        .then(response => {
            console.log(response);
            if (!response.ok) throw response;
            return;
        })
        .then(() => {
            const fileNameSpan = fileItem.querySelector('.file-name');
            fileItem.classList.add('file-item-deleted');
            fileNameSpan.classList.add('file-name-deleted');
            fileDeleteBtn.disabled = true;
        })
        .catch(err => {
            apiErrorResponse(err, null);
        });
    }
});

const fileBtn = document.querySelector('.file-btn');
const fileInput = document.querySelector('#fileInput');
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

const fileSubmitBtn = document.querySelector('.file-submit-btn');

fileSubmitBtn.addEventListener('click', () => {
    const boardId = document.querySelector('.main-inner').dataset.boardId;

    const formData = new FormData();
    const fileArray = Array.from(fileInput.files);
    fileArray.forEach(file => formData.append('multipartFiles', file));

    fetch(`/api/board/${boardId}/file`, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) throw response;
        return response.json();
    })
    .then(resData => {
        const target = fileContainer;
        renderFiles(resData, target);

        fileInput.value = '';
        fileNameInput.value = '';
        alert('첨부 파일이 추가 되었습니다');
    })
    .catch(async err => {
        apiErrorResponse(err, null);
    });
});

function createFileHtml(uploadFile) {
    return (
        `<div class="file-item" data-file-id="${uploadFile.uploadFileId}">
             <span class="file-name">${uploadFile.uploadFileName}</span>
             <button type="button" class="file-delete-btn">✕</button>
        </div>`
    );
}

function renderFiles(resData, target) {
    resData.uploadFiles.forEach(uploadFile => {
        target.insertAdjacentHTML('beforeend', createFileHtml(uploadFile))
    });
}