package com.foodstagram.file;

import com.foodstagram.dto.FoodPictureDto;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    private List<String> availableExtList = Arrays.asList("jfif", "pjpeg", "jpeg", "pjp", "jpg", "png", "gif");

    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    public FoodPictureDto storeFile(MultipartFile multipartFile, boolean isThumbnail) throws Exception {
        if(multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storedFileName = createStoreFileName(originalFilename);
//        multipartFile.transferTo(new File(getFullPath(storedFileName)));

        ConfigFileReader.ConfigFile config =
                ConfigFileReader.parse("/home/opc/.oci/config", "DEFAULT");

        AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(config);

        ObjectStorage client = new ObjectStorageClient(provider);
        client.setRegion(Region.AP_CHUNCHEON_1);

//        UploadConfiguration uploadConfiguration =
//                UploadConfiguration.builder()
//                        .allowMultipartUploads(true)
//                        .allowParallelUploads(true)
//                        .build();
//
//        UploadManager uploadManager = new UploadManager(client, uploadConfiguration);

        String namespaceName = "axewvmfa4ckn";
        String bucketName = "foodstagram-bucket";
        String objectName = storedFileName;
        Map<String, String> metadata = null;
        String contentType = multipartFile.getContentType();

        String contentEncoding = null;
        String contentLanguage = null;

        InputStream inputStream = multipartFile.getInputStream();
//        File body = new File(storedFileName);

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .namespaceName(namespaceName)
                        .bucketName(bucketName)
                        .objectName(storedFileName)
                        .contentType(contentType)
                        .contentLength(multipartFile.getSize())
                        .putObjectBody(inputStream)
                        .build();

//        UploadManager.UploadRequest uploadDetails =
//                UploadManager.UploadRequest.builder(body).allowOverwrite(true).build(request);
//
//        UploadManager.UploadResponse response = uploadManager.upload(uploadDetails);
//        System.out.println(response);

        // upload file
        client.putObject(request);
        client.close();

        return new FoodPictureDto(originalFilename, storedFileName, isThumbnail);
    }

    public List<FoodPictureDto> storeFile(List<MultipartFile> multipartFile, boolean isThumbnail) throws Exception {
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
