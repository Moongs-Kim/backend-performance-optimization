package predawn.application.board.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import predawn.application.board.dto.BoardListQueryDto;
import predawn.application.board.dto.BoardPostCommand;
import predawn.application.board.dto.BoardSearchCond;
import predawn.application.file.enums.RootDirectory;
import predawn.domain.board.entity.Board;
import predawn.domain.board.entity.BoardViewHistory;
import predawn.domain.board.entity.Category;
import predawn.domain.board.enums.BoardOpen;
import predawn.domain.board.enums.CategoryName;
import predawn.domain.board.exception.BoardNotFoundException;
import predawn.domain.board.repository.BoardRepository;
import predawn.domain.board.repository.BoardViewHistoryRepository;
import predawn.domain.board.repository.CategoryRepository;
import predawn.domain.file.repository.FileStorage;
import predawn.domain.file.vo.StoredFile;
import predawn.domain.member.entity.Member;
import predawn.domain.member.enums.Gender;
import predawn.domain.member.exception.MemberNotFoundException;
import predawn.domain.member.repository.MemberRepository;
import predawn.global.pagination.PageInformation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@Slf4j
@SpringBootTest
@Transactional(readOnly = true)
class BoardServiceTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BoardViewHistoryRepository boardViewHistoryRepository;

    @MockitoBean
    private FileStorage fileStorage;

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


    @DisplayName("회원은 첨부파일 없이 게시글을 저장할 수 있다")
    @Transactional
    @Test
    void postBoard_bySimple() {
        //Given
        Member member = memberRepository.findMemberByLoginId("user1");
        BoardPostCommand boardPostCommand = new BoardPostCommand("제목", "내용", BoardOpen.ALL, CategoryName.GENERAL);
        List<MultipartFile> attachFiles = null;

        //When
        Long postedBoardId = boardService.postBoard(member.getId(), boardPostCommand, attachFiles);

        //Then
        assertThat(postedBoardId).isNotNull();

        Board board = boardRepository.findById(postedBoardId).get();
        assertThat(board.getTitle()).isEqualTo("제목");
        assertThat(board.getContent()).isEqualTo("내용");
    }

    @DisplayName("회원은 파일을 첨부하면서 게시글을 저장할 수 있다")
    @Transactional
    @Test
    void postBoard_withMultipartFile() {
        //Given
        Member member = memberRepository.findMemberByLoginId("user1");
        BoardPostCommand boardPostCommand = new BoardPostCommand("제목", "내용", BoardOpen.ALL, CategoryName.GENERAL);

        given(fileStorage.stores(any(RootDirectory.class), any(List.class)))
                .willReturn(List.of(new StoredFile("originalFileName", "storageKey", "contentType", 1L)));

        List<MultipartFile> attachFiles = createMockMultipartFiles();

        //When
        Long postedBoardId = boardService.postBoard(member.getId(), boardPostCommand, attachFiles);

        //Then
        assertThat(postedBoardId).isNotNull();

        Board board = boardRepository.findById(postedBoardId).get();
        assertThat(board.getTitle()).isEqualTo("제목");
        assertThat(board.getContent()).isEqualTo("내용");

        then(fileStorage)
                .should()
                .stores(
                        eq(RootDirectory.BOARD),
                        argThat(files -> files.size() == 3)
                );
    }

    @DisplayName("회원이 아니면 게시글을 저장할 수 없다")
    @Transactional
    @Test
    void postBoard_byAnonymousMember() {
        //Given
        Long memberId = 999L;
        BoardPostCommand boardPostCommand = new BoardPostCommand("제목", "내용", BoardOpen.ALL, CategoryName.GENERAL);
        List<MultipartFile> attachFiles = null;

        //When / Then
        assertThrows(MemberNotFoundException.class, () -> boardService.postBoard(memberId, boardPostCommand, attachFiles));
    }

    @DisplayName("게시글 목록을 검색어 없이 최신순으로 조회하고 각 게시글의 좋아요 수도 같이 조회한다")
    @Transactional(readOnly = true)
    @Test
    void getBoardList_byNoSearchTypeAndCreatedDesc() {
        //Given
        BoardSearchCond boardSearchCond = BoardSearchCond.of("", "", "latest");
        PageInformation pageInformation = new PageInformation(0, 3);

        //When
        Page<BoardListQueryDto> pageBoardList = boardService.getBoardList(boardSearchCond, pageInformation);
        List<BoardListQueryDto> content = pageBoardList.getContent();

        List<Long> boardIds = extractBoardIdsBy(content);

        List<LocalDate> boardCreatedDateDesc = extractCreatedDateBy(content);

        List<Long> likeCounts = extractLikeCountBy(content);

        //Then
        assertThat(content).hasSize(3);
        assertThat(boardCreatedDateDesc).isSortedAccordingTo(Comparator.reverseOrder());

        assertThat(likeCounts).doesNotContainNull();
        assertThat(likeCounts.stream().allMatch(count -> count >= 0)).isTrue();
    }

    @DisplayName("게시글 조회수 증가 메소드를 실행하면 조회수가 1개 증가한다")
    @Test
    void incrementViewCount() {
        //Given
        Member member = memberRepository.findMemberByLoginId("user1");
        Board board = boardRepository.findAll().stream()
                .findAny().get();

        Long boardId = board.getId();
        Long memberId = member.getId();

        Board beforeBoard = boardRepository.findById(boardId).get();
        int beforeViewCount = beforeBoard.getViewCount();

        //When
        boardService.incrementViewCount(boardId, memberId);

        em.flush();
        em.clear();

        //Then
        Board afterBoard = boardRepository.findById(boardId).get();

        assertThat(beforeViewCount).isNotEqualTo(afterBoard.getViewCount());
        assertThat(beforeViewCount).isEqualTo(afterBoard.getViewCount() - 1);
    }

    @DisplayName("게시글 조회수 증가 메소드를 실행시 게시글을 찾을 수 없으면 예외가 발생한다")
    @Test
    void incrementViewCount_BoardNotFound() {
        //Given
        Long notExistsBoardId = 999L;
        Long memberId = 1L;

        //When //Then
        assertThrows(BoardNotFoundException.class, () -> boardService.incrementViewCount(notExistsBoardId, memberId));
    }

    @DisplayName("게시글 조회수 증가 메소드를 실행시 BoardViewHistory Row가 없으면 Insert 한다")
    @Test
    void incrementViewCount_InsertBoardViewHistory() {
        //Given
        Member member = memberRepository.findMemberByLoginId("user1");
        Board board = boardRepository.findAll().stream()
                .findAny().get();

        Long boardId = board.getId();
        Long memberId = member.getId();
        Long boardViewHistoryId = 1L;

        //When
        boardService.incrementViewCount(boardId, memberId);

        //Then
        Optional<BoardViewHistory> possibleBoardViewHistory = boardViewHistoryRepository.findById(boardViewHistoryId);

        assertThat(possibleBoardViewHistory.isPresent()).isTrue();
    }

    @DisplayName("게시글 조회수 증가 메소드를 실행시 BoardViewHistory Row가 있으면 Update 한다")
    @Test
    void incrementViewCount_UpdateBoardViewHistory() {
        //Given
        Member member = memberRepository.findMemberByLoginId("user1");
        Board board = boardRepository.findAll().stream()
                .findAny().get();

        Long boardId = board.getId();
        Long memberId = member.getId();

        LocalDateTime beforeViewAt = LocalDateTime.of(2000, 01, 01, 0, 0, 0);

        BoardViewHistory boardViewHistory = new BoardViewHistory(member, board, beforeViewAt);

        boardViewHistoryRepository.save(boardViewHistory);

        //When
        boardService.incrementViewCount(boardId, memberId);

        //Then
        BoardViewHistory findBoardViewHistory = boardViewHistoryRepository.findById(boardViewHistory.getId()).get();

        assertThat(findBoardViewHistory.getViewAt()).isNotEqualTo(beforeViewAt);
    }

    private List<Board> createBoards(Member member, Category category) {
        List<Board> boards = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            boards.add(new Board(member, "제목" + i, "내용" + i, BoardOpen.ALL, category));
        }
        return boards;
    }

    private List<MultipartFile> createMockMultipartFiles() {
        MockMultipartFile attachFile1 = new MockMultipartFile("attachFiles", "test.txt", MediaType.TEXT_PLAIN_VALUE, "For Test1".getBytes());
        MockMultipartFile attachFile2 = new MockMultipartFile("attachFiles", "test.txt", MediaType.TEXT_PLAIN_VALUE, "For Test2".getBytes());
        MockMultipartFile attachFile3 = new MockMultipartFile("attachFiles", "test.txt", MediaType.TEXT_PLAIN_VALUE, "For Test3".getBytes());

        return List.of(attachFile1, attachFile2, attachFile3);
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

    private List<Long> extractLikeCountBy(List<BoardListQueryDto> boardList) {
        return boardList.stream()
                .map(BoardListQueryDto::getLikeCount)
                .toList();
    }

}