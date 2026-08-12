package org.example.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * КАСТОМНАЯ КОЛЛЕКЦИЯ (Аналог ArrayList)
 *
 * В этом классе мы реализовали собственную структуру данных — динамический массив.
 * Вместо того чтобы использовать готовый ArrayList из стандартной библиотеки Java,
 * мы написали его сами, чтобы понять, как работает управление памятью,
 * расширение массивов и алгоритмы сортировки (и выполнить доп.задачу, конечно же)
 *
 * В архитектуре MVC этот класс является внутренним хранилищем для Модели (AppModel).
 * Внешний мир (Контроллер и View) даже не подозревает о существовании этого класса,
 * так как Модель отдает данные наружу через метод toList() в виде стандартного List.
 */
public class CustomCarCollection {

    // Внутренний массив для хранения данных. Массивы в Java имеют фиксированный размер.
    private Car[] array;

    // Реальный amount (количество) элементов в коллекции.
    // Важно понимать: array.length — это вместимость (емкость), а size — это сколько реально лежит элементов.
    private int size;

    // Начальный размер массива по умолчанию.
    private static final int DEFAULT_CAPACITY = 10;

    public CustomCarCollection() {
        // При создании коллекции выделяем память под 10 элементов, но самих элементов пока 0.
        this.array = new Car[DEFAULT_CAPACITY];
        this.size = 0;
    }

    // --- Управление размером ---

    /**
     * Добавление элемента в конец коллекции.
     */
    public void add(Car car) {
        if (car == null){
            throw new IllegalArgumentException("Класс Car не может быть пустым");
        }
        if (size == array.length){
            grow();
        }
        this.array[size++] = car;
    }

    /**
     * Добавление другой коллекции в текущую.
     */
    public void addAll(CustomCarCollection collection) {
        if(collection == null){
            throw new IllegalArgumentException("Коллекция не может быть пустой");
        }
        for(int i = 0; i < collection.size(); i++){
            add(collection.get(i));
        }
    }

    /**
     * Получение элемента по индексу.
     */
    public Car get(int index) {
        checkIndex(index);
        return array[index];
    }

    /**
     * Возвращает количество реальных элементов в коллекции.
     */

    public int size() {
        return size;
    }

    /**
     * Очистка коллекции.
     */

    public void clear() {
        for (int i = 0; i < size; i++){
            array[i] = null;
        }
        size = 0;
    }

    // --- 8 МЕТОДОВ СОРТИРОВКИ ---

    // Вместо того чтобы писать 8 разных алгоритмов сортировки, мы пишем один общий метод сортировки,
    // а в эти методы передаем ему разные "правила сравнения" (Comparator).

    public void sortByBrandAsc() {
        quickSort(Comparator.comparing(Car::getBrandName));
    }

    public void sortByBrandDesc() {
        quickSort(Comparator.comparing(Car::getBrandName).reversed());
    }

    public void sortByNameAsc() {
        quickSort(Comparator.comparing(Car::getModelName));
    }

    public void sortByNameDesc() {
        quickSort(Comparator.comparing(Car::getModelName).reversed());
    }

    public void sortBySpeedAsc() {
        quickSort(Comparator.comparing(Car::getMaxSpeed));
    }

    public void sortBySpeedDesc() {
        quickSort(Comparator.comparing(Car::getMaxSpeed).reversed());
    }

    public void sortByPriceAsc() {
        quickSort(Comparator.comparingDouble(Car::getPrice));
    }

    public void sortByPriceDesc() {
        quickSort(Comparator.comparingDouble(Car::getPrice).reversed());
    }

    public void multikeySort() {
        Comparator<Car> groupComparator = Comparator.comparing(Car::getBrandName)
                .thenComparing(Car::getModelName)
                .thenComparing(Car::getMaxSpeed)
                .thenComparing(Car::getPrice);

        quickSort(groupComparator);
    }

