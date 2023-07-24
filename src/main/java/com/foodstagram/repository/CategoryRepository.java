package com.foodstagram.repository;

import com.foodstagram.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByName(String name);

    Long countCategoryByIdNotAndName(Long id, String name);

    @Query("select c from Category c where c.isDel = false")
    List<Category> findIsDelFalseCategoryAll();

    @Modifying
    @Query("update Category c set c.isDel = true where c.id = :categoryId")
    int updateIsDelTrueByCategoryId(@Param("categoryId") Long categoryId);
}
