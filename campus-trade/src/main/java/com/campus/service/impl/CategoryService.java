package com.campus.service.impl;
import com.campus.common.exception.BusinessException;
import com.campus.dto.resp.CategoryResp;
import com.campus.entity.Category;
import com.campus.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepo;

    public List<CategoryResp> tree() {
        return categoryRepo.findByParentIdIsNullOrderBySortAsc().stream().map(root -> {
            CategoryResp r = toResp(root);
            r.setChildren(categoryRepo.findByParentIdOrderBySortAsc(root.getId())
                    .stream().map(this::toResp).collect(Collectors.toList()));
            return r;
        }).collect(Collectors.toList());
    }

    @Transactional
    public CategoryResp create(String name, Long parentId, String icon, Integer sort) {
        Category c = new Category(); c.setName(name); c.setParentId(parentId);
        c.setIcon(icon); c.setSort(sort != null ? sort : 0);
        return toResp(categoryRepo.save(c));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepo.findByParentIdOrderBySortAsc(id).isEmpty())
            throw BusinessException.of("请先删除子分类");
        categoryRepo.deleteById(id);
    }

    private CategoryResp toResp(Category c) {
        CategoryResp r = new CategoryResp(); BeanUtils.copyProperties(c, r); return r;
    }
}
