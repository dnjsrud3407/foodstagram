package com.foodstagram.repository;

import com.foodstagram.dto.ListsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ListQueryRepository {
    Page<ListsDto> findListsDtoByUserId(Long userId, Pageable pageable);

    List<ListsDto> findListsDtoByUserId(Long userId);
}
