package org.example.Util;

import org.example.View.LogConsolePanel;
import java.awt.Color;

/**
 * КОНКРЕТНАЯ СТРАТЕГИЯ (CONCRETE STRATEGY) для ошибок.
 *
 * Этот класс реализует интерфейс MessageStrategy и предназначен исключительно
 * для вывода сообщений об ошибках.
 *
 * В архитектуре MVC этот класс находится в пакете Util (вспомогательные инструменты).
 * Он вызывается Контроллером (через MessageHandler), когда Модель сообщает об ошибке
 * (например, неверный формат файла или ошибка валидации данных).
 *
 * Задача класса — отформатировать сообщение об ошибке и отправить его
 * в нужные места: в консоль IDE и в панель логов графического интерфейса.
 */
public class ErrorMessageStrategy implements MessageStrategy {

    /**
     * Главный метод паттерна Стратегия.
     * @param message Основной текст ошибки (например, "Ошибка парсинга").
     * @param error   Детали ошибки из исключения (например, e.getMessage()).
     * @param logPanel Ссылка на панель логов из View, куда нужно вывести текст.
     */
    @Override
    public void execute(String message, String error, LogConsolePanel logPanel) {
        // Используем StringBuilder для эффективной сборки строки.
        // Если бы мы использовали обычный оператор "+", Java создавала бы много
        // промежуточных строк в памяти, что менее производительно.
        StringBuilder sb = new StringBuilder();

        // Проверяем на null и пустоту, чтобы не выводить лишние пустые строки.
        if (message != null && !message.isEmpty()) {
            sb.append("\n[ОШИБКА] ").append(message).append("\n");
        }

        // Детали ошибки могут быть, а могут и не быть (если error == null)
        if (error != null && !error.isEmpty()) {
            sb.append("Детали: ").append(error).append("\n");
        }

        String textToPrint = sb.toString();

        // System.err — это стандартный поток вывода ошибок в Java.
        // В IDE (например, IntelliJ IDEA или Eclipse) текст из этого потока
        // обычно подсвечивается красным цветом. Это полезно при отладке.
        System.err.print(textToPrint);

        // Отправляем готовый текст в графический интерфейс.
        // Цвет Color.RED сделает текст красным и в панели логов приложения.
        // (Сама панель логов позаботится о потокобезопасности через SwingUtilities).
        logPanel.appendColoredText(textToPrint, Color.RED);
    }
}