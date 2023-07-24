package com.foodstagram.service;

import com.foodstagram.dto.CategoryCreateDto;
import com.foodstagram.dto.CategoryDto;
import com.foodstagram.dto.CategoryModifyDto;
import com.foodstagram.entity.Category;
import com.foodstagram.repository.CategoryRepository;
import com.foodstagram.repository.FoodCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final FoodCategoryRepository foodCategoryRepository;

    /**
     * 카테고리 등록
     * @param categoryCreateDto
     * @return
     */
    public Long saveCategory(CategoryCreateDto categoryCreateDto) {
        validateDuplicateCategory(categoryCreateDto.getName());

        Category category = Category.createCategory(categoryCreateDto.getName(), categoryCreateDto.getIsDel());

        Category savedCategory = categoryRepository.save(category);

        return savedCategory.getId();
    }

    /**
     * 카테고리 이름 중복 확인
     * @param name
     */
    @Transactional(readOnly = true)
    public void validateDuplicateCategory(String name) {
        Category byName = categoryRepository.findByName(name);
        if(byName != null) {
            throw new IllegalStateException("이미 등록된 카테고리입니다.");
        }
    }

    /**
     * 카테고리 변경
     * @param categoryModifyDto
     */
    public void modifyCategory(CategoryModifyDto categoryModifyDto) {
        Optional<Category> findCategory = categoryRepository.findById(categoryModifyDto.getId());
        if(findCategory.isPresent()) {
            Category category = findCategory.get();

            // 사용 중인 카테고리인지 확인 - 있다면 에러 발생
            if(categoryModifyDto.getIsDel() == true) {
                deleteCheckCategory(categoryModifyDto.getId());
            }

            // 카테고리 이름 중복 검사 - 있다면 에러 발생
            modifyCheckCategoryName(categoryModifyDto.getId(), categoryModifyDto.getName());

            category.updateCategory(categoryModifyDto.getName(), categoryModifyDto.getIsDel());
        }
    }

    /**
     * 카테고리 이름 중복 확인 - 현재 카테고리 이름은 제외하고 체크한다
     * @param id
     * @param name
     */
    @Transactional(readOnly = true)
    public void modifyCheckCategoryName(Long id, String name) {
        // 카테고리 이름 중복 확인 필요
        Long counted = categoryRepository.countCategoryByIdNotAndName(id, name);
        if(counted > 0) {
            throw new IllegalStateException("이미 등록된 카테고리입니다.");
        }
    }

    /**
     * 카테고리 삭제 가능 확인
     * @param categoryId
     */
    public void deleteCheckCategory(Long categoryId) {
        // Food 게시글 중 해당 카테고리가 사용 중일 시 삭제 불가능
        Long counted = foodCategoryRepository.countIsDelFalseByCategoryId(categoryId);
        if(counted > 0) {
            throw new IllegalStateException("현재 사용되고 있어 비활성화 불가합니다.");
        }
    }

    /**
     * 카테고리 하나 조회
     * @param name
     * @return
     */
    public Category findCategory(String name) {
        return categoryRepository.findByName(name);
    }

    /**
     * 활성화된 카테고리 전체 조회
     * @return
     */
    public List<CategoryDto> findCategories() {
        List<Category> categories = categoryRepository.findIsDelFalseCategoryAll();

        return categories.stream()
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 전체 조회 (활성화, 비활성화 모두 포함)
     * @return
     */
    public List<CategoryDto> findAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(c -> new CategoryDto(c.getId(), c.getName(), c.getIsDel(), c.getCreatedDate(), c.getModifiedDate()))
                .collect(Collectors.toList());
    }

}
