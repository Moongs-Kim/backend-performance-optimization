const cards = document.querySelectorAll('.card');

cards.forEach(card => {
    card.addEventListener('click', () => {
        const boardId = card.dataset.boardId;
        location.href = `/board/${boardId}`;
    });
});