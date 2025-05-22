package com.heslin.postopia.vote.feign;

import com.heslin.postopia.space.dto.VoteSpaceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("space-service")
public interface SpaceFeign {
    @PostMapping("/space/expel")
    void expelUser(@RequestParam Long spaceId, @RequestParam Long userId, @RequestParam String reason);

    @PostMapping("/space/mute")
    void muteUser(@RequestParam Long spaceId, @RequestParam Long userId, @RequestParam String reason);

    @PostMapping("/space/update")
    void updateInfo(@RequestParam Long spaceId, @RequestParam String description, @RequestParam String avatar);

    @GetMapping("/space/info/vote")
    Pair<Boolean, VoteSpaceInfo> checkMemberForVote(@RequestParam Long spaceId, @RequestParam Long userId);
}
