const postCard = document.querySelector('.post-card');
const likeBtn = document.querySelector('.like-btn');

likeBtn.addEventListener('click', () => {
    const boardId = postCard.dataset.boardId;
    const isLiked = likeBtn.dataset.isLiked === 'true';
    const methodType = (isLiked) ? 'DELETE' : 'PUT';

    fetch(`/api/board/${boardId}/like`, {method: methodType})
    .then(response => {
        if (response.status === 204) return null;
        if (!response.ok) throw response;
        return response.json();
    })
    .then(resData => {
        if (resData !== null) {
            toggleLike(likeBtn, resData);
        }
    })
    .catch(async err => {
        apiErrorResponse(err, null);
    });
});

function toggleLike(likeBtn, resData) {
    if (resData.liked) {
        likeBtn.classList.add('like-check');
    } else {
        likeBtn.classList.remove('like-check');
    }
    document.querySelector('#like-count').textContent = resData.likeCount;
    likeBtn.dataset.isLiked = String(resData.liked);
}
