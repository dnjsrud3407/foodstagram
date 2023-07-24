package com.foodstagram.service;

import com.foodstagram.dto.ListsDto;
import com.foodstagram.entity.Lists;
import com.foodstagram.entity.User;
import com.foodstagram.repository.FoodRepository;
import com.foodstagram.repository.ListRepository;
import com.foodstagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class ListService {

    private final ListRepository listRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;


    /**
     * 리스트 전체 조회 - 페이징 처리
     * @param userId
     * @param pageable
     * @return
     */
    public Page<ListsDto> findLists(Long userId, Pageable pageable) {
        return listRepository.findListsDtoByUserId(userId, pageable);
    }

    /**
     * 리스트 전체 조회
     * @param userId
     * @return
     */
    public List<ListsDto> findLists(Long userId) {
        return listRepository.findListsDtoByUserId(userId);
    }

    /**
     * 유저가 생성한 리스트 수 확인
     * @param userId
     * @return
     */
    public Long countLists(Long userId) {
        return listRepository.countListsByUserId(userId).orElse(0L);
    }

    /**
     * 리스트 이름 찾기
     * @param listId
     * @return
     */
    public String findListName(Long listId) {
        return listRepository.findListNameByListId(listId).orElseThrow(
                () -> new NoSuchElementException()
        );
    }

    /**
     * 리스트 등록
     * @param userId
     * @param name
     * @return
     */
    public Long createList(Long userId, String name) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException()
        );

        // 리스트 이름 중복 확인
        Lists findList = listRepository.findByUserIdAndNameAndIsDel(userId, name, false);
        if(findList != null) {
            throw new IllegalStateException("이미 등록된 리스트입니다.");
        }

        Lists lists = Lists.createList(name, user);
        Lists saveList = listRepository.save(lists);

        return saveList.getId();
    }

    /**
     * 리스트 수정
     * @param userId
     * @param listId
     * @param newName
     * @return
     */
    public Long modifyList(Long userId, Long listId, String newName) {
        Lists lists = listRepository.findById(listId).orElseThrow(
                () -> new NoSuchElementException());

        // 변경할 리스트 명이 현재와 동일하다면 변경 필요 X
        if(newName.equals(lists.getName())) {
            throw new IllegalStateException("이름이 동일합니다.");
        }

        // 리스트 이름 중복 확인
        Lists findList = listRepository.findByUserIdAndNameAndIsDel(userId, newName, false);
        if(findList != null) {
            throw new IllegalStateException("이미 등록된 리스트입니다.");
        }

        // isDel 값 수정
        lists.updateList(newName, null);

        return listId;
    }

    /**
     * 리스트 삭제
     * @param userId
     * @param listId
     */
    public void deleteList(Long userId, Long listId) {
        // Food 게시글 중 해당 리스트가 사용중일 시 삭제 불가능
        int counted = foodRepository.countIsDelFalseByUserIdAndListId(userId, listId);

        if(counted > 0) {
            throw new IllegalStateException("현재 사용되고 있는 리스트입니다.");
        }

        listRepository.updateIsDelTrueByUserIdAndListId(userId, listId);
    }
}
