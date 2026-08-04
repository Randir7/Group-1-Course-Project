package org.example.Util;

import org.example.View.LogConsolePanel;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * ОБРАБОТЧИК СООБЩЕНИЙ (КОНТЕКСТ В ПАТТЕРНЕ "СТРАТЕГИЯ")
 *
 * Этот класс служит посредником между Контроллером и конкретными стратегиями вывода
 * (Info, Success, Error).
 *
 * Контроллер не знает, какие именно классы прячутся за выводом красного или зеленого
 * текста. Он просто передает цвет и текст в MessageHandler, а тот сам решает,
 * какую стратегию запустить.
 *
 * Архитектурная фишка: вместо того чтобы писать длинную цепочку
 * if (color == RED) {...} else if (color == BLACK) {...},
 * мы помещаем стратегии в Map (словарь), где ключом выступает цвет.
 */
public class MessageHandler {

    // Ссылка на панель логов из View. Понадобится, чтобы передать её в стратегии.
    private final LogConsolePanel logPanel;

    // Map (отображение) — это структура данных, которая хранит пары "Ключ-Значение".
    // Здесь Ключ — это Цвет (Color), а Значение — объект Стратегии (MessageStrategy).
    private final Map<Color, MessageStrategy> strategies;

    public MessageHandler(LogConsolePanel logPanel) {
        this.logPanel = logPanel;

        // Инициализируем словарь стратегий.
        // Используем HashMap — самую быструю реализацию Map в Java.
        strategies = new HashMap<>();

        // Регистрируем наши стратегии.
        // Когда Контроллер попросит вывести текст черным цветом, мы достанем InfoMessageStrategy и т.д.
        strategies.put(Color.BLACK, new InfoMessageStrategy());
        strategies.put(new Color(0, 128, 0), new SuccessMessageStrategy());
        strategies.put(Color.RED, new ErrorMessageStrategy());
    }

    /**
     * Единый метод для отправки сообщений.
     *
     * @param color   Цвет (Color.BLACK, Color.RED или new Color(0, 128, 0) для успеха)
     * @param message Основное сообщение (может быть null)
     * @param error   Детали ошибки/исключения (может быть null)
     */
    public void printMessage(Color color, String message, String error) {
        // Достаем из словаря нужную стратегию по ключу (по цвету).
        // Это работает за время O(1) — мгновенно.
        MessageStrategy strategy = strategies.get(color);

        // Защитное программирование (Defensive programming).
        // Если кто-то передал цвет, которого нет в нашем словаре (например, Color.BLUE),
        // стратегия будет null. Чтобы программа не упала с NullPointerException,
        // мы используем запасной вариант — стандартную информационную стратегию (черный цвет).
        if (strategy == null) {
            strategy = strategies.get(Color.BLACK);
        }

        // Делегируем выполнение выбранной стратегии.
        // Вызывается метод execute, а какой именно код сработает — зависит от того,
        // какой объект лежит в переменной strategy (полиморфизм в действии).
        strategy.execute(message, error, logPanel);
    }

    /**
     * Перегрузка метода (Method Overloading).
     *
     * В Java можно иметь несколько методов с одним именем, если у них разные параметры.
     * Этот метод создан для удобства: если Контроллеру нужно вывести простое
     * информационное сообщение без деталей ошибки, он вызывает этот вариант метода.
     * А этот метод внутри себя вызывает полную версию, передавая null вместо ошибки.
     */
    public void printMessage(Color color, String message) {
        printMessage(color, message, null);
    }
}