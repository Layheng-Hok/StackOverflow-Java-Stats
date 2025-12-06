package com.sustech.so_java_stats.service.impl;

import com.sustech.so_java_stats.repository.TagRepository;
import com.sustech.so_java_stats.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean checkTagExists(String tagName) {
        return tagRepository.existsById(tagName);
    }
}
