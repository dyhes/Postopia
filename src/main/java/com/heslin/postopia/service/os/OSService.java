package com.heslin.postopia.service.os;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.heslin.postopia.dto.user.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

@Service
public class OSService {
    private final Cloudinary cloudinary;

    @Autowired
    public OSService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String updateUserAvatar(UserId id, MultipartFile img) throws IOException {
        return uploadAvatar(id.toString(), "user_avatar", img);
    }

    public String updateSpaceAvatar(String name, MultipartFile img) throws IOException {
        return uploadAvatar(name, "space_avatar", img);
    }

    // 弃用
//    String convertToDataURL(MultipartFile file) throws IOException {
//        // Get the file's content type
//        String contentType = file.getContentType();
//
//        // Read the file content
//        byte[] fileContent = file.getBytes();
//
//        // Encode the file content to Base64
//        String base64Encoded = Base64.getEncoder().encodeToString(fileContent);
//
//        // Construct the data URL
//        return "data:" + contentType + ";base64," + base64Encoded;
//    }

    String uploadAvatar(String id, String prefix, MultipartFile img) throws IOException {
        return uploadFile(prefix, id, img, false);
    }

    public String uploadFile(String prefix, String publicId, MultipartFile file, boolean isVideo) throws IOException {
        var options = ObjectUtils.asMap("public_id_prefix", prefix, "public_id", publicId, "overwrite", true);
        if (isVideo) {
            options.put("resource_type", "video");
        }
        File tmp = new File(System.getProperty("java.io.tmpdir"), Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(tmp);
        Map mp = cloudinary.uploader().upload(tmp, options);
        return (String) mp.get("secure_url");
    }
}
