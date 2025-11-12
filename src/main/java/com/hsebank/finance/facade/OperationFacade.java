package com.hsebank.finance.facade;

import com.hsebank.finance.domain.model.BankAccount;
import com.hsebank.finance.domain.model.Operation;
import com.hsebank.finance.domain.model.OperationType;
import com.hsebank.finance.factory.OperationFactory;
import com.hsebank.finance.repository.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Фасад для управления финансовыми операциями
 * Автоматически обновляет баланс счёта при создании доходов/расходов
 */
public class OperationFacade {
    private final OperationFactory operationFactory;
    private final Repository<Operation, String> operationRepository;
    private final Repository<BankAccount, String> accountRepository;

    public OperationFacade(
            OperationFactory operationFactory,
            Repository<Operation, String> operationRepository,
            Repository<BankAccount, String> accountRepository
    ) {
        this.operationFactory = operationFactory;
        this.operationRepository = operationRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Создаёт расход и автоматически уменьшает баланс счёта
     */
    public Operation createExpense(String bankAccountId,
                                   BigDecimal amount,
                                   String categoryId,
                                   String description) {
        Operation operation = operationFactory.create(
                OperationType.EXPENSE,
                bankAccountId,
                amount,
                categoryId
        );

        operation.setDescription(description);
        operationRepository.save(operation);
        updateAccountBalance(bankAccountId, amount.negate());

        return operation;
    }

    /**
     * Создаёт доход и автоматически увеличивает баланс счёта
     */
    public Operation createIncome(String bankAccountId,
                                  BigDecimal amount,
                                  String categoryId,
                                  String description) {
        Operation operation = operationFactory.create(
                OperationType.INCOME,
                bankAccountId,
                amount,
                categoryId
        );

        operation.setDescription(description);
        operationRepository.save(operation);
        updateAccountBalance(bankAccountId, amount);

        return operation;
    }

    /**
     * Обновляет баланс счёта на указанную дельту
     */
    private void updateAccountBalance(String accountId, BigDecimal delta) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Счёт с ID " + accountId + " не найден"
                ));

        BigDecimal newBalance = account.getBalance().add(delta);
        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    public Optional<Operation> getOperationById(String id) {
        return operationRepository.findById(id);
    }

    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

    public void deleteOperation(String id) {
        operationRepository.delete(id);
    }
}