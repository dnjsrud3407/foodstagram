package com.foodstagram.repository;

import com.foodstagram.dto.*;
import com.foodstagram.dto.QCategoryDto;
import com.foodstagram.dto.QFoodDto;
import com.foodstagram.dto.QFoodPictureDto;
import com.foodstagram.service.FoodSearchOrder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.foodstagram.entity.QCategory.category;
import static com.foodstagram.entity.QFood.food;
import static com.foodstagram.entity.QFoodCategory.foodCategory;
import static com.foodstagram.entity.QFoodPicture.foodPicture;
import static com.foodstagram.entity.QLists.lists;
import static org.springframework.util.StringUtils.hasText;

public class FoodQueryRepositoryImpl implements FoodQueryRepository {

    private JPAQueryFactory queryFactory;

    public FoodQueryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 게시글 상세보기
     * @param foodId
     * @return
     */
    @Override
    public FoodDto findFoodDtoById(Long foodId) {
        // Food - List 1:1관계
        FoodDto foodDto = queryFactory
                .select(new QFoodDto(
                        food.id, food.storeName, food.score, food.title, food.content,
                        food.visitDate, food.address, food.latitude, food.longitude,
                        lists.id, lists.name
                        ))
                .from(food)
                .leftJoin(food.list, lists).on(lists.isDel.eq(false))
                .where(food.id.eq(foodId))
                .fetchOne();

        // Food - Category 1:N관계
        queryFoodCategory(foodId, foodDto);

        // Food - FoodPicture 1:N관계
        queryFoodPicture(foodId, foodDto);

        return foodDto;
    }

    private void queryFoodCategory(Long foodId, FoodDto foodDto) {
        List<CategoryDto> categories = queryFactory
                .select(new QCategoryDto(category.id, category.name))
                .from(foodCategory)
                .join(foodCategory.category, category).on(foodCategory.isDel.eq(false))
                .where(foodCategory.food.id.eq(foodId))
                .fetch();
        foodDto.setCategoryDtos(categories);
    }

    private void queryFoodPicture(Long foodId, FoodDto foodDto) {
        List<FoodPictureDto> foodPictureDtos = queryFactory
                .select(new QFoodPictureDto(foodPicture.id, foodPicture.originalFileName, foodPicture.storedFileName, foodPicture.isThumbnail))
                .from(foodPicture)
                .where(foodPicture.food.id.eq(foodId), foodPicture.isDel.eq(false))
                .orderBy(foodPicture.isThumbnail.desc())
                .fetch();
        foodDto.setFoodPictureDtos(foodPictureDtos);
    }

    /**
     * 게시글 검색하기
     * @param foodSearchDto
     * @param userId
     * @param pageable : countQuery 일치하는 값이 없으면 totalCount 가 null로 나온다
     * @return
     */
    @Override
    public Page<FoodDto> findFoodDtoByFoodSearch(FoodSearchDto foodSearchDto, Long userId, Pageable pageable) {
        List<FoodDto> content = queryFactory
                .select(new QFoodDto(
                        food.id, food.storeName, food.score, food.title, food.visitDate,
                        Expressions.stringTemplate("LISTAGG({0}, ' ') WITHIN GROUP(ORDER BY {1})", category.name, category.id),
                        new QFoodPictureDto(foodPicture.originalFileName, foodPicture.storedFileName)
                ))
                .from(food)
                .join(food.foodCategories, foodCategory).on(foodCategory.isDel.eq(false))
                .join(foodCategory.category, category).on(category.isDel.eq(false))
                .join(food.foodPictures, foodPicture).on(foodPicture.isThumbnail.eq(true), foodPicture.isDel.eq(false))
                .leftJoin(food.list, lists).on(lists.isDel.eq(false))
                .where(
                        food.user.id.eq(userId),
                        containsStoreNameOrAddress(foodSearchDto.getSearchText()),
                        inCategoryId(foodSearchDto.getCategoryIds()),
                        betweenVisitDate(foodSearchDto.getVisitDateStart(), foodSearchDto.getVisitDateEnd()),
                        eqListId(foodSearchDto.getListId()),
                        food.isDel.eq(false)
                )
                .groupBy(food.id, food.storeName, food.score, food.title, food.visitDate, food.createdDate, foodPicture.originalFileName, foodPicture.storedFileName)
                .orderBy(searchOrderBy(foodSearchDto.getOrderBy()), food.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(food.id.count())
                .from(food)
                .join(food.foodCategories, foodCategory).on(foodCategory.isDel.eq(false))
                .leftJoin(food.list, lists).on(lists.isDel.eq(false))
                .where(
                        containsStoreNameOrAddress(foodSearchDto.getSearchText()),
                        inCategoryId(foodSearchDto.getCategoryIds()),
                        betweenVisitDate(foodSearchDto.getVisitDateStart(), foodSearchDto.getVisitDateEnd()),
                        eqListId(foodSearchDto.getListId()),
                        food.isDel.eq(false)
                )
                .groupBy(food.id, food.storeName, food.score, food.title, food.visitDate);

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());
    }

    private BooleanExpression containsStoreNameOrAddress(String searchText) {
        return hasText(searchText) ?
                food.storeName.contains(searchText).or(food.address.contains(searchText)) : null;
    }

    private BooleanExpression inCategoryId(List<Long> categoryIds) {
        return categoryIds.size() > 0 ? category.id.in(categoryIds) : null;
    }

    private BooleanExpression betweenVisitDate(LocalDate visitDateStart, LocalDate visitDateEnd) {
        if(visitDateStart != null && visitDateEnd == null) {
            return food.visitDate.goe(visitDateStart);
        } else if (visitDateStart == null && visitDateEnd != null) {
            return food.visitDate.loe(visitDateEnd);
        } else if (visitDateStart != null && visitDateEnd != null) {
            return food.visitDate.between(visitDateStart, visitDateEnd);
        }

        return null;
    }

    private BooleanExpression eqListId(Long listId) {
        return listId != null ? food.list.id.eq(listId) : null;
    }


    private OrderSpecifier<?> searchOrderBy(FoodSearchOrder orderBy) {
        if(orderBy == FoodSearchOrder.VISIT_DATE_ASC) {
            return food.visitDate.asc();
        }
        else if(orderBy == FoodSearchOrder.HIGH_SCORE) {
            return food.score.desc();
        }
        else if(orderBy == FoodSearchOrder.LOW_SCORE) {
            return food.score.asc();
        }
        else {
            return food.visitDate.desc();
        }
    }

    /**
     * 회원이 작성한 게시글 id 가져오기
     * @param userId
     * @return
     */
    @Override
    public List<Long> findFoodIdByUserId(Long userId) {
        return queryFactory
                .select(food.id)
                .from(food)
                .where(food.user.id.eq(userId), food.isDel.eq(false))
                .fetch().stream().collect(Collectors.toList());
    }
}
