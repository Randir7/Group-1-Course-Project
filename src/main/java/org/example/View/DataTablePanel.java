package org.example.View;

import org.example.Model.Car;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * ПАНЕЛЬ ТАБЛИЦЫ С ДАННЫМИ (VIEW)
 *
 * Этот класс отвечает за отображение списка машин в виде таблицы.
 * В архитектуре MVC это Представление (View). Оно умеет только рисовать таблицу
 * и сообщать Контроллеру о действиях пользователя (например, по какой колонке кликнули).
 *
 * ОСОБЕННОСТИ РЕАЛИЗАЦИИ SWING:
 * 1. JTable и DefaultTableModel: Сама по себе JTable — это только визуальная часть.
 *    Данные для нее хранит "Модель таблицы" (DefaultTableModel).
 * 2. Контекстное меню: Мы отключили стандартную сортировку Swing по клику левой кнопкой
 *    и реализовали собственное меню по правому клику (ПКМ) на заголовках колонок.
 *
 * СВЯЗЬ С МОДЕЛЬЮ (MVC):
 * Обрати внимание, что View импортирует класс Car из пакета Model.
 * Это допустимо в "пассивной" архитектуре MVC: Представление знает о структуре данных,
 * чтобы уметь их отрисовывать, но не может их менять само по себе.
 */
public class DataTablePanel extends JPanel {

    // Модель данных таблицы (хранит строки и колонки)
    private final DefaultTableModel tableModel;
    // Визуальный компонент таблицы
    private final JTable table;

    // Пункты контекстного меню для сортировки (ПКМ по названию столбца)
    private final JMenuItem menuItemSortAsc;
    private final JMenuItem menuItemSortDesc;
    // Поле для контекстного меню строк (ПКМ по строке в таблице)
    private final JMenuItem menuItemCount;


    // Переменные для хранения индекса колонки и строки, по которой кликнули правой кнопкой.
    // Понадобится Контроллеру, чтобы знать, по какому полю сортировать.
    private int clickedColumnIndex = -1;
    private int clickedRowIndex = -1;

    public DataTablePanel() {
        // Панель использует BorderLayout, чтобы таблица заняла всё доступное место
        setLayout(new BorderLayout());

        // --- НАСТРОЙКА МОДЕЛИ ТАБЛИЦЫ ---
        String[] columnNames = {"Марка", "Модель", "Максимальная скорость", "Цена"};

        // Создаем анонимный класс, переопределяя DefaultTableModel,
        // чтобы настроить поведение таблицы под наши нужды.
        tableModel = new DefaultTableModel(columnNames, 0) {

            // Возвращаем тип данных для каждой колонки.
            // Это важно для правильной сортировки (чтобы цифры сортировались как числа, а не как текст)
            // и для выравнивания (например, числа по умолчанию прижимаются вправо).
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 1) return String.class; // Марка и Модель
                return Integer.class; // Скорость и Цена
            }

            // Запрещаем редактирование ячеек таблицы двойным кликом.
            // Данные должны меняться только через кнопки (Добавить, Очистить и т.д.),
            // что соответствует строгой архитектуре MVC.
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Привязываем модель к визуальной таблице
        table = new JTable(tableModel);

        // Устанавливаем минимальную ширину для колонок, чтобы они не сжимались в 0
        table.getColumnModel().getColumn(0).setMinWidth(80); // Марка
        table.getColumnModel().getColumn(1).setMinWidth(100); // Модель
        table.getColumnModel().getColumn(2).setMinWidth(250); // Макс. скорость
        table.getColumnModel().getColumn(3).setMinWidth(80);  // Цена

        // --- УКРУПНЕНИЕ ТЕКСТА В ТАБЛИЦЕ ---
        // 1. Устанавливаем шрифт для ячеек таблицы (например, размер 16)
        table.setFont(new Font("SansSerif", Font.PLAIN, 16));

