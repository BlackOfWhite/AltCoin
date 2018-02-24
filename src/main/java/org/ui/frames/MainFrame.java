package org.ui.frames;

import javafx.application.Platform;
import org.apache.log4j.Logger;
import org.logic.exceptions.ValueNotSetException;
import org.logic.models.misc.BalancesSet;
import org.logic.models.responses.MarketOrderResponse;
import org.logic.schedulers.monitors.model.MarketDetails;
import org.logic.transactions.model.stoploss.StopLossOption;
import org.logic.transactions.model.stoploss.StopLossOptionManager;
import org.preferences.managers.PreferenceManager;
import org.ui.Constants;
import org.ui.views.list.orders.open.ListElementOrder;
import org.ui.views.list.orders.open.OrderListCellRenderer;
import org.ui.views.list.orders.open.SLOrderListCellRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

import static org.preferences.Constants.*;

/**
 * Created by niewinskip on 2016-12-28.
 */
public class MainFrame extends JFrame {

    private final static String[] ARR_MODES = {"Classic", "Stop-loss", "Buy&Sell"};
    private static Logger logger = Logger.getLogger(MainFrame.class);
    // List of market orders
    private static JList ordersList, slOrdersList;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int width = (int) screenSize.getWidth();
    int height = (int) screenSize.getHeight();
    int count = 0;
    private JLabel labelOpenOrdersStatus, labelEmailAddress, labelApi, labelApiSecret;
    private JComboBox<String> jComboBoxMode;
    private JButton btnCreateTransaction;
    private ClassicTransactionFrame classicTransactionFrame;
    private StopLossFrame stopLossFrame;
    private EmailSetupFrame emailSetupFrame;
    private APISetupFrame apiSetupFrame;
    private PieChart pieChartPanel;
    private double LEFT_PANEL_WIDTH_RATIO = 0.4f;
    private double LIST_PANEL_WIDTH_RATIO = 0.3f;
    // Model to update orders list content
    private DefaultListModel<ListElementOrder> model = new DefaultListModel<>();
    private DefaultListModel<ListElementOrder> slModel = new DefaultListModel<>();

    public MainFrame() {
        this.setTitle("AltBot " + Constants.VERSION);
        createMenuBar();
        Container cp = getContentPane();
        GridBagLayout bag = new GridBagLayout();
        cp.setLayout(bag);

        JPanel leftPanel = createLeftPanel();
        JPanel midPanel = createListPanel(model, false);
        JPanel rightPanel = createListPanel(slModel, true);

        // Merge all main column panels. Add grid layout.
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.4;
        c.weighty = 1;
        c.gridx = 0;
        this.add(leftPanel, c);
        c.gridx = 1;
        c.weightx = 0.3;
        this.add(midPanel, c);
        c.gridx = 2;
        this.add(rightPanel, c);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);

