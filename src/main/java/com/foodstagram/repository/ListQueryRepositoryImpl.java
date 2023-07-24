package com.foodstagram.repository;

import com.foodstagram.dto.ListsDto;
import com.foodstagram.dto.QListsDto;
import com.foodstagram.entity.QFood;
import com.foodstagram.entity.QLists;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.foodstagram.entity.QFood.food;
import static com.foodstagram.entity.QLists.lists;

public class ListQueryRepositoryImpl implements ListQueryRepository{

    private JPAQueryFactory queryFactory;

    public ListQueryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 리스트 전체 조회 - 페이징 처리
     * @param userId
     * @param pageable
     * @return
     */
    @Override
    public Page<ListsDto> findListsDtoByUserId(Long userId, Pageable pageable) {
        List<ListsDto> content = queryFactory
                .select(new QListsDto(lists.id, lists.name, food.count()))
                .from(lists)
                .leftJoin(lists.foods, food).on(food.isDel.eq(false))
                .where(lists.user.id.eq(userId), lists.isDel.eq(false))
                .groupBy(lists.id, lists.user.id, lists.name)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(lists.id.count())
                .from(lists)
                .where(lists.user.id.eq(userId), lists.isDel.eq(false));

        // count 쿼리가 필요하지 않은 경우 실행하지 않는다
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<ListsDto> findListsDtoByUserId(Long userId) {
        return queryFactory
                .select(new QListsDto(lists.id, lists.name))
                .from(lists)
                .where(lists.user.id.eq(userId), lists.isDel.eq(false))
                .fetch();
    }
}
