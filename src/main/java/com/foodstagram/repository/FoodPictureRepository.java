package com.foodstagram.repository;

import com.foodstagram.entity.FoodPicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FoodPictureRepository extends JpaRepository<FoodPicture, Long> {

    @Modifying
    @Query("update FoodPicture fp set fp.isDel = true where fp.food.id = :foodId")
    int updateIsDelTrueByFoodId(@Param("foodId") Long foodId);

    @Query("select fp from FoodPicture fp where fp.food.id = :foodId and fp.isDel = false")
    List<FoodPicture> findFoodPictureByFoodId(@Param("foodId") Long foodId);

    @Query("select fp from FoodPicture fp " +
            "where fp.food.id = :foodId and fp.isThumbnail = true and fp.isDel = false")
    List<FoodPicture> findFoodPictureThumbnailByFoodId(@Param("foodId") Long foodId);

    @Query("select fp from FoodPicture fp " +
            "where fp.food.id = :foodId and fp.isThumbnail = false and fp.isDel = false")
    List<FoodPicture> findFoodPicturesByFoodId(@Param("foodId") Long foodId);

    @Modifying
    @Query("update FoodPicture fp set fp.isDel = true where fp.food.id in :foodIds")
    void updateIsDelTrueByFoodIds(@Param("foodIds") List<Long> foodIds);

}