    /**
     * Особая сортировка по числовому полю maxSpeed:
     * - объекты с чётным значением maxSpeed сортируются по возрастанию (натуральный порядок);
     * - объекты с нечётным значением maxSpeed остаются на своих исходных позициях.
     */
    public void specialSort() {
        if(size <= 1){
            return;
        }
        List<Car> evens = new ArrayList<>();
        for (int i=0; i<size; i++){
            if (array[i].getMaxSpeed() % 2 == 0){
                evens.add(array[i]);
            }
        }
        // Сортируем собранные чётные элементы по maxSpeed по возрастанию
        evens.sort(Comparator.comparingInt(Car::getMaxSpeed));

        // Возвращаем отсортированные чётные элементы обратно на те же позиции,
        // где раньше стояли чётные значения
        int evenIndex = 0;
        for (int i = 0; i < size; i++) {
            if (array[i].getMaxSpeed() % 2 == 0) {
                array[i] = evens.get(evenIndex++);
            }
        }

    }

    // --- Внутренняя реализация ---

    /**
     * Увеличение размера массива.
     */

    private void grow() {
        int newCapacity = array.length * 2;
        Car[] newArray = new Car[newCapacity];
        System.arraycopy(array, 0, newArray, 0, size);
        array = newArray;
    }

    /**
     * Проверка выхода за границы массива.
     */

    private void checkIndex(int index) {
        if (index < 0 || index >= size){
            throw new IndexOutOfBoundsException("Значение вышло за пределы массива");
        }
    }

    /**
     * Перегруженный метод addAll для добавления стандартного List.
     * Нужен для удобства, когда Модель загружает данные из файла (они лежат в List).
     */

    public void addAll(List<Car> carList) {
        if(carList == null){
            throw new IllegalArgumentException("Класс Car не может быть пустым");
        }
        for (Car car : carList){
            add(car);
        }
    }

    /**
     * Преобразование в стандартный список.
     * Это критически важный метод для инкапсуляции! Мы НЕ отдаем наш внутренний массив array[] наружу.
     * Если бы мы отдали массив, кто-то мог бы изменить его напрямую, минуя проверки коллекции.
     * Вместо этого мы создаем новую коллекцию (ArrayList), копируем туда элементы и отдаем её.
     */

    public List<Car> toList() {
        List<Car> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++){
            list.add(array[i]);
        }
        return list;
    }

    // =========================================================================
    // РЕАЛИЗАЦИЯ АЛГОРИТМА QUICKSORT (БЫСТРАЯ СОРТИРОВКА)
    // =========================================================================
    
    /**
     * Быстрая сортировка. Сложность алгоритма в среднем O(n log n).
     * @param comparator Объект, который знает, как сравнивать два объекта Car между собой.
     */

    private void quickSort(Comparator<Car> comparator) {
        if (size > 1) {
            quickSort(0, size - 1, comparator);
        }
    }

    /**
     * Рекурсивная реализация быстрой сортировки для диапазона [low, high].
     */
    private void quickSort(int low, int high, Comparator<Car> comparator) {
        if (low < high) {
            int pivotIndex = partition(low, high, comparator);
            quickSort(low, pivotIndex - 1, comparator);
            quickSort(pivotIndex + 1, high, comparator);
        }
    }

    /**
     * Разделение массива относительно опорного элемента (pivot).
     * Все элементы меньше pivot оказываются слева, больше — справа.
     * @return индекс, на котором оказался pivot после разделения
     */
    private int partition(int low, int high, Comparator<Car> comparator) {
        Car pivot = array[high];          // выбираем последний элемент как pivot
        int i = low - 1;                  // индекс последнего элемента, который меньше pivot

        for (int j = low; j < high; j++) {
            // если текущий элемент меньше или равен pivot
            if (comparator.compare(array[j], pivot) <= 0) {
                i++;
                swap(i, j);
            }
        }

        // ставим pivot на своё окончательное место
        swap(i + 1, high);
        return i + 1;
    }

    /**
     * Обмен двух элементов массива местами.
     */
    private void swap(int i, int j) {
        Car temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

}