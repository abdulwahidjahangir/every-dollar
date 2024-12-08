package com.wahid.everyDollar.controllers.categories;

import com.wahid.everyDollar.models.categories.Category;
import com.wahid.everyDollar.requests.catgories.CreateCategoryRequest;
import com.wahid.everyDollar.requests.catgories.UpdateCategoryRequest;
import com.wahid.everyDollar.responses.categories.CategoryResponse;
import com.wahid.everyDollar.security.service.UserDetailsImpl;
import com.wahid.everyDollar.services.categories.CategoryService;
import com.wahid.everyDollar.services.expenses.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/categories")
    public ResponseEntity<?> getMyCategories(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "parent-id", defaultValue = "0") Long parentId,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to
    ) {
        List<Category> categories = categoryService.getCategoriesByUserAndParentCategory(userDetails.getEmail(), parentId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime lastDayOfMonth = now.withDayOfMonth(now.getMonth().length(now.toLocalDate().isLeapYear()))
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        LocalDateTime fromDate = (from != null && !from.isEmpty()) ? LocalDateTime.parse(from + "T00:00:00") : firstDayOfMonth;
        LocalDateTime toDate = (to != null && !to.isEmpty()) ? LocalDateTime.parse(to + "T23:59:59") : lastDayOfMonth;

        List<Long> categoryIds = categories.stream()
                .map(Category::getId)
                .toList();
        Map<Long, BigDecimal> categoriesData = expenseService.getSumByCategories(categoryIds, userDetails.getId(), fromDate, toDate);

        List<CategoryResponse> categoryResponses = categories.stream()
                .map(category -> convertCategoryIntoCategoryResponse(category, categoriesData))
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryResponses);
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateCategoryRequest categoryRequest
    ) {
        List<Category> categories = categoryService.createCategoryForUser(userDetails.getEmail(), categoryRequest);

        return ResponseEntity.ok("New Category successfully created");
    }

    @PutMapping("/categories")
    public ResponseEntity<?> updateCategory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateCategoryRequest categoryRequest,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to
    ) {
        Category category = categoryService
                .updateCustomCategory(userDetails.getEmail(), categoryRequest.getId(), categoryRequest.getName());

        return ResponseEntity.ok("Category has been successfully updated");
    }

    @DeleteMapping("/categories")
    public ResponseEntity<?> deleteCategory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestParam("category-id") @NotNull(message = "Category id can not be null") Long categoryId
    ) {
        categoryService
                .deleteCategory(userDetails.getEmail(), categoryId);

        return ResponseEntity.ok("Category successfully deleted");
    }

    private CategoryResponse convertCategoryIntoCategoryResponse(Category category, Map<Long, BigDecimal> categoriesData) {
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(category.getId());
        categoryResponse.setName(category.getName());
        categoryResponse.setTotal(categoriesData.getOrDefault(category.getId(), new BigDecimal("0.0")));
        return categoryResponse;
    }
}
