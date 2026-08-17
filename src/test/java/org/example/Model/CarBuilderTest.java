package org.example.Model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CarBuilderTest {

    // =========================================================================
    // 1. ПОЗИТИВНЫЕ ТЕСТЫ (Happy Path) - проверяем, что все работает, когда данные верные
    // =========================================================================

    // ТЕСТЫ ДЛЯ НАЗВАНИЯ БРЕНДА
    @ParameterizedTest
    @MethodSource("carProvider")
    @DisplayName("Успешное создание машины с корректными данными")
    void testBuildCarWithValidData(Car car) {
        //Проверка (Assert) - убеждаемся, что поля заполнены верно
        assertNotNull(car); // Объект не должен быть null
        assertNotNull(car.getBrandName());
        assertNotNull(car.getModelName());
        assertTrue(car.getMaxSpeed() >= 0 && car.getMaxSpeed() <= 1500);
        assertTrue(car.getPrice() >= 0);
    }

     static Stream<Car> carProvider () {
        return Stream.of( new Car.CarBuilder().
                    setBrandName("Lada").
                    setModelName("Vesta").
                    setMaxSpeed(0).setPrice(20000).build(),
                new Car.CarBuilder().
                        setBrandName("Lada").
                        setModelName("Granta").
                        setMaxSpeed(0).setPrice(15000).build(),
                new Car.CarBuilder().
                        setBrandName("Lada-").
                        setModelName("2109").
                        setMaxSpeed(0).setPrice(0).build()
        );

    }


    @Test
    @DisplayName("Пробелы по краям марки и модели должны обрезаться (trim)")
    void testTrimmingNames() {
        Car car = new Car.CarBuilder()
                .setBrandName("  BMW  ")
                .setModelName("   X5   ")
                .setMaxSpeed(210)
                .setPrice(50000)
                .build();

        // Ожидаем, что лишние пробелы исчезли
        assertEquals("BMW", car.getBrandName());
        assertEquals("X5", car.getModelName());
    }


    // =========================================================================
    // 2. НЕГАТИВНЫЕ ТЕСТЫ (Maрка/Модель) - проверяем выброс исключений
    // =========================================================================

    //Тесты для марки

    @Test
    @DisplayName("Марка не может быть null")
    void testBrandNameNull() {
        // Лямбда-выражение содержит код, который должен сломаться
        assertThrows(IllegalArgumentException.class, () -> {
            new Car.CarBuilder().setBrandName(null).setModelName("TEST").setMaxSpeed(100).setPrice(100).build();
        });
    }

    @Test
    @DisplayName("Марка не может быть пустой строкой или строкой из пробелов")
    void testBrandNameEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Car.CarBuilder().setBrandName("   ").setModelName("TEST").setMaxSpeed(100).setPrice(100).build();
        });
    }

    @Test
    @DisplayName("Марка не может быть длиннее 20 символов")
    void testBrandNameTooLong() {
        String longName = "ОченьОченьДлинноеНазваниеМаркиАвтомобиляБольше20Символов";
        assertThrows(IllegalArgumentException.class, () -> {
            new Car.CarBuilder().setBrandName(longName).setModelName("TEST").setMaxSpeed(100).setPrice(100).build();
        });
    }

    @Test
    @DisplayName("Марка не может содержать спецсимволы (например, %%)")
    void testBrandNameInvalidChars() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Car.CarBuilder().setBrandName("Geely%%").setModelName("TEST").setMaxSpeed(100).setPrice(100).build();
        });
    }

    //Аналогичные тесты для модели

    @Test
    @DisplayName("Модель не может быть null")
    void testModelNameNull() {
        // Лямбда-выражение содержит код, который должен сломаться
        assertThrows(IllegalArgumentException.class, () -> {
            new Car.CarBuilder().setBrandName("TEST").setModelName(null).setMaxSpeed(100).setPrice(100).build();
        });
    }

    @Test
    @DisplayName("Модель не может быть пустой строкой или строкой из пробелов")
    void testModelNameEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Car.CarBuilder().setBrandName("TEST").setModelName("    ").setMaxSpeed(100).setPrice(100).build();

        });
    }

    @Test
    @DisplayName("Название модели не может быть длиннее 20 символов")
    void testModelNameTooLong() {
        String longName = "ОченьОченьДлинноеНазваниеМоделиАвтомобиляБольше20Символов";
        assertThrows(IllegalArgumentException.class, () -> {
            new Car.CarBuilder().setBrandName("TEST").setModelName(longName).setMaxSpeed(100).setPrice(100).build();
        });
    }

    @Test
    @DisplayName("Модель не может содержать спецсимволы (например, %%)")
    void testModelNameInvalidChars() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Car.CarBuilder().setBrandName("TEST").setModelName("Vesta%!").setMaxSpeed(100).setPrice(100).build();
        });
    }


    // =========================================================================
    // 3. НЕГАТИВНЫЕ ТЕСТЫ (Скорость и Цена)
    // =========================================================================

    @Test
    @DisplayName("Скорость не может быть отрицательной (-1)")
    void testMaxSpeedNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Car.CarBuilder().setBrandName("Lada").setModelName("Vesta").setMaxSpeed(-1).setPrice(100).build();
        });
    }

    @Test
    @DisplayName("Скорость не может превышать 1500")
    void testMaxSpeedTooHigh() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Car.CarBuilder().setBrandName("Lada").setModelName("Vesta").setMaxSpeed(1501).setPrice(100).build();
        });
    }

    @Test
    @DisplayName("Граничные значения скорости: 0 и 1500 должны проходить валидацию")
    void testMaxSpeedBoundaries() {
        // Граничные значения (Boundary Value Analysis) - частая ошибка в коде.
        // Проверяем, что ровно 0 и ровно 1500 не вызывают ошибку.

        Car carZero = new Car.CarBuilder().setBrandName("A").setModelName("B").setMaxSpeed(0).setPrice(100).build();
        assertEquals(0, carZero.getMaxSpeed());

        Car carMax = new Car.CarBuilder().setBrandName("A").setModelName("B").setMaxSpeed(1500).setPrice(100).build();
        assertEquals(1500, carMax.getMaxSpeed());
    }

    @Test
    @DisplayName("Цена не может быть отрицательной")
    void testPriceNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Car.CarBuilder().setBrandName("Lada").setModelName("Vesta").setMaxSpeed(100).setPrice(-1).build();
        });
    }

    @Test
    @DisplayName("Граничное значение цены: 0 должно проходить валидацию")
    void testPriceZero() {
        Car car = new Car.CarBuilder().setBrandName("A").setModelName("B").setMaxSpeed(100).setPrice(0).build();
        assertEquals(0, car.getPrice());
    }
}