const commentList = document.querySelector('.comment-list');

commentList.addEventListener('click', (e) => {
    const replyBtn = e.target.closest('.reply-btn');
    if (!replyBtn) return;

    const commentEl = replyBtn.closest('.comment');
    const replyForm = commentEl.querySelector('.reply-form');

    replyForm.classList.toggle('active');
});

const commentSubmitBtn = document.querySelector('.comment-submit-btn');

commentSubmitBtn.addEventListener('click', () => {
    const commentTextarea = document.querySelector('#comment-textarea');
    if (!commentTextarea.value) {
        alert('댓글을 입력해 주세요');
        return;
    }

    const boardId = postCard.dataset.boardId;

    const comment = {
        content: commentTextarea.value
    };

    fetch(`/api/board/${boardId}/comment`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(comment)
    })
    .then(response => {
        if (!response.ok) throw response;
        return response.json();
    })
    .then(resData => {
        const commentContainer = document.querySelector('.comment-list');
        commentContainer.insertAdjacentHTML('afterbegin', createCommentHtml(resData));
        commentTextarea.value = '';
    })
    .catch(err => {
        apiErrorResponse(err, null);
    });
});

const commentMoreBtn = document.querySelector('.comment-more-btn');

if (commentMoreBtn != null) {
    commentMoreBtn.addEventListener('click', () => {
        const lastCommentId = commentMoreBtn.dataset.lastCommentId;
        const boardId = postCard.dataset.boardId;

        fetch(`/api/board/${boardId}/comment?lastCommentId=${lastCommentId}`, {
            method: 'GET'
        })
        .then(response => {
            if (!response.ok) throw new Error(response.status);
            return response.json();
        })
        .then(resData => {
            const commentContainer = document.querySelector('.comment-list');
            resData.comments.forEach(comment => {
                commentContainer.insertAdjacentHTML('beforeend', createCommentHtml(comment));
            });
            if (resData.hasNext) commentMoreBtn.dataset.lastCommentId = resData.lastCommentId;
            else commentMoreBtn.remove();
        })
        .catch(err => {
            apiErrorResponse(err, null);
        });
    });
}

function createCommentHtml(comment) {
    let hidden = comment.replyCount > 0 ? '' : 'hidden';

    return (
        `<div class="comment" data-comment-id="${comment.commentId}">
            <div class="comment-meta">
                <span class="comment-writer">${comment.commentWriter}</span>
                <span class="comment-date">${comment.commentCreatedTime}</span>
            </div>
            <p class="comment-content">${comment.commentContent}</p>

            <div class="comment-actions">
                <button class="reply-btn">답글 달기</button>
                <button class="reply-toggle-btn ${hidden}" data-reply-count="${comment.replyCount}">
                    답글 보기 <span class="reply-count">(${comment.replyCount})</span>
                </button>
            </div>

            <div class="reply-section">
                <div class="reply-form">
                    <textarea class="reply-textarea" placeholder="답글을 입력하세요"></textarea>
                    <button type="button" class="reply-submit-btn">등록</button>
                </div>

                <div class="reply-list hidden"></div>
                <button class="reply-more-btn hidden">답글 더보기 ▼</button>
            </div>
        </div>`
    );
}
