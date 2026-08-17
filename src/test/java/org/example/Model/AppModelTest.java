package org.example.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppModelTest {

    private AppModel model;

    @BeforeEach
    void setUp() {
        // Перед каждым тестом создаем новую чистую модель
        model = new AppModel();
    }

    // =========================================================================
    // 1. ТЕСТИРОВАНИЕ СОЗДАНИЯ И СТРОК (validateAndCreate, carToString)
    // =========================================================================

    @Test
    @DisplayName("validateAndCreate: успешное создание корректной машины")
    void testValidateAndCreateSuccess() {
        Car car = model.validateAndCreate("Toyota", "Camry", 200, 30000);

        assertNotNull(car);
        assertEquals("Toyota", car.getBrandName());
        assertEquals("Camry", car.getModelName());
        assertEquals(200, car.getMaxSpeed());
        assertEquals(30000, car.getPrice());
    }

    @Test
    @DisplayName("validateAndCreate: неверные данные выбрасывают исключение")
    void testValidateAndCreateInvalid() {
        // Скорость 9999 невалидна
        assertThrows(IllegalArgumentException.class, () -> {
            model.validateAndCreate("Test", "Test", 9999, 100);
        });
    }

    @Test
    @DisplayName("carToString: правильный формат строки")
    void testCarToString() {
        Car car = model.validateAndCreate("BMW", "X5", 210, 50000);
        String expected = "BMW / X5 / 210 км/ч / $50000";

        assertEquals(expected, model.carToString(car));
    }

    @Test
    @DisplayName("carToString: передача null возвращает пустую строку")
    void testCarToStringNull() {
        assertEquals("", model.carToString(null));
    }

    // =========================================================================
    // 2. ТЕСТИРОВАНИЕ ПАРСИНГА ФАЙЛОВ (parseDataFromFile)
    // =========================================================================

    @Test
    @DisplayName("parseDataFromFile: успешное чтение валидного файла")
    void testParseValidFile(@TempDir Path tempDir) throws IOException {
        // 1. Создаем временный файл и пишем в него 2 валидные строки
        Path testFile = tempDir.resolve("valid_cars.txt");
        Files.writeString(testFile, "Audi / A8 / 250 км/ч / $90000\nBMW / X5 / 210 км/ч / $50000\n");

        // 2. Парсим файл
        AppModel.ParseResult result = model.parseDataFromFile(testFile);

        // 3. Проверяем результат
        assertEquals(2, result.cars.size()); // 2 машины
        assertTrue(result.errors.isEmpty()); // 0 ошибок
    }

    @Test
    @DisplayName("parseDataFromFile: файл с ошибками формата собирается в список errors")
    void testParseInvalidFile(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("invalid_cars.txt");
        // 1-я строка без слешей, 2-я строка валидная, 3-я строка со скоростью 9999
        Files.writeString(testFile,
                "Просто строка без разделителей" +
                        "\nBMW / X5 / 210 км/ч / $50000" +
                        "\nAudi / A8 / 9999 км/ч / $100\n");

        AppModel.ParseResult result = model.parseDataFromFile(testFile);

        // Валидных машин должно быть 1 (только BMW)
        assertEquals(1, result.cars.size());
        // Ошибок должно быть 2
        assertEquals(2, result.errors.size());
    }

    @Test
    @DisplayName("parseDataFromFile: несуществующий файл возвращает критическую ошибку")
    void testParseMissingFile() {
        // Передаем фейковый путь, которого не существует
        Path fakePath = Path.of("C:/nonexistent_folder/file.txt");

        AppModel.ParseResult result = model.parseDataFromFile(fakePath);

        // Машин быть не должно
        assertTrue(result.cars.isEmpty());
        // Должна быть 1 критическая ошибка
        assertEquals(1, result.errors.size());
        // Проверяем, что текст ошибки содержит нужное сообщение
        assertTrue(result.errors.get(0).contains("Критическая ошибка чтения файла"));
    }

    @Test
    @DisplayName("parseDataFromFile: строка без разделителей регистрируется как ошибка формата")
    void testParseFileMissingDelimiters(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("no_slashes.txt");
        // Строка вообще не содержит разделителей " / "
        Files.writeString(testFile, "Просто сплошной текст без слешей\n");

        AppModel.ParseResult result = model.parseDataFromFile(testFile);

        // Машин быть не должно
        assertEquals(0, result.cars.size());
        // Должна быть 1 ошибка
        assertEquals(1, result.errors.size());
        // Проверяем, что текст ошибки содержит нужное сообщение из парсера
        assertTrue(result.errors.get(0).contains("Неверный формат строки. Ожидалось 4 блока"));
    }

    @Test
    @DisplayName("parseDataFromFile: неверный текстовый формат скорости регистрируется как ошибка")
    void testParseFileInvalidSpeedFormat(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("bad_speed_format.txt");
        // Скорость написана латиницей "km/h" вместо "км/ч"
        Files.writeString(testFile, "BMW / X5 / 210 km/h / $50000\n");

        AppModel.ParseResult result = model.parseDataFromFile(testFile);

        assertEquals(0, result.cars.size());
        assertEquals(1, result.errors.size());
        // Проверяем, что парсер понял, что именно скорость кривая
        assertTrue(result.errors.get(0).contains("Неверный формат скорости. Ожидалось: '140 км/ч'."));
    }

    @Test
    @DisplayName("parseDataFromFile: неверный текстовый формат цены регистрируется как ошибка")
    void testParseFileInvalidPriceFormat(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("bad_price_format.txt");
        // Пропущен знак доллара
        Files.writeString(testFile, "Audi / A8 / 250 км/ч / 90000\n");

        AppModel.ParseResult result = model.parseDataFromFile(testFile);

        assertEquals(0, result.cars.size());
        assertEquals(1, result.errors.size());
        assertTrue(result.errors.get(0).contains("Неверный формат цены. Ожидалось: '$11500'."));
    }

    @Test
    @DisplayName("parseDataFromFile: невалидная бизнес-логика (скорость > 1500) регистрируется как ошибка")
    void testParseFileInvalidBusinessLogic(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("bad_logic.txt");
        // Формат строки идеальный, но скорость 9999 недопустима по правилам CarBuilder
        Files.writeString(testFile, "Audi / A8 / 9999 км/ч / $100\n");

        AppModel.ParseResult result = model.parseDataFromFile(testFile);

        assertEquals(0, result.cars.size());
        assertEquals(1, result.errors.size());
        // Парсер должен перехватить ошибку от CarBuilder и добавить её в список
        assertTrue(result.errors.get(0).contains("Скорость должна быть от 0 до 1500. Передано: 9999"));
    }


    // =========================================================================
    // 3. ТЕСТИРОВАНИЕ МНОГОПОТОЧНОГО ПОДСЧЕТА (multithreadCounting)
    // =========================================================================

    @Test
    @DisplayName("multithreadCounting: правильный подсчет одинаковых элементов в многопоточном режиме")
    void testMultithreadCountingSuccess() throws InterruptedException {
        // Добавляем 3 одинаковых машины и 2 других
        model.addSingleCar("BMW", "X5", 210, 50000);
        model.addSingleCar("BMW", "X5", 210, 50000); // Искомая 2
        model.addSingleCar("Audi", "A8", 250, 90000);
        model.addSingleCar("BMW", "X5", 210, 50000); // Искомая 3
        model.addSingleCar("Audi", "A8", 250, 90000);

        // Вызываем метод. Он возвращает строку с результатом.
        String result = model.multithreadCounting("BMW", "X5", 210, 50000);

        // Проверяем, что в строке результата содержится цифра 3
        assertTrue(result.contains("Количество вхождений в коллекцию: 3"));
    }

    @Test
    @DisplayName("multithreadCounting: подсчет в пустой коллекции выбрасывает исключение")
    void testMultithreadCountingEmpty() {
        // Коллекция пуста, метод должен выбросить IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            model.multithreadCounting("BMW", "X5", 210, 50000);
        });
    }

    @Test
    @DisplayName("multithreadCounting: невалидные данные выбрасывают исключение валидации")
    void testMultithreadCountingInvalidData() {
        // Добавляем нормальную машину, чтобы коллекция не была пустой
        model.addSingleCar("Audi", "A8", 250, 90000);

        // Пытаемся посчитать машину со скоростью 9999 (невалидной)
        assertThrows(IllegalArgumentException.class, () -> {
            model.multithreadCounting("Test", "Test", 9999, 100);
        });
    }
}