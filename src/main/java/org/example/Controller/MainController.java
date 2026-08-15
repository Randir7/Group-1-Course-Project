package org.example.Controller;

// Импорты классов из наших пакетов Model, View и Util.
// Контроллер обязан знать о существовании и Модели, и Представления,
// чтобы связывать их между собой.
import org.example.Model.AppModel;
import org.example.Util.MessageHandler;
import org.example.View.*;
import java.awt.Color;
import java.nio.file.Path;
import java.util.List;
import javax.swing.*;

/**
 * КЛАСС КОНТРОЛЛЕР (CONTROLLER) в паттерне MVC.
 *
 * Контроллер — это "посредник" между Моделью (данные, логика) и Представлением (GUI).
 * Его главная задача:
 * 1. Слушать события от пользователя (нажатия кнопок, клики мыши), которые приходят от View.
 * 2. Реагировать на эти события: просить Модель выполнить бизнес-логику (добавить, отсортировать, сохранить).
 * 3. Получать результат от Модели и передавать его обратно в View для отображения.
 *
 * Контроллер НЕ должен содержать бизнес-логику (вычислений, работы с БД) и НЕ должен
 * рисовать интерфейс. Он только управляет потоком данных.
 */
public class MainController {

    // Контроллер хранит ссылки на Модель и Главное окно (View).
    // Объявлены как final, так как они присваиваются один раз в конструкторе и не меняются.
    private final MainFrameView view;
    private final AppModel model;

    // Обработчик сообщений (логгер), который использует паттерн "Стратегия"
    // для вывода текста разным цветом в панель логов.
    private final MessageHandler messageHandler;

    /**
     * Конструктор Контроллера.
     * Здесь происходит "впрыскивание зависимостей" (Dependency Injection).
     * Мы передаем готовые объекты View и Model, что делает классы слабо связанными
     * (легко тестировать и заменять).
     */
    public MainController(MainFrameView view, AppModel model) {
        this.view = view;
        this.model = model;

        // Инициализируем обработчик сообщений, передавая ему панель логов из View.
        this.messageHandler = new MessageHandler(view.getLogPanel());

        // Сразу после создания контроллера настраиваем слушатели событий.
        setupButtonListeners();
        setupTableHeaderListeners();
        setupTableRowListeners();
    }

    /**
     * Метод настройки слушателей для кнопок.
     * Разбиение на отдельные приватные методы (setupAddManualButton, setupSaveToFileButton и т.д.)
     * — это отличная практика (принцип единственной ответственности).
     * Этот метод работает как "Оглавление", позволяя быстро понять, какие функции есть в приложении.
     */
    private void setupButtonListeners() {
        setupAddManualButton();
        setupAddFromFileButton();
        setupRandomListButton();
        setupSaveToFileButton();
        setupMultikeySortButton();
        setupSpecialSortButton();
        setupClearListButton();
        setupClearLogButton();
        setupExitButton();
    }

    /**
     * Настройка контекстного меню для заголовков таблицы (сортировка по ПКМ).
     * Контроллер берет пункты меню из View и вешает на них логику.
     */
    private void setupTableHeaderListeners() {
        // Слушатель для пункта "Сортировать по возрастанию"
        view.getTablePanel().getMenuItemSortAsc().addActionListener(e -> {
            // Спрашиваем у View, по какой колонке кликнул пользователь.
            int col = view.getTablePanel().getClickedColumnIndex();
            try {
                // ШАГ 1: Просим Модель отсортировать данные. Контроллер не знает, КАК модель это делает.
                model.sortCars(col, true);

                // ШАГ 2: Просим View обновить таблицу, передавая ей новые данные из Модели.
                view.getTablePanel().updateTable(model.getCars());

                // ШАГ 3: Логируем успешное действие.
                messageHandler.printMessage(new Color(0, 128, 0),
                        "Сортировка по возрастанию (колонка " + (col + 1) + ") успешно применена.");
            } catch (Exception ex) {
                // Если в Модели произошла ошибка, перехватываем её и показываем пользователю.
                messageHandler.printMessage(Color.RED, "Ошибка сортировки", ex.getMessage());
            }
        });

        // Слушатель для пункта "Сортировать по убыванию"
        view.getTablePanel().getMenuItemSortDesc().addActionListener(e -> {
            int col = view.getTablePanel().getClickedColumnIndex();
            try {
                // Передаем false, так как сортировка по убыванию (descending)
                model.sortCars(col, false);
                view.getTablePanel().updateTable(model.getCars());
                messageHandler.printMessage(new Color(0, 128, 0),
                        "Сортировка по убыванию (колонка " + (col + 1) + ") успешно применена.");
            } catch (Exception ex) {
                messageHandler.printMessage(Color.RED, "Ошибка сортировки", ex.getMessage());
            }
        });
    }

