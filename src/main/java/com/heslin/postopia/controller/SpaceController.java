package com.heslin.postopia.controller;

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
import com.heslin.postopia.dto.pageresult.PageResult;
import com.heslin.postopia.dto.response.ApiResponse;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.enums.JoinedSpaceOrder;
import com.heslin.postopia.enums.PopularSpaceOrder;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.model.User;
import com.heslin.postopia.service.space.SpaceService;
import com.heslin.postopia.util.Pair;


@RestController
@RequestMapping("/space")
public class SpaceController {
    @Autowired
    private SpaceService spaceService;

    public record SpaceDto(String name, String description) {}

    @PostMapping("create")
    public ApiResponseEntity<Long> createSpace(@AuthenticationPrincipal User user, @RequestPart("info") SpaceDto info, @RequestPart(name = "avatar", required = false) MultipartFile avatar) {
        String avatarUrl = null;
        if (info.name == null || info.description == null) {
            throw new BadRequestException("space name and description are required");
        }

        if (avatar != null) {
            // object storage
        }

        Pair<Message, Long> pair = spaceService.createSpace(user, info.name, info.description, avatarUrl);

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
    public ApiResponseEntity<PageResult<SpaceInfo>> getSpaces(@AuthenticationPrincipal User user, 
    @RequestParam int page, 
    @RequestParam(required = false, defaultValue = "250") int size, 
    @RequestParam(defaultValue = "LASTACTIVE")JoinedSpaceOrder order,
    @RequestParam(defaultValue = "DESC") Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, order.getField()));
        Page<SpaceInfo> spaces = spaceService.getSpacesByUserId(user.getId(), pageable);
        return ApiResponseEntity.ok(
            new ApiResponse<>(null, 
                                        new PageResult<>(spaces.getContent(),
                                                    spaces.getNumber() + 1,
                                                    spaces.getTotalPages())
                )
            );
    }
    
    @GetMapping("popular")
    public ApiResponseEntity<PageResult<SpaceInfo>> getPopularSpaces(@AuthenticationPrincipal User user, 
    @RequestParam int page, 
    @RequestParam(required = false, defaultValue = "250") int size, 
    @RequestParam(defaultValue = "MEMBERCOUNT")PopularSpaceOrder order) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SpaceInfo> spaces = spaceService.getPopularSpaces(order, pageable);
        return ApiResponseEntity.ok(
            new ApiResponse<>(null, 
                                        new PageResult<>(spaces.getContent(),
                                                    spaces.getNumber() + 1,
                                                    spaces.getTotalPages())
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
