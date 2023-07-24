package com.foodstagram.controller.form;

import com.foodstagram.dto.FoodDto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodModifyForm {

    private Long foodId;

    @NotBlank(message = "가게이름을 작성해주세요.")
    private String storeName;

    @NotEmpty(message = "카테고리를 하나 이상 선택해주세요.")
    private List<Long> categoryIds;

    @NotNull(message = "방문날짜는 필수입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    @NotNull(message = "별점은 0.5점 이상이어야합니다")
    @DecimalMin(value = "0.5", message = "0.5 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "5.0 이하여야 합니다.")
    private Float score;

    @NotNull(message = "리스트를 선택해주세요.")
    private Long listId;

    @NotBlank(message = "제목을 작성해주세요.")
    private String title;

    @NotBlank(message = "내용을 작성해주세요.")
    private String content;

    private String address;

    private Double latitude;

    private Double longitude;

    private MultipartFile thumbnail;

    private List<MultipartFile> foodPictures;

    private String oldThumbnailName;

    private List<String> oldFoodFileNames;

    public FoodModifyForm(FoodDto foodDto) {
        foodId = foodDto.getId();
        storeName = foodDto.getStoreName();
        categoryIds = foodDto.getCategoryDtos().stream()
                .map(c -> c.getId())
                .collect(Collectors.toList());
        visitDate = foodDto.getVisitDate();
        score = foodDto.getScore();
        listId = foodDto.getListsDto().getId();
        title = foodDto.getTitle();
        content = foodDto.getContent();
        address = foodDto.getAddress();
        latitude = foodDto.getLatitude();
        longitude = foodDto.getLongitude();

        oldThumbnailName = foodDto.getFoodPictureDtos().get(0).getStoredFileName();
        oldFoodFileNames = foodDto.getFoodPictureDtos().stream()
                            .filter(p -> p.getIsThumbnail() != true)
                            .map(p -> p.getStoredFileName())
                            .collect(Collectors.toList());
    }
}
