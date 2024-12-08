package com.wahid.everyDollar.repositories.categories;

import com.wahid.everyDollar.models.categories.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c WHERE c.userId.id = :userId AND c.parent.id = :parentId")
    List<Category> findCategoriesByUserIdAndParentId(Long userId, Long parentId);

    @Query("SELECT c FROM Category c WHERE (c.userId.id = :userId OR c.type = 'DEFAULT') AND c.parent IS NULL")
    List<Category> findByUserOrDefault(Long userId);

    @Query("SELECT c FROM Category c WHERE c.userId.id = :userId AND c.parent.id = :parentId")
    List<Category> findByUserAndParent(Long userId, Long parentId);

    @Query("SELECT c FROM Category c WHERE c.userId.id = :userId AND c.id = :categoryId")
    Optional<Category> findByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("SELECT c FROM Category c WHERE c.userId.id = :userId AND c.parent.id = :parentId AND c.name=:name")
    Optional<Category> findByUserIdAndParentAndName(Long userId, Long parentId, String name);

    @Query("SELECT c FROM Category c WHERE (c.userId.id = :userId OR c.type = 'DEFAULT') AND c.id = :categoryId ")
    Optional<Category> findByUserIdAndCategoryIdOrDefault(Long userId, Long categoryId);
}
