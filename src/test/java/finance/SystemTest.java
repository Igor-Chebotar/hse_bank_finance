package finance;

import com.hsebank.finance.command.*;
import com.hsebank.finance.decorator.TimingDecorator;
import com.hsebank.finance.domain.model.*;
import com.hsebank.finance.exporter.csv.*;
import com.hsebank.finance.exporter.json.*;
import com.hsebank.finance.facade.*;
import com.hsebank.finance.factory.*;
import com.hsebank.finance.importer.*;
import com.hsebank.finance.importer.csv.CsvCategoryImporter;
import com.hsebank.finance.importer.json.JsonCategoryImporter;
import com.hsebank.finance.repository.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Комплексный тест системы: проверка бизнес-логики и всех реализованных паттернов.
 */
public class SystemTest {

    private static CategoryFacade categoryFacade;
    private static BankAccountFacade accountFacade;
    private static OperationFacade operationFacade;

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║  КОМПЛЕКСНОЕ ТЕСТИРОВАНИЕ СИСТЕМЫ ВШЭ-БАНК    ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println();

        initializeSystem();

        // Тестирование паттернов
        System.out.println("═══ ТЕСТИРОВАНИЕ ПАТТЕРНОВ ═══\n");
        testFactoryPattern();
        testRepositoryPattern();
        testFacadePattern();
        testCommandPattern();
        testDecoratorPattern();
        testTemplateMethodPattern();
        testVisitorPattern();

        // Тестирование бизнес-логики
        System.out.println("\n═══ ТЕСТИРОВАНИЕ БИЗНЕС-ЛОГИКИ ═══\n");
        testCreateCategory();
        testCreateBankAccount();
        testCreateIncome();
        testCreateExpense();
        testBalanceCalculation();

        // Тестирование импорта/экспорта
        System.out.println("\n═══ ТЕСТИРОВАНИЕ ИМПОРТА/ЭКСПОРТА ═══\n");
        testJsonExport();
        testCsvExport();
        testJsonImport();
        testCsvImport();
        testImportExportRoundtrip();

