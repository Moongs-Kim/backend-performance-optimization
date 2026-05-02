package predawn.domain.board.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import predawn.domain.board.entity.Board;
import predawn.domain.board.entity.BoardViewHistory;
import predawn.domain.board.entity.Category;
import predawn.domain.board.enums.BoardOpen;
import predawn.domain.board.enums.CategoryName;
import predawn.domain.member.entity.Member;
import predawn.domain.member.enums.Gender;
import predawn.domain.member.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BoardViewHistoryRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private BoardViewHistoryRepository boardViewHistoryRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Long boardId = null;

    @BeforeEach
    void setUp() {
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "이메일", "주소", null);
        Category category = new Category(CategoryName.GENERAL);

        Board board = new Board(member, "제목", "내용", BoardOpen.ALL, category);

        memberRepository.save(member);
        categoryRepository.save(category);
        boardRepository.save(board);

        boardId = board.getId();
    }

    @DisplayName("게시글 ID와 회원 ID로 BoardViewHistory를 조회할 수 있다")
    @Test
    void findByBoardIdAndMemberId() {
        //Given
        Member member = memberRepository.findMemberByLoginId("user1");

        Board board = em.getReference(Board.class, boardId);

        Long memberId = member.getId();

        LocalDateTime viewAt = LocalDateTime.of(2000, 01, 01, 0, 0, 0);
        BoardViewHistory boardViewHistory = new BoardViewHistory(member, board, viewAt);

        boardViewHistoryRepository.save(boardViewHistory);

        //When
        BoardViewHistory findBoardViewHistory = boardViewHistoryRepository.findByBoardIdAndMemberId(boardId, memberId).get();

        //Then
        assertThat(findBoardViewHistory.getViewAt()).isEqualTo(viewAt);
        assertThat(findBoardViewHistory.getBoard().getId()).isEqualTo(boardId);
        assertThat(findBoardViewHistory.getMember().getId()).isEqualTo(memberId);
    }

    @DisplayName("존재하지 않는 게시글 ID와 회원 ID로 BoardViewHistory를 조회할 수 없다")
    @Test
    void findByBoardIdAndMemberId_NotFound() {
        //Given
        Member member = memberRepository.findMemberByLoginId("user1");

        Board board = em.getReference(Board.class, boardId);

        LocalDateTime viewAt = LocalDateTime.of(2000, 01, 01, 0, 0, 0);
        BoardViewHistory boardViewHistory = new BoardViewHistory(member, board, viewAt);

        boardViewHistoryRepository.save(boardViewHistory);

        Long notExistsBoardId = 999L;
        Long notExistsMemberId = 999L;

        //When
        Optional<BoardViewHistory> possibleBoardViewHistory = boardViewHistoryRepository.findByBoardIdAndMemberId(notExistsBoardId, notExistsMemberId);

        //Then
        assertThat(possibleBoardViewHistory.isEmpty()).isTrue();
    }
}