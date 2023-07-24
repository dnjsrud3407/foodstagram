package com.foodstagram.page;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@ToString
@Getter @Setter
public class MyPage {

    private int currNum;    // 현재 page Num
    private int size;       // 한 page 에 보여줄 게시글 개수
    private int totalNum;   // page 총 개수
    private int listSize;   // page list size
    private int startNum;   // page list 시작 번호
    private int endNum;    // page list 마지막 번호
    private boolean first;  // 첫 페이지인지
    private boolean last;   // 마지막 페이지인지

    public MyPage(Page result) {
        this.currNum = result.getNumber();
        this.size = result.getSize();
        this.totalNum = result.getTotalPages();
        this.listSize = 5;
        this.startNum = (currNum / listSize) * listSize;
        this.endNum = (startNum + listSize - 1 >= totalNum) ? totalNum - 1 : (startNum + listSize - 1);
        if(endNum == -1) {
            this.endNum = this.startNum;
        }
        this.first = result.isFirst();
        this.last = result.isLast();
    }
}