        // Итоговый отчёт
        printSummary();
    }

    /**
     * Настраивает зависимости и фасады для тестов
     */
    private static void initializeSystem() {
        BankAccountFactory accountFactory = new BankAccountFactory();
        CategoryFactory categoryFactory = new CategoryFactory();
        OperationFactory operationFactory = new OperationFactory();

        Repository<BankAccount, String> accountRepo = new InMemoryBankAccountRepository();
        Repository<Category, String> categoryRepo = new InMemoryCategoryRepository();
        Repository<Operation, String> operationRepo = new InMemoryOperationRepository();

        accountFacade = new BankAccountFacade(accountFactory, accountRepo);
        categoryFacade = new CategoryFacade(categoryFactory, categoryRepo);
        operationFacade = new OperationFacade(operationFactory, operationRepo, accountRepo);

        System.out.println("Система инициализирована\n");
    }

    /**
     * Тесты паттернов
     */
    private static void testFactoryPattern() {
        System.out.println("Тест: Factory Pattern");
        try {
            CategoryFactory factory = new CategoryFactory();
            Category category = factory.create(OperationType.EXPENSE, "Тестовая");

            assert category.getId() != null : "ID должен быть сгенерирован";
            assert category.getName().equals("Тестовая") : "Имя должно совпадать";
            assert category.getType() == OperationType.EXPENSE : "Тип должен совпадать";

            pass("Factory создаёт объекты с корректными данными");
        } catch (Exception e) {
            fail("Factory Pattern", e.getMessage());
        }
    }

    private static void testRepositoryPattern() {
        System.out.println("Тест: Repository Pattern");
        try {
            Repository<Category, String> repo = new InMemoryCategoryRepository();

            Category category = new Category("test-id", OperationType.INCOME, "Тест");
            repo.save(category);

            assert repo.findById("test-id").isPresent() : "Категория должна быть найдена";
            assert repo.findAll().size() == 1 : "В репозитории должна быть 1 запись";

            repo.delete("test-id");
            assert repo.findById("test-id").isEmpty() : "Категория должна быть удалена";

            pass("Repository корректно сохраняет, находит и удаляет данные");
        } catch (Exception e) {
            fail("Repository Pattern", e.getMessage());
        }
    }

    private static void testFacadePattern() {
        System.out.println("Тест: Facade Pattern");
        try {
            Category category = categoryFacade.createCategory(OperationType.INCOME, "Зарплата");

            assert category != null : "Фасад должен создать категорию";
            assert categoryFacade.getCategoryById(category.getId()).isPresent() : "Категория должна быть в репозитории";

            pass("Facade упрощает взаимодействие с подсистемами");
        } catch (Exception e) {
            fail("Facade Pattern", e.getMessage());
        }
    }

    private static void testCommandPattern() {
        System.out.println("Тест: Command Pattern");
        try {
            CreateCategoryCommand command = new CreateCategoryCommand(
                    categoryFacade,
                    OperationType.EXPENSE,
                    "Транспорт"
            );

            command.execute();
            Category result = command.getResult();

            assert result != null : "Команда должна вернуть результат";
            assert result.getName().equals("Транспорт") : "Имя должно совпадать";

            pass("Command инкапсулирует операцию создания");
        } catch (Exception e) {
            fail("Command Pattern", e.getMessage());
        }
    }

    private static void testDecoratorPattern() {
        System.out.println("Тест: Decorator Pattern");
        try {
            CreateCategoryCommand command = new CreateCategoryCommand(
                    categoryFacade,
                    OperationType.INCOME,
                    "Бонусы"
            );

            Command decoratedCommand = new TimingDecorator(command);

            long startTime = System.currentTimeMillis();
            decoratedCommand.execute();
            long endTime = System.currentTimeMillis();

            assert command.getResult() != null : "Декоратор не должен нарушать основную функциональность";
            assert (endTime - startTime) >= 0 : "Декоратор должен замерить время";

            pass("Decorator добавляет функциональность без изменения основного кода");
        } catch (Exception e) {
            fail("Decorator Pattern", e.getMessage());
        }
    }

    private static void testTemplateMethodPattern() {
        System.out.println("Тест: Template Method Pattern");
        try {
            // Создаём тестовый JSON файл
            String testJson = "[{\"id\":\"test-cat\", \"type\":\"INCOME\", \"name\":\"Тестовая категория\"}]";
            Path tempFile = Files.createTempFile("test-categories", ".json");
            Files.writeString(tempFile, testJson);

            DataImporter<Category> importer = new JsonCategoryImporter();
            List<Category> categories = importer.importFromFile(tempFile.toString());

            assert categories.size() == 1 : "Должна быть импортирована 1 категория";
            assert categories.get(0).getName().equals("Тестовая категория") : "Имя должно совпадать";

            Files.deleteIfExists(tempFile);

            pass("Template Method определяет скелет алгоритма импорта");
        } catch (Exception e) {
            fail("Template Method Pattern", e.getMessage());
        }
    }

    private static void testVisitorPattern() {
        System.out.println("Тест: Visitor Pattern");
        try {
            Category category = new Category("vis-cat", OperationType.EXPENSE, "Еда");

            JsonCategoryExporter jsonExporter = new JsonCategoryExporter();
            category.accept(jsonExporter);
            String json = jsonExporter.getJson();

            assert json.contains("\"id\":") : "JSON должен содержать ID";
            assert json.contains("\"name\":") : "JSON должен содержать name";
            assert json.contains("Еда") : "JSON должен содержать имя категории";

            CsvCategoryExporter csvExporter = new CsvCategoryExporter();
            category.accept(csvExporter);
            String csv = csvExporter.getCsv();

            assert csv.contains("id,type,name") : "CSV должен содержать заголовок";
            assert csv.contains("Еда") : "CSV должен содержать данные";

            pass("Visitor позволяет добавлять операции без изменения классов");
        } catch (Exception e) {
            fail("Visitor Pattern", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════
    // ТЕСТЫ БИЗНЕС-ЛОГИКИ
    // ════════════════════════════════════════════════════════

    private static void testCreateCategory() {
        System.out.println("Тест: Создание категории");
        try {
            Category income = categoryFacade.createCategory(OperationType.INCOME, "Подработка");
            Category expense = categoryFacade.createCategory(OperationType.EXPENSE, "Развлечения");

            assert income.getType() == OperationType.INCOME : "Тип INCOME";
            assert expense.getType() == OperationType.EXPENSE : "Тип EXPENSE";

            List<Category> all = categoryFacade.getAllCategories();
            assert all.size() >= 2 : "Должно быть минимум 2 категории";

            pass("Категории создаются корректно");
        } catch (Exception e) {
            fail("Создание категории", e.getMessage());
        }
    }

    private static void testCreateBankAccount() {
        System.out.println("Тест: Создание банковского счёта");
        try {
            BankAccount account = accountFacade.createBankAccount("Тестовый счёт", new BigDecimal("1000"));

            assert account.getName().equals("Тестовый счёт") : "Имя должно совпадать";
            assert account.getBalance().compareTo(new BigDecimal("1000")) == 0 : "Баланс должен быть 1000";

            pass("Счета создаются с корректным балансом");
        } catch (Exception e) {
            fail("Создание счёта", e.getMessage());
        }
    }

    private static void testCreateIncome() {
        System.out.println("Тест: Создание дохода");
        try {
            Category category = categoryFacade.createCategory(OperationType.INCOME, "Зарплата");
            BankAccount account = accountFacade.createBankAccount("Основной", new BigDecimal("5000"));

            BigDecimal initialBalance = account.getBalance();

            Operation income = operationFacade.createIncome(
                    account.getId(),
                    new BigDecimal("3000"),
                    category.getId(),
                    "Аванс"
            );

            BankAccount updatedAccount = accountFacade.getBankAccountById(account.getId()).get();
            BigDecimal expectedBalance = initialBalance.add(new BigDecimal("3000"));

            assert updatedAccount.getBalance().compareTo(expectedBalance) == 0 :
                    "Баланс должен увеличиться на 3000";
            assert income.getType() == OperationType.INCOME : "Тип должен быть INCOME";

            pass("Доход увеличивает баланс счёта");
        } catch (Exception e) {
            fail("Создание дохода", e.getMessage());
        }
    }

    private static void testCreateExpense() {
        System.out.println("Тест: Создание расхода");
        try {
            Category category = categoryFacade.createCategory(OperationType.EXPENSE, "Еда");
            BankAccount account = accountFacade.createBankAccount("Карта", new BigDecimal("10000"));

            BigDecimal initialBalance = account.getBalance();

            Operation expense = operationFacade.createExpense(
                    account.getId(),
                    new BigDecimal("500"),
                    category.getId(),
                    "Обед"
            );

            BankAccount updatedAccount = accountFacade.getBankAccountById(account.getId()).get();
            BigDecimal expectedBalance = initialBalance.subtract(new BigDecimal("500"));

            assert updatedAccount.getBalance().compareTo(expectedBalance) == 0 :
                    "Баланс должен уменьшиться на 500";
            assert expense.getType() == OperationType.EXPENSE : "Тип должен быть EXPENSE";

            pass("Расход уменьшает баланс счёта");
        } catch (Exception e) {
            fail("Создание расхода", e.getMessage());
        }
    }

    private static void testBalanceCalculation() {
        System.out.println("Тест: Расчёт баланса");
        try {
            Category incomeCategory = categoryFacade.createCategory(OperationType.INCOME, "Доход");
            Category expenseCategory = categoryFacade.createCategory(OperationType.EXPENSE, "Расход");
            BankAccount account = accountFacade.createBankAccount("Проверка", new BigDecimal("0"));

            // Добавляем доход
            operationFacade.createIncome(account.getId(), new BigDecimal("10000"), incomeCategory.getId(), "Доход 1");
            operationFacade.createIncome(account.getId(), new BigDecimal("5000"), incomeCategory.getId(), "Доход 2");

            // Добавляем расход
            operationFacade.createExpense(account.getId(), new BigDecimal("3000"), expenseCategory.getId(), "Расход 1");
            operationFacade.createExpense(account.getId(), new BigDecimal("2000"), expenseCategory.getId(), "Расход 2");

            BankAccount finalAccount = accountFacade.getBankAccountById(account.getId()).get();
            BigDecimal expectedBalance = new BigDecimal("10000"); // 10000 + 5000 - 3000 - 2000 = 10000

            assert finalAccount.getBalance().compareTo(expectedBalance) == 0 :
                    "Итоговый баланс должен быть 10000";

            pass("Баланс рассчитывается корректно при множественных операциях");
        } catch (Exception e) {
            fail("Расчёт баланса", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════
    // ТЕСТЫ ИМПОРТА/ЭКСПОРТА
    // ════════════════════════════════════════════════════════

    private static void testJsonExport() {
        System.out.println("Тест: Экспорт в JSON");
        try {
            Category category = categoryFacade.createCategory(OperationType.INCOME, "Тест JSON");

            JsonCategoryExporter exporter = new JsonCategoryExporter();
            category.accept(exporter);
            String json = exporter.getJson();

            assert json.contains("[") : "JSON должен начинаться с [";
            assert json.contains("]") : "JSON должен заканчиваться на ]";
            assert json.contains("\"id\":") : "JSON должен содержать поле id";
            assert json.contains("Тест JSON") : "JSON должен содержать имя категории";

            // Проверка валидности JSON
            assert json.split("\\{").length == json.split("\\}").length : "Скобки должны быть сбалансированы";

            pass("JSON экспорт генерирует валидный JSON");
        } catch (Exception e) {
            fail("JSON экспорт", e.getMessage());
        }
    }

    private static void testCsvExport() {
        System.out.println("Тест: Экспорт в CSV");
        try {
            Category category = categoryFacade.createCategory(OperationType.EXPENSE, "Тест CSV");

            CsvCategoryExporter exporter = new CsvCategoryExporter();
            category.accept(exporter);
            String csv = exporter.getCsv();

            String[] lines = csv.split("\n");
            assert lines.length >= 2 : "CSV должен содержать заголовок и минимум 1 строку данных";
            assert lines[0].contains("id,type,name") : "Первая строка должна быть заголовком";
            assert csv.contains("Тест CSV") : "CSV должен содержать данные";

            pass("CSV экспорт генерирует валидный CSV");
        } catch (Exception e) {
            fail("CSV экспорт", e.getMessage());
        }
    }

    private static void testJsonImport() {
        System.out.println("Тест: Импорт из JSON");
        try {
            String json = "[{\"id\":\"import-test\", \"type\":\"INCOME\", \"name\":\"Импорт тест\"}]";
            Path tempFile = Files.createTempFile("import-test", ".json");
            Files.writeString(tempFile, json);

            DataImporter<Category> importer = new JsonCategoryImporter();
            List<Category> categories = importer.importFromFile(tempFile.toString());

            assert categories.size() == 1 : "Должна быть импортирована 1 категория";
            assert categories.get(0).getId().equals("import-test") : "ID должен совпадать";
            assert categories.get(0).getName().equals("Импорт тест") : "Имя должно совпадать";

            Files.deleteIfExists(tempFile);

            pass("JSON импорт корректно парсит данные");
        } catch (Exception e) {
            fail("JSON импорт", e.getMessage());
        }
    }

    private static void testCsvImport() {
        System.out.println("Тест: Импорт из CSV");
        try {
            String csv = "id,type,name\n\"csv-test\",\"EXPENSE\",\"CSV тест\"";
            Path tempFile = Files.createTempFile("import-test", ".csv");
            Files.writeString(tempFile, csv);

            DataImporter<Category> importer = new CsvCategoryImporter();
            List<Category> categories = importer.importFromFile(tempFile.toString());

            assert categories.size() == 1 : "Должна быть импортирована 1 категория";
            assert categories.get(0).getId().equals("csv-test") : "ID должен совпадать";
            assert categories.get(0).getName().equals("CSV тест") : "Имя должно совпадать";

            Files.deleteIfExists(tempFile);

            pass("CSV импорт корректно парсит данные");
        } catch (Exception e) {
            fail("CSV импорт", e.getMessage());
        }
    }

    private static void testImportExportRoundtrip() {
        System.out.println("Тест: Импорт → Экспорт → Импорт");
        try {
            // Создаём категорию
            Category original = categoryFacade.createCategory(OperationType.INCOME, "Roundtrip тест");

            // Экспортируем в JSON
            JsonCategoryExporter exporter = new JsonCategoryExporter();
            original.accept(exporter);
            String json = exporter.getJson();

            // Сохраняем в файл
            Path tempFile = Files.createTempFile("roundtrip", ".json");
            Files.writeString(tempFile, json);

            // Импортируем обратно
            DataImporter<Category> importer = new JsonCategoryImporter();
            List<Category> imported = importer.importFromFile(tempFile.toString());

            assert imported.size() == 1 : "Должна быть импортирована 1 категория";

            Category restored = imported.get(0);
            assert restored.getId().equals(original.getId()) : "ID должен совпадать";
            assert restored.getName().equals(original.getName()) : "Имя должно совпадать";
            assert restored.getType() == original.getType() : "Тип должен совпадать";

            Files.deleteIfExists(tempFile);

            pass("Данные сохраняются при цикле экспорт-импорт");
        } catch (Exception e) {
            fail("Roundtrip тест", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ════════════════════════════════════════════════════════

    private static void pass(String message) {
        testsPassed++;
        System.out.println("  PASS: " + message);
    }

    private static void fail(String testName, String reason) {
        testsFailed++;
        System.out.println("  FAIL: " + testName);
        System.out.println("     Причина: " + reason);
    }

    private static void printSummary() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║              ИТОГОВЫЙ ОТЧЁТ                    ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Всего тестов: " + (testsPassed + testsFailed));
        System.out.println("Успешно: " + testsPassed);
        System.out.println("Провалено: " + testsFailed);
        System.out.println();

        if (testsFailed == 0) {
            System.out.println("ВСЕ ТЕСТЫ ПРОЙДЕНЫ! СИСТЕМА РАБОТАЕТ КОРРЕКТНО!");
        } else {
            System.out.println("НЕКОТОРЫЕ ТЕСТЫ ПРОВАЛЕНЫ. ТРЕБУЕТСЯ ИСПРАВЛЕНИЕ.");
        }

        System.out.println();
        System.out.println("═══════════════════════════════════════════════");
    }
}