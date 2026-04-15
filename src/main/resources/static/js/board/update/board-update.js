const contentArea = document.querySelector('#content');
const textCountSpan = document.querySelector('#current-count');

textCountSpan.innerText = contentArea.value.length;

contentArea.addEventListener('input', () => {
    textCountSpan.innerText = contentArea.value.length;
});

const submitBtn = document.querySelector('.submit-btn');

submitBtn.addEventListener('click', () => {
    deleteErrorSpan();

    const boardId = document.querySelector('.main-inner').dataset.boardId;
    const boardForm = document.querySelector('#board-form');
    const formData = new FormData(boardForm);

    fetch(`/api/board/${boardId}`, {
        method: 'PATCH',
        body: formData
    })
    .then(response => {
        if (!response.ok) throw response;
    })
    .then(() => {
        alert('게시물이 변경 되었습니다');
    })
    .catch(err => {
        apiErrorResponse(err, createErrorMessage);
    });
});

function createErrorMessage(fieldErrors) {
    for (const fieldError of fieldErrors) {
        let siblingTagId = '';
        switch (fieldError.field) {
            case 'categoryName':
                siblingTagId = '#categoryName';
                break;
            case 'boardOpen':
                siblingTagId = '#radio-div';
                break;
            case 'title':
                siblingTagId = '#title';
                break;
            case 'content':
                siblingTagId = '#content';
                break;
        }
        createErrorSpan(siblingTagId, fieldError.message);
    }
}

function createErrorSpan(tagId, message) {
    const siblingTag = document.querySelector(tagId);
    const errorSpan = document.createElement('span');
    errorSpan.textContent = message;
    errorSpan.classList.add('field-error');
    siblingTag.after(errorSpan);
}

function deleteErrorSpan() {
    const fieldErrorSpans = document.querySelectorAll('.field-error');
    if (fieldErrorSpans != null) {
        fieldErrorSpans.forEach(spanTag => spanTag.remove());
    }
}