    //метод запускающий многопоточный подсчет вхождения элементов в таблицу
    private void setupTableRowListeners() {
        // --- Слушатель для пункта "Подсчитать количество в таблице" ---
        view.getTablePanel().getMenuItemCount().addActionListener(e -> {
            // 1. Просим View дать нам СЫРЫЕ данные из кликнутой строки
            Object[] rowData = view.getTablePanel().getClickedRowData();

            if (rowData != null && rowData.length == 4) {
                try {
                    // 2. Извлекаем данные. Марка и Модель - это String, числа - Integer
                    String brandName = (String) rowData[0];
                    String modelName = (String) rowData[1];
                    int speed = (int) rowData[2]; // Автоупаковка Integer -> int
                    int price = (int) rowData[3];

                    // 3. Передаем сырые данные в Модель и получаем готовый текст ответа
                    String resultMessage = model.multithreadCounting(brandName, modelName, speed, price);

                    // 4. MessageHandler выведет этот текст И в консоль, И в окно лога (черным цветом)
                    messageHandler.printMessage(Color.BLACK, resultMessage);

                } catch (IllegalArgumentException ex) {
                    // Ошибка валидации данных
                    messageHandler.printMessage(Color.RED, "Ошибка валидации", ex.getMessage());
                } catch (IllegalStateException ex) {
                    // Список пуст
                    messageHandler.printMessage(Color.RED, "Ошибка состояния", ex.getMessage());
                } catch (InterruptedException ex) {
                    // Поток был прерван
                    messageHandler.printMessage(Color.RED, "Ошибка потока", ex.getMessage());
                    Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
                } catch (Exception ex) {
                    messageHandler.printMessage(Color.RED, "Ошибка данных", "Не удалось прочитать данные строки.");
                }
            } else {
                messageHandler.printMessage(Color.RED, "Ошибка", "Строка не выбрана.");
            }
        });
    }


    /**
     * Логика кнопки "Добавить вручную".
     */
    private void setupAddManualButton() {
        // Извлекаем кнопку из панели кнопок (View) и добавляем ActionListener (лямбда-выражение).
        view.getButtonPanel().getBtnAddManual().addActionListener(e -> {
            // Создаем диалоговое окно. Модальное (true) — блокирует основное окно, пока не закроем.
            AddManualFrame addDialog = new AddManualFrame(view.getFrame());

            // Вешаем слушатель на кнопку "Добавить модель" ВНУТРИ диалогового окна.
            addDialog.getBtnAddModel().addActionListener(ev -> {
                try {
                    // Считываем данные из текстовых полей диалогового окна.
                    String brandName = addDialog.getBrandText();
                    String modelName = addDialog.getModelText();
                    // Парсим строки в числа. Может выбросить NumberFormatException.
                    int speed = Integer.parseInt(addDialog.getSpeedText());
                    int price = Integer.parseInt(addDialog.getPriceText());

                    // Передаем данные в Модель. Модель сама создаст объект и провалидирует его.
                    model.addSingleCar(brandName, modelName, speed, price);

                    // Обновляем таблицу.
                    view.getTablePanel().updateTable(model.getCars());

                    // Пишем в лог об успехе.
                    messageHandler.printMessage(new Color(0, 128, 0), "Успешно добавлена машина: " + brandName + " " + modelName);

                    // Закрываем диалоговое окно.
                    addDialog.closeDialog();
                } catch (NumberFormatException ex) {
                    // Ловим ошибку, если пользователь ввел буквы вместо цифр.
                    messageHandler.printMessage(Color.RED, "Ошибка ввода", "Скорость и цена должны быть целыми числами.");
                } catch (IllegalArgumentException ex) {
                    // Ловим ошибку валидации из Модели (например, имя длиннее 20 символов).
                    messageHandler.printMessage(Color.RED, "Ошибка валидации", ex.getMessage());
                }
            });
            // Делаем окно видимым. Эта строка остановит выполнение потока здесь,
            // пока пользователь не закроет окно (так как оно модальное).
            addDialog.setVisible(true);
        });
    }

