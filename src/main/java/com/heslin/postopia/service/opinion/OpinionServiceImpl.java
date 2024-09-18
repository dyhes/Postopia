package com.heslin.postopia.service.opinion;

import com.heslin.postopia.model.opinion.Opinion;
import com.heslin.postopia.repository.OpinionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpinionServiceImpl implements OpinionService {
    @Autowired
    private OpinionRepository opinionRepository;

    @Override
    public void saveOpinion(Opinion opinion) {
        opinionRepository.save(opinion);
    }
}
