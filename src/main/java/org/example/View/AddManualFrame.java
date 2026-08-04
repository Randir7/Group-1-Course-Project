package org.example.View;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

/**
 * ДИАЛОГОВОЕ ОКНО "ДОБАВЛЕНИЕ ВРУЧНУЮ" (VIEW)
 *
 * Этот класс представляет собой модальное диалоговое окно (JDialog) для ввода
 * данных о новой машине.
 *
 * "Модальное" (modal = true) означает, что оно блокирует основное окно программы.
 * Пользователь не сможет кликнуть по главной форме, пока не закроет это окно.
 *
 * В архитектуре MVC это чистое Представление (View):
 * - Оно умеет только рисовать себя.
 * - Оно НЕ знает, как добавить машину в базу или коллекцию.
 * - Оно предоставляет геттеры (getModelText, getBtnAddModel), чтобы Контроллер
 *   мог забрать введенные данные и повесить слушателя на кнопку.
 *
 * ОСОБЕННОСТЬ: Здесь реализована "защита от дурака" на лету с помощью DocumentFilter.
 * Пользователь физически не может ввести буквы в поле скорости или превысить лимит символов.
 */
public class AddManualFrame extends JDialog {

    // Поля ввода
    private JTextField txtModel;
    private JTextField txtSpeed;
    private JTextField txtPrice;
    // Кнопки
    private JButton btnAddModel;
    private JButton btnClearFields;

    public AddManualFrame(JFrame parent) {
        // Вызываем конструктор родителя (JDialog).
        // parent - окно, которое владеет этим диалогом (для центрирования).
        // true - делает окно модальным.
        super(parent, "Добавление модели вручную", true);
        setSize(600, 250);
        // Центрируем окно относительно главного окна
        setLocationRelativeTo(parent);
        // BorderLayout делит окно на 5 частей: Север (верх), Юг (низ), Центр, Запад, Восток.
        setLayout(new BorderLayout(10, 10));

        // --- 1. ВЕРХ (СЕВЕР): Текстовое сообщение с инструкцией ---
        // Использование HTML в JLabel позволяет делать текст жирным (<b>), переносить строки (<br>) и форматировать его.
        String instructionText = "<html><b>Инструкция:</b><br>" +
                "Имя: только русские/английские буквы, цифры, дефис и пробелы. До 20 символов.<br>" +
                "Скорость: число от 0 до 1500.<br>" +
                "Цена: целое число от 0 до 2147483647.</html>";

        JLabel lblInstructions = new JLabel(instructionText);
        // Пустая рамка (EmptyBorder) служит как отступ (padding) от краев окна
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblInstructions, BorderLayout.NORTH);

        // --- 2. ЦЕНТР: Поля ввода (3 строки) ---
        // GridBagLayout — самый сложный, но и самый мощный менеджер компоновки в Swing.
        // Он позволяет выравнивать компоненты по сетке, склеивать ячейки и точно задавать их размер.
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        // Отступы между ячейками (сверху, слева, снизу, справа)
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST; // Прижимаем к левому краю
        gbc.fill = GridBagConstraints.HORIZONTAL; // Растягиваем по горизонтали

