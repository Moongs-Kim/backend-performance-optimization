const comments = document.querySelectorAll('.comment');

commentList.addEventListener('click', (e) => {
    const replySubmitBtn = e.target.closest('.reply-submit-btn');
    if (!replySubmitBtn) return;

    const replyTextarea = replySubmitBtn.previousElementSibling;

    if (!replyTextarea.value) {
        alert('답글을 입력해 주세요');
        return;
    }

    const commentId = replySubmitBtn.closest('.comment').dataset.commentId;

    const replyContainer = replySubmitBtn.parentElement.nextElementSibling;

    const reply = {
        content: replyTextarea.value
    };

    fetch(`/api/comment/${commentId}/reply`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(reply)
    })
    .then(response => {
        if (!response.ok) throw response;
        return response.json();
    })
    .then(resData => {
        replyContainer.classList.remove('hidden');
        replyContainer.insertAdjacentHTML('beforeend', createReplyHtml(resData, 'reply-new'));

        replyTextarea.value = '';
        const replyForm = replySubmitBtn.parentElement;
        replyForm.classList.remove('active');

        const replyToggleBtn = replyContainer.parentElement.previousElementSibling.querySelector('.reply-toggle-btn');
        const replyCount = Number(replyToggleBtn.dataset.replyCount);

        if (replyCount === 0) {
            zeroCountReplyRender(replyToggleBtn, replyCount);
        }
        if (replyToggleBtn.innerText.includes('답글 보기')) {
            const replyCountSpan = replyToggleBtn.querySelector('.reply-count');
            replyCountSpan.innerText = `(${replyCount + 1})`;
        }

        const maxReplyId = replyContainer.dataset.maxReplyId;
        if (typeof maxReplyId === 'undefined') {
            replyContainer.dataset.maxReplyId = resData.replyId - 1;
        }

        replyToggleBtn.dataset.replyCount = replyCount + 1;

        replyContainer.lastElementChild.scrollIntoView({ behavior: 'smooth', block: 'center' });
    })
    .catch(err => {
        apiErrorResponse(err, null);
    });
});

commentList.addEventListener('click', (e) => {
    const replyToggleBtn = e.target.closest('.reply-toggle-btn');
    if (!replyToggleBtn) return;

    const replyContainer = replyToggleBtn.parentElement.nextElementSibling.querySelector('.reply-list');
    const replyMoreBtn = replyContainer.nextElementSibling;

    if (replyToggleBtn.classList.contains('loaded')) {
        const replyCount = replyToggleBtn.dataset.replyCount;
        replyContainer.classList.toggle('hidden');

        const html = replyContainer.classList.contains('hidden')
                        ? `답글 보기 <span class="reply-count">(${replyCount})</span>`
                        : `답글 숨기기`;
        replyToggleBtn.innerHTML = html;
        if (replyContainer.dataset.hasNext === 'true') replyMoreBtn.classList.toggle('hidden');
        return;
    }

    const commentId = replyToggleBtn.closest('.comment').dataset.commentId;

    fetch(`/api/comment/${commentId}/reply`, { method: 'GET' })
    .then(response => {
        if (!response.ok) return new Error(response.status);
        return response.json();
    })
    .then(resData => {
        replyContainer.dataset.lastReplyId = resData.lastReplyId;
        const maxReplyId = replyContainer.dataset.maxReplyId;
        if (typeof maxReplyId === 'undefined') {
            replyContainer.dataset.maxReplyId = resData.maxReplyId;
        }
        replyContainer.classList.remove('hidden');
        replyToggleBtn.classList.add('loaded');
        replyToggleBtn.innerText = '답글 숨기기';

        renderRepliesHtml(replyContainer, resData);

        if (resData.hasNext) {
            replyMoreBtn.classList.remove('hidden');
        }
        replyContainer.dataset.hasNext = resData.hasNext;
    })
    .catch(err => {
        apiErrorResponse(err, null);
    });
});

commentList.addEventListener('click', (e) => {
    const replyMoreBtn = e.target.closest('.reply-more-btn');
    if (!replyMoreBtn) return;

    const replyContainer = replyMoreBtn.previousElementSibling;

    const commentId = replyMoreBtn.closest('.comment').dataset.commentId;
    const lastReplyId = replyContainer.dataset.lastReplyId;
    const maxReplyId = replyContainer.dataset.maxReplyId;

    const URL = `/api/comment/${commentId}/reply?lastReplyId=${lastReplyId}&maxReplyId=${maxReplyId}`;

    fetch(URL, { method: 'GET' })
    .then(response => {
        if (!response.ok) return new Error(response.status);
        return response.json();
    })
    .then(resData => {
        replyContainer.dataset.lastReplyId = resData.lastReplyId;

        renderRepliesHtml(replyContainer, resData);

        if (!resData.hasNext) {
            replyContainer.dataset.hasNext = resData.hasNext;
            replyMoreBtn.classList.add('hidden');
        }
    })
    .catch(err => {
        apiErrorResponse(err, null);
    });
});

function zeroCountReplyRender(replyToggleBtn, replyCount) {
    replyToggleBtn.classList.remove('hidden');
    replyToggleBtn.classList.add('loaded');
    replyToggleBtn.innerHTML = `답글 숨기기`;
}

function createReplyHtml(reply, replyNew) {
    replyNew = (typeof replyNew === 'undefined') ? '' : 'reply-new';
    return (
        `<div class="reply ${replyNew}" data-reply-id="${reply.replyId}">
            <div class="comment-meta">
                <span class="comment-writer">${reply.replyWriter}</span>
                <span class="comment-date">${reply.replyCreatedTime}</span>
            </div>
            <p class="comment-content">${reply.replyContent}</p>
        </div>`
    );
}

function renderRepliesHtml(replyContainer, resData) {
    const newReply = replyContainer.querySelector('.reply-new');
    if (newReply === null) {
        renderReplies(replyContainer, 'beforeend', resData);
    } else {
        renderReplies(newReply, 'beforebegin', resData);
    }
}

function renderReplies(target, position, resData) {
    resData.replies.forEach(reply => {
        target.insertAdjacentHTML(position, createReplyHtml(reply));
    });
}