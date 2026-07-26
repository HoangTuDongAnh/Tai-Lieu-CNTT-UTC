document.addEventListener("DOMContentLoaded", function () {
    if (typeof Chart === "undefined") return;

    const page = document.querySelector(".dashboard-page");
    const insightsUrl = page?.dataset.dashboardInsightsUrl || "";
    const lang = document.documentElement.lang === "en" ? "en" : "vi";

    const text = lang === "en"
        ? {
            income: "Income",
            expense: "Expense",
            noData: "No data",
            trendPrefix: "Income and expense in",
            donutPrefix: "Spending categories in",
            over: "Over budget",
            reached: "Reached limit",
            near: "Near limit",
            remaining: "Remaining",
            budgetEmptyTitle: "No budgets need attention",
            budgetEmptyText: "No budget category is over or near its warning threshold this month.",
            donutEmptyTitle: "No spending data yet",
            donutEmptyText: "The doughnut chart will appear once there are expense transactions in the selected period.",
            activeCategories: "Active categories",
            shareInPeriod: "Share in period",
            totalInPeriod: "Total expense in period",
            todayExpenseEyebrow: "Today's breakdown",
            todayExpenseTitle: "Today's expense categories",
            todayExpenseSubtitle: "The same doughnut view from Transactions, simplified to a daily expense snapshot.",
            todayIncomeEyebrow: "Today's breakdown",
            todayIncomeTitle: "Today's income categories",
            todayIncomeSubtitle: "Switch to income to review which categories generated cash inflow today.",
            todayExpenseCenter: "Today's expense total",
            todayIncomeCenter: "Today's income total",
            todayLegendEmptyTitle: "No matching data yet",
            todayLegendEmptyText: "The chart will appear once today has matching transactions for the selected type."
        }
        : {
            income: "Thu",
            expense: "Chi",
            noData: "Chưa có",
            trendPrefix: "Thu và chi trong",
            donutPrefix: "Danh mục chi tiêu trong",
            over: "Đã vượt mức",
            reached: "Đã chạm mức",
            near: "Sắp chạm mức",
            remaining: "Còn lại",
            budgetEmptyTitle: "Chưa có ngân sách nào vượt ngưỡng",
            budgetEmptyText: "Hiện chưa có danh mục nào vượt hoặc gần chạm mức cảnh báo trong tháng này.",
            donutEmptyTitle: "Chưa có dữ liệu chi tiêu",
            donutEmptyText: "Biểu đồ tròn sẽ xuất hiện khi kỳ đang xem có giao dịch chi tiêu.",
            activeCategories: "Danh mục phát sinh",
            shareInPeriod: "Tỷ trọng trong kỳ",
            totalInPeriod: "Tổng chi trong kỳ",
            todayExpenseEyebrow: "Cơ cấu giao dịch hôm nay",
            todayExpenseTitle: "Danh mục chi tiêu trong hôm nay",
            todayExpenseSubtitle: "Giữ nguyên trải nghiệm biểu đồ tròn như trang Giao dịch, nhưng rút gọn còn góc nhìn chi tiêu trong ngày.",
            todayIncomeEyebrow: "Cơ cấu giao dịch hôm nay",
            todayIncomeTitle: "Danh mục thu nhập trong hôm nay",
            todayIncomeSubtitle: "Chuyển sang Thu nhập để xem hôm nay tiền vào chủ yếu đến từ danh mục nào.",
            todayExpenseCenter: "Tổng chi hôm nay",
            todayIncomeCenter: "Tổng thu hôm nay",
            todayLegendEmptyTitle: "Chưa có dữ liệu phù hợp",
            todayLegendEmptyText: "Biểu đồ sẽ hiển thị khi hôm nay có giao dịch đúng với loại đang chọn."
        };

    const parseJson = (value, fallback = []) => {
        try { return JSON.parse(value || JSON.stringify(fallback)); } catch { return fallback; }
    };
    const formatNumber = value => Number(value || 0).toLocaleString(lang === "en" ? "en-US" : "vi-VN");
    const clamp = (n, min, max) => Math.max(min, Math.min(max, n));
    const defaultPalette = ["#2166ad", "#df5d2c", "#9a3d61", "#4f8f4f", "#6f63ff", "#f0a11a"];

    const trendCanvas = document.getElementById("dashboardCashflowChart");
    const donutCanvas = document.getElementById("dashboardExpenseDonutChart");
    const trendTitle = document.getElementById("dashboardTrendTitle");
    const donutTitle = document.getElementById("dashboardDonutTitle");
    const rangeIncomeEl = document.getElementById("dashboardRangeIncome");
    const rangeExpenseEl = document.getElementById("dashboardRangeExpense");
    const rangeTxnEl = document.getElementById("dashboardRangeTransactionCount");
    const busiestEl = document.getElementById("dashboardBusiestLabel");
    const categoryPanel = document.getElementById("dashboardCategoryPanel");
    const budgetPanel = document.getElementById("dashboardBudgetPanel");
    const chips = Array.from(document.querySelectorAll("[data-period-chip]"));

    const todayCard = document.querySelector(".dashboard-today-breakdown-card");
    const todayCanvas = document.getElementById("dashboardTodayBreakdownChart");
    const todayLegend = document.getElementById("dashboardTodayBreakdownLegend");
    const todayCount = document.getElementById("dashboardTodayBreakdownCount");
    const todaySummaryLabel = document.getElementById("dashboardTodayBreakdownSummaryLabel");
    const todayEyebrow = document.getElementById("dashboardTodayBreakdownEyebrow");
    const todayTitle = document.getElementById("dashboardTodayBreakdownTitle");
    const todaySubtitle = document.getElementById("dashboardTodayBreakdownSubtitle");
    const todayTabs = Array.from(document.querySelectorAll("#dashboardTodayTypeTabs [data-type]"));

    let trendChart = null;
    let donutChart = null;
    let todayBreakdownChart = null;
    let todayBreakdownType = "expense";

    function escapeHtml(value) {
        return String(value ?? "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/\"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function alphaColor(hex, alpha) {
        const clean = String(hex || "").replace("#", "").trim();
        if (!/^[0-9a-fA-F]{6}$/.test(clean)) return `rgba(105,108,255,${alpha})`;
        const r = parseInt(clean.slice(0, 2), 16);
        const g = parseInt(clean.slice(2, 4), 16);
        const b = parseInt(clean.slice(4, 6), 16);
        return `rgba(${r}, ${g}, ${b}, ${alpha})`;
    }

    function iconMarkup(icon, label) {
        const safeLabel = escapeHtml(label || text.noData);
        if (icon && (String(icon).includes("/") || /\.(svg|png|webp|jpg|jpeg)$/i.test(String(icon)))) {
            return `<img src="${escapeHtml(String(icon))}" alt="${safeLabel}" class="tx-inline-icon tx-inline-icon-sm" />`;
        }
        const iconClass = icon && String(icon).trim() ? String(icon).trim() : "bx bx-category";
        return `<i class="${escapeHtml(iconClass)}"></i>`;
    }

    function normalizeBreakdownItems(items) {
        return (items || []).map((item, index) => ({
            categoryId: item.categoryId ?? item.CategoryId ?? index,
            categoryName: item.categoryName ?? item.CategoryName ?? text.noData,
            icon: item.icon ?? item.Icon ?? "bx bx-category",
            color: item.color ?? item.Color ?? defaultPalette[index % defaultPalette.length],
            totalAmount: Number(item.totalAmount ?? item.TotalAmount ?? item.value ?? item.Value ?? 0),
            percentage: Number(item.percentage ?? item.Percentage ?? 0)
        }));
    }

    function createTrendChart(labels, income, expense) {
        if (!trendCanvas) return;
        if (trendChart) trendChart.destroy();
        trendChart = new Chart(trendCanvas, {
            type: "line",
            data: {
                labels,
                datasets: [
                    {
                        label: text.income,
                        data: income,
                        borderColor: "rgba(39, 169, 111, 0.95)",
                        backgroundColor: "rgba(39, 169, 111, 0.14)",
                        tension: 0.32,
                        fill: false,
                        pointRadius: 3,
                        pointHoverRadius: 5,
                        borderWidth: 3
                    },
                    {
                        label: text.expense,
                        data: expense,
                        borderColor: "rgba(239, 95, 67, 0.95)",
                        backgroundColor: "rgba(239, 95, 67, 0.14)",
                        tension: 0.32,
                        fill: false,
                        pointRadius: 3,
                        pointHoverRadius: 5,
                        borderWidth: 3
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: "index", intersect: false },
                plugins: {
                    legend: {
                        position: "top",
                        align: "end",
                        labels: { usePointStyle: true, boxWidth: 10, boxHeight: 10, color: "#637381", font: { weight: 700 } }
                    },
                    tooltip: {
                        callbacks: { label: context => `${context.dataset.label}: ${formatNumber(context.parsed.y)}` }
                    }
                },
                scales: {
                    x: { grid: { display: false }, ticks: { color: "#8291a6", font: { weight: 600 } } },
                    y: {
                        beginAtZero: true,
                        ticks: { color: "#8291a6", callback: value => formatNumber(value) },
                        grid: { color: "rgba(130, 145, 166, 0.14)" }
                    }
                }
            }
        });
    }

    function updateDonutCenter(chart, activeIndex = null) {
        const shell = chart?.canvas?.parentElement;
        if (!shell) return;
        let center = shell.querySelector(".dashboard-donut-center");
        if (!center) {
            center = document.createElement("div");
            center.className = "dashboard-donut-center";
            shell.appendChild(center);
        }

        const data = chart.$items || [];
        const defaultState = chart.$defaultCenter || { label: text.totalInPeriod, value: 0, meta: `${data.length} ${text.activeCategories.toLowerCase()}` };
        const active = Number.isInteger(activeIndex) && data[activeIndex] ? data[activeIndex] : null;
        const state = active
            ? {
                label: active.categoryName,
                value: active.totalAmount,
                meta: `${Number(active.percentage || 0).toFixed(2).replace(/\.00$/, "")}% ${text.shareInPeriod.toLowerCase()}`
            }
            : defaultState;

        center.innerHTML = `<span class="dashboard-donut-center__label">${state.label}</span><span class="dashboard-donut-center__value">${formatNumber(state.value)}</span><span class="dashboard-donut-center__meta">${state.meta}</span>`;
    }

    function setActiveLegendRow(activeIndex = null, colors = []) {
        document.querySelectorAll("#dashboardCategoryLegend .legend-row").forEach((row, index) => {
            const isActive = activeIndex === index;
            row.classList.toggle("is-active", isActive);
            row.style.setProperty("--legend-active-color", colors[index] || "#d9e5f6");
        });
    }

    function createDonutChart(labels, series, colors, items = []) {
        const canvas = document.getElementById("dashboardExpenseDonutChart");
        if (!canvas) return;
        if (donutChart) donutChart.destroy();

        donutChart = new Chart(canvas, {
            type: "doughnut",
            data: {
                labels,
                datasets: [{
                    data: series,
                    backgroundColor: colors,
                    borderColor: "#fff",
                    borderWidth: 4,
                    hoverOffset: 8,
                    spacing: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: "63%",
                layout: { padding: 10 },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: context => {
                                const value = Number(context.parsed || 0);
                                const total = series.reduce((sum, current) => sum + Number(current || 0), 0);
                                const percent = total ? (value / total) * 100 : 0;
                                return `${context.label}: ${formatNumber(value)} (${percent.toFixed(1).replace(/\.0$/, "")}%)`;
                            }
                        }
                    }
                },
                onHover(event, activeElements, chart) {
                    const activeIndex = activeElements?.length ? activeElements[0].index : null;
                    updateDonutCenter(chart, activeIndex);
                    setActiveLegendRow(activeIndex, colors);
                    chart.canvas.style.cursor = activeElements?.length ? "pointer" : "default";
                }
            }
        });

        donutChart.$items = items;
        donutChart.$defaultCenter = {
            label: text.totalInPeriod,
            value: series.reduce((sum, current) => sum + Number(current || 0), 0),
            meta: `${items.length} ${text.activeCategories.toLowerCase()}`
        };
        updateDonutCenter(donutChart, null);
    }

    function bindLegendHover(colors) {
        document.querySelectorAll("#dashboardCategoryLegend .legend-row").forEach((row) => {
            row.addEventListener("mouseenter", () => {
                const index = Number(row.dataset.legendIndex);
                if (!Number.isInteger(index) || !donutChart) return;
                const meta = donutChart.getDatasetMeta(0);
                const element = meta?.data?.[index];
                if (!element) return;
                donutChart.setActiveElements([{ datasetIndex: 0, index }]);
                donutChart.tooltip?.setActiveElements([{ datasetIndex: 0, index }], { x: element.x, y: element.y });
                donutChart.update();
                updateDonutCenter(donutChart, index);
                setActiveLegendRow(index, colors);
            });
            row.addEventListener("mouseleave", () => {
                if (!donutChart) return;
                donutChart.setActiveElements([]);
                donutChart.tooltip?.setActiveElements([], { x: 0, y: 0 });
                donutChart.update();
                updateDonutCenter(donutChart, null);
                setActiveLegendRow(null, colors);
            });
        });
    }

    function renderLegend(items) {
        if (!categoryPanel) return;
        const normalized = normalizeBreakdownItems(items);
        if (!normalized.length) {
            categoryPanel.innerHTML = `
                <div class="dashboard-empty-state small-empty">
                    <span class="dashboard-empty-icon"><i class="bx bx-pie-chart-alt-2"></i></span>
                    <h3>${text.donutEmptyTitle}</h3>
                    <p>${text.donutEmptyText}</p>
                </div>`;
            return;
        }

        const colors = normalized.map((x, i) => x.color || defaultPalette[i % defaultPalette.length]);
        const labels = normalized.map(x => x.categoryName);
        const series = normalized.map(x => Number(x.totalAmount || 0));

        categoryPanel.innerHTML = `
            <div class="dashboard-breakdown-layout">
                <div class="dashboard-breakdown-chart-card">
                    <div class="dashboard-donut-shell">
                        <canvas id="dashboardExpenseDonutChart"></canvas>
                    </div>
                </div>
                <div class="dashboard-breakdown-sidecard">
                    <div class="dashboard-breakdown-summary">
                        <div class="summary-pill summary-pill--highlight">
                            <span>${text.activeCategories}</span>
                            <strong>${normalized.length}</strong>
                        </div>
                    </div>
                    <div class="dashboard-legend-shell">
                        <div class="dashboard-legend-scroll" id="dashboardCategoryLegend"></div>
                    </div>
                </div>
            </div>`;

        const legend = categoryPanel.querySelector("#dashboardCategoryLegend");
        legend.innerHTML = normalized.map((item, index) => {
            const iconClass = item.icon && String(item.icon).trim() ? String(item.icon).trim() : "bx bx-category";
            return `
                <div class="legend-row" data-legend-index="${index}">
                    <div class="legend-left">
                        <span class="legend-icon" style="--legend-color:${colors[index]}"><i class="${iconClass}"></i></span>
                        <div>
                            <strong>${item.categoryName}</strong>
                            <small>${text.shareInPeriod}</small>
                        </div>
                    </div>
                    <div class="legend-right">
                        <strong>${Number(item.percentage || 0).toFixed(2).replace(/\.00$/, "")}%</strong>
                        <small>${formatNumber(item.totalAmount)}</small>
                    </div>
                </div>`;
        }).join("");

        createDonutChart(labels, series, colors, normalized);
        bindLegendHover(colors);
    }

    function renderBudgetAlerts(items) {
        if (!budgetPanel) return;
        if (!items || !items.length) {
            budgetPanel.innerHTML = `
                <div class="dashboard-empty-state small-empty alert-empty-ok">
                    <span class="dashboard-empty-icon success"><i class="bx bx-check-shield"></i></span>
                    <h3>${text.budgetEmptyTitle}</h3>
                    <p>${text.budgetEmptyText}</p>
                </div>`;
            return;
        }

        budgetPanel.innerHTML = `<div class="budget-alert-grid" id="dashboardBudgetAlerts"></div>`;
        const grid = budgetPanel.querySelector("#dashboardBudgetAlerts");
        grid.innerHTML = items.map(item => {
            const status = item.status === "over" ? text.over : item.status === "reached" ? text.reached : text.near;
            const statusClass = item.status === "over" ? "over" : item.status === "reached" ? "reached" : "warning";
            const width = clamp(Number(item.percentageUsed || 0), 0, 100);
            return `
                <article class="budget-alert-card ${statusClass}">
                    <div class="budget-alert-head">
                        <div class="budget-alert-title-wrap">
                            <span class="budget-alert-dot" style="background:${item.categoryColor}"></span>
                            <div>
                                <h3>${item.categoryName}</h3>
                                <p>${status}</p>
                            </div>
                        </div>
                        <span class="budget-alert-badge">${Math.round(Number(item.percentageUsed || 0))}%</span>
                    </div>
                    <div class="budget-alert-bar"><span style="width:${width}%"></span></div>
                    <div class="budget-alert-meta">
                        <span>${formatNumber(item.spentAmount)} / ${formatNumber(item.limitAmount)}</span>
                        <span>${text.remaining}: ${formatNumber(item.remainingAmount)}</span>
                    </div>
                </article>`;
        }).join("");
    }

    function getTodayBreakdownData(type) {
        if (!todayCard) return [];
        const raw = type === "income" ? todayCard.dataset.todayIncome : todayCard.dataset.todayExpense;
        return normalizeBreakdownItems(parseJson(raw, []));
    }

    function getTodayCopy(type) {
        if (type === "income") {
            return {
                eyebrow: text.todayIncomeEyebrow,
                title: text.todayIncomeTitle,
                subtitle: text.todayIncomeSubtitle,
                centerLabel: text.todayIncomeCenter
            };
        }
        return {
            eyebrow: text.todayExpenseEyebrow,
            title: text.todayExpenseTitle,
            subtitle: text.todayExpenseSubtitle,
            centerLabel: text.todayExpenseCenter
        };
    }

    function updateTodayBreakdownCenter(chart, activeIndex = null) {
        const shell = chart?.canvas?.parentElement;
        if (!shell) return;

        let center = shell.querySelector(".tx-breakdown-center");
        if (!center) {
            center = document.createElement("div");
            center.className = "tx-breakdown-center";
            shell.appendChild(center);
        }

        const items = chart.$items || [];
        const defaultState = chart.$defaultCenter || {
            label: getTodayCopy(todayBreakdownType).centerLabel,
            value: 0,
            meta: `0 ${text.activeCategories.toLowerCase()}`
        };
        const active = Number.isInteger(activeIndex) && items[activeIndex] ? items[activeIndex] : null;
        const total = items.reduce((sum, item) => sum + Number(item.value || 0), 0);
        const state = active
            ? {
                label: active.name,
                value: Number(active.value || 0),
                meta: total > 0
                    ? `${((Number(active.value || 0) / total) * 100).toFixed(2).replace(/\.00$/, "")}% ${text.shareInPeriod.toLowerCase()}`
                    : `0% ${text.shareInPeriod.toLowerCase()}`
            }
            : defaultState;

        center.innerHTML = `
            <span class="tx-breakdown-center__label">${escapeHtml(state.label)}</span>
            <strong class="tx-breakdown-center__value">${escapeHtml(formatNumber(state.value) + " VND")}</strong>
            <span class="tx-breakdown-center__meta">${escapeHtml(state.meta)}</span>`;
    }

    function setTodayLegendActive(activeIndex = null, colors = []) {
        document.querySelectorAll("#dashboardTodayBreakdownLegend .legend-row").forEach((row, index) => {
            const isActive = activeIndex === index;
            row.classList.toggle("is-active", isActive);
            row.style.setProperty("--legend-active-color", colors[index] || "#d9e5f6");
        });
    }

    function bindTodayLegendHover(colors) {
        document.querySelectorAll("#dashboardTodayBreakdownLegend .legend-row").forEach((row) => {
            row.addEventListener("mouseenter", () => {
                const index = Number(row.dataset.legendIndex);
                if (!Number.isInteger(index) || !todayBreakdownChart) return;
                const meta = todayBreakdownChart.getDatasetMeta(0);
                const element = meta?.data?.[index];
                if (!element) return;
                todayBreakdownChart.setActiveElements([{ datasetIndex: 0, index }]);
                todayBreakdownChart.tooltip?.setActiveElements([{ datasetIndex: 0, index }], { x: element.x, y: element.y });
                todayBreakdownChart.update();
                updateTodayBreakdownCenter(todayBreakdownChart, index);
                setTodayLegendActive(index, colors);
            });

            row.addEventListener("mouseleave", () => {
                if (!todayBreakdownChart) return;
                todayBreakdownChart.setActiveElements([]);
                todayBreakdownChart.tooltip?.setActiveElements([], { x: 0, y: 0 });
                todayBreakdownChart.update();
                updateTodayBreakdownCenter(todayBreakdownChart, null);
                setTodayLegendActive(null, colors);
            });
        });
    }

    function renderTodayBreakdownLegend(items) {
        if (!todayLegend) return;
        if (!items.length) {
            todayLegend.innerHTML = `
                <div class="tx-breakdown-empty">
                    <span class="tx-breakdown-empty__icon"><i class="bx bx-pie-chart-alt-2"></i></span>
                    <h3>${text.todayLegendEmptyTitle}</h3>
                    <p>${text.todayLegendEmptyText}</p>
                </div>`;
            return;
        }

        const total = items.reduce((sum, item) => sum + Number(item.value || 0), 0);
        todayLegend.innerHTML = items.map((item, index) => `
            <div class="legend-row" data-legend-index="${index}" style="--legend-row-color:${item.color}">
                <div class="legend-left">
                    <span class="legend-icon tx-legend-icon" style="--legend-color:${item.color}; background:${alphaColor(item.color, 0.12)}; color:${item.color}">${iconMarkup(item.icon, item.name)}</span>
                    <div>
                        <strong>${escapeHtml(item.name)}</strong>
                        <small>${text.shareInPeriod}</small>
                    </div>
                </div>
                <div class="legend-right">
                    <strong>${total > 0 ? `${((item.value / total) * 100).toFixed(2).replace(/\.00$/, "")}%` : "0%"}</strong>
                    <small>${formatNumber(item.value)} VND</small>
                </div>
            </div>`).join("");
    }

    function renderTodayBreakdown() {
        if (!todayCanvas || !todayCard) return;

        const copy = getTodayCopy(todayBreakdownType);
        const items = getTodayBreakdownData(todayBreakdownType).map((item, index) => ({
            name: item.categoryName,
            value: Number(item.totalAmount || 0),
            color: item.color || defaultPalette[index % defaultPalette.length],
            icon: item.icon || "bx bx-category"
        }));
        const total = items.reduce((sum, item) => sum + Number(item.value || 0), 0);
        const colors = items.map((item, index) => item.color || defaultPalette[index % defaultPalette.length]);

        if (todayEyebrow) todayEyebrow.textContent = copy.eyebrow;
        if (todayTitle) todayTitle.textContent = copy.title;
        if (todaySubtitle) todaySubtitle.textContent = copy.subtitle;
        if (todaySummaryLabel) todaySummaryLabel.textContent = text.activeCategories;
        if (todayCount) todayCount.textContent = String(items.length);

        renderTodayBreakdownLegend(items);

        if (todayBreakdownChart) {
            todayBreakdownChart.destroy();
            todayBreakdownChart = null;
        }

        if (!items.length) {
            todayBreakdownChart = new Chart(todayCanvas, {
                type: "doughnut",
                data: {
                    labels: [text.noData],
                    datasets: [{
                        data: [1],
                        backgroundColor: ["#edf1f7"],
                        borderWidth: 0,
                        hoverOffset: 0,
                        spacing: 0
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    cutout: "63%",
                    plugins: { legend: { display: false }, tooltip: { enabled: false } }
                }
            });
            todayBreakdownChart.$items = [];
            todayBreakdownChart.$defaultCenter = {
                label: copy.centerLabel,
                value: 0,
                meta: `0 ${text.activeCategories.toLowerCase()}`
            };
            updateTodayBreakdownCenter(todayBreakdownChart, null);
            setTodayLegendActive(null, colors);
            return;
        }

        todayBreakdownChart = new Chart(todayCanvas, {
            type: "doughnut",
            data: {
                labels: items.map((item) => item.name),
                datasets: [{
                    data: items.map((item) => item.value),
                    backgroundColor: colors,
                    borderColor: "#ffffff",
                    borderWidth: 4,
                    hoverOffset: 8,
                    spacing: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: "63%",
                layout: { padding: 10 },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: (ctx) => {
                                const value = Number(ctx.parsed || 0);
                                const percent = total ? (value / total) * 100 : 0;
                                return `${ctx.label}: ${formatNumber(value)} VND (${percent.toFixed(1).replace(/\.0$/, "")}%)`;
                            }
                        }
                    }
                },
                onHover(event, activeElements, chart) {
                    const activeIndex = activeElements?.length ? activeElements[0].index : null;
                    updateTodayBreakdownCenter(chart, activeIndex);
                    setTodayLegendActive(activeIndex, colors);
                    chart.canvas.style.cursor = activeElements?.length ? "pointer" : "default";
                }
            }
        });

        todayBreakdownChart.$items = items;
        todayBreakdownChart.$defaultCenter = {
            label: copy.centerLabel,
            value: total,
            meta: `${items.length} ${text.activeCategories.toLowerCase()}`
        };

        updateTodayBreakdownCenter(todayBreakdownChart, null);
        setTodayLegendActive(null, colors);
        bindTodayLegendHover(colors);
    }

    async function loadPeriod(period) {
        if (!insightsUrl) return;
        chips.forEach(btn => btn.disabled = true);
        page?.classList.add("dashboard-loading");
        try {
            const response = await fetch(`${insightsUrl}?period=${encodeURIComponent(period)}`, {
                headers: { "X-Requested-With": "XMLHttpRequest" }
            });
            const data = await response.json();
            if (!response.ok || !data?.success) throw new Error(data?.message || "Failed");

            chips.forEach(btn => btn.classList.toggle("active", btn.dataset.periodChip === data.periodPreset));
            if (trendTitle) trendTitle.textContent = `${text.trendPrefix} ${String(data.periodLabel || "").toLowerCase()}`;
            if (donutTitle) donutTitle.textContent = `${text.donutPrefix} ${String(data.periodLabel || "").toLowerCase()}`;
            if (rangeIncomeEl) rangeIncomeEl.textContent = formatNumber(data.rangeIncome);
            if (rangeExpenseEl) rangeExpenseEl.textContent = formatNumber(data.rangeExpense);
            if (rangeTxnEl) rangeTxnEl.textContent = formatNumber(data.rangeTransactionCount);
            if (busiestEl) busiestEl.textContent = data.busiestLabel || text.noData;

            createTrendChart(data.trendLabels || [], (data.trendIncome || []).map(Number), (data.trendExpense || []).map(Number));
            renderLegend(data.categoryBreakdown || []);
            renderBudgetAlerts(data.budgetAlerts || []);
        } catch (error) {
            window.AppToast?.error?.(error.message || "Không thể tải dữ liệu dashboard.");
        } finally {
            chips.forEach(btn => btn.disabled = false);
            page?.classList.remove("dashboard-loading");
        }
    }

    if (trendCanvas) {
        createTrendChart(
            parseJson(trendCanvas.dataset.labels),
            parseJson(trendCanvas.dataset.income).map(Number),
            parseJson(trendCanvas.dataset.expense).map(Number)
        );
    }
    if (donutCanvas) {
        renderLegend(parseJson(donutCanvas.dataset.items, []));
    }
    if (todayCanvas) {
        renderTodayBreakdown();
    }

    todayTabs.forEach((button) => {
        button.addEventListener("click", function () {
            const nextType = this.dataset.type === "income" ? "income" : "expense";
            if (nextType === todayBreakdownType) return;
            todayBreakdownType = nextType;
            todayTabs.forEach(tab => tab.classList.toggle("active", tab === this));
            renderTodayBreakdown();
        });
    });

    chips.forEach(btn => btn.addEventListener("click", function () {
        const period = this.dataset.periodChip;
        if (!period || this.classList.contains("active")) return;
        loadPeriod(period);
    }));
});
