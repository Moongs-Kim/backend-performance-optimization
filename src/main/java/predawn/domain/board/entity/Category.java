package predawn.domain.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import predawn.domain.board.enums.CategoryName;
import predawn.domain.common.BaseTimeEntity;

@Entity
@Getter
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private CategoryName categoryName;

    protected Category() {
    }

    public Category(CategoryName categoryName) {
        this.categoryName = categoryName;
    }
}
