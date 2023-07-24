package com.foodstagram.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FoodDto {

    private Long id;
    private ListsDto listsDto = new ListsDto();

    private List<CategoryDto> categoryDtos = new ArrayList<>();
    private List<FoodPictureDto> foodPictureDtos = new ArrayList<>();

    private String categoryNameStr;

    private String storeName;
    private Float score;
    private String title;
    private String content;
    private LocalDate visitDate;
    private String address;
    private Double latitude;
    private Double longitude;

    /**
     * 게시글 상세보기, 게시글 수정하기
     */
    @QueryProjection
    public FoodDto(Long id, String storeName, Float score, String title, String content, LocalDate visitDate,
                   String address, Double latitude, Double longitude, Long listId, String name) {
        this.id = id;
        this.storeName = storeName;
        this.score = score;
        this.title = title;
        this.content = content;
        this.visitDate = visitDate;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;

        // list 정보
        this.listsDto.setId(listId);
        this.listsDto.setName(name);
    }

    /**
     * 게시글 검색하기
     */
    @QueryProjection
    public FoodDto(Long id, String storeName, Float score, String title, LocalDate visitDate,
                   String categoryNameStr,
                   FoodPictureDto thumbnailDto) {
        this.id = id;
        this.storeName = storeName;
        this.score = score;
        this.title = title;
        this.visitDate = visitDate;
        this.categoryNameStr = categoryNameStr;

        this.foodPictureDtos.add(thumbnailDto);
    }
}
