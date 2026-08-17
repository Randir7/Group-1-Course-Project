package org.example.Util;

import org.example.View.LogConsolePanel;
import java.awt.Color;

/**
 * КОНКРЕТНАЯ СТРАТЕГИЯ (CONCRETE STRATEGY) для информационных сообщений.
 *
 * Этот класс реализует интерфейс MessageStrategy и предназначен для вывода
 * обычных системных сообщений (не ошибок и не успехов).
 * Например: "Список машин очищен", "Команда 'Особая сортировка' вызвана".
 *
 * В архитектуре MVC этот класс находится в пакете Util.
 * Он вызывается Контроллером (через MessageHandler), когда нужно просто
 * проинформировать пользователя о каком-то действии.
 */
public class InfoMessageStrategy implements MessageStrategy {

    /**
     * Главный метод паттерна Стратегия.
     * @param message Основной текст сообщения.
     * @param error   Детали ошибки. Для информационного сообщения этот параметр
     *                не используется (мы передаем null при вызове из Контроллера),
     *                но он обязан быть в сигнатуре метода, чтобы реализовать интерфейс.
     * @param logPanel Ссылка на панель логов из View.
     */
    @Override
    public void execute(String message, String error, LogConsolePanel logPanel) {
        // Проверяем, что сообщение реально существует и не пустое.
        // Если оно пустое, мы просто ничего не выводим (избегаем пустых строк в логе).
        String textToPrint = (message != null && !message.isEmpty()) ? message + "\n" : "";

        // System.out — стандартный поток вывода в Java (консоль IDE).
        // В отличие от System.err, текст здесь будет обычного цвета (обычно черный/белый).
        System.out.print(textToPrint);

        // Отправляем текст в графическую панель логов черным цветом (Color.BLACK).
        // В интерфейсе это будет выглядеть как нейтральная информация,
        // которая не отвлекает внимание, как красные ошибки или зеленые успехи.
        logPanel.appendColoredText("\n" + textToPrint, Color.BLACK);
    }
}