        // 2. Устанавливаем шрифт для заголовков колонок (жирный, размер 16)
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));

        // 3. Увеличиваем высоту строк, чтобы крупный текст не обрезался
        // table.getRowHeight() возвращает текущую высоту, мы прибавляем к ней запас.
        table.setRowHeight(table.getRowHeight() + 10);

        // --- ОТКЛЮЧАЕМ ПЕРЕТАСКИВАНИЕ СТОЛБЦОВ ---
        table.getTableHeader().setReorderingAllowed(false);

        // 1. ОТКЛЮЧАЕМ СТАНДАРТНУЮ СОРТИРОВКУ Swing (которая срабатывает по клику ЛКМ на заголовок).
        // Мы хотим реализовать собственную сортировку через Модель.
        table.setAutoCreateRowSorter(false);
        table.setRowSorter(null);

        // 2. СОЗДАЕМ КОНТЕКСТНОЕ МЕНЮ (Popup Menu)
        JPopupMenu popupMenu = new JPopupMenu();
        menuItemSortAsc = new JMenuItem("Сортировать по возрастанию");
        menuItemSortDesc = new JMenuItem("Сортировать по убыванию");
        popupMenu.add(menuItemSortAsc);
        popupMenu.add(menuItemSortDesc);

        // 3. ВЕШАЕМ СЛУШАТЕЛЬ ПКМ НА ЗАГОЛОВКИ ТАБЛИЦЫ
        // Мы хотим, чтобы меню появлялось только при клике на "шапку" таблицы.
        table.getTableHeader().addMouseListener(new MouseAdapter() {

            // На Windows контекстное меню обычно вызывается при отпускании кнопки (Released)
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            // На macOS контекстное меню вызывается при нажатии (Pressed)
            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            // Вспомогательный метод для показа меню
            private void showPopup(MouseEvent e) {
                // isPopupTrigger() проверяет, является ли это событие вызовом контекстного меню (обычно ПКМ)
                if (e.isPopupTrigger()) {
                    // Определяем, по какой колонке кликнули, основываясь на координатах мыши
                    int col = table.columnAtPoint(e.getPoint());
                    if (col != -1) {
                        // Запоминаем колонку
                        clickedColumnIndex = col;
                        // Показываем меню в месте клика
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        // 4. СОЗДАЕМ КОНТЕКСТНОЕ МЕНЮ ДЛЯ СТРОК (Подсчет количества)
        JPopupMenu rowPopupMenu = new JPopupMenu();
        menuItemCount = new JMenuItem("Подсчитать количество в таблице");
        rowPopupMenu.add(menuItemCount);

        // 5. ВЕШАЕМ СЛУШАТЕЛЬ ПКМ НА САМУ ТАБЛИЦУ (НА СТРОКИ)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showRowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showRowPopup(e); // Кроссплатформенность (Windows/Mac)
            }

            private void showRowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) { // Проверка на ПКМ
                    // Находим индекс строки, по которой кликнули
                    int row = table.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        // Выделяем строку визуально (чтобы пользователь видел, что выбрал)
                        table.setRowSelectionInterval(row, row);
                        // Запоминаем индекс выбранной строки
                        clickedRowIndex = row;
                        // Показываем меню
                        rowPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });


        // --- ОФОРМЛЕНИЕ И КОМПОНОВКА ---
        // Без JScrollPane заголовки таблицы не будут отображаться, а сама таблица не будет прокручиваться.
        JScrollPane scrollPane = new JScrollPane(table);

        // Создаем красивую рамку с текстовым заголовком
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "ОКНО - ТАБЛИЦА С ДАННЫМИ",
                TitledBorder.LEFT, TitledBorder.TOP));

        // Добавляем прокрутку с таблицей в центр панели
        add(scrollPane, BorderLayout.CENTER);
    }

    // =========================================================================
    // API ДЛЯ КОНТРОЛЛЕРА (Геттеры)
    // =========================================================================
    // Контроллер получит эти элементы, чтобы повесить на них ActionListener.
    public JMenuItem getMenuItemSortAsc() { return menuItemSortAsc; }
    public JMenuItem getMenuItemSortDesc() { return menuItemSortDesc; }

    // Контроллер вызовет этот метод, чтобы узнать, какую колонку выбрал пользователь
    public int getClickedColumnIndex() {
        return clickedColumnIndex;
    }

    //TODO удалить в релизной версии если не найдется применения
    public JTable getTable() {
        return table;
    }

    //TODO удалить в релизной версии если не найдется применения
    public DefaultTableModel getTableModel() {
        return tableModel;
    }


    public JMenuItem getMenuItemCount() {
        return menuItemCount;
    }

    /**
     * Возвращает сырые данные из выбранной строки таблицы.
     * Метод НЕ создает объект Car, чтобы не нарушать архитектуру MVC.
     * @return Массив Object: [modelName (String), maxSpeed (Integer), price (Integer)]
     * или null, если строка не выбрана.
     */
    public Object[] getClickedRowData() {
        if (clickedRowIndex != -1) {
            Object brandName = tableModel.getValueAt(clickedRowIndex, 0);
            Object modelName = tableModel.getValueAt(clickedRowIndex, 1);
            Object speed = tableModel.getValueAt(clickedRowIndex, 2);
            Object price = tableModel.getValueAt(clickedRowIndex, 3);
            return new Object[]{brandName, modelName, speed, price};
        }
        return null;
    }

    // =========================================================================
    // МЕТОДЫ УПРАВЛЕНИЯ ДАННЫМИ (Вызываются Контроллером)
    // =========================================================================

    //TODO удалить в релизной версии если не найдется применения
//    public void addRow(Object[] rowData) {
//        tableModel.addRow(rowData);
//    }

    // setRowCount(0) — самый быстрый способ удалить все строки из DefaultTableModel
    public void clearTable() {
        tableModel.setRowCount(0);
    }

    /**
     * Полное обновление таблицы новыми данными из Модели.
     * @param cars Список машин, полученный от Модели.
     */
    public void updateTable(List<Car> cars) {
        clearTable();
        if (cars == null || cars.isEmpty()) {
            return;
        }
        for (Car car : cars) {
            Object[] rowData = new Object[]{
                    car.getBrandName(),  // Добавлено
                    car.getModelName(),
                    car.getMaxSpeed(),
                    car.getPrice()
            };
            tableModel.addRow(rowData);
        }
    }
}