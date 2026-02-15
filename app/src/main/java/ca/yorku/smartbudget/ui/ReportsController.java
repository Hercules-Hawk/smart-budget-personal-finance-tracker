package ca.yorku.smartbudget.ui;

import ca.yorku.smartbudget.domain.CategoryTotal;
import ca.yorku.smartbudget.domain.PeriodRange;
import ca.yorku.smartbudget.domain.ReportResult;
import ca.yorku.smartbudget.domain.SummaryReport;
import ca.yorku.smartbudget.service.ReportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

final class ReportsController {
    private static final String SUBTITLE_GREY = "#6b7280";
    private static final String ACCENT_GREEN = "#22c55e";
    private static final String INCOME_GREEN = "#16a34a";
    private static final String EXPENSE_RED = "#dc2626";
    private static final String CARD_STYLE = "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);";

    private final ReportService reportService;
    private BorderPane pane;
    private Label incomeValueLabel;
    private Label expenseValueLabel;
    private Label balanceValueLabel;
    private Label incomeSubLabel;
    private Label expenseSubLabel;
    private ComboBox<String> periodCombo;
    private VBox customRangeFields;
    private javafx.scene.control.DatePicker startDatePicker;
    private javafx.scene.control.DatePicker endDatePicker;
    private StackPane chartArea;
    private ToggleButton pieBtn;
    private ToggleButton barBtn;
    private boolean usePieChart = true;

    ReportsController(ReportService reportService) {
        this.reportService = reportService;
    }

    BorderPane getPane() {
        return pane;
    }

    void build() {
        Label title = new Label("Reports & Analytics");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        title.setWrapText(true);
        Label subtitle = new Label("Visualize your spending patterns and trends");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        subtitle.setWrapText(true);
        VBox headerText = new VBox(6, title, subtitle);
        headerText.setPadding(new Insets(24, 24, 8, 24));

        Label periodLabel = new Label("Period Type");
        periodLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #374151;");
        periodCombo = new ComboBox<>();
        periodCombo.getItems().addAll("This Month", "All Time", "Custom Range");
        periodCombo.setValue("This Month");
        periodCombo.setMaxWidth(220);

        // Create custom range date pickers
        Label startDateLabel = new Label("Start Date");
        startDateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #374151;");
        startDatePicker = new DatePicker();
        startDatePicker.setMaxWidth(220);
        startDatePicker.setPromptText("Select start date");
        startDatePicker.valueProperty().addListener((o, oldVal, newVal) -> refresh());

        Label endDateLabel = new Label("End Date");
        endDateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #374151;");
        endDatePicker = new DatePicker();
        endDatePicker.setMaxWidth(220);
        endDatePicker.setPromptText("Select end date");
        endDatePicker.valueProperty().addListener((o, oldVal, newVal) -> refresh());

        customRangeFields = new VBox(8, startDateLabel, startDatePicker, endDateLabel, endDatePicker);
        customRangeFields.setVisible(false);
        customRangeFields.setManaged(false);

        periodCombo.valueProperty().addListener((o, oldVal, newVal) -> {
            boolean isCustomRange = "Custom Range".equals(newVal);
            customRangeFields.setVisible(isCustomRange);
            customRangeFields.setManaged(isCustomRange);
            refresh();
        });

        VBox periodSection = new VBox(8, periodLabel, periodCombo, customRangeFields);
        periodSection.setPadding(new Insets(20, 24, 20, 24));
        periodSection.setStyle(CARD_STYLE);

        incomeValueLabel = new Label("$0.00");
        expenseValueLabel = new Label("$0.00");
        balanceValueLabel = new Label("$0.00");
        Label incomeLabel = new Label("Total Income");
        incomeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        incomeValueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + INCOME_GREEN + ";");
        incomeSubLabel = new Label("This Month");
        incomeSubLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        VBox incomeCard = new VBox(10, incomeLabel, incomeValueLabel, incomeSubLabel);
        incomeCard.setPadding(new Insets(24));
        incomeCard.setStyle(CARD_STYLE);
        incomeCard.setPrefWidth(200);
        incomeCard.setMinWidth(200);
        incomeCard.setMaxWidth(200);

