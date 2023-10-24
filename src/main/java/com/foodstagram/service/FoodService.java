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
     * 리스트에 남아있는 게시글 수 확인
     * @param userId
     * @param listId
     * @return
     */
    public int countListFoods(Long userId, Long listId) {
        return foodRepository.countIsDelFalseByUserIdAndListId(userId, listId);
    }

    /**
     * 게시글 검색하기
     * @param foodSearchDto
     * @return
     */
    public Page<FoodDto> searchFoods(FoodSearchDto foodSearchDto, Long userId, Pageable pageable) {
        // 전체보기 클릭시
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
        Lists list = listRepository.findById(listId).orElseThrow(
                () -> new NoSuchElementException()
        );

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
        Lists list =  listRepository.findById(listId).orElseThrow(
                () -> new NoSuchElementException()
        );

        // 1. 기존 FoodCategory del 처리 후 새로운 FoodCategoryList 만들기
        List<Long> newCategoryIds = foodModifyDto.getCategoryIds();
        List<FoodCategory> foodCategoryList = makeFoodCategoryList(foodId, newCategoryIds);

        // 2. 기존 FoodPicture del 처리 후 새로운 FoodPictureList 만들기
        List<FoodPicture> foodPictureList = makeModifyFoodPictureList(foodId, foodModifyDto, foodPictureDtos);

        // FoodEntity에서 setter로 변경시켜준다.
        food.updateFood(list, foodCategoryList, foodPictureList,
                foodModifyDto.getStoreName(), foodModifyDto.getScore(), foodModifyDto.getTitle(), foodModifyDto.getContent(),
                foodModifyDto.getVisitDate(), foodModifyDto.getAddress(), foodModifyDto.getLatitude(), foodModifyDto.getLongitude());

        return food.getId();
    }

    @Transactional
    private List<FoodCategory> makeFoodCategoryList(Long foodId, List<Long> newCategoryIds) {
        // -1. 현재 DB에 저장된 FoodCategory list를 조회한다 => dbCategoryIds
        List<Long> dbCategoryIds = foodCategoryRepository.findCategoryIdByFoodId(foodId);

        // -2. 기존 DB 카테고리와 newCategoryIds가 겹친다면 isDel = false 로 변경(원래 갑이 true인 경우 할 필요 X)
        // -3. 기존 DB 카테고리와 newCategoryIds가 겹치지 않는다면 isDel = true 로 변경(원래 갑이 false인 경우 할 필요 X)
        // -4. 기존 DB 카테고리에 없는 새로운 카테고리라면 FoodCategory 새로 생성하기
        List<Long> updateCategoryIds = new ArrayList<>();
        List<Long> deleteCategoryIds = new ArrayList<>();
        List<Long> insertCategoryIds = new ArrayList<>();

        for (Long newCategoryId : newCategoryIds) {
            if(!dbCategoryIds.contains(newCategoryId)) {
                insertCategoryIds.add(newCategoryId);
            } else {
                updateCategoryIds.add(newCategoryId);
            }
        }

        for (Long dbCategoryId : dbCategoryIds) {
            if(!newCategoryIds.contains(dbCategoryId)) {
                deleteCategoryIds.add(dbCategoryId);
            }
        }

        // -2. 실행 (기존 DB 카테고리와 newCategoryIds가 겹치는 경우)
        List<FoodCategory> updateCategories = foodCategoryRepository.findFoodCategoryByFoodIdAndCategoryIds(foodId, updateCategoryIds);
        for (FoodCategory updateCategory : updateCategories) {
            if(updateCategory.getIsDel()) {
                updateCategory.changeIsDel(false);
            }
        }

        // -3. 실행 (기존 DB 카테고리와 newCategoryIds가 겹치지 않은 경우)
        List<FoodCategory> deleteCategories = foodCategoryRepository.findFoodCategoryByFoodIdAndCategoryIds(foodId, deleteCategoryIds);
        for (FoodCategory deleteCategory : deleteCategories) {
            if(!deleteCategory.getIsDel()) {
                deleteCategory.changeIsDel(true);
            }
        }

        // -4. 실행 (기존 DB 카테고리에 없는 새로운 카테고리인 경우)
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
    private List<FoodPicture> makeModifyFoodPictureList(Long foodId, FoodModifyDto foodModifyDto, List<FoodPictureDto> foodPictureDtos) {
        // 1. 기존 FoodPicture del 처리하기
        List<FoodPicture> foodPictures = null;
        // -1. Thumbnail FoodPictures 모두 변경된 경우
        if(foodModifyDto.getIsThumbnailChange() && foodModifyDto.getIsFoodPicturesChange()) {
            foodPictures = foodPictureRepository.findFoodPictureByFoodId(foodId);
        } // -2. Thumbnail 만 변경된 경우
        else if(foodModifyDto.getIsThumbnailChange()) {
            foodPictures = foodPictureRepository.findFoodPictureThumbnailByFoodId(foodId);
        } // -3. FoodPictures 만 변경된 경우
        else if(foodModifyDto.getIsFoodPicturesChange()) {
            foodPictures = foodPictureRepository.findFoodPicturesByFoodId(foodId);
        }

        for (FoodPicture foodPicture : foodPictures) {
            foodPicture.changeIsDel(true);
        }

        // 2. 새로운 FoodPictureList 만들기
        // 바뀐게 없다면 기존 FoodPicture 로 유지
        List<FoodPicture> foodPictureList = null;
        if(foodPictureDtos != null) {
            foodPictureList = makeFoodPictureList(foodPictureDtos);
        }

        return foodPictureList;
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

    /**
     * 회원탈퇴 시 게시글 삭제
     * @param userId
     */
    @Transactional
    public void deleteFoodByDeleteUser(Long userId) {
        // food 게시글 id 가져오기
        List<Long> foodIds = foodRepository.findFoodIdByUserId(userId);

        // foodCategory 비활성화
        foodCategoryRepository.updateIsDelTrueByFoodIds(foodIds);

        // foodPicture 비활성화
        foodPictureRepository.updateIsDelTrueByFoodIds(foodIds);

        // food 게시글 비활성화
        foodRepository.updateIsDelTrueByUserId(userId);
    }

    /**
     * 리스트에서 게시글 제거
     * @param deleteFoodIds
     */
    @Transactional
    public void modifyListNotDecided(List<Long> deleteFoodIds) {
        foodRepository.updateListIdNotDecidedByFoodIds(deleteFoodIds);
    }

}
