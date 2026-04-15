const idDuplicationCheckBtn = document.querySelector('#id-duplication-check-btn');
const loginIdInput = document.querySelector('#loginId');
const idDuplicationCheckInput = document.querySelector('#idDuplicationCheck');

idDuplicationCheckBtn.addEventListener('click', () => {
    const checkId = loginIdInput.value;
    if ((checkId === null && checkId === undefined) || checkId.trim().length === 0) {
        alert('아이디를 입력해 주세요');
        return;
    }
    fetch('/api/signup/id-check', {
        method: 'POST',
        headers: {
            'Content-Type': 'text/plain'
        },
        body: checkId
    })
    .then(response => {
        if (!response.ok) throw new Error(response.status);
        return response.json();
    })
    .then(resData => {
        idDuplicationCheckInput.value = resData;
        const message = resData ? '사용 가능한 아이디 입니다' : '사용 불가능한 아이디 입니다';
        alert(message);
        const idDuplicationCheckSpan = document.querySelector("span[data-login-id='error']");
        if (resData && idDuplicationCheckSpan != null) {
            idDuplicationCheckSpan.remove();
        }

    })
    .catch(() => {
        alert('아이디를 입력해 주세요');
    });
});

loginIdInput.addEventListener('change', () => {
    idDuplicationCheckInput.value = false;
});