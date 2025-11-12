package com.hsebank.finance.command;

import com.hsebank.finance.domain.model.Category;
import com.hsebank.finance.domain.model.OperationType;
import com.hsebank.finance.facade.CategoryFacade;

/**
 * Команда для создания категории
 */
public class CreateCategoryCommand implements Command {
    private final CategoryFacade categoryFacade;
    private final OperationType type;
    private final String name;

    private Category result;

    public CreateCategoryCommand(CategoryFacade categoryFacade,
                                 OperationType type,
                                 String name) {
        this.categoryFacade = categoryFacade;
        this.type = type;
        this.name = name;
    }

    @Override
    public void execute() {
        result = categoryFacade.createCategory(type, name);
    }

    public Category getResult() {
        return result;
    }
}