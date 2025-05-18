package com.piotrgrochowiecki.manager.domain.service;

import com.piotrgrochowiecki.manager.domain.exception.DataLoaderServiceException;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.ports.DataLoaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class DataLoaderService {

    private final DataLoaderRepository dataLoaderRepository;

    @Transactional
    public DataLoaderModel update(DataLoaderModel dataLoaderModel) {
        return Optional.of(dataLoaderModel)
                .filter(it -> Objects.nonNull(it.getId()))
                .map(dataLoaderRepository::save)
                .orElseThrow(() -> new DataLoaderServiceException("Cannot update Data Loader as its id is null"));
    }

}
