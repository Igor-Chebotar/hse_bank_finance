package finance;

import com.hsebank.finance.di.DIContainer;
import com.hsebank.finance.di.annotations.Inject;
import com.hsebank.finance.factory.CategoryFactory;
import com.hsebank.finance.repository.InMemoryCategoryRepository;

/**
 * Простое тестирование работы DI-контейнера:
 * - создание объектов
 * - singleton-поведение
 * - автоматическое внедрение зависимостей
 */
public class DIContainerTest {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║       ТЕСТ DI-КОНТЕЙНЕРА                       ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        testBasicDI();
        testSingletonBehavior();
        testDependencyInjection();

        System.out.println("\nВСЕ ТЕСТЫ DI-КОНТЕЙНЕРА ПРОЙДЕНЫ!");
    }

    /**
     * Проверяет базовую регистрацию и разрешение зависимостей.
     */
    private static void testBasicDI() {
        System.out.println("Тест 1: Базовая работа контейнера");

        DIContainer container = new DIContainer();
        container.register(CategoryFactory.class);

        CategoryFactory factory = container.resolve(CategoryFactory.class);
        assert factory != null : "Контейнер должен создать экземпляр";

        System.out.println("  Контейнер создаёт объекты\n");
    }

    /**
     * Проверяет, что контейнер возвращает singleton-экземпляры.
     */
    private static void testSingletonBehavior() {
        System.out.println("Тест 2: Singleton поведение");

        DIContainer container = new DIContainer();
        container.register(InMemoryCategoryRepository.class);

        InMemoryCategoryRepository repo1 = container.resolve(InMemoryCategoryRepository.class);
        InMemoryCategoryRepository repo2 = container.resolve(InMemoryCategoryRepository.class);

        assert repo1 == repo2 : "Контейнер должен возвращать один экземпляр";

        System.out.println("  Контейнер реализует Singleton паттерн\n");
    }

    /**
     * Проверяет автоматическое внедрение зависимостей в конструктор.
     */
    private static void testDependencyInjection() {
        System.out.println("Тест 3: Автоматическое внедрение зависимостей");

        DIContainer container = new DIContainer();

        container.register(ServiceA.class);
        container.register(ServiceB.class);
        container.register(ServiceC.class);

        ServiceC serviceC = container.resolve(ServiceC.class);

        assert serviceC != null : "ServiceC должен быть создан";
        assert serviceC.getServiceA() != null : "ServiceA должен быть внедрён";
        assert serviceC.getServiceB() != null : "ServiceB должен быть внедрён";

        System.out.println("  Зависимости внедряются автоматически\n");
    }

    // Вспомогательные классы для демонстрации внедрения зависимостей

    public static class ServiceA {
        public ServiceA() {
            System.out.println("    → Создан ServiceA");
        }
    }

    public static class ServiceB {
        public ServiceB() {
            System.out.println("    → Создан ServiceB");
        }
    }

    public static class ServiceC {
        private final ServiceA serviceA;
        private final ServiceB serviceB;

        @Inject
        public ServiceC(ServiceA serviceA, ServiceB serviceB) {
            this.serviceA = serviceA;
            this.serviceB = serviceB;
            System.out.println("    → Создан ServiceC с внедрёнными зависимостями");
        }

        public ServiceA getServiceA() { return serviceA; }
        public ServiceB getServiceB() { return serviceB; }
    }
}