        // Строка 1: Модель
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; // Колонка 0 не растягивается (вес 0)
        formPanel.add(new JLabel("Модель:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; // Колонка 1 забирает всё свободное место (вес 1)
        txtModel = new JTextField(20);
        formPanel.add(txtModel, gbc);

        // Строка 2: Максимальная скорость
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Максимальная скорость:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtSpeed = new JTextField(20);
        formPanel.add(txtSpeed, gbc);

        // Строка 3: Цена
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Цена:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        txtPrice = new JTextField(20);
        formPanel.add(txtPrice, gbc);

        formPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(formPanel, BorderLayout.CENTER);

        // --- 3. ВОСТОК (ПРАВО): Кнопки ---
        // GridLayout располагает элементы в виде сетки (2 строки, 1 колонка)
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        btnAddModel = new JButton("Добавить модель");
        btnClearFields = new JButton("Очистить поля");
        buttonPanel.add(btnAddModel);
        buttonPanel.add(btnClearFields);

        // Оборачиваем панель с кнопками в FlowLayout, чтобы они не растягивались на всю высоту
        JPanel eastWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        eastWrapper.add(buttonPanel);
        add(eastWrapper, BorderLayout.EAST);

        // --- ВЕШАЕМ ФИЛЬТРЫ ---
        // Вызываем метод, который запретит ввод недопустимых символов
        setupInputFilters();

        // --- Логика кнопки "Очистить поля" ---
        // Эту логику можно оставить внутри View, так как она не требует обращения к Модели.
        btnClearFields.addActionListener(e -> clearFields());
    }

    /**
     * Настройка фильтров ввода для текстовых полей.
     * DocumentFilter в Swing перехватывает любой ввод (с клавиатуры, вставка из буфера обмена)
     * ДО того, как он попадет в текстовое поле. Это позволяет разрешать или запрещать символы на лету.
     */
    private void setupInputFilters() {
        // Модель: буквы, цифры, дефис, пробел. Максимум 20 символов.
        ((AbstractDocument) txtModel.getDocument()).setDocumentFilter(new InputRestrictionFilter(20, "[a-zA-Zа-яА-ЯёЁ0-9\\- ]+"));

        // Скорость: только цифры. Максимум 4 символа (до 1500)
        ((AbstractDocument) txtSpeed.getDocument()).setDocumentFilter(new InputRestrictionFilter(4, "[0-9]+"));

        // Цена: только цифры. Максимум 10 символов (до 2147483647)
        ((AbstractDocument) txtPrice.getDocument()).setDocumentFilter(new InputRestrictionFilter(10, "[0-9]+"));
    }

    /**
     * Вспомогательный класс-фильтр.
     * Объявлен как static, чтобы не держать ссылку на внешний класс (экономит память).
     */
    private static class InputRestrictionFilter extends DocumentFilter {
        private final int maxLength;
        private final String allowedCharsRegex;

        public InputRestrictionFilter(int maxLength, String allowedCharsRegex) {
            this.maxLength = maxLength;
            this.allowedCharsRegex = allowedCharsRegex;
        }

        /**
         * Метод проверки, разрешить ли ввод текста.
         * @param text Вводимый текст
         * @param currentLength Текущее количество символов в поле
         * @param replaceLength Сколько символов будет заменено (при выделении и вставке)
         */
        private boolean isValid(String text, int currentLength, int replaceLength) {
            // Разрешаем удаление (когда text пустой или null при нажатии Backspace)
            if (text == null || text.isEmpty()) {
                return currentLength - replaceLength <= maxLength;
            }
            // Проверяем символы по регулярному выражению
            if (!text.matches(allowedCharsRegex)) {
                return false;
            }
            // Проверяем, не превысит ли итоговая длина максимума
            return currentLength - replaceLength + text.length() <= maxLength;
        }

        // Вызывается при вставке текста (Ctrl+V)
        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
            if (isValid(text, fb.getDocument().getLength(), 0)) {
                super.insertString(fb, offset, text, attr);
            }
        }

        // Вызывается при печати символов или замене выделенного текста
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {
            if (isValid(text, fb.getDocument().getLength(), length)) {
                super.replace(fb, offset, length, text, attr);
            }
        }
    }

    // --- МЕТОДЫ API ДЛЯ КОНТРОЛЛЕРА ---

    public void clearFields() {
        txtModel.setText("");
        txtSpeed.setText("");
        txtPrice.setText("");
    }

    // Геттеры для текста. Контроллер вызовет их, когда пользователь нажмет "Добавить модель"
    public String getModelText() { return txtModel.getText(); }
    public String getSpeedText() { return txtSpeed.getText(); }
    public String getPriceText() { return txtPrice.getText(); }

    // Геттер для кнопки. Контроллер через него навесит ActionListener (слушатель нажатий)
    public JButton getBtnAddModel() { return btnAddModel; }

    // Метод закрытия окна
    public void closeDialog() {
        dispose(); // dispose() уничтожает окно и освобождает ресурсы, в отличие от setVisible(false)
    }
}