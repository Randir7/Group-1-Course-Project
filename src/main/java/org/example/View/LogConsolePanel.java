package org.example.View;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;

/**
 * ПАНЕЛЬ КОНСОЛИ ЛОГОВ (VIEW)
 *
 * Этот класс является частью Представления (View) и предназначен для вывода
 * текстовых сообщений (логов, ошибок, информации) для пользователя.
 *
 * ОСОБЕННОСТЬ SWING:
 * Вместо привычного JTextArea (который умеет выводить только обычный монохромный текст),
 * здесь используется JTextPane. JTextPane поддерживает "стилизованный документ"
 * (StyledDocument), что позволяет раскрашивать разные части текста в разные цвета
 * (например, ошибки — в красный, успехи — в зеленый).
 */
public class LogConsolePanel extends JPanel {

    // Текстовая область с поддержкой стилей
    private final JTextPane textPane;

    // Документ, который хранит текст и его стили (цвета, шрифты)
    private final StyledDocument styledDoc;

    public LogConsolePanel() {
        setLayout(new BorderLayout());

        //Создаем JTextPane
        textPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                // Возвращаем true, чтобы панель всегда сжималась по ширине порта просмотра,
                // заставляя длинный текст переноситься на следующую строку.
                return true;
            }
        };

        textPane.setEditable(false);
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 16));
        textPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Отступы от краев

        styledDoc = textPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(textPane);

        // 2. Полностью отключаем горизонтальную полосу прокрутки
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "ЛОГ КОНСОЛИ",
                TitledBorder.LEFT, TitledBorder.TOP));

        add(scrollPane, BorderLayout.CENTER);
    }


    /**
     * Метод добавления цветного текста в консоль.
     *
     * В архитектуре MVC этот метод вызывается НЕ напрямую из Контроллера,
     * а через специальный обработчик (MessageHandler и его Стратегии).
     *
     * ВАЖНЫЙ МОМЕНТ (Многопоточность):
     * Обрати внимание на конструкцию SwingUtilities.invokeLater(...).
     * Swing — однопоточный фреймворк. Отрисовка интерфейса происходит в специальном
     * потоке EDT (Event Dispatch Thread). Если мы попытаемся изменить текст из
     * другого потока (например, из потока чтения файла), интерфейс может зависнуть
     * или начать "глючить".
     * invokeLater говорит Swing: "Выполни этот код как только доберешься до потока EDT".
     * Это делает обновление логов потокобезопасным!
     */
    public void appendColoredText(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            // Создаем набор атрибутов (стилей)
            SimpleAttributeSet attr = new SimpleAttributeSet();
            // Устанавливаем цвет текста
            StyleConstants.setForeground(attr, color);

            try {
                // Вставляем строку в конец документа (getLength() возвращает текущий размер текста)
                // Если произойдет ошибка координат, будет выброшено BadLocationException
                styledDoc.insertString(styledDoc.getLength(), message, attr);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            // АВТОПРОКРУТКА ВНИЗ
            // setCaretPosition перемещает текстовый курсор в самый конец текста.
            // Благодаря этому JScrollPane автоматически прокручивается вниз,
            // позволяя пользователю всегда видеть самые свежие сообщения.
            textPane.setCaretPosition(styledDoc.getLength());
        });
    }

    /**
     * Очистка консоли (вызывается по кнопке "Очистить лог").
     */
    public void clearLog() {
        textPane.setText("");
    }
}