        // Initialize only after all components sizes were calculated.
        pieChartPanel.initChart();
    }

    private JPanel createLeftPanel() {
        // Top left panel.
        JPanel leftPanel = new JPanel();
        leftPanel.setBorder(new TitledBorder(new EtchedBorder()));
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), height));
        leftPanel.setMaximumSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), height));
        leftPanel.setMinimumSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), height));

        JPanel pMain = new JPanel();
        pMain.setBorder(new TitledBorder(new EtchedBorder()));
        pMain.setLayout(new GridLayout(4, 1));
        pMain.setPreferredSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), 120));
        pMain.setMaximumSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), 120));

        // Status bar
        JPanel statusBar = new JPanel();
        statusBar.setBorder(new TitledBorder(new EtchedBorder()));
        statusBar.setLayout(new GridLayout(1, 2));
        labelOpenOrdersStatus = new JLabel();
        labelOpenOrdersStatus.setText("Open orders: ?");
        JPanel status1 = new JPanel();
        status1.setAlignmentX(Component.LEFT_ALIGNMENT);
        status1.add(labelOpenOrdersStatus);
        labelEmailAddress = new JLabel();
        labelEmailAddress.setText(PreferenceManager.getEmailAddress());
        JPanel status2 = new JPanel();
        status2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        status2.add(labelEmailAddress);
        statusBar.add(status1);
        statusBar.add(status2);
        pMain.add(statusBar);

        // Api status bar
        JPanel apiStatusBar = new JPanel();
        apiStatusBar.setBorder(new TitledBorder(new EtchedBorder()));
        apiStatusBar.setLayout(new GridLayout(1, 2));
        labelApi = new JLabel();
        labelApi.setText("API: " + PreferenceManager.getApiKeyObfucate());
        JPanel apiStatusBar1 = new JPanel();
        apiStatusBar1.setAlignmentX(Component.LEFT_ALIGNMENT);
        apiStatusBar1.add(labelApi);
        labelApiSecret = new JLabel();
        labelApiSecret.setText("Secret: " + PreferenceManager.getApiKeySecretObfucate());
        JPanel apiStatusBar2 = new JPanel();
        apiStatusBar2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        apiStatusBar2.add(labelApiSecret);
        apiStatusBar.add(apiStatusBar1);
        apiStatusBar.add(apiStatusBar2);
        pMain.add(apiStatusBar);

        // Combo box
        jComboBoxMode = new JComboBox<>(ARR_MODES);
        btnCreateTransaction = new JButton("Create transaction");
        btnCreateTransaction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = jComboBoxMode.getSelectedIndex();
                switch (id) {
                    case 0:
                        openClassicTransactionFrame();
                        break;
                    case 1:
                        openStopLossFrame();
                        break;
                    default:
                        break;
                }
            }
        });
        pMain.add(jComboBoxMode);
        pMain.add(btnCreateTransaction);
        leftPanel.add(pMain, BorderLayout.NORTH);

        // Mid view.
        pieChartPanel = new PieChart((int) (width * LEFT_PANEL_WIDTH_RATIO), height);
        leftPanel.add(pieChartPanel, BorderLayout.CENTER);

        // Bottom view
        JPanel donationPanel = new JPanel();
        donationPanel.setLayout(new GridLayout(3, 1));
        donationPanel.setMaximumSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), 60));
        donationPanel.setPreferredSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), 60));
        Border border = LineBorder.createGrayLineBorder();
        JTextField btcLabel = new JTextField("Donate BTC: " + BTC_DONATION_ADDRESS);
        btcLabel.setEditable(false);
        btcLabel.setBorder(border);
        donationPanel.add(btcLabel);
        JTextField ethLabel = new JTextField("Donate ETH: " + ETH_DONATION_ADDRESS);
        ethLabel.setEditable(false);
        ethLabel.setBorder(border);
        donationPanel.add(ethLabel);
        JTextField ltcLabel = new JTextField("Donate LTC: " + LTC_DONATION_ADDRESS);
        ltcLabel.setEditable(false);
        ltcLabel.setBorder(border);
        donationPanel.add(ltcLabel);
        leftPanel.add(donationPanel, BorderLayout.SOUTH);
        return leftPanel;
    }

    private JPanel createListPanel(DefaultListModel<ListElementOrder> model, boolean stopLoss) {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(new EtchedBorder()));
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension((int) (width * LIST_PANEL_WIDTH_RATIO), height));
        panel.setMinimumSize(new Dimension((int) (width * LIST_PANEL_WIDTH_RATIO), height));
        panel.setMaximumSize(new Dimension((int) (width * LIST_PANEL_WIDTH_RATIO), height));

        JList list = new JList();
        list.setModel(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        JScrollPane listScroller = null;
        if (stopLoss) {
            list.setCellRenderer(new SLOrderListCellRenderer());
            slOrdersList = list;
            listScroller = new JScrollPane(slOrdersList);
        } else {
            list.setCellRenderer(new OrderListCellRenderer());
            ordersList = list;
            listScroller = new JScrollPane(ordersList);
        }

        listScroller.setPreferredSize(new Dimension(panel.getMaximumSize()));
        panel.add(listScroller);
        return panel;
    }

    private void createMenuBar() {
        JMenuBar jMenuBar = new JMenuBar();
        ImageIcon icon = new ImageIcon("exit.png");

        // File
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        JMenuItem eMenuItem = new JMenuItem("Exit", icon);
        eMenuItem.setMnemonic(KeyEvent.VK_E);
        eMenuItem.setToolTipText("Exit application");
        eMenuItem.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });
        file.add(eMenuItem);

        // Settings
        JMenu settings = new JMenu("Settings");
        file.setMnemonic(KeyEvent.VK_S);
        JCheckBoxMenuItem jCheckBoxMenuItem = new JCheckBoxMenuItem("Email notifications");
        jCheckBoxMenuItem.setToolTipText("Enable email notifications.");
        jCheckBoxMenuItem.setState(PreferenceManager.isEmailNotificationEnabled());
        jCheckBoxMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PreferenceManager.changeEmailNotificationEnabled();
                jCheckBoxMenuItem.setState(PreferenceManager.isEmailNotificationEnabled());
            }
        });
        JMenuItem emailSetupMenuItem = new JMenuItem("Email setup");
        emailSetupMenuItem.setToolTipText("Setup your email address to use notifications");
        emailSetupMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emailSetupFrame == null || emailSetupFrame.isClosed()) {
                    emailSetupFrame = new EmailSetupFrame();
                }
            }
        });
        JMenuItem apiSetupMenuItem = new JMenuItem("API setup");
        apiSetupMenuItem.setToolTipText("Setup your API keys");
        apiSetupMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (apiSetupFrame == null || apiSetupFrame.isClosed()) {
                    apiSetupFrame = new APISetupFrame();
                }
            }
        });
        JCheckBoxMenuItem jCheckBoxMenuItem2 = new JCheckBoxMenuItem("Hide if insignificant");
        jCheckBoxMenuItem2.setMnemonic(KeyEvent.VK_H);
        jCheckBoxMenuItem2.setToolTipText("Hide when value less than " + CHART_SIGNIFICANT_MINIMUM + " BTC");
        jCheckBoxMenuItem2.addActionListener((ActionEvent event) -> {
            PreferenceManager.changeHideInsignificantEnabled();
            jCheckBoxMenuItem2.setState(PreferenceManager.isHideInsignificantEnabled());
        });
        jCheckBoxMenuItem2.setState(PreferenceManager.isHideInsignificantEnabled());

        jMenuBar.add(file);
        jMenuBar.add(settings);
        settings.add(apiSetupMenuItem);
        settings.add(jCheckBoxMenuItem);
        settings.add(jCheckBoxMenuItem2);
        settings.add(emailSetupMenuItem);

        jMenuBar.add(file);
        jMenuBar.add(settings);
        setJMenuBar(jMenuBar);
    }

    public void updateStatusBar(int openOrders, int buyOrdersCount) {
        this.labelOpenOrdersStatus.setText("Open orders: " + openOrders + " / Buy: " + buyOrdersCount + " / Sell: " + (openOrders - buyOrdersCount));
        this.labelOpenOrdersStatus.validate();

        String email = PreferenceManager.getEmailAddress();
        if (email.isEmpty()) {
            this.labelEmailAddress.setText("Email address not found - go to Settings menu");
        } else {
            this.labelEmailAddress.setText("Welcome: " + email);
        }
        this.labelEmailAddress.validate();
        logger.info("Status bar value: " + labelOpenOrdersStatus.getText() + " || " + email);
    }

    public void updateAPIStatusBar() {
        labelApi.setText("API: " + PreferenceManager.getApiKeyObfucate());
        labelApiSecret.setText("Secret: " + PreferenceManager.getApiKeySecretObfucate());
    }

    private void openClassicTransactionFrame() {
        if (classicTransactionFrame == null || classicTransactionFrame.isClosed()) {
            classicTransactionFrame = new ClassicTransactionFrame();
        }
    }

    private void openStopLossFrame() {
        if (stopLossFrame == null || stopLossFrame.isClosed()) {
            stopLossFrame = new StopLossFrame();
        }
    }

    public void updatePieChartFrame(Map<String, BalancesSet> map) {
        if (pieChartPanel != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    pieChartPanel.updateChart(map);
                }
            });
        }
    }

    public PieChart getPieChartFrame() {
        return pieChartPanel;
    }

    /**
     * Update orders list model - is equivalent to right outer join, where right side is model's collection.
     * Does not update fields that already exist in the model. See equals method of ListElementOrders class.
     *
     * @param openMarketOrders Collection of open market orders.
     */
    public synchronized void updateOrdersList(final MarketOrderResponse openMarketOrders, Map<String, MarketDetails> marketDetailsMap) {
        // Remove from model
        List<ListElementOrder> toRemove = new ArrayList<>();
        for (int x = 0; x < model.size(); x++) {
            boolean contains = false;
            ListElementOrder listElementOrder1 = model.getElementAt(x);
            for (MarketOrderResponse.Result result : openMarketOrders.getResult()) {
                double last = getLast(marketDetailsMap, result.getExchange());
                ListElementOrder listElementOrder2 = new ListElementOrder(result.getExchange(), result.getOrderType(), last, result.getLimit());
                if (listElementOrder2.equals(listElementOrder1)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                toRemove.add(model.getElementAt(x));
            }
        }
        for (ListElementOrder listElementOrder : toRemove) {
            model.removeElement(listElementOrder);
        }

        // Merge & update
        int x = -1;
        for (MarketOrderResponse.Result result : openMarketOrders.getResult()) {
            x++;
            double last = getLast(marketDetailsMap, result.getExchange());
            ListElementOrder listElementOrder = new ListElementOrder(result.getExchange(),
                    result.getOrderType(), last, result.getLimit());
            if (!model.contains(listElementOrder)) {
                model.addElement(listElementOrder);
                continue;
            }
            model.getElementAt(x).setLast(last);
        }
        // Sort
        Arrays.sort(new Enumeration[]{model.elements()}, ListElementOrder.getComparator());
        ordersList.validate();
        ordersList.repaint();
    }

    /**
     * Update orders list model - is equivalent to right outer join, where right side is model's collection.
     * Does not update fields that already exist in the model. See equals method of ListElementOrders class.
     *
     * @param marketDetailsMap
     */
    public synchronized void updateSLOrdersList(Map<String, MarketDetails> marketDetailsMap) {
        List<StopLossOption> stopLossOptionList = new ArrayList<>(StopLossOptionManager.getInstance().getOptionList());
        // Remove from model if not present in stop loss options anymore
        List<ListElementOrder> toRemove = new ArrayList<>();
        for (int x = 0; x < slModel.size(); x++) {
            boolean contains = false;
            ListElementOrder listElementOrder1 = slModel.getElementAt(x);
            for (StopLossOption stopLossOption : stopLossOptionList) {
                String orderType = stopLossOption.getMode() + " / " + stopLossOption.getCondition();
                if (stopLossOption.getMarketName().equals(listElementOrder1.getCoinName()) &&
                        listElementOrder1.getOrderType().equals(orderType)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                toRemove.add(slModel.getElementAt(x));
            }
        }
        for (ListElementOrder listElementOrder : toRemove) {
            slModel.removeElement(listElementOrder);
        }

        // Merge & update
        int x = -1;
        for (StopLossOption stopLossOption : stopLossOptionList) {
            x++;
            double last = getLast(marketDetailsMap, stopLossOption.getMarketName());
            double max = stopLossOption.getCancelAt() * 2;
            while (max < last) {
                max *= 2;
            }
            ListElementOrder listElementOrder = new ListElementOrder(stopLossOption.getMarketName(),
                    stopLossOption.getMode() + " / " + stopLossOption.getCondition(), last, max);
            listElementOrder.setMin(stopLossOption.getCancelAt());
            if (!slModel.contains(listElementOrder) && last > 0.0d) {
                slModel.addElement(listElementOrder);
                continue;
            }
            slModel.getElementAt(x).setLast(last);
        }
        Arrays.sort(new Enumeration[]{slModel.elements()}, ListElementOrder.getComparator());
        slOrdersList.validate();
        slOrdersList.repaint();
    }


    private double getLast(Map<String, MarketDetails> marketDetailsMap, String exchange) {
        double last;
        try {
            if (exchange.equalsIgnoreCase("ALL")) {
                return pieChartPanel.getBtcSum();
            }
            last = marketDetailsMap.get(exchange).getLast();
        } catch (ValueNotSetException e) {
            return 0.0d;
        } catch (NullPointerException e) {
            return 0.0d;
        }
        return last;
    }
}

