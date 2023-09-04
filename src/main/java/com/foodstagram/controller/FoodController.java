package com.foodstagram.controller;

import com.foodstagram.config.auth.PrincipalDetails;
import com.foodstagram.controller.form.FoodCreateForm;
import com.foodstagram.controller.form.FoodModifyForm;
import com.foodstagram.dto.*;
import com.foodstagram.file.FileStore;
import com.foodstagram.page.MyPage;
import com.foodstagram.service.CategoryService;
import com.foodstagram.service.FoodService;
import com.foodstagram.service.ListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/food")
public class FoodController {

    private final FoodService foodService;
    private final CategoryService categoryService;
    private final ListService listService;
    private final FileStore fileStore;

    @Value("${kakao.api}")
    private String kakao_api;

    @ModelAttribute("categories")
    private List<CategoryDto> categories() {
        return categoryService.findCategories();
    }

    @GetMapping("/create")
    public String createFood(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        log.info("createFood");
        Long userId = principalDetails.getUser().getId();
        List<ListsDto> lists = listService.findLists(userId);

        model.addAttribute("foodCreateForm", new FoodCreateForm());
        model.addAttribute("foodPictureDtos", new FoodPictureDto());
        model.addAttribute("lists", lists);

        return "food/createForm";
    }

    @PostMapping("/create")
    public String createFood(@Validated @ModelAttribute FoodCreateForm foodCreateForm, BindingResult result,
                             @AuthenticationPrincipal PrincipalDetails principalDetails, Model model, RedirectAttributes redirectAttributes) throws IOException {
        Long userId = principalDetails.getUser().getId();
        String loginId = principalDetails.getUser().getLoginId();

        // 유효성 검사
        validate(result);
        validateFile(foodCreateForm.getThumbnail(), foodCreateForm.getFoodPictures(), result);

        if(result.hasErrors()) {
            List<ListsDto> lists = listService.findLists(userId);

            model.addAttribute("lists", lists);

            return "food/createForm";
        }

        // 파일 서버에 저장
        List<FoodPictureDto> foodPictureDtos = saveFoodPicture(loginId, foodCreateForm.getThumbnail(), foodCreateForm.getFoodPictures());

        FoodCreateDto foodCreateDto = new FoodCreateDto(foodCreateForm);

        // 데이터 베이스 저장
        Long foodId = foodService.createFood(foodCreateDto, userId, foodPictureDtos);
        redirectAttributes.addAttribute("foodId", foodId);

        return "redirect:/food/{foodId}";
    }

    private void validate(BindingResult result) {
        // DateTimeFormat 에 안 맞는 경우
        if(result.hasFieldErrors("visitDate")) {
            result.rejectValue("visitDate", "format");
        }

        // 별점 범위가 안 맞을 경우
        if(result.hasFieldErrors("score")) {
            result.rejectValue("score", "range");
        }
    }

    private void validateFile(MultipartFile thumbnail, List<MultipartFile> foodPictures, BindingResult result) {
        // Thumbnail 파일 선택 안 한 경우
        if(thumbnail.isEmpty()) {
            result.rejectValue("thumbnail", "required");
        }

        // Thumbnail 이미지 파일이 아닌 경우
        if(!fileStore.isAvailableFile(thumbnail.getOriginalFilename())) {
            result.rejectValue("thumbnail", "notAvailable");
        }

        if(foodPictures != null && foodPictures.size() > 0 && !foodPictures.get(0).isEmpty()) {
            // foodPictures 4개 이상 선택한 경우
            if(foodPictures.size() > 4) {
                result.rejectValue("foodPictures", "max");
            }

            // 이미지 파일이 아닌 경우
            for (MultipartFile foodPicture : foodPictures) {
                if(!fileStore.isAvailableFile(foodPicture.getOriginalFilename())) {
                    result.rejectValue("foodPictures", "notAvailable");
                    break;
                }
            }
        }
    }

