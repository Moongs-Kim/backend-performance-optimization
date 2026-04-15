const deleteBtn = document.querySelector('.delete-btn');

deleteBtn.addEventListener('click', () => {
    const wantToDelete = confirm('정말로 삭제하시겠습니까?');
    if (wantToDelete) {
        const boardId = document.querySelector('.main-inner').dataset.boardId;

        fetch(`/api/board/${boardId}`, { method: 'DELETE' })
        .then(response => {
            if (!response.ok) throw response;
            return;
        })
        .then(() => {
            alert('게시글이 삭제되었습니다');
            location.href = '/boards';
        })
        .catch(err => {
            apiErrorResponse(err, null);
        });
    }
});