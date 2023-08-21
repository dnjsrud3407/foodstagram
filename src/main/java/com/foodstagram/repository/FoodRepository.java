package com.foodstagram.repository;

import com.foodstagram.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long>, FoodQueryRepository {

    @Modifying
    @Query("update Food f set f.isDel = true where f.id = :foodId")
    int updateIsDelTrueByFoodId(@Param("foodId") Long foodId);

    /**
     * 회원 탈퇴시 게시글 삭제
     * @param userId
     */
    @Modifying
    @Query("update Food f set f.isDel = true where f.user.id = :userId")
    void updateIsDelTrueByUserId(@Param("userId") Long userId);

    @Query("select count(f.id) from Food f where f.user.id = :userId and f.list.id = :listId and f.isDel = false")
    int countIsDelFalseByUserIdAndListId(@Param("userId") Long userId, @Param("listId") Long listId);

    @Query("select count(f.id) from Food f where f.user.id = :userId and f.isDel = false")
    Optional<Long> countFoodByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("update Food f set f.list.id = 1 where f.id in :deleteFoodIds")
    void updateListIdNotDecidedByFoodIds(@Param("deleteFoodIds") List<Long> deleteFoodIds);
}