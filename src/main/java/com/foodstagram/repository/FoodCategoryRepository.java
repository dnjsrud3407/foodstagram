package com.foodstagram.repository;

import com.foodstagram.entity.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FoodCategoryRepository extends JpaRepository<FoodCategory, Long> {
    @Modifying
    @Query("update FoodCategory fc set fc.isDel = true where fc.food.id = :foodId and fc.category.id not in :newCategoryIds")
    int updateIsDelTrueByFoodIdAndNotInNewCategoryIds(@Param("foodId") Long foodId, @Param("newCategoryIds") List<Long> newCategoryIds);

    @Modifying
    @Query("update FoodCategory fc set fc.isDel = false where fc.food.id = :foodId and fc.category.id in :newCategoryIds")
    int updateIsDelFalseByFoodIdAndInNewCategoryIds(@Param("foodId") Long foodId, @Param("newCategoryIds") List<Long> newCategoryIds);

    @Modifying
    @Query("update FoodCategory fc set fc.isDel = true where fc.food.id = :foodId")
    int updateIsDelTrueByFoodId(@Param("foodId") Long foodId);

    @Modifying
    @Query("update FoodCategory fc set fc.isDel = true where fc.food.id in :foodIds")
    void updateIsDelTrueByFoodIds(@Param("foodIds") List<Long> foodIds);

    @Query("select fc.category.id from FoodCategory fc where fc.food.id = :foodId")
    List<Long> findCategoryIdByFoodId(@Param("foodId") Long foodId);

    List<FoodCategory> findFoodCategoryByFoodId(Long foodId);

    @Query("select count(id) from FoodCategory fc where fc.category.id = :categoryId and fc.isDel = false")
    Long countIsDelFalseByCategoryId(@Param("categoryId") Long categoryId);
}
