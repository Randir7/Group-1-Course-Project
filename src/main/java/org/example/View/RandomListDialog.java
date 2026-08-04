package org.example.View;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

/**
 * ДИАЛОГОВОЕ ОКНО "СЛУЧАЙНЫЙ СПИСОК" (VIEW)
 *
 * Этот класс — еще один пример модального диалога (JDialog) в слое Представления.
 * Его единственная цель: спросить у пользователя, сколько случайных машин нужно создать.
 *
 * АРХИТЕКТУРНЫЙ ПРИНЦИП:
 * Диалоговое окно — это самостоятельный элемент. Оно не должно само добавлять машины
 * в Модель. Его задача — получить корректное число от пользователя и передать его
 * Контроллеру через метод getValidatedCount().
 *
 * Если пользователь ввел мусор или нажал "Отмена", окно вернет -1 (специальное значение-флаг).
 */
public class RandomListDialog extends JDialog {

    private JTextField txtCount;
    private JButton btnOk;
    private JButton btnCancel;

    // Переменная для хранения результата.
    // Изначально -1, что означает "пользователь ничего не выбрал/отменил действие".
    private int validatedCount = -1;

    public RandomListDialog(JFrame parent) {
        // Создаем модальное окно (true)
        super(parent, "Генерация случайного списка", true);
        setSize(450, 180);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // --- ВЕРХ: Инструкция ---
        JLabel lblInstructions = new JLabel("<html>Укажите, сколько случайных моделей требуется добавить в таблицу:<br>(От 1 до 100)</html>");
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(lblInstructions, BorderLayout.NORTH);

        // --- ЦЕНТР: Поле ввода ---
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        txtCount = new JTextField(10);

        // --- МАГИЯ ВАЛИДАЦИИ НА ЛЕТУ (DocumentFilter) ---
        // Мы вешаем фильтр прямо на документ текстового поля.
        // Здесь мы используем АНОНИМНЫЙ КЛАСС (new DocumentFilter() { ... }),
        // так как этот фильтр нужен нам только в этом месте и имеет уникальную логику.
        ((AbstractDocument) txtCount.getDocument()).setDocumentFilter(new DocumentFilter() {

            // Ограничение длины: максимум 3 символа (число до 100)
            private final int MAX_LENGTH = 3;

            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
                // Проверяем, что вставляемый текст состоит только из цифр ([0-9]+)
                // И что итоговая длина не превысит MAX_LENGTH
                if (text.matches("[0-9]+") && (fb.getDocument().getLength() + text.length() <= MAX_LENGTH)) {
                    super.insertString(fb, offset, text, attr);
                }
                // Если условие не выполняется, мы просто не вызываем super.insertString(),
                // и символ просто не появляется в текстовом поле!
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {
                // Аналогичная проверка при замене выделенного текста новым
                if (text.matches("[0-9]+") && (fb.getDocument().getLength() - length + text.length() <= MAX_LENGTH)) {
                    super.replace(fb, offset, length, text, attr);
                }
            }
        });

        txtCount.setColumns(10);
        inputPanel.add(txtCount);
        add(inputPanel, BorderLayout.CENTER);

        // --- НИЗ: Кнопки ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnOk = new JButton("Сгенерировать");
        btnCancel = new JButton("Отмена");
        buttonPanel.add(btnOk);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        // Навешиваем слушатели на кнопки
        setupListeners();
    }

    /**
     * Логика кнопок.
     * Обрати внимание: мы НЕ вызываем Модель здесь. Мы только проверяем данные
     * и сохраняем результат в переменную validatedCount.
     */
    private void setupListeners() {
        // Кнопка "Отмена": сбрасываем результат в -1 и закрываем окно
        btnCancel.addActionListener(e -> {
            validatedCount = -1;
            dispose(); // Уничтожаем окно
        });

        // Кнопка "ОК" (Сгенерировать)
        btnOk.addActionListener(e -> {
            String text = txtCount.getText().trim();

            // 1. Проверка на пустоту
            if (text.isEmpty()) {
                // JOptionPane — стандартный способ показать простое предупреждающее окно
                JOptionPane.showMessageDialog(this, "Поле не может быть пустым!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return; // Прерываем выполнение метода, окно не закрываем
            }

            // 2. Парсинг числа.
            // Благодаря нашему DocumentFilter мы на 100% уверены, что в строке только цифры
            // и их не больше 3. Поэтому Integer.parseInt НИКОГДА не выбросит NumberFormatException здесь.
            int count = Integer.parseInt(text);

            // 3. Проверка бизнес-диапазона (от 1 до 100)
            if (count < 1 || count > 100) {
                JOptionPane.showMessageDialog(this, "Число должно быть от 1 до 100!", "Ошибка диапазона", JOptionPane.ERROR_MESSAGE);
            } else {
                // Если все проверки пройдены, сохраняем результат и закрываем окно
                validatedCount = count;
                dispose();
            }
        });
    }

    /**
     * Единственный способ для Контроллера узнать, что выбрал пользователь.
     * Контроллер вызывает этот метод ПОСЛЕ того, как модальное окно закроется.
     *
     * @return Введенное число (от 1 до 100) или -1, если пользователь нажал "Отмена".
     */
    public int getValidatedCount() {
        return validatedCount;
    }
}