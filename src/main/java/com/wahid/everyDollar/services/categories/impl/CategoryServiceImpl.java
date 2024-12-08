package com.wahid.everyDollar.services.categories.impl;

import com.wahid.everyDollar.errors.CustomException;
import com.wahid.everyDollar.models.categories.Category;
import com.wahid.everyDollar.models.categories.CategoryType;
import com.wahid.everyDollar.models.users.User;
import com.wahid.everyDollar.repositories.categories.CategoryRepository;
import com.wahid.everyDollar.requests.catgories.CreateCategoryRequest;
import com.wahid.everyDollar.services.categories.CategoryService;
import com.wahid.everyDollar.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@EnableAsync
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    @Override
    public List<Category> getCategoriesByUserAndParentCategory(String email, Long parentId) {
        User currentUser = userService.getUser(email);

        List<Category> categories = new ArrayList<>();
        if (parentId == 0) {
            categories = categoryRepository.findByUserOrDefault(currentUser.getId());
        } else {
            categories = categoryRepository.findCategoriesByUserIdAndParentId(currentUser.getId(), parentId);
        }

        return categories;
    }

    @Override
    public List<Category> createCategoryForUser(String email, CreateCategoryRequest createCategoryRequest) {
        createCategoryRequest.setName(createCategoryRequest.getName().toLowerCase().trim());
        if (!validCategoryName(createCategoryRequest.getName())) {
            throw new CustomException("Category name length must between 2 - 30 characters", HttpStatus.BAD_REQUEST);
        }

        User currentUser = userService.getUser(email);

        List<Category> categoriesByUserAndParent;
        Category parentCategoryForNewCategory = null;
        if (createCategoryRequest.getParentId() == 0) {
            categoriesByUserAndParent = categoryRepository.findByUserOrDefault(currentUser.getId());
        } else {
            parentCategoryForNewCategory = categoryRepository.findById(createCategoryRequest.getParentId())
                    .orElseThrow(() -> new CustomException("Invalid Category Id", HttpStatus.BAD_REQUEST));
            if (parentCategoryForNewCategory.getType() == CategoryType.CUSTOM &&
                    !Objects.equals(parentCategoryForNewCategory.getUserId().getId(), currentUser.getId())) {
                throw new CustomException("Invalid Category Id", HttpStatus.BAD_REQUEST);
            }
            categoriesByUserAndParent = categoryRepository.findByUserAndParent(currentUser.getId(), createCategoryRequest.getParentId());
        }

        // Ensure there are no more than 10 subcategories for a parent category
        if (categoriesByUserAndParent.size() >= 10) {
            String errorMessage = (createCategoryRequest.getParentId() == 0) ?
                    "You cannot have more than 10 categories on root level." :
                    "A category cannot have more than 10 subcategories.";
            throw new CustomException(errorMessage, HttpStatus.BAD_REQUEST);
        }

        // Check if a category with the same name already exists for the user and parent
        if (this.findCategoryByUserIdCategoryNameAndParentId(
                currentUser.getId(), createCategoryRequest.getParentId(), createCategoryRequest.getName()).isPresent()) {
            throw new CustomException("A category with the same name already exists", HttpStatus.BAD_REQUEST);
        }

        // Ensure the category hierarchy does not exceed 3 levels
        if (parentCategoryForNewCategory != null && parentCategoryForNewCategory.getLevel() >= 3) {
            throw new CustomException("A category cannot go deeper than 3 levels", HttpStatus.BAD_REQUEST);
        }

        // Create and save the new category
        Category categoryToCreate = new Category();
        categoryToCreate.setName(createCategoryRequest.getName());
        categoryToCreate.setParent(parentCategoryForNewCategory);
        categoryToCreate.setUserId(currentUser);
        if (parentCategoryForNewCategory == null) {
            categoryToCreate.setLevel(1);
        } else {
            categoryToCreate.setLevel(parentCategoryForNewCategory.getLevel() + 1);
        }
        categoryToCreate.setType(CategoryType.CUSTOM);

        categoryToCreate = categoryRepository.save(categoryToCreate);

        // Return the updated list of categories for this user and parent
        categoriesByUserAndParent.add(categoryToCreate);
        return categoriesByUserAndParent;
    }

    @Override
    public Category updateCustomCategory(String userEmail, Long categoryId, String newCategoryName) {
        newCategoryName = newCategoryName.toLowerCase().trim();
        if (!validCategoryName(newCategoryName)) {
            throw new CustomException("Category name length must between 2 - 30 characters", HttpStatus.BAD_REQUEST);
        }

        User user = userService.getUser(userEmail);

        Category currCategory = categoryRepository.findByUserIdAndCategoryId(user.getId(), categoryId)
                .orElseThrow(() -> new CustomException("Invalid category data", HttpStatus.BAD_REQUEST));

        currCategory.setName(newCategoryName);

        try {
            currCategory = categoryRepository.save(currCategory);
        } catch (Exception e) {
            System.out.println("Error while updating category name");
            e.printStackTrace();

            throw new CustomException("Error while updating category", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return currCategory;
    }

    @Override
    public void deleteCategory(String userEmail, Long categoryId) {
        User user = userService.getUser(userEmail);

        List<Category> categories = categoryRepository.findCategoriesByUserIdAndParentId(user.getId(), categoryId);

        if (!categories.isEmpty()) {
            throw new CustomException("You can not delete a category with sub-categories", HttpStatus.BAD_REQUEST);
        }

        Category currCategory = categoryRepository.findByUserIdAndCategoryId(user.getId(), categoryId)
                .orElseThrow(() -> new CustomException("Invalid category data", HttpStatus.BAD_REQUEST));

        try {
            categoryRepository.delete(currCategory);
        } catch (Exception e) {
            System.out.println("Error while deleting category");
            e.printStackTrace();

            throw new CustomException("Error while deleting category", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Category getCategoryIfItBelongsToTheUser(Long userId, Long categoryId) {
        return categoryRepository.findByUserIdAndCategoryIdOrDefault(userId, categoryId)
                .orElseThrow(() -> new CustomException("Invalid category id", HttpStatus.BAD_REQUEST));
    }

    private boolean validCategoryName(String name) {
        return name.length() >= 2 && name.length() < 30;
    }

    private Optional<Category> findCategoryByUserIdCategoryNameAndParentId(Long userId, Long parentId, String name) {
        return categoryRepository.findByUserIdAndParentAndName(userId, parentId, name);
    }
}
