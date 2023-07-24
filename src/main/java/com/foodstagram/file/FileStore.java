package com.foodstagram.file;

import com.foodstagram.dto.FoodPictureDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    private List<String> availableExtList = Arrays.asList("jfif", "pjpeg", "jpeg", "pjp", "jpg", "png", "gif");

    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    public FoodPictureDto storeFile(MultipartFile multipartFile, boolean isThumbnail) throws IOException {
        if(multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storedFileName = createStoreFileName(originalFilename);
        multipartFile.transferTo(new File(getFullPath(storedFileName)));

        return new FoodPictureDto(originalFilename, storedFileName, isThumbnail);
    }

    public List<FoodPictureDto> storeFile(List<MultipartFile> multipartFile, boolean isThumbnail) throws IOException {
        List<FoodPictureDto> foodPictureDtos = new ArrayList<>();

        if(multipartFile == null) {
            return null;
        }

        for (MultipartFile file : multipartFile) {
            foodPictureDtos.add(storeFile(file, isThumbnail));
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
}
