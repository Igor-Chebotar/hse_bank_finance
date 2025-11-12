package com.hsebank.finance.facade;

import com.hsebank.finance.domain.model.BankAccount;
import com.hsebank.finance.factory.BankAccountFactory;
import com.hsebank.finance.repository.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Фасад для управления банковскими счетами
 * Упрощает работу клиента, скрывая взаимодействие фабрики и репозитория
 */
public class BankAccountFacade {
    private final BankAccountFactory accountFactory;
    private final Repository<BankAccount, String> accountRepository;

    public BankAccountFacade(BankAccountFactory accountFactory,
                             Repository<BankAccount, String> accountRepository) {
        this.accountFactory = accountFactory;
        this.accountRepository = accountRepository;
    }

    public BankAccount createBankAccount(String name, BigDecimal balance) {
        BankAccount bankAccount = accountFactory.create(name, balance);

        accountRepository.save(bankAccount);

        return bankAccount;
    }

    public Optional<BankAccount> getBankAccountById(String id) {
        return accountRepository.findById(id);
    }

    public List<BankAccount> getAllBankAccounts() {
        return accountRepository.findAll();
    }

    public void updateBankAccount(BankAccount bankAccount) {
        if (bankAccount.getName() == null || bankAccount.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название счета не может быть пустым");
        }

        accountRepository.save(bankAccount);
    }

    public void deleteBankAccount(String id) {
        accountRepository.delete(id);
    }

}
