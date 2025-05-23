package com.heslin.postopia.user.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;


@Service
public class OStorageService {
    private final Cloudinary cloudinary;

    @Autowired
    public OStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadAsset(String prefix, String publicId, MultipartFile file, boolean isVideo) throws IOException {
        var options = ObjectUtils.asMap("public_id_prefix", prefix, "public_id", publicId, "overwrite", false);
        if (isVideo) {
            options.put("resource_type", "video");
        } else {
            options.put("resource_type", "image");
        }
        System.out.println(options.get("resource_type"));
        File tmp = new File(System.getProperty("java.io.tmpdir"), Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(tmp);
        System.out.println("saved in local");
        Map mp = cloudinary.uploader().upload(tmp, options);
        System.out.println(mp);
        return (String) mp.get("secure_url");
    }
}
