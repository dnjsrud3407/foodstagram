package com.foodstagram.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileStore fileStore;

    /**
     * 파일이 요청 크기 보다 큰 경우 MaxUploadSizeExceededException.class 오류가 터진다.
     * 이때 ajax를 통해 비동기 방식으로 에러를 처리하면 BindingResult 가 View 에 그대로 남아 있을 수 있다.
     * @param files
     * @return
     */
    @PostMapping("/file/sizeValidate")
    public ResponseEntity fileSizeValidate(MultipartFile[] files) {
        // 파일 확인 완료
        HashMap<String, String> result = new HashMap<>();
        result.put("status", "ok");
        return new ResponseEntity(result, HttpStatus.OK);
    }
}
