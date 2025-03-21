package com.heslin.postopia.service.os;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.heslin.postopia.dto.UserId;
import com.heslin.postopia.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

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

    String convertToDataURL(MultipartFile file) throws IOException {
        // Get the file's content type
        String contentType = file.getContentType();

        // Read the file content
        byte[] fileContent = file.getBytes();

        // Encode the file content to Base64
        String base64Encoded = Base64.getEncoder().encodeToString(fileContent);

        // Construct the data URL
        return "data:" + contentType + ";base64," + base64Encoded;
    }

    String uploadAvatar(String id, String prefix, MultipartFile img) throws IOException {
        Map mp = cloudinary.uploader().upload(convertToDataURL(img), ObjectUtils.asMap("public_id_prefix", prefix, "public_id", id, "overwrite", true));
        return (String) mp.get("secure_url");
    }
}
