package com.foodstagram.repository;

import com.foodstagram.dto.FoodDto;
import com.foodstagram.dto.FoodSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FoodQueryRepository {
    FoodDto findFoodDtoById(Long foodId);
    Page<FoodDto> findFoodDtoByFoodSearch(FoodSearchDto foodSearchDto, Long userId, Pageable pageable);
}
