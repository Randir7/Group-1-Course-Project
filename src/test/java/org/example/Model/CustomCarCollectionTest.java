package org.example.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomCarCollectionTest {

    // Наша коллекция для тестирования
    private CustomCarCollection collection;

    // Вспомогательный метод для быстрого создания машины в тестах
    // Вынесли сюда, чтобы не писать длинный код Builder'а в каждом тесте
    private Car createCar(String brand, String model, int speed, int price) {
        return new Car.CarBuilder()
                .setBrandName(brand)
                .setModelName(model)
                .setMaxSpeed(speed)
                .setPrice(price)
                .build();
    }

    /**
     * Этот метод будет выполняться ПЕРЕД КАЖДЫМ тестовым методом (@Test).
     * Благодаря этому каждый тест начинается с "чистого листа" — новой пустой коллекции.
     * Это гарантирует, что тесты не влияют друг на друга.
     */
    @BeforeEach
    void setUp() {
        collection = new CustomCarCollection();
    }

    // =========================================================================
    // 1. ТЕСТИРОВАНИЕ БАЗОВЫХ ОПЕРАЦИЙ (add, get, size, clear, toList)
    // =========================================================================

    @Test
    @DisplayName("Добавление одного элемента и проверка size()")
    void testAddSingleElement() {
        // Изначально коллекция пуста
        assertEquals(0, collection.size());

        Car car = createCar("Toyota", "Camry", 180, 25000);
        collection.add(car);

        assertEquals(1, collection.size());
        // Проверяем, что именно тот объект вернулся по индексу 0
        assertEquals(car, collection.get(0));
    }

    @Test
    @DisplayName("Добавление 11 элементов для проверки расширения массива (grow)")
    void testGrowArrayCapacity() {
        // Начальная capacity (вместимость) массива = 10.
        // Если мы добавим 11 элементов, массив должен увеличиться внутри себя без ошибок.
        for (int i = 0; i < 11; i++) {
            collection.add(createCar("Brand" + i, "Model" + i, 100 + i, 1000 + i));
        }

        assertEquals(11, collection.size());
        // Проверяем первый и последний элементы
        assertEquals("Brand0", collection.get(0).getBrandName());
        assertEquals("Brand10", collection.get(10).getBrandName());
    }

    @Test
    @DisplayName("Получение элемента по неверному индексу выбрасывает исключение")
    void testGetInvalidIndex() {
        collection.add(createCar("A", "B", 100, 100));

        // Индекс отрицательный
        assertThrows(IndexOutOfBoundsException.class, () -> collection.get(-1));
        // Индекс равен размеру (выход за границу)
        assertThrows(IndexOutOfBoundsException.class, () -> collection.get(1));
    }

    @Test
    @DisplayName("Очистка коллекции (clear)")
    void testClearCollection() {
        collection.add(createCar("A", "B", 100, 100));
        collection.add(createCar("C", "D", 200, 200));
        assertEquals(2, collection.size());

        collection.clear();

        assertEquals(0, collection.size());
        assertTrue(collection.toList().isEmpty()); // Список должен быть пуст
    }

    @Test
    @DisplayName("Преобразование в стандартный List (toList)")
    void testToList() {
        Car car1 = createCar("Audi", "A8", 250, 90000);
        Car car2 = createCar("BMW", "X5", 210, 50000);
        collection.add(car1);
        collection.add(car2);

        List<Car> list = collection.toList();

        // Проверяем, что размер списка совпадает
        assertEquals(2, list.size());
        // Проверяем порядок элементов
        assertEquals(car1, list.get(0));
        assertEquals(car2, list.get(1));
    }

    // =========================================================================
    // 2. ТЕСТИРОВАНИЕ СОРТИРОВОК
    // =========================================================================

    // Тест сортировки по нескольким ключам (кнопка "Общая сортировка")
    @Test
    @DisplayName("Общая сортировка (multikey) проверяет цепочку сравнений")
    void testMultikeySort() {
        // После сортировки авто должен быть ПЕРВЫМ (скорость меньше)
        Car firstPos = createCar("Audi", "A8", 200, 80000);
        Car secondPos = createCar("Audi", "A8", 250, 90000);
        // Марка та же, модель другая
        Car thirdPos = createCar("Audi", "Q7", 220, 70000);
        // Должен встать ПОСЛЕ всех Audi
        Car lastPos = createCar("BMW", "X5", 210, 50000);

        // Создаем ситуацию, где марки одинаковые, модели одинаковые, отличается только скорость
        collection.add(firstPos);
        collection.add(secondPos);
        collection.add(thirdPos);
        collection.add(lastPos);

        collection.multikeySort();

        List<Car> sorted = collection.toList();

        // 1. Проверяем, что Audi A8 со скоростью 200 на первом месте
        assertEquals(firstPos, sorted.get(0));

        // 2. Проверяем, что Audi A8 со скоростью 250 на втором месте
        assertEquals(secondPos, sorted.get(1));

        // 3. Проверяем, что Audi Q7 на третьем месте
        assertEquals(thirdPos, sorted.get(2));

        // 4. Проверяем, что BMW на самом последнем месте
        assertEquals("BMW", sorted.get(3).getBrandName());
    }



    @Test
    @DisplayName("Сортировка пустой коллекции не вызывает ошибок")
    void testSortEmptyCollection() {
        // Очень важный краевой тест! Сортировка пустого массива часто вызывает ArrayIndexOutOfBounds
        // В нашей реализации quickSort есть проверка "if (size > 1)", поэтому тест должен пройти гладко.
        assertEquals(0, collection.size());
        assertDoesNotThrow(() -> collection.sortByBrandAsc());
        assertDoesNotThrow(() -> collection.multikeySort());
    }

    // =========================================================================
    // ТЕСТИРОВАНИЕ ОСОБОЙ СОРТИРОВКИ (specialSort)
    // =========================================================================

    @Test
    @DisplayName("Особая сортировка: чётные сортируются по возрастанию, нечётные остаются на местах")
    void testSpecialSortMixedValues() {
        // Подготовка данных. Скорости: [151 (неч), 200 (чет), 120 (чет), 180 (чет), 155 (неч)]
        collection.add(createCar("A", "A", 151, 100)); // Индекс 0 (неч) - должен остаться
        collection.add(createCar("B", "B", 200, 100)); // Индекс 1 (чет)
        collection.add(createCar("C", "C", 120, 100)); // Индекс 2 (чет)
        collection.add(createCar("D", "D", 180, 100)); // Индекс 3 (чет)
        collection.add(createCar("E", "E", 155, 100)); // Индекс 4 (неч) - должен остаться

        // Вызываем особую сортировку
        collection.specialSort();

        // 1. Проверяем, что нечётные значения (151 и 155) остались на СВОИХ исходных позициях (0 и 4)
        assertEquals(151, collection.get(0).getMaxSpeed());
        assertEquals(155, collection.get(4).getMaxSpeed());

        // 2. Проверяем, что чётные значения на позициях 1, 2, 3 отсортировались по возрастанию.
        // Исходные чётные: 200, 120, 180. Ожидаемый порядок: 120, 180, 200.
        assertEquals(120, collection.get(1).getMaxSpeed());
        assertEquals(180, collection.get(2).getMaxSpeed());
        assertEquals(200, collection.get(3).getMaxSpeed());
    }

    @Test
    @DisplayName("Особая сортировка: коллекция только с нечётными значениями не меняется")
    void testSpecialSortOnlyOdds() {
        collection.add(createCar("A", "A", 101, 100));
        collection.add(createCar("B", "B", 103, 100));
        collection.add(createCar("C", "C", 99, 100));

        collection.specialSort();

        // Порядок и значения должны остаться строго прежними
        assertEquals(101, collection.get(0).getMaxSpeed());
        assertEquals(103, collection.get(1).getMaxSpeed());
        assertEquals(99, collection.get(2).getMaxSpeed());
    }

    @Test
    @DisplayName("Особая сортировка: коллекция только с чётными значениями полностью сортируется")
    void testSpecialSortOnlyEvens() {
        collection.add(createCar("A", "A", 200, 100));
        collection.add(createCar("B", "B", 120, 100));
        collection.add(createCar("C", "C", 180, 100));

        collection.specialSort();


        // Так как все элементы чётные, вся коллекция должна отсортироваться по возрастанию
        assertEquals(120, collection.get(0).getMaxSpeed());
        assertEquals(180, collection.get(1).getMaxSpeed());
        assertEquals(200, collection.get(2).getMaxSpeed());
    }

    @Test
    @DisplayName("Особая сортировка: пустая коллекция и коллекция из 1 элемента не вызывают ошибок")
    void testSpecialSortEdgeCases() {
        // Пустая коллекция
        assertDoesNotThrow(() -> collection.specialSort());
        assertEquals(0, collection.size());

        // 1 элемент (нечётный)
        collection.add(createCar("A", "A", 101, 100));
        assertDoesNotThrow(() -> collection.specialSort());
        assertEquals(1, collection.size());
        assertEquals(101, collection.get(0).getMaxSpeed());
    }


    // =========================================================================
    // ПОЛНОЕ ТЕСТИРОВАНИЕ ВСЕХ 8 МЕТОДОВ СОРТИРОВКИ
    // =========================================================================

    /**
     * Вспомогательный метод для тестов сортировки.
     * Заполняет коллекцию 3-мя машинами в заведомо случайном порядке.
     * Toyota (скорость 180, цена 25000)
     * Audi (скорость 250, цена 90000)
     * BMW (скорость 210, цена 50000)
     */
    private void populateForSorting() {
        collection.add(createCar("Toyota", "Camry", 180, 25000));
        collection.add(createCar("Audi", "A8", 250, 90000));
        collection.add(createCar("BMW", "X5", 210, 50000));
    }

    // --- СОРТИРОВКА ПО МАРКЕ ---
    @Test
    @DisplayName("Сортировка по марке по возрастанию (sortByBrandAsc)")
    void testSortByBrandAsc() {
        populateForSorting();
        collection.sortByBrandAsc();

        List<Car> sorted = collection.toList();
        assertEquals("Audi", sorted.get(0).getBrandName());   // А
        assertEquals("BMW", sorted.get(1).getBrandName());    // Б
        assertEquals("Toyota", sorted.get(2).getBrandName()); // Т
    }

    @Test
    @DisplayName("Сортировка по марке по убыванию (sortByBrandDesc)")
    void testSortByBrandDesc() {
        populateForSorting();
        collection.sortByBrandDesc();

        List<Car> sorted = collection.toList();
        assertEquals("Toyota", sorted.get(0).getBrandName());
        assertEquals("BMW", sorted.get(1).getBrandName());
        assertEquals("Audi", sorted.get(2).getBrandName());
    }

    // --- СОРТИРОВКА ПО МОДЕЛИ ---
    @Test
    @DisplayName("Сортировка по модели по возрастанию (sortByNameAsc)")
    void testSortByNameAsc() {
        populateForSorting();
        collection.sortByNameAsc();

        List<Car> sorted = collection.toList();
        assertEquals("A8", sorted.get(0).getModelName());    // A8
        assertEquals("Camry", sorted.get(1).getModelName()); // Camry
        assertEquals("X5", sorted.get(2).getModelName());    // X5
    }

    @Test
    @DisplayName("Сортировка по модели по убыванию (sortByNameDesc)")
    void testSortByNameDesc() {
        populateForSorting();
        collection.sortByNameDesc();

        List<Car> sorted = collection.toList();
        assertEquals("X5", sorted.get(0).getModelName());
        assertEquals("Camry", sorted.get(1).getModelName());
        assertEquals("A8", sorted.get(2).getModelName());
    }

    // --- СОРТИРОВКА ПО СКОРОСТИ ---
    @Test
    @DisplayName("Сортировка по скорости по возрастанию (sortBySpeedAsc)")
    void testSortBySpeedAsc() {
        populateForSorting();
        collection.sortBySpeedAsc();

        List<Car> sorted = collection.toList();
        assertEquals(180, sorted.get(0).getMaxSpeed()); // 180
        assertEquals(210, sorted.get(1).getMaxSpeed()); // 210
        assertEquals(250, sorted.get(2).getMaxSpeed()); // 250
    }

    @Test
    @DisplayName("Сортировка по скорости по убыванию (sortBySpeedDesc)")
    void testSortBySpeedDesc() {
        populateForSorting();
        collection.sortBySpeedDesc();

        List<Car> sorted = collection.toList();
        assertEquals(250, sorted.get(0).getMaxSpeed());
        assertEquals(210, sorted.get(1).getMaxSpeed());
        assertEquals(180, sorted.get(2).getMaxSpeed());
    }

    // --- СОРТИРОВКА ПО ЦЕНЕ ---
    @Test
    @DisplayName("Сортировка по цене по возрастанию (sortByPriceAsc)")
    void testSortByPriceAsc() {
        populateForSorting();
        collection.sortByPriceAsc();

        List<Car> sorted = collection.toList();
        assertEquals(25000, sorted.get(0).getPrice()); // 25000
        assertEquals(50000, sorted.get(1).getPrice()); // 50000
        assertEquals(90000, sorted.get(2).getPrice()); // 90000
    }

    @Test
    @DisplayName("Сортировка по цене по убыванию (sortByPriceDesc)")
    void testSortByPriceDesc() {
        populateForSorting();
        collection.sortByPriceDesc();

        List<Car> sorted = collection.toList();
        assertEquals(90000, sorted.get(0).getPrice());
        assertEquals(50000, sorted.get(1).getPrice());
        assertEquals(25000, sorted.get(2).getPrice());
    }

}