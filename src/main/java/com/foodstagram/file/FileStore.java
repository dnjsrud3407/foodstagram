package com.foodstagram.file;

import com.foodstagram.dto.FoodPictureDto;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
//        multipartFile.transferTo(new File(getFullPath(storedFileName)));

        ConfigFileReader.ConfigFile config =
                ConfigFileReader.parse("~/.oci/config", "DEFAULT");

        AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(config);

        ObjectStorage client = new ObjectStorageClient(provider);
        client.setRegion(Region.AP_CHUNCHEON_1);

        UploadConfiguration uploadConfiguration =
                UploadConfiguration.builder()
                        .allowMultipartUploads(true)
                        .allowParallelUploads(true)
                        .build();

        UploadManager uploadManager = new UploadManager(client, uploadConfiguration);

        String namespaceName = "axewvmfa4ckn";
        String bucketName = "foodstagram-bucket";
        String objectName = storedFileName;
        Map<String, String> metadata = null;
        String contentType = "image/jpg";

        String contentEncoding = null;
        String contentLanguage = null;

        File body = new File("/test.jpg");

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucketName(bucketName)
                        .namespaceName(namespaceName)
                        .objectName(objectName)
                        .contentType(contentType)
                        .contentLanguage(contentLanguage)
                        .contentEncoding(contentEncoding)
                        .opcMeta(metadata)
                        .build();
        UploadManager.UploadRequest uploadDetails =
                UploadManager.UploadRequest.builder(body).allowOverwrite(true).build(request);

        UploadManager.UploadResponse response = uploadManager.upload(uploadDetails);
        System.out.println(response);

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