    /**
     * Логика кнопки "Добавить из файла".
     */
    private void setupAddFromFileButton() {
        view.getButtonPanel().getBtnAddFromFile().addActionListener(e -> {
            // JFileChooser — стандартный компонент Swing для выбора файлов.
            JFileChooser fileChooser = new JFileChooser();
            // Настраиваем фильтр, чтобы показывать только .txt файлы.
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Текстовые файлы (*.txt)", "txt"));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);

            // Показываем диалог открытия файла.
            int result = fileChooser.showOpenDialog(view.getFrame());

            // Если пользователь нажал "Открыть" (APPROVE_OPTION)
            if (result == JFileChooser.APPROVE_OPTION) {
                Path filePath = fileChooser.getSelectedFile().toPath();

                // Просим Модель загрузить данные из файла.
                // Модель возвращает список строк с ошибками (если какие-то строки в файле были некорректны).
                List<String> errors = model.addCarsFromUserFile(filePath);

                // В любом случае обновляем таблицу (могли добавиться часть машин).
                view.getTablePanel().updateTable(model.getCars());

                if (errors.isEmpty()) {
                    messageHandler.printMessage(new Color(0, 128, 0), "Файл успешно загружен. Всего машин: " + model.getCars().size());
                } else {
                    // Если были ошибки парсинга, выводим каждую в лог красным цветом.
                    errors.forEach(err -> messageHandler.printMessage(Color.RED, "Ошибка парсинга", err));
                    messageHandler.printMessage(Color.BLACK, "Загрузка завершена с ошибками. Всего машин: " + model.getCars().size());
                }
            }
        });
    }

    /**
     * Логика кнопки "Случайный список".
     */
    private void setupRandomListButton() {
        view.getButtonPanel().getBtnRandomList().addActionListener(e -> {
            // Открываем диалог для ввода количества машин.
            RandomListDialog randomDialog = new RandomListDialog(view.getFrame());
            randomDialog.setVisible(true); // Ждем закрытия модального окна

            // После закрытия окна получаем введенное число.
            // Если пользователь нажал "Отмена", метод вернет -1.
            int count = randomDialog.getValidatedCount();
            if (count != -1) {
                // Просим Модель сгенерировать случайные машины.
                List<String> errors = model.addRandomCars(count);
                view.getTablePanel().updateTable(model.getCars());

                if (errors.isEmpty()) {
                    messageHandler.printMessage(new Color(0, 128, 0), "Успешно добавлено случайных машин: " + count);
                } else {
                    // Если файл ресурсов был пуст или поврежден.
                    errors.forEach(err -> messageHandler.printMessage(Color.RED, "Ошибка ресурса", err));
                }
            }
        });
    }

