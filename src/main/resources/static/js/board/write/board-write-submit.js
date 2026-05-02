const form = document.querySelector('.post-form');

form.addEventListener('submit', e => {
    e.preventDefault();

    deleteErrorSpan();

    const formData = new FormData(form);

    fetch('/api/board/write', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) throw response;
        return;
    })
    .then(() => {
        alert('게시글이 등록 되었습니다');
        location.href = '/boards';
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