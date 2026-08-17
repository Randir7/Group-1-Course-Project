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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    private final CustomCarCollection cars = new CustomCarCollection();

    /**
     * Создает и добавляет один объект Car в коллекцию.
     * @return созданный объект Car
     */
    public Car addSingleCar(String brandName, String modelName, int maxSpeed, int price) {
        Car car = validateAndCreate(brandName, modelName, maxSpeed, price);
        cars.add(car);
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
        //заполнение через стрим чтобы соответсвовать условию задачи
        result.cars.forEach(cars::add);
        return result.errors;
    }

    /**
     * Из заранее созданного списка создает указанное количество машин
     * и добавляет их в список cars модели.
     * Заполнение коллекции реализовано через Stream API.
     */
    public List<String> addRandomCars(int count) {
        List<String> errorMessages = new ArrayList<>();

        try {
            // 1. Получаем путь к файлу из папки resources
            URL resourceUrl = getClass().getResource("/randomData.txt");
            if (resourceUrl == null) {
                throw new IllegalArgumentException("Файл ресурсов randomData.txt не найден!");
            }

            Path filePath = Paths.get(resourceUrl.toURI());

            // 2. Читаем и парсим все машины из файла
            ParseResult result = parseDataFromFile(filePath);
            //заполнение коллекции в стриме для выполнения условия задачи
            result.errors.forEach(errorMessages::add);

            List<Car> availableCars = result.cars;

            if (availableCars.isEmpty() && count > 0) {
                errorMessages.add("В файле randomData.txt нет валидных машин для генерации.");
                return errorMessages;
            }

            Random random = new Random();

            // 3. СОЗДАНИЕ МАШИН ЧЕРЕЗ STREAM API
            // IntStream.range(0, count) создает поток чисел от 0 до count (не включая count)
            List<Car> generatedCars = IntStream.range(0, count)
                    .mapToObj(i -> {
                        // Генерируем случайный индекс
                        int randomIndex = random.nextInt(availableCars.size());
                        Car originalCar = availableCars.get(randomIndex);

                        // Создаем независимую копию машины, передавая все 4 параметра
                        return validateAndCreate(
                                originalCar.getBrandName(),
                                originalCar.getModelName(),
                                originalCar.getMaxSpeed(),
                                originalCar.getPrice()
                        );
                    })
                    .collect(Collectors.toList()); // Собираем результаты в список

            // 4. Добавляем сгенерированный список в нашу кастомную коллекцию
            cars.addAll(generatedCars);

        } catch (Exception e) {
            errorMessages.add("Критическая ошибка при генерации случайных машин: " + e.getMessage());
        }

        return errorMessages;
    }

    /**
     * Валидация и создание объекта Car.
     * Мы используем паттерн проектирования "Строитель" (Builder).
     * Если данные невалидны (например, скорость 2000), конструктор Builder'а
     * бросит IllegalArgumentException. Мы НЕ ловим её здесь!
     * Задача Модели — выбросить исключение, а задача Контроллера — поймать его
     * и сообщить пользователю.
     */
    public Car validateAndCreate(String brandName, String modelName, int maxSpeed, int price) {
        return new Car.CarBuilder()
                .setBrandName(brandName)
                .setModelName(modelName)
                .setMaxSpeed(maxSpeed)
                .setPrice(price)
                .build();
    }

    /**
     * Преобразование объекта Car в строку для записи в файл.
     * Формат жестко задан: "Имя бренда / Имя модели / Скорость км/ч / $Цена"
     */
    public String carToString(Car car) {
        if (car == null) return "";
        // Добавляем %s для марки в начало
        return String.format("%s / %s / %d км/ч / $%d",
                car.getBrandName(),
                car.getModelName(),
                car.getMaxSpeed(),
                car.getPrice());
    }

    /**
     * Метод парсинга (разбора) файла.
     * Читает файл построчно, проверяет формат с помощью регулярных выражений.
     * Не добавляет машины в основную коллекцию, а возвращает результат в объекте ParseResult.
     */
    public ParseResult parseDataFromFile(Path filePath) {
        List<Car> parsedCars = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        // Регулярные выражения (Regex) для проверки строк.
        // ^(\\d+)\\s*км/ч$ означает: строка должна начинаться(^) с цифр (\\d+),
        // затем возможны пробелы (\\s*), и заканчиваться($) строкой "км/ч".
        Pattern speedPattern = Pattern.compile("^(\\d+)\\s*км/ч$");
        // ^\\$(\\d+)$ означает: начинается со знака доллара (\\$), затем цифры, и конец строки.
        Pattern pricePattern = Pattern.compile("^\\$(\\d+)$");

        // Используем try-with-resources (try со скобками).
        // Это гарантирует, что файл автоматически закроется после чтения, даже если произойдет ошибка.
        try (Stream<String> lines = Files.lines(filePath)) {
            lines.forEach(line -> {
                try {
                    // Разбиваем строку по разделителю " / " (с пробелами)
                    String[] parts = line.split("\\s+/\\s+");
                    if (parts.length != 4) {
                        throw new IllegalArgumentException("Неверный формат строки. Ожидалось 4 блока, разделенных ' / '.");
                    }

                    String brandName = parts[0].trim(); // Проверка на пустоту brandName...
                    if (brandName.isEmpty()) {
                        throw new IllegalArgumentException("Имя бренда не может быть пустым.");
                    }

                    String modelName = parts[1].trim();
                    if (modelName.isEmpty()) {
                        throw new IllegalArgumentException("Имя модели не может быть пустым.");
                    }

                    // Проверяем блок скорости
                    Matcher speedMatcher = speedPattern.matcher(parts[2].trim());
                    if (!speedMatcher.matches()) {
                        throw new IllegalArgumentException("Неверный формат скорости. Корректный формат: '140 км/ч'.");
                    }
                    // group(1) достает из Matcher первую группу в скобках (только цифры)
                    int speed = Integer.parseInt(speedMatcher.group(1));

                    // Проверяем блок цены
                    Matcher priceMatcher = pricePattern.matcher(parts[3].trim());
                    if (!priceMatcher.matches()) {
                        throw new IllegalArgumentException("Неверный формат цены. Корректный формат: '$11500'.");
                    }
                    int price = Integer.parseInt(priceMatcher.group(1));

                    Car car = validateAndCreate(brandName, modelName, speed, price);
                    parsedCars.add(car);

                } catch (IllegalArgumentException e) {
                    // Если строка кривая, мы не роняем программу, а добавляем ошибку в список.
                    // Парсинг следующих строк продолжится.
                    errorMessages.add("Строка: \"" + line + "\" | Ошибка: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            // Сюда попадем, если файл не существует или нет прав на чтение
            errorMessages.add("Критическая ошибка чтения файла: " + e.getMessage());
        }

        return new ParseResult(parsedCars, errorMessages);
    }

    /**
     * Сохранение данных в текстовый файл.
     * Метод объявлен с throws IOException. Это значит, что он перекладывает
     * ответственность за обработку ошибок ввода-вывода на того, кто его вызывает (на Контроллер).
     */
    public void saveDataToFile(Path filePath) throws IOException {
        if (cars.size() == 0) {
            throw new IllegalStateException("Список машин пуст, нечего сохранять.");
        }

        List<Car> carsList = cars.toList();
        try (var writer = Files.newBufferedWriter(filePath)) {
            for (Car car : carsList) {
                String line = String.format("%s / %s / %d км/ч / $%d%n",
                        car.getBrandName(),
                        car.getModelName(),
                        car.getMaxSpeed(),
                        car.getPrice());
                writer.write(line);
            }
        }
    }

    /**
     * Сортировка коллекции. Модель делегирует выполнение кастомной коллекции.
     * @param columnIndex Индекс колонки (0 - имя, 1 - скорость, 2 - цена)
     * @param ascending Флаг: true - по возрастанию, false - по убыванию
     */
    public void sortCars(int columnIndex, boolean ascending) {
        switch (columnIndex) {
            case 0: // Марка
                if (ascending) cars.sortByBrandAsc();
                else cars.sortByBrandDesc();
                break;
            case 1: // Модель
                if (ascending) cars.sortByNameAsc();
                else cars.sortByNameDesc();
                break;
            case 2: // Скорость
                if (ascending) cars.sortBySpeedAsc();
                else cars.sortBySpeedDesc();
                break;
            case 3: // Цена
                if (ascending) cars.sortByPriceAsc();
                else cars.sortByPriceDesc();
                break;
            default:
                throw new IllegalArgumentException("Неизвестный индекс колонки: " + columnIndex);
        }
    }

    //Логика групповой сортировки по нескольким ключам (для кнопки "Общая сортировка").
    public void multikeySort() {
        cars.multikeySort();
    }

    // Метод особой сортировки(для кнопки "Специальная сортировка").
    public void specialSort() {
        cars.specialSort();
    }

    /**
     * Многопоточный подсчет количества вхождений элемента в коллекцию.
     * Метод возвращает готовый текст результата для Контроллера.
     *
     * @return Строка с результатами подсчета.
     * @throws IllegalStateException Если список машин пуст.
     * @throws InterruptedException Если потоки были прерваны.
     */
    public String multithreadCounting(String brandName, String modelName, int maxSpeed, int price)
            throws InterruptedException {
        // Переменная для записи количества найденных одинаковых автомобилей
        AtomicInteger count = new AtomicInteger(0);
        // Создаем эталонный объект Car (если данные невалидны, будет IllegalArgumentException)
        Car targetCar = new Car.CarBuilder()
                .setBrandName(brandName)
                .setModelName(modelName)
                .setMaxSpeed(maxSpeed)
                .setPrice(price)
                .build();

        final int size = cars.size();

        if(size == 0) {
            throw new IllegalStateException("Список машин пуст, подсчет невозможен.");
        }
//        Количество потоков
        int threadCount = Runtime.getRuntime().availableProcessors();
        threadCount = Math.min(threadCount, size);

        final int chunkSize = (size + threadCount - 1) / threadCount;

        Thread[] threads = new Thread[threadCount];

        for(int t = 0; t < threadCount; t++){
            final int from = t*chunkSize;
            final int to = Math.min(from + chunkSize, size);

            threads[t] = new Thread(()->{
               for (int i = from; i < to; i++){
                   Car car = cars.get(i);

                   if (targetCar.getBrandName().equals(car.getBrandName())
                           && targetCar.getModelName().equals(car.getModelName())
                           && targetCar.getMaxSpeed() == car.getMaxSpeed()
                           && targetCar.getPrice() == car.getPrice()) {
                       count.incrementAndGet();
                   }
               }
            });
            threads[t].start();
        }
        for(Thread thread: threads){
            thread.join();
        }
        // Формируем текстовый ответ
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("Многопоточный подсчет завершен.\n");
        sb.append("Искомый элемент: ").append(targetCar.getModelName())
                .append(" / ").append(targetCar.getMaxSpeed()).append(" км/ч / $")
                .append(targetCar.getPrice()).append("\n");
        sb.append("Количество вхождений в коллекцию: ").append(count.get()).append("\n");
        sb.append("==================================================\n");

        return sb.toString();
    }


    /**
     * Очистка всех данных.
     */
    public void clearData() {
        cars.clear();
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
     * в один объект-контейнер. Это отличный пример вложенного класса.
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