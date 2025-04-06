package com.heslin.postopia.service.search;

import com.heslin.postopia.elasticsearch.model.SpaceDoc;
import com.heslin.postopia.elasticsearch.repository.SpaceDocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticSevice implements SearchService {
    private final SpaceDocRepository spaceDocRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    public ElasticSevice(SpaceDocRepository spaceDocRepository, ElasticsearchTemplate elasticsearchTemplate) {
        this.spaceDocRepository = spaceDocRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public Page<SpaceDoc> searchSpaces(String keyword, Pageable pageable) {
        return spaceDocRepository.matchSpaceDoc(keyword, pageable);
    }

    public void indexSpaces(List<SpaceDoc> docs) {
        List<IndexQuery> queries = docs.stream()
        .map(doc -> new IndexQueryBuilder().withObject(doc).build())
        .toList();
        elasticsearchTemplate.bulkIndex(queries, SpaceDoc.class);
    }
}
