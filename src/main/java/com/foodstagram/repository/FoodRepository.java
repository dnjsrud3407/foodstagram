package com.foodstagram.repository;

import com.foodstagram.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long>, FoodQueryRepository {

    @Modifying
    @Query("update Food f set f.isDel = true where f.id = :foodId")
    int updateIsDelTrueByFoodId(@Param("foodId") Long foodId);

    @Query("select count(f.id) from Food f where f.user.id = :userId and f.list.id = :listId and f.isDel = false")
    int countIsDelFalseByUserIdAndListId(@Param("userId") Long userId, @Param("listId") Long listId);

    @Query("select count(f.id) from Food f where f.user.id = :userId and f.isDel = false")
    Optional<Long> countFoodByUserId(Long userId);
}