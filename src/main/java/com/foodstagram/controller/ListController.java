package com.foodstagram.controller;

import com.foodstagram.config.auth.PrincipalDetails;
import com.foodstagram.controller.form.ListCreateForm;
import com.foodstagram.controller.form.ListModifyForm;
import com.foodstagram.dto.*;
import com.foodstagram.error.ErrorResult;
import com.foodstagram.page.MyPage;
import com.foodstagram.service.FoodService;
import com.foodstagram.service.ListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/list")
public class ListController {

    private final ListService listService;
    private final FoodService foodService;
    private final MessageSource ms;

    @GetMapping("")
    public String list(@PageableDefault(page = 0, size = 10) Pageable pageable,
                       @AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        Long userId = principalDetails.getUser().getId();
        String loginId = principalDetails.getUser().getLoginId();

        Page<ListsDto> lists = listService.findLists(userId, pageable);
        MyPage paging = new MyPage(lists);

        model.addAttribute("totalCount", lists.getTotalElements());
        model.addAttribute("lists", lists);
        model.addAttribute("paging", paging);
        model.addAttribute("loginId", loginId);

        return "list/list";
    }

    /**
     * 리스트 추가하기
     * @param listCreateForm
     * @param result
     * @param principalDetails
     * @return
     */
    @PostMapping("/create")
    public ResponseEntity createList(@RequestBody @Validated ListCreateForm listCreateForm, BindingResult result, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if(result.hasErrors()) {
            ErrorResult errorResult = new ErrorResult("name", result.getFieldError("name").getDefaultMessage());
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        Long userId = principalDetails.getUser().getId();
        ListCreateDto listCreateDto = new ListCreateDto(listCreateForm);
        try {
            listService.createList(listCreateDto, userId);
        } catch (IllegalStateException e) {
            ErrorResult errorResult = new ErrorResult("name", e.getMessage());
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        HashMap<String, String> serviceResult = new HashMap<>();
        serviceResult.put("status", "ok");

        return new ResponseEntity(serviceResult, HttpStatus.OK);
    }

    /**
     * 리스트 삭제하기
     * @param param
     * @param principalDetails
     * @return
     */
    @PostMapping("/delete")
    public ResponseEntity deleteList(@RequestBody Map<String, Object> param, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long listId = Long.parseLong(param.get("listId").toString());
        Long userId = principalDetails.getUser().getId();

        if(listId == 1L) {
            ErrorResult errorResult = new ErrorResult("global", ms.getMessage("illegalDelete.list", null, null));
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        try {
            listService.deleteList(userId, listId);
        } catch (IllegalStateException e) {
            ErrorResult errorResult = new ErrorResult("global", e.getMessage());
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        HashMap<String, String> serviceResult = new HashMap<>();
        serviceResult.put("status", "ok");

        return new ResponseEntity(serviceResult, HttpStatus.OK);
    }

    /**
     * 리스트 수정하기
     * @param listModifyForm
     * @param result
     * @param principalDetails
     * @return
     */
    @PostMapping("/modify")
    public ResponseEntity modifyList(@RequestBody @Validated ListModifyForm listModifyForm, BindingResult result,
                                     @AuthenticationPrincipal PrincipalDetails principalDetails) {
        // 유효성 검사
        if(result.hasErrors()) {
            if(result.hasFieldErrors("listId")) {
                ErrorResult errorResult = new ErrorResult("global", null);
                return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
            } else {
                ErrorResult errorResult = new ErrorResult("modifyName", result.getFieldError("modifyName").getDefaultMessage());
                return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
            }
        }

        ListModifyDto listModifyDto = new ListModifyDto(listModifyForm);
        Long userId = principalDetails.getUser().getId();

        try {
            // 중복 검사, 수정하기 전과 동일한지 검사
            listService.modifyList(userId, listModifyDto);
        } catch (IllegalStateException e) {
            ErrorResult errorResult = new ErrorResult("modifyName", e.getMessage());
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        HashMap<String, String> serviceResult = new HashMap<>();
        serviceResult.put("status", "ok");

        return new ResponseEntity(serviceResult, HttpStatus.OK);
    }

    /**
     * 리스트 상세보기
     * @return
     */
    @GetMapping("/{listId}")
    public String detail(@PathVariable Long listId, @PageableDefault(page = 0, size = 5) Pageable pageable,
                         @AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        Long userId = principalDetails.getUser().getId();
        FoodSearchDto foodSearchDto = new FoodSearchDto(listId);

        Page<FoodDto> foodList = foodService.searchFoods(foodSearchDto, userId, pageable);

        MyPage paging = new MyPage(foodList);

        String listName = listService.findListName(listId);
        ListsDto listsDto = new ListsDto(listId, listName, foodList.getTotalElements());

        model.addAttribute("foodList", foodList);
        model.addAttribute("listsDto", listsDto);
        model.addAttribute("paging", paging);

        return "list/detail";
    }

    /**
     * 리스트에서 게시글 제거
     * @return
     */
    @PostMapping("/deleteFood")
    public String deleteFood(@PageableDefault(page = 0, size = 5) Pageable pageable,
                             @RequestParam Long listId, @RequestParam List<Long> deleteFoodIds,
                             @AuthenticationPrincipal PrincipalDetails principalDetails, RedirectAttributes redirectAttributes) {
        foodService.modifyListNotDecided(deleteFoodIds);

        // 리스트에 남아있는 게시글 수가 0인 경우 /list로 redirect 해준다
        Long userId = principalDetails.getUser().getId();
        int foodCount = foodService.countListFoods(userId, listId);
        if(foodCount < 1) {
            return "redirect:/list";
        }

        // 삭제되는 리스트 개수가 페이지 사이즈와 같은 경우 page - 1 해준다
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        if(deleteFoodIds.size() == size) {
            page -= 1;
        }

        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);

        return "redirect:/list/" + listId;
    }
}
