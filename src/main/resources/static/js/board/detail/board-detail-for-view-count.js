document.addEventListener('DOMContentLoaded', () => {

    const boardId = postCard.dataset.boardId;

    fetch(`/api/board/${boardId}/view`, { method: 'POST' });
});