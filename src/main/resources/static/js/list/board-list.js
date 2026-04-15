const boardRows = document.querySelectorAll('.board-row');

boardRows.forEach(row => {
    row.addEventListener('click', () => {
        const boardId = row.dataset.boardId;
        location.href = `/board/${boardId}`;
    });
});