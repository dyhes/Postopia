package com.heslin.postopia.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.dto.response.ApiResponse;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.enums.JoinedSpaceOrder;
import com.heslin.postopia.enums.PopularSpaceOrder;
import com.heslin.postopia.model.User;
import com.heslin.postopia.service.space.SpaceService;


@RestController
@RequestMapping("/space")
public class SpaceController {
    @Autowired
    private SpaceService spaceService;

    public record SpaceDto(String name, String description) {}

    @PostMapping("create")
    public BasicApiResponseEntity createSpace(@AuthenticationPrincipal User user, @RequestPart("info") SpaceDto info, @RequestPart(name = "avatar", required = false) MultipartFile avatar) {
        String avatarUrl = null;
        if (info.name == null || info.description == null) {
            return BasicApiResponseEntity.badRequest("space name and description are required");
        }

        if (avatar != null) {
            // object storage
        }

        Message message = spaceService.createSpace(user, info.name, info.description, avatarUrl);
        return BasicApiResponseEntity.ok(message);
    }
    
    public record SpaceIdDto(Long id){};

    @PostMapping("join")
    public BasicApiResponseEntity joinSpace(@AuthenticationPrincipal User user, @RequestBody SpaceIdDto space) {
        if (space.id == null) {
            return BasicApiResponseEntity.badRequest("spaceId is required");
        }
        
        Message message = spaceService.joinSpace(space.id, user);
        return BasicApiResponseEntity.ok(message);
    }

    public record SpaceListDto(List<SpaceInfo> infos, int count, int totalPages, long totalCount) {}


    @GetMapping("list")
    public ApiResponseEntity<SpaceListDto> getSpaces(@AuthenticationPrincipal User user, 
    @RequestParam(name = "page") int page, 
    @RequestParam(name = "size", required = false, defaultValue = "10") int size, 
    @RequestParam(name = "order", defaultValue = "LASTACTIVE")JoinedSpaceOrder order,
    @RequestParam(name = "direction", defaultValue = "DESC") Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, order.getField()));
        Page<SpaceInfo> spaces = spaceService.getSpacesByUserId(user.getId(), pageable);
        return ApiResponseEntity.ok(
            new ApiResponse<>(null, 
                                        new SpaceListDto(spaces.getContent(), 
                                                    spaces.getNumberOfElements(),
                                                    spaces.getTotalPages(),
                                                    spaces.getTotalElements())
                )
            );
    }
    
    @GetMapping("popular")
    public ApiResponseEntity<SpaceListDto> getPopularSpaces(@AuthenticationPrincipal User user, 
    @RequestParam(name = "page") int page, 
    @RequestParam(name = "size", required = false, defaultValue = "10") int size, 
    @RequestParam(name = "order", defaultValue = "MEMBERCOUNT")PopularSpaceOrder order) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SpaceInfo> spaces = spaceService.getPopularSpaces(order, pageable);
        return ApiResponseEntity.ok(
            new ApiResponse<>(null, 
                                        new SpaceListDto(spaces.getContent(), 
                                                    spaces.getNumberOfElements(),
                                                    spaces.getTotalPages(),
                                                    spaces.getTotalElements())
                )
            );
    }

    @PostMapping("leave")
        public BasicApiResponseEntity leaveSpace(@AuthenticationPrincipal User user, @RequestBody SpaceIdDto space) {
            if (space.id == null) {
                return BasicApiResponseEntity.badRequest("spaceId is required");
            }
            
            Message message = spaceService.leaveSpace(space.id, user);
            return BasicApiResponseEntity.ok(message);
        }
        
}
