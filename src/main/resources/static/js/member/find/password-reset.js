const form = document.querySelector('.post-form');

form.addEventListener('submit', e => {
    e.preventDefault();

    deleteErrorSpan();

    const submitBtn = document.querySelector('#find-password-btn');

    if (submitBtn.innerText === '비밀번호 찾기') {
        authenticate();
    } else {
        passwordChange();
    }
});

function authenticate() {
    const loginId = document.querySelector('#login-id').value;
    const email = document.querySelector('#email').value;

    const authValues = {
        loginId: loginId,
        email: email
    };

    fetch('/api/password-reset/request', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(authValues)
    })
    .then(response => {
        if (!response.ok) throw response;
        return;
    })
    .then(() => {
        makeDisabled();
        createFormGroupTag();
        changeSubmitBtnText();
    })
    .catch(err => {
        apiErrorResponse(err, createErrorMessage);
    });
}

function passwordChange() {
    const newPassword = document.querySelector('#password').value;
    const passwordCheck = document.querySelector('#password-check').value;

    const changePasswordValues = {
        newPassword: newPassword,
        passwordCheck: passwordCheck
    };

    fetch('/api/password-reset/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(changePasswordValues)
    })
    .then(response => {
        if (!response.ok) throw response;
        return;
    })
    .then(() => {
        alert('비밀번호가 변경 되었습니다');
        location.href = '/login';
    })
    .catch(err => {
        apiErrorResponse(err, createErrorMessage);
    });
}

function makeDisabled() {
    document.querySelector('#login-id').disabled = true;
    document.querySelector('#email').disabled = true;
}

function createFormGroupTag() {
    const passwordTagAttribute = {
        labelText: '비밀번호',
        labelFor: 'password',
        inputId: 'password',
        inputPlaceholder: '변경할 비밀번호를 입력하세요'
    }

    const passwordCheckTagAttribute = {
        labelText: '비밀번호 확인',
        labelFor: 'password-check',
        inputId: 'password-check',
        inputPlaceholder: '변경할 비밀번호 확인을 입력하세요'
    }

    const tagAttributeList = [passwordTagAttribute, passwordCheckTagAttribute];

    const findPasswordBtn = document.querySelector('#find-password-btn');

    tagAttributeList.forEach(attribute => {
        const formGroup = document.createElement('div');
        formGroup.classList.add('form-group');

        const passwordLabel = document.createElement('label');
        passwordLabel.textContent = attribute.labelText;
        passwordLabel.setAttribute('for', attribute.labelFor);

        const passwordInput = document.createElement('input');
        passwordInput.id = attribute.inputId;
        passwordInput.type = 'password';
        passwordInput.placeholder = attribute.inputPlaceholder;

        formGroup.appendChild(passwordLabel);
        formGroup.appendChild(passwordInput);

        findPasswordBtn.before(formGroup);
    });
}

function changeSubmitBtnText() {
    document.querySelector('#find-password-btn').innerText = '비밀번호 변경';
}

function createErrorMessage(fieldErrors) {
    for (const fieldError of fieldErrors) {
        let siblingTagId = '';
        switch (fieldError.field) {
            case 'loginId':
                siblingTagId = '#login-id';
                break;
            case 'email':
                siblingTagId = '#email';
                break;
            case 'newPassword':
                siblingTagId = '#password';
                break;
            case 'passwordCheck':
                siblingTagId = '#password-check';
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