    /**
     * Логика кнопки "Сохранить в файл".
     */
    private void setupSaveToFileButton() {
        view.getButtonPanel().getBtnSaveToFile().addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохранить данные в файл");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Текстовые файлы (*.txt)", "txt"));
            fileChooser.setAcceptAllFileFilterUsed(false);

            // Показываем диалог сохранения файла.
            int userSelection = fileChooser.showSaveDialog(view.getFrame());
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                String filePathStr = fileToSave.getAbsolutePath();

                // Проверяем, что у файла есть расширение .txt. Если нет — добавляем.
                if (!filePathStr.toLowerCase().endsWith(".txt")) {
                    fileToSave = new java.io.File(filePathStr + ".txt");
                }
                Path filePath = fileToSave.toPath();

                // Если файл уже существует, спрашиваем подтверждение на перезапись.
                if (java.nio.file.Files.exists(filePath)) {
                    int confirm = JOptionPane.showConfirmDialog(view.getFrame(), "Файл существует. Перезаписать?", "Подтверждение", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return; // Прерываем выполнение, если нажали "Нет"
                }

                try {
                    // Делегируем запись в файл Модели.
                    model.saveDataToFile(filePath);
                    messageHandler.printMessage(new Color(0, 128, 0), "Данные сохранены в: " + fileToSave.getName());
                } catch (Exception ex) {
                    // Например, нет прав на запись в директорию.
                    messageHandler.printMessage(Color.RED, "Ошибка сохранения", ex.getMessage());
                }
            }
        });
    }

    /**
     * Логика кнопки "Общая сортировка".
     */
    private void setupMultikeySortButton() {
        view.getButtonPanel().getBtnMultikeySort().addActionListener(e -> {
            try {
                // 1. Просим Модель выполнить общую сортировку
                model.multikeySort();

                // 2. Обновляем таблицу новыми (отсортированными) данными
                view.getTablePanel().updateTable(model.getCars());

                // 3. Логируем успешное выполнение
                messageHandler.printMessage(new Color(0, 128, 0), "Общая сортировка успешно выполнена.");
            } catch (Exception ex) {
                // Если в будущем метод выбросит ошибку, перехватим её
                messageHandler.printMessage(Color.RED, "Ошибка при общей сортировке", ex.getMessage());
            }
        });
    }

    /**
     * Логика кнопки "Особая сортировка".
     */
    private void setupSpecialSortButton() {
        view.getButtonPanel().getBtnSpecialSort().addActionListener(e -> {
            try {
                // Вызываем метод Модели. Пока это заглушка.
                model.specialSort();

                view.getTablePanel().updateTable(model.getCars());

                // Выводим информационное сообщение (черный цвет).
                messageHandler.printMessage(Color.BLACK, "Особая сортировка успешно выполнена.");



            } catch (Exception ex) {
                messageHandler.printMessage(Color.RED, "Ошибка при особой сортировке", ex.getMessage());
            }
        });
    }

    /**
     * Логика кнопки "Очистить список".
     */
    private void setupClearListButton() {
        JButton clearListButton = view.getButtonPanel().getBtnClearList();
        clearListButton.addActionListener(e -> {
            //Очищаем модель (коллекцию cars)
            model.clearData();
            //Обновляем таблицу, передав пустой список (updateTable сам очистит таблицу)
            view.getTablePanel().updateTable(model.getCars());
            //Выводим сообщение в лог
            messageHandler.printMessage(Color.BLACK, "Список машин очищен.");
        });
    }

    /**
     * Логика кнопки "Очистить лог".
     */
    private void setupClearLogButton() {
        JButton clearLogButton = view.getButtonPanel().getBtnClearLog();
        clearLogButton.addActionListener(e -> {
            view.getLogPanel().clearLog();
        });
    }

    /**
     * Логика кнопки "Выход".
     */
    private void setupExitButton() {
        JButton exitButton = view.getButtonPanel().getBtnExit();
        exitButton.addActionListener(e -> {
            System.exit(0);
        });
    }


    // ========================================================
    // ТОЧКА ВХОДА В ПРОГРАММУ (Main Method)
    // ========================================================

    /**
     * С этого метода начинается выполнение любой Java-программы.
     * Здесь мы собираем все компоненты MVC вместе и запускаем приложение.
     */
    public static void main(String[] args) {
        // Swing — однопоточный фреймворк. Все изменения графического интерфейса
        // должны происходить в специальном потоке — Event Dispatch Thread (EDT).
        // SwingUtilities.invokeLater гарантирует, что создание окна выполнится в EDT.
        // Это предотвращает зависания интерфейса и ошибки отрисовки.
        SwingUtilities.invokeLater(() -> {
            try {
                // Применяем системный стиль оформления (Look and Feel).
                // Благодаря этому приложение будет выглядеть как нативная программа
                // в Windows/macOS/Linux, а не как стандартная "джавовская" серая форма.
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // --- СБОРКА ПРИЛОЖЕНИЯ ПО ПАТТЕРНУ MVC ---

            // 1. Создаем панели интерфейса (части View)
            LogConsolePanel logPanel = new LogConsolePanel();
            ButtonPanel buttonPanel = new ButtonPanel();
            DataTablePanel tablePanel = new DataTablePanel();

            // 2. Создаем главное окно, передаем ему панели (View)
            MainFrameView view = new MainFrameView(logPanel, buttonPanel, tablePanel);

            // 3. Создаем Модель (бизнес-логика и данные)
            AppModel model = new AppModel();

            // 4. Создаем Контроллер, передаем ему View и Model.
            // В конструкторе Контроллера навешиваются все слушатели, и приложение начинает жить.
            new MainController(view, model);
        });
    }
}