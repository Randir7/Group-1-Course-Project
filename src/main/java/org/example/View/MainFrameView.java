package org.example.View;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * ГЛАВНОЕ ОКНО ПРИЛОЖЕНИЯ (VIEW)
 *
 * Этот класс является "сборщиком" интерфейса. В архитектуре MVC это корневой
 * компонент Представления (View).
 *
 * ОСОБЕННОСТЬ АРХИТЕКТУРЫ:
 * Обрати внимание, что этот класс НЕ наследует JFrame (не пишет "extends JFrame").
 * Вместо этого он содержит JFrame внутри себя (паттерн "Композиция" / "Делегирование").
 * Это более гибкий подход: мы скрываем внутреннее устройство окна и предоставляем
 * наружу только нужные методы.
 *
 * Класс принимает готовые панели (LogConsolePanel, ButtonPanel, DataTablePanel)
 * через конструктор. Это называется "Внедрение зависимостей" (Dependency Injection).
 * Такой подход делает классы слабо связанными: мы можем легко заменить одну панель
 * на другую без изменения кода этого класса.
 */
public class MainFrameView {

    // Ссылки на панели интерфейса. Они нужны, чтобы добавить их на JFrame
    // и чтобы Контроллер мог получить к ним доступ.
    private final LogConsolePanel logPanel;
    private final ButtonPanel buttonPanel;
    private final DataTablePanel tablePanel;

    // Главное окно программы
    private JFrame frame;

    public MainFrameView(LogConsolePanel logPanel, ButtonPanel buttonPanel, DataTablePanel tablePanel) {
        this.logPanel = logPanel;
        this.buttonPanel = buttonPanel;
        this.tablePanel = tablePanel;
        // Сразу же запускаем процесс сборки и отображения окна
        createAndShowGUI();
    }

    /**
     * Метод сборки графического интерфейса.
     * Здесь происходит создание окна, настройка его свойств и расположение панелей.
     */
    private void createAndShowGUI() {
        frame = new JFrame("Курсовой проект группы №1");

        // EXIT_ON_CLOSE означает, что при закрытии окна программа полностью завершит работу.
        // Если бы мы хотели просто скрыть окно, использовали бы HIDE_ON_CLOSE.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Задаем минимальный размер окна, чтобы пользователь не мог сжать его
        // до такой степени, что элементы наложатся друг на друга и станут нечитаемыми.
        frame.setMinimumSize(new Dimension(1024, 768));

        // Стартовый размер окна при запуске
        frame.setSize(1280, 800);

        // null означает "отцентрировать окно по центру экрана"
        frame.setLocationRelativeTo(null);

        // --- РАСПРЕДЕЛЕНИЕ ПАНЕЛЕЙ С ПОМОЩЬЮ GridBagLayout ---
        // Создаем главную панель, которая будет держать внутри себя три другие.
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // BOTH означает, что панели будут растягиваться И по горизонтали, И по вертикали.
        gbc.fill = GridBagConstraints.BOTH;
        // Внешние отступы от краев окна до панелей (по 10 пикселей со всех сторон)
        gbc.insets = new Insets(10, 10, 10, 10);

        // 1. ЛЕВАЯ КОЛОНКА (Логи)
        gbc.gridx = 0; gbc.gridy = 0; // Колонка 0, Строка 0
        // weightx и weighty определяют, как распределяется СВОБОДНОЕ пространство.
        // weightx = 0.5 означает, что эта колонка заберет 50% свободной ширины.
        gbc.weightx = 0.5; gbc.weighty = 1.0;
        mainPanel.add(logPanel, gbc);

        // 2. ЦЕНТРАЛЬНАЯ КОЛОНКА (Кнопки)
        gbc.gridx = 1; gbc.gridy = 0; // Колонка 1, Строка 0
        // weightx = 0.0 означает, что колонка кнопок НЕ будет растягиваться по ширине.
        // Она сохранит свой предпочтительный размер (preferred size), а излишки места
        // отдадут соседним колонкам.
        gbc.weightx = 0.0; gbc.weighty = 1.0;
        mainPanel.add(buttonPanel, gbc);

        // 3. ПРАВАЯ КОЛОНКА (Таблица)
        gbc.gridx = 2; gbc.gridy = 0; // Колонка 2, Строка 0
        // Снова weightx = 0.5. Таким образом, логи и таблица делят свободное место пополам,
        // а кнопки между ними остаются фиксированной ширины.
        gbc.weightx = 0.5; gbc.weighty = 1.0;
        mainPanel.add(tablePanel, gbc);

        // Помещаем главную панель в центр JFrame (BorderLayout.CENTER растягивает компонент на всё окно)
        frame.add(mainPanel, BorderLayout.CENTER);

        // Делаем окно видимым.
        // ВАЖНО: Этот метод всегда должен вызываться в самом конце, когда все элементы уже добавлены!
        frame.setVisible(true);
    }

    // =========================================================================
    // ГЕТТЕРЫ ДЛЯ КОНТРОЛЛЕРА
    // =========================================================================
    // Контроллер использует эти методы, чтобы добраться до кнопок, таблицы и логов,
    // а также чтобы получать главный JFrame (например, для центрирования диалоговых окон).

    public LogConsolePanel getLogPanel() { return logPanel; }

    public ButtonPanel getButtonPanel() { return buttonPanel; }

    public DataTablePanel getTablePanel() { return tablePanel; }

    public JFrame getFrame() { return frame; }

}