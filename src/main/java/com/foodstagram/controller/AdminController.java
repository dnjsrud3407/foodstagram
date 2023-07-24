package com.foodstagram.controller;

import com.foodstagram.controller.form.CategoryCreateForm;
import com.foodstagram.controller.form.CategoryModifyForm;
import com.foodstagram.dto.CategoryCreateDto;
import com.foodstagram.dto.CategoryDto;
import com.foodstagram.dto.CategoryModifyDto;
import com.foodstagram.error.ErrorResult;
import com.foodstagram.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final CategoryService categoryService;

    @GetMapping("/category/create")
    public String createCategoryForm(Model model) {
        model.addAttribute("categoryCreateForm", new CategoryCreateForm());

        return "admin/category/createForm";
    }

    @PostMapping("/category/create")
    @ResponseBody
    public ResponseEntity categoryNameCreate(@Validated CategoryCreateForm categoryCreateForm, BindingResult result) {
        // 1. 유효성 검사 - 빈 값인지 확인
        if(result.hasErrors()) {
            ErrorResult errorResult = new ErrorResult(result.getFieldError("name").getField(), result.getFieldError("name").getDefaultMessage());
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        CategoryCreateDto categoryCreateDto = new CategoryCreateDto(categoryCreateForm);

        // view 에서는 활성화할건지를 물어보기 때문에 form에서 true값은 isDel = false로 변경해주어야함.
        categoryCreateDto.setIsDel(!categoryCreateDto.getIsDel());

        // 2. 유효성 검사 - 카테고리 이름 중복 검사
        // 유효성 통과된다면 카테고리 등록하기
        try {
            categoryService.saveCategory(categoryCreateDto);
        } catch (IllegalStateException e) {
            ErrorResult errorResult = new ErrorResult("name", e.getMessage());
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        HashMap<String, String> validationResult = new HashMap<>();
        validationResult.put("status", "ok");

        return new ResponseEntity(validationResult, HttpStatus.OK);
    }

    @GetMapping("/category/list")
    public String categoryList(Model model) {
        List<CategoryDto> categories = categoryService.findAllCategories();

        model.addAttribute("categories", categories);
        model.addAttribute("categoryCreateForm", new CategoryCreateForm());

        return "admin/category/list";
    }

    @PostMapping("/category/modify")
    @ResponseBody
    public ResponseEntity categoryNameModify(@RequestBody @Validated CategoryModifyForm categoryModifyForm, BindingResult result) {
        // 1. 유효성 검사 - 빈 값인지 확인
        if(result.hasErrors()) {
            log.info("유효성 X");
            ErrorResult errorResult = new ErrorResult(result.getFieldError("name").getField(), result.getFieldError("name").getDefaultMessage());
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        CategoryModifyDto categoryModifyDto = new CategoryModifyDto(categoryModifyForm);

        // view 에서는 활성화할건지를 물어보기 때문에 form에서의 true값은 isDel = false로 변경해주어야함.
        categoryModifyDto.setIsDel(!categoryModifyDto.getIsDel());

        // 2. 유효성 검사 - 사용 중인 카테고리인지 확인, 카테고리 이름 중복 검사
        // 유효성 통과된다면 카테고리 등록하기
        try {
            categoryService.modifyCategory(categoryModifyDto);
        } catch (IllegalStateException e) {
            ErrorResult errorResult = new ErrorResult("name", e.getMessage());
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        HashMap<String, String> validationResult = new HashMap<>();
        validationResult.put("status", "ok");

        return new ResponseEntity(validationResult, HttpStatus.OK);
    }
}