    /**
     * 파일 서버에 저장하고 Dto 객체로 반환
     * @param thumbnail
     * @param formFoodPictures
     * @return
     * @throws IOException
     */
    private List<FoodPictureDto> saveFoodPicture(String loginId, MultipartFile thumbnail, List<MultipartFile> formFoodPictures) throws IOException {
        List<FoodPictureDto> foodPictureDtos = new ArrayList<>();

        FoodPictureDto savedThumbnailDto = fileStore.storeFile(loginId, thumbnail, true);
        if(savedThumbnailDto != null) {
            foodPictureDtos.add(savedThumbnailDto);
        }

        List<FoodPictureDto> savedFoodPictureDtos = fileStore.storeFile(loginId, formFoodPictures, false);
        if(savedFoodPictureDtos != null && savedFoodPictureDtos.size() > 0) {
            foodPictureDtos.addAll(savedFoodPictureDtos);
        }

        return foodPictureDtos;
    }

    /**
     * 주소 찾기
     * @return
     */
    @GetMapping("/findAddress")
    public String findAddress(Model model) {
        model.addAttribute("kakaoAPI", kakao_api);

        return "food/findAddress";
    }

    /**
     * 게시글 하나 조회
     * @param foodId
     * @param model
     * @return
     */
    @GetMapping("/{foodId}")
    public String foodDetail(@PathVariable Long foodId, @AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        FoodDto food = foodService.findFood(foodId);
        String loginId = principalDetails.getUser().getLoginId();

        model.addAttribute("food", food);
        model.addAttribute("loginId", loginId);
        model.addAttribute("kakaoAPI", kakao_api);

        return "food/detail";
    }

    /**
     * 게시글 수정
     * @param foodId
     * @return
     */
    @GetMapping("/modify/{foodId}")
    public String modifyFood(@PathVariable Long foodId, @AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        log.info("modifyFood");
        Long userId = principalDetails.getUser().getId();
        String loginId = principalDetails.getUser().getLoginId();

        FoodDto foodDto = foodService.findFood(foodId);
        FoodModifyForm foodModifyForm = new FoodModifyForm(foodDto);

        List<ListsDto> lists = listService.findLists(userId);

        model.addAttribute("foodModifyForm", foodModifyForm);
        model.addAttribute("lists", lists);
        model.addAttribute("loginId", loginId);

        return "food/modifyForm";
    }

    @PostMapping("/modify/{foodId}")
    public String modifyFood(@Validated @ModelAttribute FoodModifyForm foodModifyForm, BindingResult result,
                             @AuthenticationPrincipal PrincipalDetails principalDetails, Model model, @PathVariable Long foodId) throws IOException {
        Long userId = principalDetails.getUser().getId();
        String loginId = principalDetails.getUser().getLoginId();

        // 유효성 검사
        validate(result);

        // 파일이 변경되었는지 확인
        // old 정보가 그대로 있다면(파일이 변경 X라면) thumbnail null 값으로 set
        // => fileValidation 필요가 없다.
        boolean isThumbnailChange = false, isFoodPicturesChange = false;
        String oldThumbnailName = foodModifyForm.getOldThumbnailName();
        List<String> oldFoodFileNames = foodModifyForm.getOldFoodFileNames();

        if(oldThumbnailName != null && oldThumbnailName.length() > 0) {
            foodModifyForm.setThumbnail(null);
        } else {
            isThumbnailChange = true;
        }

        if(oldFoodFileNames != null && oldFoodFileNames.size() > 0) {
            foodModifyForm.setFoodPictures(null);
        } else {
            isFoodPicturesChange = true;
        }

        // 파일이 변경 되었다면 유효성 검사
        if(isThumbnailChange || isFoodPicturesChange) {
            validateModifyFile(foodModifyForm.getThumbnail(), foodModifyForm.getFoodPictures(), result);
        }

        if(result.hasErrors()) {
            List<ListsDto> lists = listService.findLists(userId);

            model.addAttribute("lists", lists);

            return "food/modifyForm";
        }

        // 파일 서버에 저장
        List<FoodPictureDto> foodPictureDtos = null;
        if(isThumbnailChange || isFoodPicturesChange) {
            foodPictureDtos = saveFoodPicture(loginId, foodModifyForm.getThumbnail(), foodModifyForm.getFoodPictures());
        }

        FoodModifyDto foodModifyDto = new FoodModifyDto(foodModifyForm, isThumbnailChange, isFoodPicturesChange);
        foodService.modifyFood(foodModifyDto, foodPictureDtos);

        return "redirect:/food/{foodId}";
    }

