package predawn.domain.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import predawn.domain.board.entity.Category;
import predawn.domain.board.enums.CategoryName;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByCategoryName(CategoryName categoryName);
}
