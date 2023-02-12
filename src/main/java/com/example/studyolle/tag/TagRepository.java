package com.example.studyolle.tag;

import com.example.studyolle.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long>{

    Tag findByTitle(String tagTitle);

}

