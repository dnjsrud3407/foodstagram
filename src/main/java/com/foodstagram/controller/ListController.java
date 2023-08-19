package com.foodstagram.controller;

import com.foodstagram.config.auth.PrincipalDetails;
import com.foodstagram.controller.form.ListCreateForm;
import com.foodstagram.dto.ListCreateDto;
import com.foodstagram.dto.ListsDto;
import com.foodstagram.error.ErrorResult;
import com.foodstagram.page.MyPage;
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

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/list")
public class ListController {

    private final ListService listService;
    private final MessageSource ms;

    @GetMapping("")
    public String list(@PageableDefault(page = 0, size = 10) Pageable pageable,
                       @AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        Long userId = principalDetails.getUser().getId();

        Page<ListsDto> lists = listService.findLists(userId, pageable);
        MyPage paging = new MyPage(lists);

        model.addAttribute("totalCount", lists.getTotalElements());
        model.addAttribute("lists", lists);
        model.addAttribute("paging", paging);

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
            ErrorResult errorResult = new ErrorResult(result.getFieldError("name").getField(), result.getFieldError("name").getDefaultMessage());
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
    public ResponseEntity deleteList(@RequestParam Map<String, Object> param, @AuthenticationPrincipal PrincipalDetails principalDetails) {
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
}
