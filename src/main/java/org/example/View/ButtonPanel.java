package org.example.View;

import javax.swing.*;
import java.awt.*;

/**
 * ПАНЕЛЬ КНОПОК (VIEW)
 *
 * Этот класс является чистым Представлением (View) в архитектуре MVC.
 * Его единственная задача — отрисовать панель с кнопками и предоставить
 * Контроллеру доступ к этим кнопкам через геттеры.
 *
 * Сам класс НЕ содержит логики (он не знает, что произойдет при нажатии на кнопку).
 * Он просто говорит: "Вот мои кнопки, делай с ними что хочешь".
 *
 * ОСОБЕННОСТЬ ВЕРСТКИ:
 * Для расположения кнопок используется GridBagLayout. Это самый мощный, но и самый
 * сложный менеджер компоновки в Swing. Он позволяет размещать элементы в сетке,
 * где каждая ячейка может иметь свой размер и вес.
 */
public class ButtonPanel extends JPanel {

    // Объявляем кнопки. Они final, так как сами объекты кнопок не меняются
    // (меняется только их состояние и текст).
    private final JButton btnAddManual = new JButton("Добавить вручную");
    private final JButton btnAddFromFile = new JButton("Добавить из файла");
    private final JButton btnRandomList = new JButton("Случайный список");
    private final JButton btnSaveToFile = new JButton("Сохранить в файл");
    private final JButton btnSpecialSort = new JButton("Особая сортировка");
    private final JButton btnClearList = new JButton("Очистить список");
    private final JButton btnClearLog = new JButton("Очистить лог");
    private final JButton btnExit = new JButton("Выход");

    public ButtonPanel() {
        // Устанавливаем GridBagLayout для этой панели
        setLayout(new GridBagLayout());

        // GridBagConstraints — это набор правил (ограничений) для конкретного элемента в сетке.
        // Мы создаем один объект правил и просто меняем его свойства перед добавлением каждой кнопки.
        GridBagConstraints bc = new GridBagConstraints();

        // fill = HORIZONTAL означает, что кнопка будет растягиваться по горизонтали,
        // занимая всю ширину ячейки (очень удобно для колонки кнопок).
        bc.fill = GridBagConstraints.HORIZONTAL;

        // Внутренние отступы вокруг каждой кнопки (сверху, слева, снизу, справа = 5 пикселей)
        bc.insets = new Insets(5, 5, 5, 5);

        // Все кнопки будут в колонке 0 (так как мы не меняем bc.gridx)
        bc.gridx = 0;
        // Начинаем с нулевой строки
        bc.gridy = 0;

        // weightx = 1.0 дает колонке "вес". Это значит, что при растягивании окна
        // колонка с кнопками будет забирать себе все свободное пространство по горизонтали.
        bc.weightx = 1.0;

        // Добавляем кнопки по одной, каждый раз увеличивая номер строки (gridy)
        add(btnAddManual, bc);
        bc.gridy++;
        add(btnAddFromFile, bc);
        bc.gridy++;
        add(btnRandomList, bc);
        bc.gridy++;
        add(btnSaveToFile, bc);
        bc.gridy++;
        add(btnSpecialSort, bc);
        bc.gridy++;
        add(btnClearList, bc);
        bc.gridy++;
        add(btnClearLog, bc);
        bc.gridy++;

        // --- ТРЮК С "ПРУЖИНОЙ" ---
        // Мы хотим, чтобы кнопка "Выход" была прижата к самому низу панели,
        // а не висела сразу за остальными кнопками.
        // Для этого мы добавляем невидимый "клей" (Glue).

        // weighty = 1.0 дает этой "пружине" весь свободный вес по вертикали.
        bc.weighty = 1.0;
        // Box.createVerticalGlue() создает невидимый компонент, который растягивается.
        add(Box.createVerticalGlue(), bc);
        bc.gridy++;

        // Сбрасываем вертикальный вес обратно в 0, чтобы сама кнопка "Выход"
        // не стала огромной по высоте.
        bc.weighty = 0;

        // Добавляем кнопку выхода в самую нижнюю строку
        add(btnExit, bc);
    }

    // =========================================================================
    // ГЕТТЕРЫ ДЛЯ КОНТРОЛЛЕРА
    // =========================================================================
    // Контроллер будет вызывать эти методы, чтобы получить конкретную кнопку
    // и привязать к ней ActionListener (слушатель, реагирующий на клики).
    // Это стандартный способ связи View и Controller: View отдает компоненты,
    // Controller вешает на них логику.

    public JButton getBtnAddManual() {
        return btnAddManual;
    }

    public JButton getBtnAddFromFile() {
        return btnAddFromFile;
    }

    public JButton getBtnRandomList() {
        return btnRandomList;
    }

    public JButton getBtnSaveToFile() {
        return btnSaveToFile;
    }

    public JButton getBtnSpecialSort() {
        return btnSpecialSort;
    }

    public JButton getBtnClearList() {
        return btnClearList;
    }

    public JButton getBtnClearLog() {
        return btnClearLog;
    }

    public JButton getBtnExit() {
        return btnExit;
    }
}