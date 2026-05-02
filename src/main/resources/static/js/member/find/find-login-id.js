const form = document.querySelector('.post-form');

form.addEventListener('submit', e => {
    e.preventDefault();

    deleteErrorSpan();

    const email = document.querySelector('#email').value;

    fetch('/api/member/find-id', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email })
    })
    .then(response => {
        if (!response.ok) throw response;
        return response.json();
    })
    .then(data => {
        createResponseDiv(data.value);
        disabledSubmitBtn();
    })
    .catch(err => {
        apiErrorResponse(err, createErrorMessage);
    });

});

function createResponseDiv(loginId) {
    const siblingTag = document.querySelector('.form-group');
    const responseTextDiv = document.createElement('div');
    const responseDiv = document.createElement('div');

    responseTextDiv.textContent = '회원님의 아이디';
    responseTextDiv.classList.add('find-id-text');

    responseDiv.textContent = loginId;
    responseDiv.classList.add('find-id');

    siblingTag.after(responseTextDiv);
    responseTextDiv.after(responseDiv);
}

function disabledSubmitBtn() {
    const submitBtn = document.querySelector("#find-id-btn");
    submitBtn.disabled = true;
}

function createErrorMessage(fieldErrors) {
     const message = fieldErrors[0].message;
     createErrorSpan(message)
}

function createErrorSpan(message) {
    const siblingTag = document.querySelector('#email');
    const errorSpan = document.createElement('span');
    errorSpan.textContent = message;
    errorSpan.classList.add('field-error');
    siblingTag.after(errorSpan);
}

function deleteErrorSpan() {
    const fieldErrorSpan = document.querySelector('.field-error');
    const findIdDiv = document.querySelector('.find-id');
    if (fieldErrorSpan != null) {
        fieldErrorSpan.remove();
    }
    if (findIdDiv != null) {
        findIdDiv.remove();
    }
}