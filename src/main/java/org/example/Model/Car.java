package org.example.Model;

import java.util.regex.Pattern;

/**
 * КЛАСС СУЩНОСТЬ (ENTITY / DOMAIN MODEL)
 *
 * Этот класс описывает конкретный объект предметной области — автомобиль.
 * Он является "пассивной моделью": просто хранит данные и предоставляет к ним доступ.
 * В архитектуре MVC этот класс относится к слою Model, но не содержит бизнес-логики
 * всего приложения (как AppModel), а лишь описывает структуру одной единицы данных.
 *
 * ОСОБЕННОСТИ АРХИТЕКТУРЫ:
 * 1. ИММУТАБЕЛЬНОСТЬ (Неизменяемость): После создания объекта изменить его поля нельзя.
 *    Это делает объект потокобезопасным и защищает от случайных изменений.
 * 2. ПАТТЕРН СТРОИТЕЛЬ (BUILDER): Для создания объекта используется вложенный класс CarBuilder.
 *    Это позволяет не создавать огромные конструкторы с кучей параметров и проводить
 *    сложную валидацию данных перед самим созданием объекта.
 */
public class Car {

    // Поля объекта объявлены как private. Доступ к ним извне только через геттеры.
    // Они не final, но логически объект неизменяем, так как сеттеров нет.
    private String modelName;
    private int maxSpeed;
    private int price;

    /**
     * ПРИВАТНЫЙ КОНСТРУКТОР.
     * Обрати внимание на модификатор private. Это значит, что написать
     * "new Car(...)" в любом другом месте программы НЕЛЬЗЯ.
     * Единственный способ создать машину — использовать вложенный класс CarBuilder.
     */
    private Car(String modelName, int maxSpeed, int price) {
        this.modelName = modelName;
        this.maxSpeed = maxSpeed;
        this.price = price;
    }

    // ГЕТТЕРЫ. Предоставляют доступ на чтение. Названия методов строго стандартизированы (getXxx),
    // чтобы многие фреймворки и библиотеки (например, для работы с таблицами в Swing) могли их находить автоматически.
    public String getModelName() { return modelName; }
    public int getMaxSpeed() { return maxSpeed; }
    public int getPrice() { return price; }

    /**
     * Переопределенный метод toString().
     * Вызывается автоматически, когда объект Car пытаются вывести в консоль (System.out.println)
     * или добавить в строку. Без него мы бы видели что-то вроде "org.example.Model.Car@7a81197d".
     */
    @Override
    public String toString() {
        return "Название модели: " + modelName + ", максимальная скорость: " + maxSpeed + " км/ч , цена: $" + price;
    }

    // =========================================================================
    // ВЛОЖЕННЫЙ КЛАСС BUILDER (СТРОИТЕЛЬ)
    // =========================================================================

    /**
     * Паттерн проектирования "Строитель".
     * Класс объявлен как static. Это важно! Статический вложенный класс не требует
     * создания объекта внешнего класса (Car) для своей работы. Он живет сам по себе.
     */
    public static class CarBuilder {

        // Временные поля строителя, куда мы будем сохранять значения до создания машины.
        private String modelName;
        private int maxSpeed;
        private int price;

        // Компилируем регулярное выражение один раз (static final) для производительности.
        // Шаблон означает: от начала(^) до конца($) строки могут идти только буквы (рус/англ),
        // цифры, дефис и пробел в количестве от одного и более (+).
        private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Zа-яА-ЯёЁ0-9\\- ]+$");

        /**
         * Метод для установки имени с одновременной валидацией.
         * Возвращает "this" (самого себя), что позволяет вызывать методы по цепочке:
         * builder.setModelName(...).setMaxSpeed(...).setPrice(...).build();
         */
        public CarBuilder setModelName(String modelName) {
            if (modelName == null) {
                throw new IllegalArgumentException("Имя модели не может быть null.");
            }
            String trimmedName = modelName.trim(); // Убираем пробелы по краям
            if (trimmedName.isEmpty()) {
                throw new IllegalArgumentException("Имя модели не должно быть пустым.");
            }
            if (trimmedName.length() > 20) {
                throw new IllegalArgumentException("Имя модели длиннее 20 символов.");
            }
            // Проверяем строку на соответствие регулярному выражению.
            if (!NAME_PATTERN.matcher(trimmedName).matches()) {
                throw new IllegalArgumentException("Имя модели содержит недопустимые символы. Разрешены только русские/английские буквы, цифры, дефис и пробелы.");
            }
            this.modelName = trimmedName;
            return this; // Возвращаем текущий объект Builder'а
        }

        // Метод установки скорости с проверкой бизнес-правил (от 0 до 1500)
        public CarBuilder setMaxSpeed(int maxSpeed) {
            if (maxSpeed < 0 || maxSpeed > 1500) {
                throw new IllegalArgumentException("Скорость должна быть от 0 до 1500. Передано: " + maxSpeed);
            }
            this.maxSpeed = maxSpeed;
            return this;
        }

        // Метод установки цены. Цена не может быть отрицательной.
        public CarBuilder setPrice(int price) {
            if (price < 0) {
                throw new IllegalArgumentException("Цена должна быть не меньше 0. Передано: " + price);
            }
            this.price = price;
            return this;
        }

        /**
         * Финальный метод, который собирает объект Car.
         * Он вызывает приватный конструктор внешнего класса Car.
         * Если все предыдущие проверки (валидация) прошли успешно, объект будет создан.
         */
        public Car build() {
            return new Car(modelName, maxSpeed, price);
        }
    }
}