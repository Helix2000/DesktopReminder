package Reminder;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;


import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MainClass {

    private ArrayList<Record> events = new ArrayList<>();
    private final int windowWidth = 640;
    private final int windowHeight = 480;
    private final String windowName = "Reminder";
    private final int offsetX = 20;
    private final int offsetY = 20;
    private final int eventBoxWidth = 500;
    private final int buttonSize = 50;
    private final String eventFilename = "data";
    private final int popupWidth = 400;
    private final int popupHeight = 200;
    private final String popupName = "New record";
    private final int dateLabelWidth = 50;
    private final int frequencyLabelWidth = 100;
    private final int descriptionLabelWidth = 100;
    private final int labelHeight = 20;
    private final int okCancelButtonWidth = 100;
    private final int getOkCancelButtonHeight = 20;


    private JFrame constructWindow(int width, int height, String title) {
        JFrame window = new JFrame();
        window.setSize(width, height);
        window.setTitle(title);
        window.setLayout(null);
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        return window;
    }
    private void readData() {
        try {
            Files.createFile(Path.of(eventFilename));
        } catch (FileAlreadyExistsException ignored) {
            try (BufferedReader file = new BufferedReader(new FileReader(new File(eventFilename)))) {
                String line;
                while ((line=file.readLine())!=null) {
                    String[] buff = line.split(",");
                    events.add(new Record(buff[0], buff[1], buff[2]));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private JButton constructImageButton(Icon icon, int x, int y, int width, int height, ActionListener action) {
        JButton button = new JButton(icon);
        button.setBounds(x, y, width, height);
        button.addActionListener(action);
        return button;
    }
    private JPanel constructPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setBounds(x, y, width, height);
        return panel;
    }
    private JScrollPane constructJScrollPane(int x, int y, int width, int height) {
        String[] columns = {"Date", "Frequency", "Description"};

        DefaultTableModel tableModel = new DefaultTableModel(0, 3) {
            @Override
            public String getColumnName(int index) { return columns[index]; }
        };
        tableModel.setNumRows(events.size());
        for (int i = 0; i < events.size(); i++) {
            tableModel.setValueAt(events.get(i).getDate(), i, 0);
            tableModel.setValueAt(events.get(i).getRepeat(), i, 1);
            tableModel.setValueAt(events.get(i).getDescription(), i, 2);
        }
        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(100);
        table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.setCellSelectionEnabled(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(x, y, width, height);
        return scrollPane;
    }
    private void callPopupWindow(JFrame mainWindow, JScrollPane eventBox) {
        JDialog popup = new JDialog(mainWindow, "Add new event", true);
        popup.setSize(popupWidth, popupHeight);

        JLabel dateLabel = new JLabel("Date:");
        JLabel frequencyLabel = new JLabel("Frequency:");
        JLabel descriptionLabel = new JLabel("Description:");

        popup.add(dateLabel);
        popup.add(frequencyLabel);
        popup.add(descriptionLabel);

        JTextField frequencyText = new JTextField("Select frequency");
        SpinnerModel spinnermModel = new SpinnerNumberModel(2000, 0, 3000, 1);
        JSpinner selectYear = new JSpinner(spinnermModel);
        String[] months = {"January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"};
        JComboBox<String> selectMonth = new JComboBox<>(months);
        Integer[] days = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
        JComboBox<Integer> selectDay = new JComboBox<>(days);
        selectMonth.addActionListener(e -> {
            YearMonth buff = YearMonth.of((Integer) selectYear.getValue(), selectMonth.getSelectedIndex() + 1);
            int daysInMonth = buff.lengthOfMonth();
            while (selectDay.getItemCount() > daysInMonth) {
                if (selectDay.getSelectedIndex() == selectDay.getItemCount() - 1)
                    selectDay.setSelectedIndex(selectDay.getSelectedIndex() - 1);
                selectDay.removeItemAt(selectDay.getItemCount() - 1);
            }
            while (selectDay.getItemCount() < daysInMonth)
                selectDay.addItem(selectDay.getItemCount());
        });
        JTextField descriptionText = new JTextField("Add description");

        JButton okButton = new JButton("OK");
        okButton.addActionListener( e -> {
                    //events.add(record);
                    System.out.println(selectMonth.getItemAt(selectMonth.getSelectedIndex()));
                    events.sort(new Record.SortByDate());
                    JViewport viewport = eventBox.getViewport();
                    DefaultTableModel table = ((DefaultTableModel) ((JTable) viewport.getView()).getModel());
                    table.setNumRows(events.size());
                    for (int i = 0; i < events.size(); i++) {
                        table.setValueAt(events.get(i).getDate(), i, 0);
                        table.setValueAt(events.get(i).getRepeat(), i, 1);
                        table.setValueAt(events.get(i).getDescription(), i, 2);
                    }
                    popup.dispose();
                });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener( e -> popup.dispose());
        popup.add(okButton);
        popup.add(cancelButton);

        GroupLayout layout = new GroupLayout(popup.getContentPane());
        popup.getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(dateLabel)
                        .addComponent(frequencyLabel)
                        .addComponent(descriptionLabel)
                        .addComponent(okButton))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(selectDay)
                        .addComponent(frequencyText)
                        .addComponent(descriptionText)
                        .addComponent(cancelButton))
                    .addComponent(selectMonth)
                    .addComponent(selectYear)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(dateLabel)
                            .addComponent(selectDay)
                            .addComponent(selectMonth)
                            .addComponent(selectYear))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(frequencyLabel)
                            .addComponent(frequencyText))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(descriptionLabel)
                            .addComponent(descriptionText))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(okButton)
                            .addComponent(cancelButton))
        );
        popup.setLocationRelativeTo(null);
        popup.setVisible(true);
        //new Record("31.01.2000", "EveryFortnight", "My birthday");
    }
    private void run() {
        readData();
        JFrame mainWindow = constructWindow(windowWidth, windowHeight, windowName);
        mainWindow.setDefaultCloseOperation(EXIT_ON_CLOSE);
        mainWindow.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                try (FileWriter file = new FileWriter(eventFilename)) {
                    for (Record record : events)
                        file.write(record.toString() + '\n');
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JScrollPane eventBox = constructJScrollPane(offsetX, offsetY, eventBoxWidth, windowHeight - 3 * offsetY);
        mainWindow.add(eventBox);

        ImageIcon plusIcon = new ImageIcon("./Icons/Plus.png");
        ImageIcon minusIcon = new ImageIcon("./Icons/Minus.png");

        JPanel buttonPanel = constructPanel(eventBoxWidth + 2 * offsetX, offsetY,
                windowWidth - 3 * offsetX - eventBoxWidth, windowHeight - 3 * offsetY);
        JButton addButton = constructImageButton(plusIcon, 0, 0, buttonSize, buttonSize,
                e -> callPopupWindow(mainWindow, eventBox));
        JButton removeButton = constructImageButton(minusIcon, 0, buttonSize + offsetY,
                buttonSize, buttonSize, e -> {
                    JViewport viewport = eventBox.getViewport();
                    JTable table = (JTable)viewport.getView();
                    int[] selectedRows = table.getSelectedRows();
                    for (int i = selectedRows.length - 1; i > -1; i--) {
                        ((DefaultTableModel) table.getModel()).removeRow(selectedRows[i]);
                        events.remove(selectedRows[i]);
                    }
                });
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        mainWindow.add(buttonPanel);

        mainWindow.setVisible(true);
    }
    public static void main(String[] args) {
        MainClass mc = new MainClass();
        mc.run();
    }
}
