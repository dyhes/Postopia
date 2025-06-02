package com.heslin.postopia.vote.service;

import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.vote.feign.OpinionFeign;
import com.heslin.postopia.vote.model.CommonVote;
import com.heslin.postopia.vote.model.SpaceVote;
import com.heslin.postopia.vote.model.Vote;
import com.heslin.postopia.vote.repository.CommonVoteRepository;
import com.heslin.postopia.vote.repository.SpaceVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

@Service
public class ActionService {
    private final OpinionFeign opinionFeign;
    private final CommonVoteRepository commonVoteRepository;
    private final SpaceVoteRepository spaceVoteRepository;
    private final KafkaService kafkaService;

    @Autowired
    public ActionService(OpinionFeign opinionFeign, CommonVoteRepository commonVoteRepository, SpaceVoteRepository spaceVoteRepository, KafkaService kafkaService) {
        this.opinionFeign = opinionFeign;
        this.commonVoteRepository = commonVoteRepository;
        this.spaceVoteRepository = spaceVoteRepository;
        this.kafkaService = kafkaService;
    }

    @Transactional
    protected void scheduledAction(boolean isCommon, Long voteId, String voteActionMessage, String relatedUserMessage, Function<Long, Void> voteAction) {
        Vote vote = isCommon? commonVoteRepository.findById(voteId).orElseThrow() : spaceVoteRepository.findById(voteId).orElseThrow();
        System.out.println("here");
        System.out.println(vote.getId());
        String tail;
        if (vote.isFulfilled()) {
            voteAction.apply(vote.getRelatedEntity());
            tail = "的投票已成功通过";
            if (relatedUserMessage != null) {
                kafkaService.sendMessage(vote.getRelatedUser(), relatedUserMessage);
            }
        } else {
            tail = "的投票未通过, 赞成人数 %d，反对人数 %d, 所需最少投票人数 %d".formatted(vote.getPositiveCount(), vote.getNegativeCount(), vote.getThreshold());
        }
        kafkaService.sendMessage(vote.getInitiator(), "您发起的%s%s".formatted(voteActionMessage, tail));
        opinionFeign.notifyVoter(voteId, "您%s的" + voteActionMessage + tail);
        if (vote instanceof CommonVote) {
            commonVoteRepository.delete((CommonVote) vote);
        } else {
            spaceVoteRepository.delete((SpaceVote) vote);
        }
    }
}
