package com.foodstagram.file;

import com.foodstagram.dto.FoodPictureDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    private List<String> availableExtList = Arrays.asList("jfif", "pjpeg", "jpeg", "pjp", "jpg", "png", "gif");

    /**
     * 사용자 별 폴더 > 사진 저장한다.
     * 폴더가 없다면 생성해준다.
     * @param loginId
     * @param filename
     * @return
     * @throws IOException
     */
    public String getFullPath(String loginId, String filename) throws IOException {
        Path dirPath = Path.of(fileDir + loginId);
        if(!Files.isDirectory(dirPath)) {
            Files.createDirectory(dirPath);
        }
        return fileDir + loginId + "/" + filename;
    }

    public FoodPictureDto storeFile(String loginId, MultipartFile multipartFile, boolean isThumbnail) throws IOException {
        if(multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storedFileName = createStoreFileName(originalFilename);

        File file = new File(getFullPath(loginId, storedFileName));
        multipartFile.transferTo(file);

        return new FoodPictureDto(originalFilename, storedFileName, isThumbnail);
    }

    public List<FoodPictureDto> storeFile(String loginId, List<MultipartFile> multipartFile, boolean isThumbnail) throws IOException {
        List<FoodPictureDto> foodPictureDtos = new ArrayList<>();

        if(multipartFile == null) {
            return null;
        }

        for (MultipartFile file : multipartFile) {
            foodPictureDtos.add(storeFile(loginId, file, isThumbnail));
        }

        return foodPictureDtos;
    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    public String extractExt(String fileName) {
        int dotIndex = fileName.indexOf(".");
        return fileName.substring(dotIndex + 1);
    }

    public boolean isAvailableExt(String ext) {
        return availableExtList.contains(ext.toLowerCase());
    }

    public boolean isAvailableFile(String fileName) {
        String ext = extractExt(fileName);
        return availableExtList.contains(ext.toLowerCase());
    }

    /**
     * 회원 탈퇴시 폴더 삭제
     * @param loginId
     * @throws IOException
     */
    public void deleteFolder(String loginId) throws IOException {
        Path dirPath = Path.of(fileDir + loginId);

        if(Files.isDirectory(dirPath)) {
            FileSystemUtils.deleteRecursively(dirPath);
        }
    }
}
