const updateBtn = document.querySelector('.update-btn');

if (updateBtn != null) {
    updateBtn.addEventListener('click', () => {
        const boardId = postCard.dataset.boardId;
        location.href = `/board/${boardId}/update`;
    });
}