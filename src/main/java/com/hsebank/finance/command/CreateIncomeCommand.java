package com.hsebank.finance.command;

import com.hsebank.finance.domain.model.Operation;
import com.hsebank.finance.facade.OperationFacade;

import java.math.BigDecimal;

/**
 * Команда для создания дохода
 * Автоматически увеличивает баланс счёта
 */
public class CreateIncomeCommand implements Command {
    private final OperationFacade operationFacade;
    private final String bankAccountId;
    private final BigDecimal amount;
    private final String categoryId;
    private final String description;

    private Operation result;

    public CreateIncomeCommand(OperationFacade operationFacade,
                               String bankAccountId,
                               BigDecimal amount,
                               String categoryId,
                               String description) {
        this.operationFacade = operationFacade;
        this.bankAccountId = bankAccountId;
        this.amount = amount;
        this.categoryId = categoryId;
        this.description = description;
    }

    @Override
    public void execute() {
        result = operationFacade.createIncome(bankAccountId, amount, categoryId, description);
    }

    public Operation getResult() {
        return result;
    }
}