package org.example.Model;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * КЛАСС МОДЕЛЬ (MODEL) в паттерне MVC.
 *
 * Модель отвечает за бизнес-логику приложения, работу с данными и их хранение.
 * Главная особенность Модели: она АБСОЛЮТНО ничего не знает о пользовательском
 * интерфейсе (в ней нет ни одного импорта из javax.swing или java.awt).
 * Это позволяет использовать эту же логику в консольном приложении, веб-сервере
 * или мобильном приложении без изменения кода.
 */
public class AppModel {

    // Хранилище данных. Мы используем нашу собственную реализацию коллекции
    // вместо стандартного ArrayList, чтобы продемонстрировать работу алгоритмов "под капотом".
    //TODO сейчас это заглушка, надо дописать класс кастомной коллекции чтобы программа работала корректно
    private final CustomCarCollection cars = new CustomCarCollection();

    /**
     * Создает и добавляет один объект Car в коллекцию.
     * @return созданный объект Car
     */
    public Car addSingleCar(String modelName, int maxSpeed, int price) {
        Car car = validateAndCreate(modelName, maxSpeed, price);
        //расскомментировать после реализации кастомной коллекции
        //cars.add(car);
        return car;
    }

    /**
     * Читает машины из файла, выбранного пользователем, и добавляет их к существующему списку.
     * Обратите внимание: Модель не показывает пользователю окошки с ошибками.
     * Вместо этого она собирает тексты ошибок в список и возвращает его Контроллеру.
     * Контроллер уже решит, как показать эти ошибки (например, в логе красным цветом).
     */
    public List<String> addCarsFromUserFile(Path filePath) {
        ParseResult result = parseDataFromFile(filePath);
        //расскомментировать после реализации кастомной коллекции
        //cars.addAll(result.cars);
        return result.errors;
    }

    /**
     * Генерация случайного списка машин.
     * Модель берет заготовки машин из специального файла ресурсов (randomData.txt),
     * случайно выбирает нужное количество и делает их независимые копии.
     * Возвращает лист со списком ошибок
     */
    public List<String> addRandomCars(int count) {
        return null;
    }

    /**
     * Валидация и создание объекта Car.
     * Мы используем паттерн проектирования "Строитель" (Builder).
     * Если данные невалидны (например, скорость 2000), конструктор Builder'а
     * бросит IllegalArgumentException. Мы НЕ ловим исключение здесь!
     * Задача Модели — выбросить исключение, а задача Контроллера — поймать его
     * и сообщить пользователю.
     * Следует использовать этот метод для создания всех экземпляров машин
     */
    public Car validateAndCreate(String modelName, int maxSpeed, int price) {
        return new Car.CarBuilder().setModelName(modelName).setMaxSpeed(maxSpeed).setPrice(price).build();
    }

    /**
     * Преобразование объекта Car в строку для записи в файл.
     * Формат жестко задан: "Имя / Скорость км/ч / $Цена"
     */
    public String carToString(Car car) {
        if (car == null) return "";
        return String.format("%s / %d км/ч / $%d",
                car.getModelName(),
                car.getMaxSpeed(),
                car.getPrice());
    }

    /**
     * Метод парсинга (разбора) файла.
     * Читает файл построчно, должен проверять формат с помощью регулярных выражений.
     * Не добавляет машины в основную коллекцию, а возвращает результат в объекте ParseResult.
     */

    //TODO требуется реализовать метод
    public ParseResult parseDataFromFile(Path filePath) {
        return null;
    }

    /**
     * Сохранение данных в текстовый файл.
     * Метод объявлен с throws IOException. Это значит, что он перекладывает
     * ответственность за обработку ошибок ввода-вывода на того, кто его вызывает (на Контроллер).
     */
    //TODO требуется реализовать метод
    public void saveDataToFile(Path filePath) throws IOException {

    }

    /**
     * Сортировка коллекции. Модель делегирует выполнение кастомной коллекции.
     * @param columnIndex Индекс колонки (0 - имя, 1 - скорость, 2 - цена)
     * @param ascending Флаг: true - по возрастанию, false - по убыванию
     */
    public void sortCars(int columnIndex, boolean ascending) {
        switch (columnIndex) {
            case 0: // Имя
                if (ascending) cars.sortByNameAsc();
                else cars.sortByNameDesc();
                break;
            case 1: // Скорость
                if (ascending) cars.sortBySpeedAsc();
                else cars.sortBySpeedDesc();
                break;
            case 2: // Цена
                if (ascending) cars.sortByPriceAsc();
                else cars.sortByPriceDesc();
                break;
            default:
                throw new IllegalArgumentException("Неизвестный индекс колонки: " + columnIndex);
        }
    }

    //Метод-заглушка. Требуется организовать особую сортировку в кастомной коллекции для его работы
    //TODO реализовать метод
    public void specialSort() {
        //cars.specialSort();
    }

    /**
     * Многопоточный подсчет количества вхождений элемента в коллекцию.
     * Метод возвращает готовый текст результата для Контроллера.
     *
     * @return Строка с результатами подсчета.
     * @throws IllegalArgumentException Если данные невалидны.
     * @throws IllegalStateException Если список машин пуст.
     * @throws InterruptedException Если потоки были прерваны.
     */
    //TODO реализовать метод многопоточного подсчета
    public String multithreadCounting(String modelName, int maxSpeed, int price)
            throws InterruptedException {
        return null;
    }


    /**
     * Геттер для получения списка машин.
     * Возвращает стандартный List<Car>, чтобы View (таблица) могла не знать
     * о нашей внутренней реализации CustomCarCollection.
     */
    public List<Car> getCars() {
        return cars.toList();
    }

    /**
     * Вспомогательный статический класс-контейнер.
     * В Java метод может вернуть только одно значение. Если нужно вернуть
     * и список успешно распарсенных машин, и список ошибок, их упаковывают
     * в один объект-контейнер, чтобы избежать использования кортежа
     */
    public static class ParseResult {
        public final List<Car> cars;
        public final List<String> errors;

        public ParseResult(List<Car> cars, List<String> errors) {
            this.cars = cars;
            this.errors = errors;
        }
    }
}