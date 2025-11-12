package com.hsebank.finance.command;

import com.hsebank.finance.domain.model.BankAccount;
import com.hsebank.finance.facade.BankAccountFacade;

import java.math.BigDecimal;

/**
 * Команда для создания банковского счёта
 */
public class CreateBankAccountCommand implements Command {
    private final BankAccountFacade accountFacade;
    private final String name;
    private final BigDecimal balance;

    private BankAccount result;

    public CreateBankAccountCommand(BankAccountFacade accountFacade,
                                    String name,
                                    BigDecimal balance) {
        this.accountFacade = accountFacade;
        this.name = name;
        this.balance = balance;
    }

    @Override
    public void execute() {
        result = accountFacade.createBankAccount(name, balance);
    }

    public BankAccount getResult() {
        return result;
    }
}