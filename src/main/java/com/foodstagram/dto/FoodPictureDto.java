package com.foodstagram.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter @Setter
@NoArgsConstructor
public class FoodPictureDto {

    private Long id;
    private String originalFileName;
    private String storedFileName;
    private Boolean isThumbnail;

    public FoodPictureDto(String originalFileName, String storedFileName, Boolean isThumbnail) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.isThumbnail = isThumbnail;
    }

    @QueryProjection
    public FoodPictureDto(Long id, String originalFileName, String storedFileName, Boolean isThumbnail) {
        this.id = id;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.isThumbnail = isThumbnail;
    }

    @QueryProjection
    public FoodPictureDto(String originalFileName, String storedFileName) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
    }

}
