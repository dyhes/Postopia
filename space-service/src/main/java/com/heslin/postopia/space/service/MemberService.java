package com.heslin.postopia.space.service;

import com.heslin.postopia.space.model.MemberLog;
import com.heslin.postopia.space.repository.ForbiddenRepository;
import com.heslin.postopia.space.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MemberService {
    private final ForbiddenRepository forbiddenRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(ForbiddenRepository forbiddenRepository, MemberRepository memberRepository) {
        this.forbiddenRepository = forbiddenRepository;
        this.memberRepository = memberRepository;
    }

    public void joinSpace(String username, Long userId, Long spaceId) {
        Instant f = forbiddenRepository.findByMemberLog(userId, spaceId);
        if (f != null) {
            throw new RuntimeException(f.toString());
        }
        MemberLog memberLog = MemberLog.builder().userId(userId).spaceId(spaceId).username(username).build();
        try {
            memberRepository.save(memberLog);
        } catch (DataIntegrityViolationException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("用户已加入空间");
        }
    }

    public boolean leaveSpace(Long userId, Long spaceId) {
        return memberRepository.deleteBySpaceIdAndUserId(userId, spaceId) > 1;
    }

    public Page<MemberLog> searchByPrefix(Long spaceId, String prefix, Pageable pageable) {
        return memberRepository.findBySpaceIdAndUsernameStartingWith(spaceId, prefix, pageable);
    }
}
