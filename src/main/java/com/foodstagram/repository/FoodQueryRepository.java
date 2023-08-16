package com.foodstagram.repository;

import com.foodstagram.dto.FoodDto;
import com.foodstagram.dto.FoodSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FoodQueryRepository {
    FoodDto findFoodDtoById(Long foodId);
    Page<FoodDto> findFoodDtoByFoodSearch(FoodSearchDto foodSearchDto, Long userId, Pageable pageable);
    List<Long> findFoodIdByUserId(Long userId);
}
