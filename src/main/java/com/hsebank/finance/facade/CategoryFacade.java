package com.hsebank.finance.facade;

import com.hsebank.finance.domain.model.Category;
import com.hsebank.finance.domain.model.OperationType;
import com.hsebank.finance.factory.CategoryFactory;
import com.hsebank.finance.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Фасад для управления категориями
 * Упрощает работу клиента, скрывая взаимодействие фабрики и репозитория
 */
public class CategoryFacade {
    private final CategoryFactory categoryFactory;
    private final Repository<Category, String> categoryRepository;

    public CategoryFacade(CategoryFactory categoryFactory,
                          Repository<Category, String> categoryRepository) {
        this.categoryFactory = categoryFactory;
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(OperationType type, String name) {
        Category category = categoryFactory.create(type, name);

        categoryRepository.save(category);

        return category;
    }

    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public void updateCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }

        categoryRepository.save(category);
    }

    public void deleteCategory(String id) {
        categoryRepository.delete(id);
    }
}
