package com.foodstagram.repository;

import com.foodstagram.entity.FoodPicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodPictureRepository extends JpaRepository<FoodPicture, Long> {

    @Modifying
    @Query("update FoodPicture fp set fp.isDel = true where fp.food.id = :foodId")
    int updateIsDelTrueByFoodId(@Param("foodId") Long foodId);

    @Modifying
    @Query("update FoodPicture fp set fp.isDel = true where fp.isThumbnail = true and fp.food.id = :foodId")
    int updateThumbnailIsDelTrueByFoodId(@Param("foodId") Long foodId);

    @Modifying
    @Query("update FoodPicture fp set fp.isDel = true where fp.isThumbnail = false and fp.food.id = :foodId")
    int updateFoodPictureIsDelTrueByFoodId(@Param("foodId") Long foodId);
}
