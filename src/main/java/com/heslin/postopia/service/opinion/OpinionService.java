package com.heslin.postopia.service.opinion;


import com.heslin.postopia.model.opinion.Opinion;

public interface OpinionService {
    void upsertOpinion(Opinion opinion);
}
