package com.wahid.everyDollar.utils;

import com.wahid.everyDollar.models.categories.Category;
import com.wahid.everyDollar.models.categories.CategoryType;
import com.wahid.everyDollar.models.users.AppRole;
import com.wahid.everyDollar.models.users.Role;
import com.wahid.everyDollar.repositories.categories.CategoryRepository;
import com.wahid.everyDollar.repositories.users.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultDataInitializer implements CommandLineRunner {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Initialize Default Categories
            createCategoryIfNotExists("food");
            createCategoryIfNotExists("housing");
            createCategoryIfNotExists("transport");
            createCategoryIfNotExists("health");
            createCategoryIfNotExists("entertainment");

            // Initialize Default Roles
            createRoleIfNotExists(AppRole.ROLE_USER);
            createRoleIfNotExists(AppRole.ROLE_ADMIN);

        } catch (Exception e) {
            System.out.println("Error While Creating default data");
        }
    }

    private void createCategoryIfNotExists(String categoryName) {
        if (categoryRepository.findByName(categoryName).isEmpty()) {
            Category category = new Category();
            category.setName(categoryName);
            category.setType(CategoryType.DEFAULT);
            category.setParent(null);
            category.setUserId(null);
            category.setLevel(0);
            categoryRepository.save(category);
        }
    }

    private void createRoleIfNotExists(AppRole roleName) {
        roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

}
