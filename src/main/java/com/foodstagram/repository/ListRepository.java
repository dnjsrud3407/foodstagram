package com.foodstagram.repository;

import com.foodstagram.entity.Lists;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ListRepository extends JpaRepository<Lists, Long>, ListQueryRepository {

    @Query("select l.name from Lists l where l.id = :listId")
    Optional<String> findListNameByListId(@Param("listId") Long listId);

    Lists findByUserIdAndNameAndIsDel(Long userId, String name, Boolean isDel);

    @Modifying
    @Query("update Lists l set l.isDel = true where l.user.id = :userId and l.id = :listId")
    int updateIsDelTrueByUserIdAndListId(@Param("userId") Long userId, @Param("listId") Long listId);

    @Query("select count(l.id) from Lists l where l.user.id = :userId and l.isDel = false")
    Optional<Long> countListsByUserId(@Param("userId") Long userId);
}
