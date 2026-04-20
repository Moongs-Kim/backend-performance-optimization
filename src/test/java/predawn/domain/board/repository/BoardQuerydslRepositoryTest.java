package predawn.domain.board.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import predawn.application.board.dto.BoardListQueryDto;
import predawn.application.board.dto.BoardSearchCond;
import predawn.domain.board.entity.Board;
import predawn.domain.board.entity.Category;
import predawn.domain.board.enums.BoardOpen;
import predawn.domain.board.enums.CategoryName;
import predawn.domain.member.entity.Member;
import predawn.domain.member.enums.Gender;
import predawn.domain.member.repository.MemberRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Transactional(readOnly = true)
class BoardQuerydslRepositoryTest {

    @Autowired
    BoardQuerydslRepository boardQuerydslRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        Member member = new Member("user1", "1234", "user1", LocalDate.of(2000, 1, 1),
                Gender.MALE, "이메일", "주소", null);
        Category category = new Category(CategoryName.GENERAL);
        List<Board> boards = createBoards(member, category);

        memberRepository.save(member);
        categoryRepository.save(category);
        boardRepository.saveAll(boards);
    }

    @DisplayName("검색 조건 없이 게시글 최신순으로 게시글 목록을 조회한다")
    @Test
    void searchBoardsByComplex_byLatest() {
        //Given
        BoardSearchCond boardSearchCond = BoardSearchCond.of("", "", "latest");
        PageRequest pageRequest = PageRequest.of(0, 3);

        //When
        List<BoardListQueryDto> boardList = boardQuerydslRepository.searchBoardsByComplex(boardSearchCond, pageRequest).getContent();

        List<Long> boardIds = extractBoardIdsBy(boardList);

        List<LocalDate> boardCreatedDateDesc = extractCreatedDateBy(boardList);

        //Then
        assertThat(boardList).hasSize(3);
        assertThat(boardCreatedDateDesc).isSortedAccordingTo(Comparator.reverseOrder());
    }

    @DisplayName("검색 조건 없이 게시글 조회순으로 게시글 목록을 조회한다")
    @Test
    void searchBoardsByComplex_byViewCountDesc() {
        //Given
        BoardSearchCond boardSearchCond = BoardSearchCond.of("", "", "view_count");
        PageRequest pageRequest = PageRequest.of(0, 3);

        //When
        List<BoardListQueryDto> boardList = boardQuerydslRepository.searchBoardsByComplex(boardSearchCond, pageRequest).getContent();

        List<Long> boardIds = extractBoardIdsBy(boardList);

        List<Integer> boardViewCountDesc = extractViewCountBy(boardList);

        //Then
        assertThat(boardList).hasSize(3);
        assertThat(boardViewCountDesc).isSortedAccordingTo(Comparator.reverseOrder());
    }

    @DisplayName("게시글 제목 검색 조건으로 게시글을 검색하고 최신순으로 게시글 목록을 조회한다")
    @Test
    void searchBoardsByComplex_byTitleKeywordAndLatest() {
        //Given
        BoardSearchCond boardSearchCond = BoardSearchCond.of("title", "1", "latest");
        PageRequest pageRequest = PageRequest.of(0, 3);

        //When
        List<BoardListQueryDto> boardList = boardQuerydslRepository.searchBoardsByComplex(boardSearchCond, pageRequest).getContent();

        List<Long> boardIds = extractBoardIdsBy(boardList);

        List<LocalDate> boardCreatedDateDesc = extractCreatedDateBy(boardList);

        //Then
        assertThat(boardList).hasSize(1);
        assertThat(boardCreatedDateDesc).isSortedAccordingTo(Comparator.reverseOrder());
    }

    @DisplayName("게시글 작성자 검색 조건으로 게시글을 검색하고 최신순으로 게시글 목록을 조회한다")
    @Test
    void searchBoardsByComplex_byWriterKeywordAndLatest() {
        //Given
        BoardSearchCond boardSearchCond = BoardSearchCond.of("writer", "1", "latest");
        PageRequest pageRequest = PageRequest.of(0, 3);

        //When
        List<BoardListQueryDto> boardList = boardQuerydslRepository.searchBoardsByComplex(boardSearchCond, pageRequest).getContent();

        List<Long> boardIds = extractBoardIdsBy(boardList);

        List<LocalDate> boardCreatedDateDesc = extractCreatedDateBy(boardList);

        //Then
        assertThat(boardList).hasSize(3);
        assertThat(boardCreatedDateDesc).isSortedAccordingTo(Comparator.reverseOrder());
    }

    private List<Board> createBoards(Member member, Category category) {
        List<Board> boards = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            boards.add(new Board(member, "제목" + i, "내용" + i, BoardOpen.ALL, category));
        }
        return boards;
    }

    private List<Long> extractBoardIdsBy(List<BoardListQueryDto> boardList) {
        return boardList.stream()
                .map(BoardListQueryDto::getBoardId)
                .toList();
    }

    private List<LocalDate> extractCreatedDateBy(List<BoardListQueryDto> boardList) {
        return boardList.stream()
                .map(BoardListQueryDto::getCreatedBoardDate)
                .toList();
    }

    private List<Integer> extractViewCountBy(List<BoardListQueryDto> boardList) {
        return boardList.stream()
                .map(BoardListQueryDto::getViewCount)
                .toList();
    }

}