package com.foodstagram.dto;

import com.foodstagram.service.FoodSearchOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodSearchDto {

    private String searchText;

    private List<Long> categoryIds = new ArrayList<>();

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDateStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDateEnd;
    private FoodSearchOrder orderBy = FoodSearchOrder.VISIT_DATE_DESC;    // DATE_DESC, DATE_ASC, HIGH_SCORE, LOW_SCORE

    private Long listId = 1L;

}
