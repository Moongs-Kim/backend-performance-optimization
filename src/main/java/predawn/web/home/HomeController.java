package predawn.web.home;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import predawn.application.board.dto.BoardListQueryDto;
import predawn.application.board.service.BoardService;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private static final int CONTENT_SIZE = 15;
    private final BoardService boardService;

    @GetMapping("/")
    public String home(Model model) {
        List<BoardListQueryDto> boardList = boardService.getBoardListTopN(CONTENT_SIZE);

        model.addAttribute("boardList", boardList);

        return "index";
    }
}
