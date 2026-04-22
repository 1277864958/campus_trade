package com.campus.repository;
import com.campus.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIdIsNullOrderBySortAsc();
    List<Category> findByParentIdOrderBySortAsc(Long parentId);
}