        Label expenseLabel = new Label("Total Expenses");
        expenseLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        expenseValueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + EXPENSE_RED + ";");
        expenseSubLabel = new Label("This Month");
        expenseSubLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        VBox expenseCard = new VBox(10, expenseLabel, expenseValueLabel, expenseSubLabel);
        expenseCard.setPadding(new Insets(24));
        expenseCard.setStyle(CARD_STYLE);
        expenseCard.setPrefWidth(200);
        expenseCard.setMinWidth(200);
        expenseCard.setMaxWidth(200);

        Label balanceLabel = new Label("Net Balance");
        balanceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        balanceValueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");
        Label balanceSub = new Label("Surplus");
        balanceSub.setStyle("-fx-font-size: 11px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        VBox balanceCard = new VBox(10, balanceLabel, balanceValueLabel, balanceSub);
        balanceCard.setPadding(new Insets(24));
        balanceCard.setStyle(CARD_STYLE);
        balanceCard.setPrefWidth(200);
        balanceCard.setMinWidth(200);
        balanceCard.setMaxWidth(200);

        HBox summaryCards = new HBox(24, incomeCard, expenseCard, balanceCard);
        summaryCards.setPadding(new Insets(24, 24, 24, 24));
        summaryCards.setAlignment(Pos.CENTER_LEFT);

        Label chartSectionTitle = new Label("Spending by Category");
        chartSectionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #111827;");
        chartSectionTitle.setWrapText(true);
        ToggleGroup chartGroup = new ToggleGroup();
        pieBtn = new ToggleButton("Pie Chart");
        pieBtn.setToggleGroup(chartGroup);
        pieBtn.setSelected(true);
        pieBtn.setStyle("-fx-background-color: " + ACCENT_GREEN + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 8 14;");
        barBtn = new ToggleButton("Bar Chart");
        barBtn.setToggleGroup(chartGroup);
        barBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #4b5563; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 8 14;");
        chartGroup.selectedToggleProperty().addListener((o, oldVal, newVal) -> {
            usePieChart = (newVal == pieBtn);
            if (usePieChart) {
                pieBtn.setStyle("-fx-background-color: " + ACCENT_GREEN + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 8 14;");
                barBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #4b5563; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 8 14;");
            } else {
                barBtn.setStyle("-fx-background-color: " + ACCENT_GREEN + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 8 14;");
                pieBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #4b5563; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 8 14;");
            }
            refresh();
        });
        HBox chartHeader = new HBox(16);
        chartHeader.getChildren().add(chartSectionTitle);
        HBox.setHgrow(chartSectionTitle, javafx.scene.layout.Priority.ALWAYS);
        chartHeader.getChildren().addAll(pieBtn, barBtn);
        chartHeader.setAlignment(Pos.CENTER_LEFT);

        chartArea = new StackPane();
        chartArea.setPadding(new Insets(24));
        chartArea.setMinHeight(280);

        VBox chartCard = new VBox(16, chartHeader, chartArea);
        chartCard.setPadding(new Insets(20, 24, 24, 24));
        chartCard.setStyle(CARD_STYLE);

        VBox mainContent = new VBox(headerText, periodSection, summaryCards, chartCard);
        mainContent.setSpacing(24);
        mainContent.setPadding(new Insets(0, 24, 32, 24));
        mainContent.setStyle("-fx-background-color: #f0f2f5;");
        mainContent.setMinWidth(400);

        ScrollPane scroll = new ScrollPane(mainContent);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setStyle("-fx-background-color: #f0f2f5; -fx-background: #f0f2f5;");
        scroll.setPadding(new Insets(0));
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        pane = new BorderPane(scroll);
        refresh();
    }

    void refresh() {
        if (incomeValueLabel == null) return;
        PeriodRange range = currentRange();
        ReportResult result = reportService.generateReport(range);
        SummaryReport summary = result.getSummary();

        incomeValueLabel.setText("$" + summary.getTotalIncome().setScale(2, RoundingMode.HALF_UP).toPlainString());
        expenseValueLabel.setText("$" + summary.getTotalExpense().setScale(2, RoundingMode.HALF_UP).toPlainString());
        balanceValueLabel.setText("$" + summary.getBalance().setScale(2, RoundingMode.HALF_UP).toPlainString());

        // Update period labels dynamically based on selection
        String periodText = getPeriodLabelText();
        incomeSubLabel.setText(periodText);
        expenseSubLabel.setText(periodText);

        List<CategoryTotal> breakdown = result.getBreakdown();
        chartArea.getChildren().clear();
        if (!result.isHasData() || breakdown.isEmpty()) {
            Label placeholder = new Label(result.getMessage() != null ? result.getMessage() : "Add expense transactions to see spending by category here.");
            placeholder.setWrapText(true);
            placeholder.setStyle("-fx-text-fill: " + SUBTITLE_GREY + "; -fx-font-size: 14px;");
            placeholder.setMaxWidth(400);
            chartArea.getChildren().add(placeholder);
        } else {
            if (usePieChart) {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                for (CategoryTotal ct : breakdown) {
                    double val = ct.getTotal().doubleValue();
                    if (val > 0) {
                        pieData.add(new PieChart.Data(ct.getCategory().getDisplayName(), val));
                    }
                }
                PieChart chart = new PieChart(pieData);
                chart.setTitle("Spending by Category");
                chart.setLegendVisible(true);
                chartArea.getChildren().add(chart);
            } else {
                CategoryAxis xAxis = new CategoryAxis();
                NumberAxis yAxis = new NumberAxis();
                yAxis.setLabel("Amount ($)");
                BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
                chart.setTitle("Spending by Category");
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                for (CategoryTotal ct : breakdown) {
                    series.getData().add(new XYChart.Data<>(ct.getCategory().getDisplayName(), ct.getTotal().doubleValue()));
                }
                chart.getData().add(series);
                chart.setLegendVisible(false);
                chartArea.getChildren().add(chart);
            }
        }
    }

    private PeriodRange currentRange() {
        String period = periodCombo != null ? periodCombo.getValue() : "This Month";
        if ("This Month".equals(period)) {
            return PeriodRange.ofMonth(YearMonth.now());
        } else if ("Custom Range".equals(period)) {
            java.time.LocalDate start = startDatePicker != null ? startDatePicker.getValue() : null;
            java.time.LocalDate end = endDatePicker != null ? endDatePicker.getValue() : null;
            if (start != null && end != null) {
                return new PeriodRange(start, end);
            }
            // If dates not selected, return all time
            return PeriodRange.allTime();
        }
        return PeriodRange.allTime();
    }

    private String getPeriodLabelText() {
        String period = periodCombo != null ? periodCombo.getValue() : "This Month";
        if ("All Time".equals(period)) {
            return "All Time";
        } else if ("This Month".equals(period)) {
            return "This Month";
        } else if ("Custom Range".equals(period)) {
            java.time.LocalDate start = startDatePicker != null ? startDatePicker.getValue() : null;
            java.time.LocalDate end = endDatePicker != null ? endDatePicker.getValue() : null;
            if (start != null && end != null) {
                return start.toString() + " to " + end.toString();
            }
            return "Custom Range";
        }
        return "This Month";
    }
}
