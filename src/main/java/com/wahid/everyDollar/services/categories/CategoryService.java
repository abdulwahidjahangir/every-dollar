package com.wahid.everyDollar.services.categories;

import com.wahid.everyDollar.models.categories.Category;
import com.wahid.everyDollar.requests.catgories.CreateCategoryRequest;

import java.util.List;

public interface CategoryService {
    List<Category> getCategoriesByUserAndParentCategory(String email, Long parentId);

    List<Category> createCategoryForUser(String email, CreateCategoryRequest createCategoryRequest);

    Category updateCustomCategory(String userEmail, Long categoryId, String newCategoryName);

    void deleteCategory(String userEmail, Long categoryId);

    Category getCategoryIfItBelongsToTheUser(Long userId, Long categoryId);
}
