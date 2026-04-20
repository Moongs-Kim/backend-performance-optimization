package predawn.application.board.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import predawn.application.board.dto.*;
import predawn.application.board.factory.BoardFactory;
import predawn.application.comment.dto.CommentQueryDto;
import predawn.application.comment.dto.CommentResult;
import predawn.application.comment.dto.ReplyCountDto;
import predawn.application.file.dto.UploadFileResult;
import predawn.application.file.enums.RootDirectory;
import predawn.application.file.mapper.FileMapper;
import predawn.application.like.dto.LikeCountDto;
import predawn.application.like.dto.LikeToggleResult;
import predawn.domain.board.entity.Board;
import predawn.domain.board.entity.BoardViewHistory;
import predawn.domain.board.entity.Category;
import predawn.domain.board.exception.BoardAccessDeniedException;
import predawn.domain.board.exception.BoardConflictException;
import predawn.domain.board.exception.BoardNotFoundException;
import predawn.domain.board.repository.*;
import predawn.domain.board.repository.BoardViewHistoryRepository;
import predawn.domain.comment.entity.Comment;
import predawn.domain.comment.repository.CommentRepository;
import predawn.domain.comment.repository.ReplyRepository;
import predawn.domain.file.entity.BoardAttachFile;
import predawn.domain.file.entity.UploadFile;
import predawn.domain.file.exception.FileAccessDeniedException;
import predawn.domain.file.exception.FileNotFoundException;
import predawn.domain.file.repository.BoardAttachFileQuerydslRepository;
import predawn.domain.file.repository.BoardAttachFileRepository;
import predawn.domain.file.repository.FileStorage;
import predawn.domain.file.repository.UploadFileRepository;
import predawn.domain.file.vo.DownloadFile;
import predawn.domain.file.vo.StoredFile;
import predawn.domain.like.entity.Like;
import predawn.domain.like.exception.LikeUniqueViolationApiException;
import predawn.domain.like.repository.LikeQuerydslRepository;
import predawn.domain.like.repository.LikeRepository;
import predawn.domain.member.entity.Member;
import predawn.domain.member.exception.MemberNotFoundException;
import predawn.domain.member.repository.MemberRepository;
import predawn.global.pagination.PageInformation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final EntityManager em;
    private final BoardRepository boardRepository;
    private final BoardQuerydslRepository boardQuerydslRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final UploadFileRepository uploadFileRepository;
    private final BoardAttachFileRepository boardAttachFileRepository;
    private final BoardAttachFileQuerydslRepository boardAttachFileQuerydslRepository;
    private final LikeRepository likeRepository;
    private final LikeQuerydslRepository likeQuerydslRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final BoardViewHistoryRepository boardViewRepository;
    private final FileStorage fileStorage;

    @Transactional
    public Long postBoard(Long memberId, BoardPostCommand postCommand, List<MultipartFile> attachFiles) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        Category category = categoryRepository.findByCategoryName(postCommand.getCategoryName());

        Board board = BoardFactory.create(member, postCommand, category);

        boardRepository.save(board);

        List<StoredFile> storedFiles = fileStorage.stores(RootDirectory.BOARD, attachFiles);

        if (storedFiles != null) saveFileMetaToDB(storedFiles, board);

        return board.getId();
    }

    @Transactional(readOnly = true)
    public Page<BoardListQueryDto> getBoardList(BoardSearchCond boardSearchCond, PageInformation pageInformation) {
        Page<BoardListQueryDto> boardListQueryDtos = boardQuerydslRepository.searchBoardsByComplex(
                boardSearchCond,
                PageRequest.of(pageInformation.getPageIndex(), pageInformation.getPageSize())
        );

        if (boardListQueryDtos.isEmpty()) return boardListQueryDtos;

        List<Long> boardIds = boardListQueryDtos.stream()
                .map(BoardListQueryDto::getBoardId)
                .toList();

        Map<Long, Long> likeCountMap = likeQuerydslRepository.countByBoardIds(boardIds).stream()
                .collect(Collectors.toMap(
                        LikeCountDto::getBoardId,
                        LikeCountDto::getLikeCount
                ));

        boardListQueryDtos.forEach(boardDto -> {
            boardDto.applyLikeCount(likeCountMap.getOrDefault(boardDto.getBoardId(), 0L));
        });

        return boardListQueryDtos;
    }

    @Transactional(readOnly = true)
    public Page<BoardListQueryDto> getBoardListTopN(BoardSearchCond boardSearchCond, PageInformation pageInformation) {
        List<Long> boardIds = boardQuerydslRepository.searchBoardIdsTopNBy(boardSearchCond);

        if (boardIds.isEmpty()) return Page.empty();

        PageRequest pageRequest = PageRequest.of(pageInformation.getPageIndex(), pageInformation.getPageSize());

        List<BoardListQueryDto> boardListQueryDtos = boardRepository.findTopNOrderByLikeCountDesc(boardIds, pageRequest);

        likeCountNullToZero(boardListQueryDtos);

        return new PageImpl<>(boardListQueryDtos, pageRequest, boardIds.size());
    }

    @Transactional(readOnly = true)
    public List<BoardListQueryDto> getBoardListTopN(int contentSize) {
        List<Long> boardIds = boardQuerydslRepository.findBoardIdsTopN(contentSize);

        if (boardIds.isEmpty()) return List.of();

        List<BoardListQueryDto> boardListQueryDtos = boardRepository.findTopNOrderByLikeCountDesc(boardIds, PageRequest.ofSize(contentSize));

        likeCountNullToZero(boardListQueryDtos);

        return boardListQueryDtos;
    }

    @Transactional(readOnly = true)
    public BoardDetailQueryDto getBoardDetail(Long boardId, Long memberId, int commentPageSize) {
        Board board = boardRepository.findBoardWithMemberAndAttachFile(boardId)
                .orElseThrow(BoardNotFoundException::new);

        boolean isLiked = likeRepository.isLikedByUser(memberId, boardId);

        Long likeCount = likeRepository.countByBoardId(boardId);

        List<Comment> comments = commentRepository.findByBoardId(boardId, commentPageSize + 1);

        List<Long> commentIds = comments.stream()
                .map(Comment::getId).toList();

        Map<Long, Integer> replyCountMap = replyRepository.countByCommentIds(commentIds).stream()
                .collect(Collectors.toMap(
                        ReplyCountDto::getCommentId,
                        ReplyCountDto::getReplyCount
                ));

        List<CommentQueryDto> commentDtos = comments.stream()
                .map(comment -> new CommentQueryDto(comment, replyCountMap))
                .toList();

        CommentResult commentResult = new CommentResult(commentDtos, commentPageSize);

        return new BoardDetailQueryDto(board, likeCount, isLiked, commentResult);
    }

    @Transactional(readOnly = true)
    public Board getBoardDetailForUpdate(Long boardId, Long memberId) {
        Board board = boardQuerydslRepository.findBoardWithAttachFile(boardId)
                .orElseThrow(BoardNotFoundException::new);

        if (!board.getMember().getId().equals(memberId)) throw new BoardAccessDeniedException();

        return board;
    }

    @Transactional
    public void updateBoard(BoardUpdateCommand boardUpdateCommand, Long memberId) {
        Board board = boardRepository.findById(boardUpdateCommand.getBoardId())
                .orElseThrow(BoardNotFoundException::new);

        if (!board.getMember().getId().equals(memberId)) throw new BoardAccessDeniedException();

        Category category = categoryRepository.findByCategoryName(boardUpdateCommand.getCategoryName());

        boardUpdate(board, category, boardUpdateCommand);
    }

    @Transactional
    public List<UploadFileResult> updateBoardFile(Long boardId, Long memberId, List<MultipartFile> multipartFiles) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(BoardNotFoundException::new);

        if (!board.getMember().getId().equals(memberId)) throw new FileAccessDeniedException();

        List<StoredFile> storedFiles = fileStorage.stores(RootDirectory.BOARD, multipartFiles);

        List<UploadFile> uploadFiles = saveFileMetaToDB(storedFiles, board);

        return uploadFiles.stream()
                .map(UploadFileResult::new)
                .toList();
    }

    @Transactional
    public void deleteBoardFile(Long boardId, Long fileId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(BoardNotFoundException::new);

        if (!board.getMember().getId().equals(memberId)) throw new FileAccessDeniedException();

        BoardAttachFile boardAttachFile = boardAttachFileRepository.findBoardAttachFile(boardId, fileId)
                .orElseThrow(FileNotFoundException::new);

        boardAttachFileRepository.delete(boardAttachFile);
    }

    @Transactional
    public void deleteBoard(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(BoardNotFoundException::new);

        if (!board.getMember().getId().equals(memberId)) throw new BoardAccessDeniedException();

        List<BoardAttachFile> boardAttachFiles = boardAttachFileQuerydslRepository.findAllByBoardId(boardId);

        LocalDateTime now = LocalDateTime.now();
        boardAttachFiles.forEach(boardAttachFileRepository::delete);

        boardRepository.delete(board);
    }

    @Transactional
    public void incrementViewCount(Long boardId, Long memberId) {
        boardRepository.findById(boardId)
                .orElseThrow(BoardNotFoundException::new);

        boolean canIncrease = true;
        if (memberId != null) {
            BoardViewHistory boardViewHistory = boardViewRepository.findByMemberIdAndBoardId(memberId, boardId);

            if (boardViewHistory == null) {
                Member member = em.getReference(Member.class, memberId);
                Board board = em.getReference(Board.class, boardId);
                boardViewRepository.save(new BoardViewHistory(member, board, LocalDateTime.now()));
            } else {
                LocalDateTime viewAt = boardViewHistory.getViewAt();
                canIncrease = viewAt.plusHours(24).isBefore(LocalDateTime.now());
            }
        }

        if (canIncrease) boardRepository.increaseViewCount(boardId);
    }

    @Transactional
    public LikeToggleResult incrementLikeCount(Long boardId, Long memberId) {
        if (likeRepository.isLikedByUser(memberId, boardId)) return null;

        Member member = em.getReference(Member.class, memberId);
        Board board = em.getReference(Board.class, boardId);

        registerLike(member, board);
        Long likeCount = likeRepository.countByBoardId(boardId);

        return new LikeToggleResult(likeCount, true);
    }

    @Transactional
    public LikeToggleResult decrementLikeCount(Long boardId, Long memberId) {
        if (!likeRepository.isLikedByUser(memberId, boardId)) return null;

        Like like = likeRepository.findByMemberIdAndBoardId(memberId, boardId);
        if (like == null) return null;

        likeRepository.delete(like);
        Long likeCount = likeRepository.countByBoardId(boardId);

        return new LikeToggleResult(likeCount, false);
    }

    public DownloadFile downloadFile(Long boardId, Long fileId) {
        BoardAttachFile attachedFile = boardAttachFileRepository.findByUploadFileId(fileId)
                .orElseThrow(FileNotFoundException::new);

        Board board = attachedFile.getBoard();

        if (!board.getId().equals(boardId)) throw new FileNotFoundException();

        UploadFile uploadFile = attachedFile.getUploadFile();

        StoredFile storedFile = FileMapper.toStoredFile(uploadFile);

        return fileStorage.download(storedFile);
    }

    private List<UploadFile> saveFileMetaToDB(List<StoredFile> storedFiles, Board board) {
        List<UploadFile> uploadFiles = new ArrayList<>();

        storedFiles.forEach(storedFile -> {
            UploadFile uploadFile = uploadFileRepository.save(FileMapper.toEntity(storedFile));
            uploadFiles.add(uploadFile);
        });

        uploadFiles.forEach(uploadFile -> boardAttachFileRepository.save(new BoardAttachFile(board, uploadFile)));

        return uploadFiles;
    }

    private static void likeCountNullToZero(List<BoardListQueryDto> boardListQueryDtos) {
        for (BoardListQueryDto boardListQueryDto : boardListQueryDtos) {
            if (boardListQueryDto.getLikeCount() == null) {
                boardListQueryDto.applyLikeCount(0L);
            }
        }
    }

    private static void boardUpdate(Board board, Category category, BoardUpdateCommand boardUpdateCommand) {
        try {
            board.changeContent(boardUpdateCommand.getTitle(), boardUpdateCommand.getContent());
            board.changeBoardOpen(boardUpdateCommand.getBoardOpen());
            board.changeCategory(category);
        } catch (OptimisticLockException e) {
            throw new BoardConflictException(e);
        }
    }

    private void registerLike(Member member, Board board) {
        try {
            likeRepository.save(new Like(member, board));
        } catch (DataIntegrityViolationException e) {
            throw new LikeUniqueViolationApiException(e);
        }
    }
}
