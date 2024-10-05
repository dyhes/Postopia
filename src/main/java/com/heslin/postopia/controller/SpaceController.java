package com.heslin.postopia.controller;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.dto.pageresult.PageResult;
import com.heslin.postopia.dto.response.ApiResponse;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.enums.PopularSpaceOrder;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.model.User;
import com.heslin.postopia.service.os.OSService;
import com.heslin.postopia.service.space.SpaceService;
import com.heslin.postopia.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/space")
public class SpaceController {
    private final SpaceService spaceService;
    private final OSService osService;

    @Autowired
    public SpaceController(SpaceService spaceService, OSService osService) {
        this.spaceService = spaceService;
        this.osService = osService;
    }

    public record SpaceDto(String name, String description) {
    }

    @PostMapping("create")
    public ApiResponseEntity<Long> createSpace(@AuthenticationPrincipal User user, @RequestPart("info") SpaceDto info, @RequestPart(name = "avatar", required = false) MultipartFile avatar) {
        String avatarUrl = null;
        if (info.name == null || info.description == null) {
            throw new BadRequestException("space name and description are required");
        }

        Pair<Message, Long> pair = spaceService.createSpace(user, info.name, info.description, avatar);

        return ApiResponseEntity.ok(new ApiResponse<>(pair.second(), pair.first()));
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

    @GetMapping("list")
    public ApiResponseEntity<PageResult<SpaceInfo>> getPopularSpaces(@AuthenticationPrincipal User user, 
    @RequestParam int page, 
    @RequestParam(required = false, defaultValue = "250") int size, 
    @RequestParam(defaultValue = "MEMBERCOUNT")PopularSpaceOrder order) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SpaceInfo> spaces = spaceService.getPopularSpaces(order, pageable);
        return ApiResponseEntity.ok(new ApiResponse<>(null,new PageResult<>(spaces)));
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