    private void pictureChangeCheck(FoodModifyForm foodModifyForm, boolean isThumbnailChange, boolean isFoodPicturesChange) {
        String oldThumbnailName = foodModifyForm.getOldThumbnailName();
        List<String> oldFoodFileNames = foodModifyForm.getOldFoodFileNames();

        if(oldThumbnailName != null && oldThumbnailName.length() > 0) {
            foodModifyForm.setThumbnail(null);
        } else {
            isThumbnailChange = true;
        }

        if(oldFoodFileNames != null && oldFoodFileNames.size() > 0) {
            foodModifyForm.setFoodPictures(null);
        } else {
            isFoodPicturesChange = true;
        }
    }

    private void validateModifyFile(MultipartFile thumbnail, List<MultipartFile> foodPictures, BindingResult result) {
        // Thumbnail 변경 한 경우
        if(thumbnail != null) {
            // Thumbnail 파일 선택 안 한 경우
            if(thumbnail.isEmpty()) {
                result.rejectValue("thumbnail", "required");
            }

            // Thumbnail 이미지 파일이 아닌 경우
            if(!fileStore.isAvailableFile(thumbnail.getOriginalFilename())) {
                result.rejectValue("thumbnail", "notAvailable");
            }
        }

        // FoodPictures 변경 한 경우
        if(foodPictures != null && foodPictures.size() > 0 && !foodPictures.get(0).isEmpty()) {
            // foodPictures 4개 이상 선택한 경우
            if(foodPictures.size() > 4) {
                result.rejectValue("foodPictures", "max");
            }

            // 이미지 파일이 아닌 경우
            for (MultipartFile foodPicture : foodPictures) {
                if(!fileStore.isAvailableFile(foodPicture.getOriginalFilename())) {
                    result.rejectValue("foodPictures", "notAvailable");
                    break;
                }
            }
        }
    }

    @GetMapping("/delete/{foodId}")
    public String deleteFood(@PathVariable Long foodId) {
        foodService.deleteFood(foodId);

        return "redirect:/food/list";
    }

    @GetMapping("/list")
    public String list(FoodSearchDto foodSearchDto, @RequestParam(required = false) String query, @PageableDefault(page = 0, size = 10) Pageable pageable,
                       @AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        log.info("list");
        Long userId = principalDetails.getUser().getId();
        String loginId = principalDetails.getUser().getLoginId();

        if(foodSearchDto == null) {
            foodSearchDto = new FoodSearchDto();
        }

        // 상단바에서 검색할 때 - 검색 조건 초기화 시킴
        if(StringUtils.hasText(query)) {
            foodSearchDto = new FoodSearchDto();
            foodSearchDto.setSearchText(query);
        }

        // 날짜가 visitDateStart > visitDateEnd 일 경우
        // start <-> end 변경해 준다
        if(foodSearchDto.getVisitDateStart() != null && foodSearchDto.getVisitDateEnd() != null) {
            changeVisitDate(foodSearchDto);
        }

        Page<FoodDto> foodList = foodService.searchFoods(foodSearchDto, userId, pageable);

        MyPage paging = new MyPage(foodList);

        List<ListsDto> lists = listService.findLists(userId);

        model.addAttribute("query", query);
        model.addAttribute("totalCount", foodList.getTotalElements());
        model.addAttribute("lists", lists);
        model.addAttribute("foodSearchDto", foodSearchDto);
        model.addAttribute("foodList", foodList.getContent());
        model.addAttribute("paging", paging);
        model.addAttribute("loginId", loginId);

        return "food/list";
    }

    private void changeVisitDate(FoodSearchDto foodSearchDto) {
        LocalDate visitDateStart = foodSearchDto.getVisitDateStart();
        LocalDate visitDateEnd = foodSearchDto.getVisitDateEnd();

        if(visitDateStart.isAfter(visitDateEnd)) {
            foodSearchDto.setVisitDateStart(visitDateEnd);
            foodSearchDto.setVisitDateEnd(visitDateStart);
        }
    }

}
