package com.foodstagram.service;

import com.foodstagram.dto.*;
import com.foodstagram.entity.*;
import com.foodstagram.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ListRepository listRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final FoodPictureRepository foodPictureRepository;

    /**
     * 게시글 하나 조회
     * @param foodId
     * @return
     */
    public FoodDto findFood(Long foodId) {
        FoodDto foodDto = foodRepository.findFoodDtoById(foodId);

        return foodDto;
    }

    /**
     * 유저가 등록한 게시글 수 확인
     * @param userId
     * @return
     */
    public Long countFoods(Long userId) {
        return foodRepository.countFoodByUserId(userId).orElse(0L);
    }

    /**
     * 게시글 검색하기
     * @param foodSearchDto
     * @return
     */
    public Page<FoodDto> searchFoods(FoodSearchDto foodSearchDto, Long userId, Pageable pageable) {
        if(foodSearchDto.getListId() == 0) {
            foodSearchDto.setListId(null);
        }


        Page<FoodDto> foodDtos = foodRepository.findFoodDtoByFoodSearch(foodSearchDto, userId, pageable);
        return foodDtos;
    }

    /**
     * 게시글 등록
     * @param foodCreateDto
     * @param userId
     * @param foodPictureDtos
     * @return
     */
    @Transactional
    public Long createFood(FoodCreateDto foodCreateDto, Long userId, List<FoodPictureDto> foodPictureDtos) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException()
        );

        Long listId = foodCreateDto.getListId();
        Lists list = null;
        if(listId > 0) {
            list = listRepository.findById(listId).orElseThrow(
                    () -> new NoSuchElementException()
            );
        }

        // foodCategoryList 만들기
        List<Long> categoryIds = foodCreateDto.getCategoryIds();
        List<FoodCategory> foodCategoryList = new ArrayList<>();
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId).orElseThrow(
                    () -> new NoSuchElementException()
            );
            FoodCategory createFoodCategory = FoodCategory.createFoodCategory(category);

            foodCategoryList.add(createFoodCategory);
        }

        // foodPictureList 만들기
        List<FoodPicture> foodPictureList = makeFoodPictureList(foodPictureDtos);

        Food createFood = Food.createFood(user, list, foodCategoryList, foodPictureList,
                foodCreateDto.getStoreName(), foodCreateDto.getScore(), foodCreateDto.getTitle(), foodCreateDto.getContent(), foodCreateDto.getVisitDate(),
                foodCreateDto.getAddress(), foodCreateDto.getLatitude(), foodCreateDto.getLongitude());

        Food saveFood = foodRepository.save(createFood);

        return saveFood.getId();
    }

    /**
     * 게시물 변경
     * @param foodModifyDto : 더티 체킹으로 변경
     * @param foodPictureDtos : food 연관메서드로 변경
     * @return
     */
    @Transactional
    public Long modifyFood(FoodModifyDto foodModifyDto, List<FoodPictureDto> foodPictureDtos) {
        Long foodId = foodModifyDto.getFoodId();
        Food food = foodRepository.findById(foodId).orElseThrow(
                () -> new NoSuchElementException());

        Long listId = foodModifyDto.getListId();
        Lists list = null;
        if(listId > 0) {
            list = listRepository.findById(listId).orElseThrow(
                    () -> new NoSuchElementException()
            );
        }

        // 기존 FoodCategory del 처리 후 새로운 FoodCategoryList 만들기
        // -1.  기존 DB 카테고리와 새로운 카테고리가 겹치지 않는다면 isDel = true 로 변경
        List<Long> newCategoryIds = foodModifyDto.getCategoryIds();
        foodCategoryRepository.updateIsDelTrueByFoodIdAndNotInNewCategoryIds(foodId, newCategoryIds);
        List<FoodCategory> foodCategoryList = makeFoodCategoryList(foodId, newCategoryIds);

        // 1. 기존 FoodPicture del 처리하기 
        // -1. Thumbnail FoodPictures 모두 변경된 경우
        if(foodModifyDto.getIsThumbnailChange() && foodModifyDto.getIsFoodPicturesChange()) {
            foodPictureRepository.updateIsDelTrueByFoodId(foodId);
        }   // -2. Thumbnail 만 변경된 경우
        else if(foodModifyDto.getIsThumbnailChange()) {
            foodPictureRepository.updateThumbnailIsDelTrueByFoodId(foodId);
        }   // -3. FoodPictures 만 변경된 경우
        else if(foodModifyDto.getIsFoodPicturesChange()) {
            foodPictureRepository.updateFoodPictureIsDelTrueByFoodId(foodId);
        }
        // 2. 새로운 FoodPictureList 만들기
        // 바뀐게 없다면 기존 FoodPicture 로 유지
        List<FoodPicture> foodPictureList = null;
        if(foodPictureDtos != null) {
            foodPictureList = makeFoodPictureList(foodPictureDtos);
        }

        // FoodEntity에서 setter로 변경시켜준다.
        food.updateFood(list, foodCategoryList, foodPictureList,
                foodModifyDto.getStoreName(), foodModifyDto.getScore(), foodModifyDto.getTitle(), foodModifyDto.getContent(),
                foodModifyDto.getVisitDate(), foodModifyDto.getAddress(), foodModifyDto.getLatitude(), foodModifyDto.getLongitude());

        return food.getId();
    }

    @Transactional
    private List<FoodCategory> makeFoodCategoryList(Long foodId, List<Long> newCategoryIds) {
        // -2. 기존 DB 카테고리와 새로운 카테고리가 겹친다면 isDel = false 로 변경
        foodCategoryRepository.updateIsDelFalseByFoodIdAndInNewCategoryIds(foodId, newCategoryIds);

        // -3. 현재 DB에 저장된 FoodCategory list를 조회한다 => dbCategoryIds
        List<Long> dbCategoryIds = foodCategoryRepository.findCategoryIdByFoodId(foodId);

        // -4. 기존 DB 카테고리에 없는 새로운 카테고리라면 FoodCategory 새로 생성하기
        List<Long> insertCategoryIds = new ArrayList<>();
        for (Long newCategoryId : newCategoryIds) {
            if(!dbCategoryIds.contains(newCategoryId)) {
                insertCategoryIds.add(newCategoryId);
            }
        }

        List<FoodCategory> foodCategoryList = new ArrayList<>();
        for (Long categoryId : insertCategoryIds) {
            Category category = categoryRepository.findById(categoryId).orElseThrow(
                    () -> new NoSuchElementException()
            );
            FoodCategory foodCategory = FoodCategory.createFoodCategory(category);

            foodCategoryList.add(foodCategory);
        }

        return foodCategoryList;
    }

    @Transactional
    private List<FoodPicture> makeFoodPictureList(List<FoodPictureDto> foodPictureDtos) {
        List<FoodPicture> foodPictureList = new ArrayList<>();
        for (FoodPictureDto foodPictureDto : foodPictureDtos) {
            if(foodPictureDto != null) {
                FoodPicture createFoodPicture = FoodPicture.createFoodPicture(foodPictureDto.getOriginalFileName(), foodPictureDto.getStoredFileName(), foodPictureDto.getIsThumbnail());
                foodPictureList.add(createFoodPicture);
            }
        }

        return foodPictureList;
    }

    /**
     * 게시글 삭제하기
     * @param foodId
     */
    @Transactional
    public void deleteFood(Long foodId) {
        // isDel 활성화하기 : Food, FoodCategory, FoodPicture
        foodRepository.updateIsDelTrueByFoodId(foodId);
        foodCategoryRepository.updateIsDelTrueByFoodId(foodId);
        foodPictureRepository.updateIsDelTrueByFoodId(foodId);
    }

}