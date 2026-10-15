package com.suguna.weighment_api_service.util;

import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.exception.ApiException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public final class MultipartPhotos {

    private MultipartPhotos() {}

    public static List<MultipartFile> resolve(MultipartFile single, MultipartFile[] multiple) {
        List<MultipartFile> files = new ArrayList<>();
        if (single != null && !single.isEmpty()) {
            files.add(single);
        }
        if (multiple != null) {
            for (MultipartFile file : multiple) {
                if (file != null && !file.isEmpty()) {
                    files.add(file);
                }
            }
        }
        if (files.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "At least one image is required (photo or photos)");
        }
        return files;
    }
}
