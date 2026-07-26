document.addEventListener('DOMContentLoaded', () => {
  const lang = (window.transactionPageLang || 'vi').toLowerCase();
  const locale = lang === 'en' ? 'en-US' : 'vi-VN';
  const t = (vi, en) => (lang === 'en' ? en : vi);
  const qs = (id) => document.getElementById(id);
  const qsa = (selector, root = document) => Array.from(root.querySelectorAll(selector));
  const parseJson = (id) => {
    try {
      return JSON.parse(qs(id)?.textContent || '[]');
    } catch {
      return [];
    }
  };

  const transactions = parseJson('tx-transactions-json');
  const categories = parseJson('tx-categories-json');
  const wallets = parseJson('tx-wallets-json');

  const state = {
    breakdownType: 'expense',
    ledgerType: 'all',
    walletId: 'all',
    keyword: '',
    period: 'today',
    from: null,
    to: null,
    anchorDate: startOfDay(findLatestDate(transactions) || new Date()),
    calendarMonth: monthStart(findLatestDate(transactions) || new Date()),
    filteredBreakdown: [],
    filteredList: [],
    page: 1,
    pageSize: 5,
    charts: { breakdown: null },
    detailId: null,
    selectedCalendarDate: null,
    reopenDayDrawerOnDetailClose: false,
    categorySelections: {
      add: { expense: '', income: '' },
      detail: { expense: '', income: '' }
    }
  };

  const addModalEl = qs('addTransactionModal');
  const detailModalEl = qs('transactionDetailModal');
  const dayOffcanvasEl = qs('transactionDayOffcanvas');
  const addModal = addModalEl ? bootstrap.Modal.getOrCreateInstance(addModalEl) : null;
  const detailModal = detailModalEl ? bootstrap.Modal.getOrCreateInstance(detailModalEl) : null;
  const dayOffcanvas = dayOffcanvasEl ? bootstrap.Offcanvas.getOrCreateInstance(dayOffcanvasEl) : null;

  const fpLocale = lang === 'en' ? 'default' : flatpickr.l10ns.vn;
  const fpBaseConfig = {
    altInput: true,
    altFormat: 'd/m/Y',
    dateFormat: 'Y-m-d',
    locale: fpLocale
  };

  const fromPicker = qs('fromDateFilter') ? flatpickr(qs('fromDateFilter'), {
    ...fpBaseConfig,
    onChange: (selectedDates, dateStr) => {
      state.from = dateStr || null;
      if (state.period === 'custom') { state.page = 1; refresh(); }
    }
  }) : null;

  const toPicker = qs('toDateFilter') ? flatpickr(qs('toDateFilter'), {
    ...fpBaseConfig,
    onChange: (selectedDates, dateStr) => {
      state.to = dateStr || null;
      if (state.period === 'custom') { state.page = 1; refresh(); }
    }
  }) : null;

  const addDatePicker = qs('addTransactionDate') ? flatpickr(qs('addTransactionDate'), {
    ...fpBaseConfig,
    defaultDate: state.anchorDate
  }) : null;

  const detailDatePicker = qs('detailEditDate') ? flatpickr(qs('detailEditDate'), fpBaseConfig) : null;

  function startOfDay(date) {
    const value = new Date(date);
    value.setHours(0, 0, 0, 0);
    return value;
  }

  function monthStart(date) {
    const value = startOfDay(date);
    value.setDate(1);
    return value;
  }

  function addDays(date, amount) {
    const value = startOfDay(date);
    value.setDate(value.getDate() + amount);
    return value;
  }

  function addMonths(date, amount) {
    const value = startOfDay(date);
    value.setDate(1);
    value.setMonth(value.getMonth() + amount);
    return value;
  }

  function formatIso(date) {
    const value = startOfDay(date);
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  function parseIso(value) {
    if (!value) return null;
    return startOfDay(new Date(`${value}T00:00:00`));
  }

  function formatDate(date) {
    return date.toLocaleDateString(locale, { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  function formatLongDate(date) {
    return date.toLocaleDateString(locale, { weekday: 'short', day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  function formatMonth(date) {
    return date.toLocaleDateString(locale, { month: 'long', year: 'numeric' });
  }

  function formatMoney(value) {
    return `${Number(value || 0).toLocaleString(locale)} VND`;
  }

  function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (char) => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;'
    }[char]));
  }

  function isImageIcon(icon) {
    return !!icon && (icon.includes('/') || /\.(svg|png|webp|jpe?g)$/i.test(icon));
  }

  function iconMarkup(icon, alt, cls = '') {
    const safeClass = cls ? ` ${cls}` : '';
    if (isImageIcon(icon)) {
      return `<img src="${escapeHtml(icon)}" alt="${escapeHtml(alt || 'icon')}" class="tx-inline-icon${safeClass}" />`;
    }
    return `<i class="${escapeHtml(icon || 'bx bx-category')}${safeClass}"></i>`;
  }

  function alphaColor(hex, alpha) {
    if (!hex || !hex.startsWith('#')) return `rgba(105,108,255,${alpha})`;
    const normalized = hex.length === 4
      ? `#${hex[1]}${hex[1]}${hex[2]}${hex[2]}${hex[3]}${hex[3]}`
      : hex;
    const raw = Number.parseInt(normalized.slice(1), 16);
    const red = (raw >> 16) & 255;
    const green = (raw >> 8) & 255;
    const blue = raw & 255;
    return `rgba(${red},${green},${blue},${alpha})`;
  }

  function recurringText(value) {
    const map = {
      daily: t('Hàng ngày', 'Daily'),
      weekly: t('Hàng tuần', 'Weekly'),
      monthly: t('Hàng tháng', 'Monthly'),
      yearly: t('Hàng năm', 'Yearly')
    };
    return map[value] || t('Một lần', 'One-time');
  }

  function findLatestDate(items) {
    if (!items.length) return null;
    return items
      .map((item) => parseIso(item.transactionDate))
      .filter(Boolean)
      .sort((a, b) => b - a)[0];
  }

  function getCategory(categoryId) {
    return categories.find((item) => item.category_id === categoryId) || null;
  }

  function getWallet(walletId) {
    return wallets.find((item) => item.wallet_id === walletId) || null;
  }

  function getTransaction(transactionId) {
    return transactions.find((item) => item.id === transactionId) || null;
  }

  function buildUiTransaction(raw) {
    const category = getCategory(raw.category_id);
    const wallet = getWallet(raw.wallet_id);
    const currency = wallet?.currency || 'VND';
    const amount = Number(raw.amount || 0);
    const signedPrefix = raw.transaction_type === 'income' ? '+' : '-';

    return {
      id: raw.transaction_id,
      walletId: raw.wallet_id,
      walletName: wallet?.wallet_name || raw.wallet_name || 'N/A',
      categoryId: raw.category_id,
      categoryName: category?.category_name || raw.category_name || t('Khác', 'Other'),
      categoryIcon: category?.icon || raw.category_icon || 'bx bx-category',
      categoryColor: category?.color || raw.category_color || '#8592A3',
      transactionType: raw.transaction_type,
      isTransfer: !!raw.isTransfer,
      amountValue: amount,
      amountText: `${signedPrefix}${amount.toLocaleString(locale)} ${currency}`,
      transactionDate: String(raw.transaction_date || '').slice(0, 10),
      transactionDateText: raw.transaction_date ? formatDate(parseIso(String(raw.transaction_date).slice(0, 10))) : '--/--/----',
      note: raw.note && raw.note.trim() ? raw.note.trim() : t('Không có ghi chú', 'No note'),
      recurringBadgeText: raw.is_recurring ? recurringText(raw.recur_interval) : t('Một lần', 'One-time'),
      currency
    };
  }

  function replaceTransaction(raw) {
    const next = buildUiTransaction(raw);
    const index = transactions.findIndex((item) => item.id === next.id);
    if (index >= 0) {
      transactions[index] = next;
    } else {
      transactions.unshift(next);
    }
  }

  function removeTransaction(transactionId) {
    const index = transactions.findIndex((item) => item.id === transactionId);
    if (index >= 0) transactions.splice(index, 1);
  }

  function filterByWallet(items) {
    return items.filter((item) => {
      if (state.walletId !== 'all' && item.walletId !== state.walletId) return false;
      return true;
    });
  }

  function filterByType(items, type = 'all') {
    return items.filter((item) => {
      if (type === 'transfer') {
        return item.isTransfer;
      }
      if (type !== 'all') {
        if (item.isTransfer) return false;
        return item.transactionType === type;
      }
      return true;
    });
  }

  function filterByKeyword(items, keyword = '') {
    if (!keyword) return items;
    return items.filter((item) => {
      const haystack = `${item.note} ${item.categoryName} ${item.walletName}`.toLowerCase();
      return haystack.includes(keyword);
    });
  }

  function getPeriodRange() {
    const anchor = startOfDay(state.anchorDate);

    if (state.period === 'today') {
      return { from: anchor, to: anchor };
    }

    if (state.period === 'week') {
      return { from: addDays(anchor, -6), to: anchor };
    }

    if (state.period === 'month') {
      const from = monthStart(anchor);
      const to = addDays(addMonths(from, 1), -1);
      return { from, to };
    }

    if (state.period === 'custom') {
      const from = state.from ? parseIso(state.from) : null;
      const to = state.to ? parseIso(state.to) : null;
      return { from, to };
    }

    return { from: null, to: null };
  }

  function applyPeriodFilter(items) {
    const range = getPeriodRange();
    return items
      .filter((item) => {
        const date = parseIso(item.transactionDate);
        if (!date) return false;
        if (range.from && date < range.from) return false;
        if (range.to && date > range.to) return false;
        return true;
      })
      .sort((left, right) => `${right.transactionDate}${right.id}`.localeCompare(`${left.transactionDate}${left.id}`));
  }

  function getBreakdownItems() {
    return applyPeriodFilter(filterByType(filterByWallet(transactions), state.breakdownType));
  }

  function getLedgerItems() {
    return applyPeriodFilter(filterByKeyword(filterByType(filterByWallet(transactions), state.ledgerType), state.keyword));
  }

  function getCalendarItems() {
    return filterByWallet(transactions);
  }

  function refreshFilteredState() {
    state.filteredBreakdown = getBreakdownItems();
    state.filteredList = getLedgerItems();
    const totalPages = Math.max(1, Math.ceil(state.filteredList.length / state.pageSize));
    if (state.page > totalPages) state.page = totalPages;
    if (state.page < 1) state.page = 1;
  }

  function renderLegend(container, groups) {
    if (!container) return;
    if (!groups.length) {
      container.innerHTML = `<div class="tx-summary-empty">${t('Chưa có dữ liệu phù hợp.', 'No matching data.')}</div>`;
      return;
    }

    const total = groups.reduce((sum, item) => sum + item.value, 0);
    const itemsHtml = groups.map((item) => `
      <div class="tx-summary-item">
        <div class="tx-summary-item-left">
          <span class="tx-summary-dot" style="background:${item.color}"></span>
          <span class="tx-summary-name">${escapeHtml(item.name)}</span>
        </div>
        <div class="tx-summary-item-right">
          <strong>${formatMoney(item.value)}</strong>
          <span>${total > 0 ? `${((item.value / total) * 100).toFixed(1).replace('.0', '')}%` : '0%'}</span>
        </div>
      </div>
    `).join('');

    container.innerHTML = `${itemsHtml}
      <div class="tx-summary-total-row">
        <span>${t('Tổng cộng', 'Total')}</span>
        <strong>${formatMoney(total)}</strong>
      </div>`;
  }

  function buildCategoryGroups() {
    const bucket = new Map();
    state.filteredBreakdown.forEach((item) => {
      const key = item.categoryId || item.categoryName || item.id;
      if (!bucket.has(key)) {
        bucket.set(key, {
          name: item.categoryName || t('Khác', 'Other'),
          value: 0,
          color: item.categoryColor || '#8592A3',
          icon: item.categoryIcon || ''
        });
      }
      bucket.get(key).value += Number(item.amountValue || 0);
    });
    return [...bucket.values()].sort((left, right) => right.value - left.value).slice(0, 6);
  }

  function currentBreakdownCopy() {
    switch (state.breakdownType) {
      case 'expense':
        return {
          eyebrow: t('Cơ cấu chi tiêu', 'Expense breakdown'),
          title: t('Danh mục chi tiêu trong kỳ đang xem', 'Expense categories in the current period'),
          subtitle: t('Biểu đồ tròn cho biết nhóm chi tiêu nổi bật, còn danh sách bên cạnh giúp bạn đọc nhanh số tiền và tỷ trọng của từng danh mục.', 'The doughnut chart highlights top expense groups, while the side panel summarizes amount and share for each category.'),
          summary: t('Danh mục phát sinh', 'Active categories'),
          centerLabel: t('Tổng chi trong kỳ', 'Total expense in period'),
          centerMetaSuffix: t('danh mục phát sinh', 'active categories')
        };
      case 'income':
        return {
          eyebrow: t('Cơ cấu thu nhập', 'Income breakdown'),
          title: t('Danh mục thu nhập trong kỳ đang xem', 'Income categories in the current period'),
          subtitle: t('Biểu đồ tròn hiển thị nguồn thu nổi bật trong bộ lọc hiện tại để bạn so sánh tỷ trọng từng nhóm.', 'The doughnut chart shows the most significant income sources in the current filter so you can compare each group\'s share.'),
          summary: t('Danh mục phát sinh', 'Active categories'),
          centerLabel: t('Tổng thu trong kỳ', 'Total income in period'),
          centerMetaSuffix: t('danh mục phát sinh', 'active categories')
        };
      case 'transfer':
        return {
          eyebrow: t('Cơ cấu chuyển ví', 'Transfer breakdown'),
          title: t('Luồng chuyển ví trong kỳ đang xem', 'Wallet transfer groups in the current period'),
          subtitle: t('Dữ liệu chuyển ví được gom theo nhóm hiển thị để bạn theo dõi các lần điều chuyển tiền giữa các ví.', 'Transfers are grouped for the selected period so you can quickly review money moved between wallets.'),
          summary: t('Danh mục phát sinh', 'Active categories'),
          centerLabel: t('Tổng chuyển trong kỳ', 'Total transfers in period'),
          centerMetaSuffix: t('nhóm phát sinh', 'active groups')
        };
      default:
        return {
          eyebrow: t('Cơ cấu giao dịch', 'Transaction breakdown'),
          title: t('Danh mục giao dịch trong kỳ đang xem', 'Transaction categories in the current period'),
          subtitle: t('Biểu đồ tròn và danh sách bên cạnh sẽ đổi theo lựa chọn lọc phía trên để bạn xem nhanh tỷ trọng từng nhóm danh mục.', 'The doughnut chart and side list update with the filter above so you can quickly compare category shares.'),
          summary: t('Danh mục phát sinh', 'Active categories'),
          centerLabel: t('Tổng giao dịch trong kỳ', 'Total in period'),
          centerMetaSuffix: t('danh mục phát sinh', 'active categories')
        };
    }
  }

  function updateBreakdownCenter(chart, activeIndex = null) {
    const shell = chart?.canvas?.parentElement;
    if (!shell) return;

    let center = shell.querySelector('.tx-breakdown-center');
    if (!center) {
      center = document.createElement('div');
      center.className = 'tx-breakdown-center';
      shell.appendChild(center);
    }

    const items = chart.$items || [];
    const defaultState = chart.$defaultCenter || {
      label: currentBreakdownCopy().centerLabel,
      value: 0,
      meta: `0 ${currentBreakdownCopy().centerMetaSuffix}`
    };
    const active = Number.isInteger(activeIndex) && items[activeIndex] ? items[activeIndex] : null;
    const total = items.reduce((sum, item) => sum + Number(item.value || 0), 0);
    const stateCenter = active
      ? {
          label: active.name,
          value: Number(active.value || 0),
          meta: total > 0
            ? `${((Number(active.value || 0) / total) * 100).toFixed(2).replace(/\.00$/, '')}% ${t('tỷ trọng trong kỳ', 'share in period')}`
            : `0% ${t('tỷ trọng trong kỳ', 'share in period')}`
        }
      : defaultState;

    center.innerHTML = `
      <span class="tx-breakdown-center__label">${escapeHtml(stateCenter.label)}</span>
      <strong class="tx-breakdown-center__value">${escapeHtml(formatMoney(stateCenter.value))}</strong>
      <span class="tx-breakdown-center__meta">${escapeHtml(stateCenter.meta)}</span>`;
  }

  function setBreakdownLegendActive(activeIndex = null, colors = []) {
    qsa('#transactionBreakdownLegend .legend-row').forEach((row, index) => {
      const isActive = activeIndex === index;
      row.classList.toggle('is-active', isActive);
      row.style.setProperty('--legend-active-color', colors[index] || '#d9e5f6');
    });
  }

  function bindBreakdownLegendHover(chart, colors) {
    qsa('#transactionBreakdownLegend .legend-row').forEach((row) => {
      row.addEventListener('mouseenter', () => {
        const index = Number(row.dataset.legendIndex);
        if (!Number.isInteger(index)) return;
        const meta = chart.getDatasetMeta(0);
        const element = meta?.data?.[index];
        if (!element) return;
        chart.setActiveElements([{ datasetIndex: 0, index }]);
        chart.tooltip?.setActiveElements([{ datasetIndex: 0, index }], { x: element.x, y: element.y });
        chart.update();
        updateBreakdownCenter(chart, index);
        setBreakdownLegendActive(index, colors);
      });

      row.addEventListener('mouseleave', () => {
        chart.setActiveElements([]);
        chart.tooltip?.setActiveElements([], { x: 0, y: 0 });
        chart.update();
        updateBreakdownCenter(chart, null);
        setBreakdownLegendActive(null, colors);
      });
    });
  }

  function renderBreakdownLegend(container, groups) {
    if (!container) return;
    if (!groups.length) {
      container.innerHTML = `
        <div class="tx-breakdown-empty">
          <span class="tx-breakdown-empty__icon"><i class="bx bx-pie-chart-alt-2"></i></span>
          <h3>${t('Chưa có dữ liệu phù hợp', 'No matching data yet')}</h3>
          <p>${t('Biểu đồ sẽ hiển thị khi bộ lọc hiện tại có giao dịch hợp lệ.', 'The doughnut chart will appear once the current filter has matching transactions.')}</p>
        </div>`;
      return;
    }

    const total = groups.reduce((sum, item) => sum + Number(item.value || 0), 0);
    container.innerHTML = groups.map((item, index) => `
      <div class="legend-row" data-legend-index="${index}" style="--legend-row-color:${item.color}">
        <div class="legend-left">
          <span class="legend-icon tx-legend-icon" style="--legend-color:${item.color}; background:${alphaColor(item.color, 0.12)}; color:${item.color}">${iconMarkup(item.icon, item.name)}</span>
          <div>
            <strong>${escapeHtml(item.name)}</strong>
            <small>${t('Tỷ trọng trong kỳ', 'Share in period')}</small>
          </div>
        </div>
        <div class="legend-right">
          <strong>${total > 0 ? `${((item.value / total) * 100).toFixed(2).replace(/\.00$/, '')}%` : '0%'}</strong>
          <small>${formatMoney(item.value)}</small>
        </div>
      </div>
    `).join('');
  }

  function renderBreakdownChart() {
    const canvas = qs('transactionBreakdownChart');
    if (!canvas) return;

    const groups = buildCategoryGroups();
    const copy = currentBreakdownCopy();
    const wrap = canvas.parentElement;
    if (qs('txBreakdownEyebrow')) qs('txBreakdownEyebrow').textContent = copy.eyebrow;
    if (qs('txBreakdownTitle')) qs('txBreakdownTitle').textContent = copy.title;
    if (qs('txBreakdownSubtitle')) qs('txBreakdownSubtitle').textContent = copy.subtitle;
    if (qs('txBreakdownSummaryLabel')) qs('txBreakdownSummaryLabel').textContent = copy.summary;
    if (qs('transactionBreakdownCount')) qs('transactionBreakdownCount').textContent = String(groups.length);
    renderBreakdownLegend(qs('transactionBreakdownLegend'), groups);

    if (state.charts.breakdown) {
      state.charts.breakdown.destroy();
      state.charts.breakdown = null;
    }

    const labels = groups.map((item) => item.name);
    const series = groups.map((item) => Number(item.value || 0));
    const colors = groups.map((item) => item.color || '#696cff');

    if (!groups.length) {
      state.charts.breakdown = new Chart(canvas, {
        type: 'doughnut',
        data: {
          labels: [t('Không có dữ liệu', 'No data')],
          datasets: [{
            data: [1],
            backgroundColor: ['#edf1f7'],
            borderWidth: 0,
            hoverOffset: 0,
            spacing: 0
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '63%',
          plugins: { legend: { display: false }, tooltip: { enabled: false } }
        }
      });
      state.charts.breakdown.$items = [];
      state.charts.breakdown.$defaultCenter = {
        label: copy.centerLabel,
        value: 0,
        meta: `0 ${copy.centerMetaSuffix}`
      };
      updateBreakdownCenter(state.charts.breakdown, null);
      setBreakdownLegendActive(null, colors);
      if (wrap) wrap.style.cursor = 'default';
      return;
    }

    state.charts.breakdown = new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [{
          data: series,
          backgroundColor: colors,
          borderColor: '#ffffff',
          borderWidth: 4,
          hoverOffset: 8,
          spacing: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '63%',
        layout: { padding: 10 },
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (ctx) => {
                const total = series.reduce((sum, value) => sum + Number(value || 0), 0);
                const value = Number(ctx.parsed || 0);
                const percent = total ? (value / total) * 100 : 0;
                return `${ctx.label}: ${formatMoney(value)} (${percent.toFixed(1).replace(/\.0$/, '')}%)`;
              }
            }
          }
        },
        onHover(event, activeElements, chart) {
          const activeIndex = activeElements?.length ? activeElements[0].index : null;
          updateBreakdownCenter(chart, activeIndex);
          setBreakdownLegendActive(activeIndex, colors);
          chart.canvas.style.cursor = activeElements?.length ? 'pointer' : 'default';
        }
      }
    });

    state.charts.breakdown.$items = groups;
    state.charts.breakdown.$defaultCenter = {
      label: copy.centerLabel,
      value: series.reduce((sum, value) => sum + Number(value || 0), 0),
      meta: `${groups.length} ${copy.centerMetaSuffix}`
    };

    updateBreakdownCenter(state.charts.breakdown, null);
    setBreakdownLegendActive(null, colors);
    bindBreakdownLegendHover(state.charts.breakdown, colors);
  }

  function renderPagination() {
    const container = qs('transactionPagination');
    const info = qs('transactionPaginationInfo');
    if (!container) return;

    const totalItems = state.filteredList.length;
    const totalPages = Math.max(1, Math.ceil(totalItems / state.pageSize));
    const start = totalItems ? ((state.page - 1) * state.pageSize) + 1 : 0;
    const end = Math.min(state.page * state.pageSize, totalItems);

    if (info) {
      info.innerHTML = `${t('Hiển thị', 'Showing')} <strong>${start} - ${end}</strong> ${t('trong tổng số', 'of total')} <strong>${totalItems}</strong> ${t('giao dịch', 'transactions')}`;
    }

    if (totalItems <= state.pageSize) {
      container.innerHTML = '';
      return;
    }

    const startPage = Math.max(1, Math.min(state.page - 1, totalPages - 2));
    const endPage = Math.min(totalPages, startPage + 2);
    const pages = [];
    for (let page = startPage; page <= endPage; page += 1) {
      pages.push(page);
    }

    const html = [
      `<button type="button" class="tx-page-nav" data-nav="prev" ${state.page === 1 ? 'disabled' : ''}><i class="bx bx-chevron-left"></i></button>`,
      ...pages.map((page) => `<button type="button" class="${page === state.page ? 'active' : ''}" data-page="${page}">${page}</button>`),
      `<button type="button" class="tx-page-nav" data-nav="next" ${state.page === totalPages ? 'disabled' : ''}><i class="bx bx-chevron-right"></i></button>`
    ].join('');

    container.innerHTML = html;

    qsa('button[data-page]', container).forEach((button) => {
      button.addEventListener('click', () => {
        state.page = Number(button.dataset.page || '1');
        renderList();
      });
    });

    const prev = container.querySelector('[data-nav="prev"]');
    const next = container.querySelector('[data-nav="next"]');
    if (prev && state.page > 1) prev.addEventListener('click', () => {
      state.page -= 1;
      renderList();
    });
    if (next && state.page < totalPages) next.addEventListener('click', () => {
      state.page += 1;
      renderList();
    });
  }

  function renderList() {
    const shell = qs('transactionListShell');
    const empty = qs('transactionEmptyState');
    if (!shell || !empty) return;

    if (!state.filteredList.length) {
      shell.innerHTML = '';
      empty.classList.remove('d-none');
      qs('transactionPagination').innerHTML = '';
      const info = qs('transactionPaginationInfo');
      if (info) info.textContent = '';
      return;
    }

    empty.classList.add('d-none');
    const start = (state.page - 1) * state.pageSize;
    const pageItems = state.filteredList.slice(start, start + state.pageSize);

    shell.innerHTML = pageItems.map((item) => `
      <div class="tx-row-card">
        <div class="tx-main">
          <div class="tx-main-icon" style="background:${alphaColor(item.categoryColor, 0.15)}; color:${item.categoryColor}">
            ${iconMarkup(item.categoryIcon, item.categoryName)}
          </div>
          <div class="tx-main-copy">
            <h6>${escapeHtml(item.note)}</h6>
          </div>
        </div>
        <div>
          <span class="tx-pill" style="background:${alphaColor(item.categoryColor, 0.12)}; color:${item.categoryColor}">
            ${iconMarkup(item.categoryIcon, item.categoryName, 'tx-inline-icon-sm')}
            ${escapeHtml(item.categoryName)}
          </span>
        </div>
        <div class="tx-meta-col">
          <span>${escapeHtml(item.transactionDateText)}</span>
        </div>
        <div class="tx-meta-col">
          <span>${escapeHtml(item.recurringBadgeText)}</span>
        </div>
        <div class="tx-amount ${item.transactionType === 'income' ? 'income' : 'expense'}">${escapeHtml(item.amountText)}</div>
        <div class="tx-actions">
          <button type="button" class="tx-action-btn view" data-action="view" data-id="${item.id}" title="${t('Xem chi tiết', 'View')}">
            <i class="bx bx-show"></i>
          </button>
          <button type="button" class="tx-action-btn delete" data-action="delete" data-id="${item.id}" title="${t('Xóa', 'Delete')}">
            <i class="bx bx-trash"></i>
          </button>
        </div>
      </div>
    `).join('');

    qsa('[data-action="view"]', shell).forEach((button) => button.addEventListener('click', () => openDetail(button.dataset.id)));
    qsa('[data-action="delete"]', shell).forEach((button) => button.addEventListener('click', () => deleteTransaction(button.dataset.id)));
    renderPagination();
  }

  function renderPeriodAssist() {
    const dayCard = qs('dayNavigatorCard');
    const customCard = qs('customDateRangeWrap');
    const pill = qs('currentDayPill');
    const hint = qs('periodAssistText');
    const prevButton = qs('dayPrevBtn');
    const nextButton = qs('dayNextBtn');

    if (!dayCard || !customCard || !pill || !hint || !prevButton || !nextButton) return;

    dayCard.classList.toggle('is-active', state.period !== 'custom');
    dayCard.classList.toggle('is-inactive', state.period === 'custom');
    customCard.classList.toggle('is-active', state.period === 'custom');
    customCard.classList.toggle('is-inactive', state.period !== 'custom');

    const customDisabled = state.period !== 'custom';
    [qs('fromDateFilter'), qs('toDateFilter')].forEach((input) => {
      if (input) input.disabled = customDisabled;
    });
    prevButton.disabled = state.period === 'custom';
    nextButton.disabled = state.period === 'custom';

    if (state.period === 'today') {
      pill.textContent = formatLongDate(state.anchorDate);
      hint.textContent = t('Điều hướng theo ngày đang xem', 'Navigate by the current day');
      return;
    }

    if (state.period === 'week') {
      const range = getPeriodRange();
      pill.textContent = `${formatDate(range.from)} - ${formatDate(range.to)}`;
      hint.textContent = t('Mỗi lần di chuyển là 7 ngày', 'Each step moves by 7 days');
      return;
    }

    if (state.period === 'month') {
      pill.textContent = formatMonth(state.anchorDate);
      hint.textContent = t('Mỗi lần di chuyển là 1 tháng', 'Each step moves by 1 month');
      return;
    }

    const fromText = state.from ? formatDate(parseIso(state.from)) : '--/--/----';
    const toText = state.to ? formatDate(parseIso(state.to)) : '--/--/----';
    pill.textContent = `${fromText} - ${toText}`;
    hint.textContent = t('Hãy chọn khoảng ngày tùy chọn', 'Choose your custom range');
  }

  function compactMoney(value) {
    const number = Math.abs(Number(value || 0));
    if (number >= 1000000) {
      return `${(number / 1000000).toLocaleString(locale, { maximumFractionDigits: 1 })}M`;
    }
    if (number >= 1000) {
      return `${Math.round(number / 1000)}k`;
    }
    return Number(number).toLocaleString(locale);
  }

  function buildCalendarDayMap() {
    const map = new Map();
    getCalendarItems().forEach((item) => {
      if (!map.has(item.transactionDate)) {
        map.set(item.transactionDate, {
          count: 0,
          incomeCount: 0,
          expenseCount: 0,
          incomeAmount: 0,
          expenseAmount: 0
        });
      }
      const bucket = map.get(item.transactionDate);
      bucket.count += 1;
      if (item.transactionType === 'income') {
        bucket.incomeCount += 1;
        bucket.incomeAmount += Number(item.amountValue || 0);
      } else {
        bucket.expenseCount += 1;
        bucket.expenseAmount += Number(item.amountValue || 0);
      }
    });
    return map;
  }

  function calendarBucketStatus(bucket) {
    if (!bucket) return 'empty';
    if (bucket.incomeCount && bucket.expenseCount) return 'mix';
    if (bucket.incomeCount) return 'income';
    if (bucket.expenseCount) return 'expense';
    return 'empty';
  }

  function calendarStatusLabel(status) {
    if (status === 'income') return t('Có thu', 'Income');
    if (status === 'expense') return t('Có chi', 'Expense');
    if (status === 'mix') return t('Cả hai', 'Both');
    return t('Trống', 'Empty');
  }

  function renderCalendar() {
    const grid = qs('transactionMiniCalendarGrid');
    const title = qs('calendarTitle');
    if (!grid || !title) return;

    const currentMonth = monthStart(state.calendarMonth);
    const firstDay = monthStart(currentMonth);
    const startWeekDay = firstDay.getDay();
    const gridStart = addDays(firstDay, -startWeekDay);
    const dayMap = buildCalendarDayMap();
    const selectedIso = state.selectedCalendarDate || formatIso(state.anchorDate);
    const todayIso = formatIso(new Date());

    title.textContent = formatMonth(currentMonth);

    let html = '';
    for (let offset = 0; offset < 42; offset += 1) {
      const date = addDays(gridStart, offset);
      const isoDate = formatIso(date);
      const bucket = dayMap.get(isoDate);
      const status = calendarBucketStatus(bucket);
      const isCurrentMonth = date.getMonth() === currentMonth.getMonth();
      const isSelected = isoDate === selectedIso;
      const isToday = isoDate === todayIso;
      const totalCount = Number(bucket?.count || 0);
      const totalClass = status === 'mix'
        ? 'mix'
        : status === 'income'
          ? 'income'
          : status === 'expense'
            ? 'expense'
            : '';
      const chips = bucket
        ? `
            ${bucket.incomeCount ? `<span class="tx-day-chip is-income"><i class="mini-dot" style="background:#28c76f"></i><b>${t('Thu', 'In')} ${bucket.incomeCount}</b></span>` : ''}
            ${bucket.expenseCount ? `<span class="tx-day-chip is-expense"><i class="mini-dot" style="background:#ff5c39"></i><b>${t('Chi', 'Out')} ${bucket.expenseCount}</b></span>` : ''}
          `
        : `<span class="tx-day-chip empty">${t('Không có giao dịch', 'No transactions')}</span>`;
      html += `
        <button type="button" class="tx-calendar-day ${!isCurrentMonth ? 'is-muted' : ''} ${isSelected ? 'is-selected' : ''} ${isToday ? 'is-today' : ''} ${bucket ? 'has-data' : ''}" data-date="${isoDate}">
          <span class="tx-calendar-day-top">
            <span class="tx-calendar-day-number">${date.getDate()}</span>
            <span class="tx-calendar-day-meta">
              <span class="tx-day-count">${bucket ? `${bucket.count} ${t('gd', 'tx')}` : `0 ${t('gd', 'tx')}`}</span>
              <span class="tx-day-status ${status !== 'empty' ? status : ''}">${status !== 'empty' ? '<i></i>' : ''}${calendarStatusLabel(status)}</span>
            </span>
          </span>
          <span class="tx-calendar-day-body">
            <span class="tx-day-chips">${chips}</span>
          </span>
          <span class="tx-calendar-day-footer">
            <span class="tx-day-total ${totalClass}">${t('Tổng giao dịch', 'Transactions')}<span class="value">${bucket ? `${totalCount} ${t('giao dịch', 'transactions')}` : `0 ${t('giao dịch', 'transactions')}`}</span></span>
          </span>
        </button>
      `;
    }

    grid.innerHTML = html;
    qsa('[data-date]', grid).forEach((button) => {
      button.addEventListener('click', () => openDay(button.dataset.date));
    });
  }

  function renderDayDrawer(dateText) {
    const date = parseIso(dateText);
    const items = getCalendarItems()
      .filter((item) => item.transactionDate === dateText)
      .sort((left, right) => right.id.localeCompare(left.id));

    const incomeTotal = items
      .filter((item) => item.transactionType === 'income')
      .reduce((sum, item) => sum + Number(item.amountValue || 0), 0);
    const expenseTotal = items
      .filter((item) => item.transactionType === 'expense')
      .reduce((sum, item) => sum + Number(item.amountValue || 0), 0);

    if (qs('transactionDayTitle')) qs('transactionDayTitle').textContent = t('Giao dịch trong ngày', 'Transactions of day');
    if (qs('transactionDaySubTitle')) qs('transactionDaySubTitle').textContent = formatLongDate(date);
    if (qs('dayStatCount')) qs('dayStatCount').textContent = String(items.length);
    if (qs('dayStatIncome')) qs('dayStatIncome').textContent = formatMoney(incomeTotal);
    if (qs('dayStatExpense')) qs('dayStatExpense').textContent = formatMoney(expenseTotal);

    const summaryCard = qs('transactionDaySummaryCard');
    const summaryValue = qs('transactionDaySummaryValue');
    const hintCard = qs('transactionDayHintCard');
    if (summaryCard && summaryValue) {
      summaryCard.classList.toggle('is-empty', !items.length);
      summaryValue.textContent = items.length
        ? `${items.length} ${t('giao dịch', 'transactions')} · ${t('Thu', 'In')} ${formatMoney(incomeTotal)} · ${t('Chi', 'Out')} ${formatMoney(expenseTotal)}`
        : t('Không có giao dịch trong ngày này.', 'No transactions for this day.');
    }
    if (hintCard) {
      hintCard.classList.toggle('is-empty', !items.length);
      hintCard.textContent = items.length
        ? t('Chạm vào từng giao dịch để mở popup chi tiết.', 'Tap any transaction to open detail popup.')
        : t('Bấm ra ngoài để đóng bảng này.', 'Tap outside to close this panel.');
    }

    const list = qs('transactionDayList');
    if (!list) return;

    if (!items.length) {
      list.innerHTML = '';
      return;
    }

    list.innerHTML = items.map((item) => {
      const badgeText = item.transactionType === 'income' ? t('Thu nhập', 'Income') : t('Chi tiêu', 'Expense');
      return `
        <button type="button" class="tx-day-item" data-id="${item.id}">
          <div class="tx-day-item-icon" style="background:${alphaColor(item.categoryColor, 0.15)}; color:${item.categoryColor}">
            ${iconMarkup(item.categoryIcon, item.categoryName)}
          </div>
          <div class="tx-day-item-body">
            <div class="tx-day-item-title">
              <strong>${escapeHtml(item.note)}</strong>
              <span class="tx-day-item-badge">${badgeText}</span>
            </div>
            <div class="tx-day-item-meta">
              <span>${escapeHtml(item.categoryName)}</span>
              <span>${escapeHtml(item.walletName)}</span>
              <span>${escapeHtml(item.recurringBadgeText)}</span>
            </div>
          </div>
          <div class="tx-day-item-amount ${item.transactionType === 'income' ? 'income' : 'expense'}">
            ${escapeHtml(item.amountText)}
            <small>${escapeHtml(item.transactionDateText)}</small>
          </div>
        </button>
      `;
    }).join('');

    qsa('[data-id]', list).forEach((button) => {
      button.addEventListener('click', () => {
        state.reopenDayDrawerOnDetailClose = true;
        if (dayOffcanvas) dayOffcanvas.hide();
        openDetail(button.dataset.id);
      });
    });
  }

  function syncAddPreview() {
    const wallet = getWallet(qs('addTransactionWallet')?.value);
    const categoryId = qs('addTransactionCategoryId')?.value;
    const category = getCategory(categoryId);
    const amount = Number(qs('addTransactionAmount')?.value || 0);
    const type = qs('addTransactionType')?.value || 'expense';
    const note = (qs('addTransactionNote')?.value || '').trim();
    const repeat = qs('addTransactionRecurrence')?.value || '';
    const date = parseIso(qs('addTransactionDate')?.value) || state.anchorDate;
    const sign = type === 'income' ? '+' : '-';

    if (qs('addPreviewIconWrap')) {
      const color = category?.color || '#8592A3';
      qs('addPreviewIconWrap').style.background = alphaColor(color, 0.18);
      qs('addPreviewIconWrap').style.color = color;
    }
    if (qs('addPreviewIcon')) qs('addPreviewIcon').innerHTML = iconMarkup(category?.icon, category?.category_name || 'icon');
    if (qs('addPreviewNote')) qs('addPreviewNote').textContent = note || t('Chưa có ghi chú cho giao dịch này.', 'No note yet.');
    if (qs('addPreviewAmount')) qs('addPreviewAmount').textContent = `${sign}${amount.toLocaleString(locale)} ${wallet?.currency || 'VND'}`;
    if (qs('addPreviewCategory')) qs('addPreviewCategory').textContent = category?.category_name || '---';
    if (qs('addPreviewWallet')) qs('addPreviewWallet').textContent = wallet?.wallet_name || '---';
    if (qs('addPreviewDate')) qs('addPreviewDate').textContent = formatDate(date);
    if (qs('addPreviewRepeat')) qs('addPreviewRepeat').textContent = repeat ? recurringText(repeat) : t('Không lặp', 'No repeat');
  }

  function syncDetailPreview() {
    const transaction = getTransaction(state.detailId);
    if (!transaction) return;

    const wallet = getWallet(qs('detailEditWallet')?.value || transaction.walletId);
    const categoryId = qs('detailEditCategoryId')?.value || transaction.categoryId;
    const category = getCategory(categoryId);
    const amount = Number(qs('detailEditAmount')?.value || transaction.amountValue || 0);
    const type = qs('detailEditType')?.value || transaction.transactionType;
    const note = (qs('detailEditNote')?.value || '').trim();
    const repeat = qs('detailEditRecurrence')?.value || '';
    const date = parseIso(qs('detailEditDate')?.value || transaction.transactionDate) || state.anchorDate;
    const sign = type === 'income' ? '+' : '-';

    if (qs('detailBadgeText')) qs('detailBadgeText').textContent = repeat ? recurringText(repeat) : t('Một lần', 'One-time');
    if (qs('detailHeroIconWrap')) {
      const color = category?.color || transaction.categoryColor || '#8592A3';
      qs('detailHeroIconWrap').style.background = alphaColor(color, 0.18);
      qs('detailHeroIconWrap').style.color = color;
    }
    if (qs('detailHeroIcon')) qs('detailHeroIcon').innerHTML = iconMarkup(category?.icon || transaction.categoryIcon, category?.category_name || transaction.categoryName);
    if (qs('detailNoteText')) qs('detailNoteText').textContent = note || t('Không có ghi chú', 'No note');
    if (qs('detailAmountText')) qs('detailAmountText').textContent = `${sign}${amount.toLocaleString(locale)} ${wallet?.currency || transaction.currency || 'VND'}`;
    if (qs('detailCategoryText')) qs('detailCategoryText').textContent = category?.category_name || transaction.categoryName;
    if (qs('detailWalletText')) qs('detailWalletText').textContent = wallet?.wallet_name || transaction.walletName;
    if (qs('detailDateText')) qs('detailDateText').textContent = formatDate(date);
    if (qs('detailTypeText')) qs('detailTypeText').textContent = type === 'income' ? t('Thu nhập', 'Income') : t('Chi tiêu', 'Expense');
  }
  function applyTypeToggle(containerId, hiddenInputId, initialValue, onChange) {
    const container = document.getElementById(containerId);
    const hidden = document.getElementById(hiddenInputId);
    if (!container || !hidden) return;

    const buttons = container.querySelectorAll('button[data-value]');

    const update = (value) => {
      hidden.value = value;
      buttons.forEach((btn) => {
        btn.classList.toggle('active', btn.dataset.value === value);
      });
      if (typeof onChange === 'function') onChange(value);
    };

    buttons.forEach((btn) => {
      btn.onclick = (e) => {
        e.preventDefault();
        update(btn.dataset.value);
      };
    });

    update(initialValue);
  }

  function clearCategorySelection(mode) {
    const isAdd = mode === 'add';
    const grid = qs(isAdd ? 'addCategoryGrid' : 'detailCategoryGrid');
    const idInput = qs(isAdd ? 'addTransactionCategoryId' : 'detailEditCategoryId');
    const iconInput = qs(isAdd ? 'addTransactionCategoryIcon' : 'detailEditCategoryIcon');
    const colorInput = qs(isAdd ? 'addTransactionCategoryColor' : 'detailEditCategoryColor');

    qsa('.tx-category-chip', grid).forEach((chip) => chip.classList.remove('active'));
    if (idInput) idInput.value = '';
    if (iconInput) iconInput.value = 'bx bx-category';
    if (colorInput) colorInput.value = '#8592A3';
  }

  function ensureCategoryEmptyState(grid) {
    if (!grid) return null;

    let emptyState = grid.querySelector('.tx-category-empty');
    if (!emptyState) {
      emptyState = document.createElement('div');
      emptyState.className = 'tx-summary-empty tx-category-empty d-none';
      grid.appendChild(emptyState);
    }

    return emptyState;
  }

  function filterCategoryGrid(mode, type, preferredCategoryId = '') {
    const isAdd = mode === 'add';
    const grid = qs(isAdd ? 'addCategoryGrid' : 'detailCategoryGrid');
    const currentType = (type || 'expense').toLowerCase();
    if (!grid) return;

    const visibleButtons = [];
    qsa('.tx-category-chip', grid).forEach((chip) => {
      const chipType = (chip.dataset.type || 'expense').toLowerCase();
      const isMatch = chipType === currentType;

      chip.classList.toggle('d-none', !isMatch);
      chip.disabled = !isMatch;
      chip.setAttribute('aria-hidden', isMatch ? 'false' : 'true');

      if (!isMatch) {
        chip.classList.remove('active');
        return;
      }

      visibleButtons.push(chip);
    });

    const emptyState = ensureCategoryEmptyState(grid);
    if (emptyState) {
      emptyState.textContent = currentType === 'income'
        ? t('Chưa có danh mục thu nhập nào.', 'No income categories available.')
        : t('Chưa có danh mục chi tiêu nào.', 'No expense categories available.');
      emptyState.classList.toggle('d-none', visibleButtons.length > 0);
    }

    const savedCategoryId = state.categorySelections?.[mode]?.[currentType] || '';
    const currentSelectedId = qs(isAdd ? 'addTransactionCategoryId' : 'detailEditCategoryId')?.value || '';
    const nextButton = visibleButtons.find((button) => button.dataset.id === savedCategoryId)
      || visibleButtons.find((button) => button.dataset.id === currentSelectedId)
      || visibleButtons.find((button) => button.dataset.id === preferredCategoryId)
      || visibleButtons[0]
      || null;

    if (nextButton) {
      selectCategory(mode, nextButton);
      return;
    }

    clearCategorySelection(mode);
    if (isAdd) {
      syncAddPreview();
    } else {
      syncDetailPreview();
    }
  }

  function selectCategory(mode, button) {
    const isAdd = mode === 'add';

    const gridId = isAdd ? 'addCategoryGrid' : 'detailCategoryGrid';
    const idInputId = isAdd ? 'addTransactionCategoryId' : 'detailEditCategoryId';
    const iconInputId = isAdd ? 'addTransactionCategoryIcon' : 'detailEditCategoryIcon';
    const colorInputId = isAdd ? 'addTransactionCategoryColor' : 'detailEditCategoryColor';

    const grid = qs(gridId);
    const idInput = qs(idInputId);
    const iconInput = qs(iconInputId);
    const colorInput = qs(colorInputId);

    if (!grid || !idInput || !button) return;

    qsa('.tx-category-chip', grid).forEach((chip) => {
      chip.classList.remove('active');
    });

    button.classList.add('active');

    idInput.value = button.dataset.id || '';

    const categoryType = (button.dataset.type || 'expense').toLowerCase();
    if (state.categorySelections?.[mode]) {
      state.categorySelections[mode][categoryType] = button.dataset.id || '';
    }

    if (iconInput) iconInput.value = button.dataset.icon || 'bx bx-category';
    if (colorInput) colorInput.value = button.dataset.color || '#8592A3';

    button.style.transition = "transform 0.1s ease";
    button.style.transform = "scale(0.94)";

    setTimeout(() => {
      button.style.transform = "scale(1)";
    }, 100);

    if (isAdd) {
      syncAddPreview();
    } else {
      syncDetailPreview();
    }
  }

  function resetAddForm() {
    const walletSelect = qs('addTransactionWallet');
    if (walletSelect) {
      const defaultOption = Array.from(walletSelect.options).find(opt => opt.dataset.isDefault === 'true');
      if (defaultOption) {
        walletSelect.value = defaultOption.value;
      } else {
        walletSelect.selectedIndex = 0;
      }
    }

    if (qs('addTransactionAmount')) qs('addTransactionAmount').value = '';
    if (qs('addTransactionNote')) qs('addTransactionNote').value = '';
    state.categorySelections.add.expense = '';
    state.categorySelections.add.income = '';

    applyTypeToggle('addTypeSwitch', 'addTransactionType', 'expense', (value) => {
      filterCategoryGrid('add', value);
      syncAddPreview();
    });
  }

  function switchAddModalMode(mode) {
    const isTransfer = (mode === 'transfer');

    qs('addTransactionPanel')?.classList.toggle('d-none', isTransfer);
    qs('addTransferPanel')?.classList.toggle('d-none', !isTransfer);
    qs('addTransactionPreviewPanel')?.classList.toggle('d-none', isTransfer);
    qs('addTransferPreviewPanel')?.classList.toggle('d-none', !isTransfer);

    qs('btnSaveTransactionStatic')?.classList.toggle('d-none', isTransfer);
    qs('btnSaveTransferStatic')?.classList.toggle('d-none', !isTransfer);

    const titleEl = qs('addModalTitle');
    const subtitleEl = qs('addModalSubtitle');

    if (isTransfer) {
      if (titleEl) titleEl.textContent = t('Chuyển tiền giữa ví', 'Wallet Transfer');
      if (subtitleEl) subtitleEl.textContent = t('Hệ thống sẽ tạo 2 giao dịch chi/thu liên kết.', 'Creates linked expense and income transactions.');

      resetTransferForm();
    } else {
      if (titleEl) titleEl.textContent = t('Thêm giao dịch', 'Add Transaction');
      if (subtitleEl) subtitleEl.textContent = t('Ghi lại chi tiêu hoặc thu nhập mới.', 'Record a new expense or income.');

      resetAddForm();
    }

    qsa('#addModalModeTabs .tx-mode-tab').forEach(tab => {
      tab.classList.toggle('active', tab.dataset.mode === mode);
    });
  }

  function populateDetailForm(transactionId) {
    const transaction = getTransaction(transactionId);
    if (!transaction) return;

    state.detailId = transactionId;
    if (qs('detailEditWallet')) qs('detailEditWallet').value = transaction.walletId;
    if (qs('detailEditAmount')) qs('detailEditAmount').value = transaction.amountValue;
    if (detailDatePicker) detailDatePicker.setDate(transaction.transactionDate, true);
    if (qs('detailEditRecurrence')) {
      const recurringValue = transaction.recurringBadgeText === t('Một lần', 'One-time') ? '' : reverseRecurringText(transaction.recurringBadgeText);
      qs('detailEditRecurrence').value = recurringValue;
    }
    if (qs('detailEditNote')) qs('detailEditNote').value = transaction.note === t('Không có ghi chú', 'No note') ? '' : transaction.note;
    state.categorySelections.detail.expense = transaction.transactionType === 'expense' ? transaction.categoryId : '';
    state.categorySelections.detail.income = transaction.transactionType === 'income' ? transaction.categoryId : '';
    applyTypeToggle('detailTypeSwitch', 'detailEditType', transaction.transactionType, (value) => {
      filterCategoryGrid('detail', value, transaction.categoryId);
      syncDetailPreview();
    });
  }

  function reverseRecurringText(value) {
    const map = {
      [t('Hàng ngày', 'Daily')]: 'daily',
      [t('Hàng tuần', 'Weekly')]: 'weekly',
      [t('Hàng tháng', 'Monthly')]: 'monthly',
      [t('Hàng năm', 'Yearly')]: 'yearly'
    };
    return map[value] || '';
  }

  function buildCreatePayload() {
    return {
      wallet_id: qs('addTransactionWallet')?.value || '',
      category_id: qs('addTransactionCategoryId')?.value || qs('#addCategoryGrid .tx-category-chip.active')?.dataset.id || '',
      transaction_type: qs('addTransactionType')?.value || 'expense',
      amount: Number(qs('addTransactionAmount')?.value || 0),
      transaction_date: qs('addTransactionDate')?.value || '',
      note: (qs('addTransactionNote')?.value || '').trim() || null,
      is_recurring: !!qs('addTransactionRecurrence')?.value,
      recur_interval: qs('addTransactionRecurrence')?.value || null
    };
  }

  function buildUpdatePayload() {
    return {
      wallet_id: qs('detailEditWallet')?.value || null,
      category_id: qs('detailEditCategoryId')?.value || qs('#detailCategoryGrid .tx-category-chip.active')?.dataset.id || null,
      transaction_type: qs('detailEditType')?.value || null,
      amount: Number(qs('detailEditAmount')?.value || 0),
      transaction_date: qs('detailEditDate')?.value || null,
      note: (qs('detailEditNote')?.value || '').trim(),
      is_recurring: !!qs('detailEditRecurrence')?.value,
      recur_interval: qs('detailEditRecurrence')?.value || null
    };
  }

  function validatePayload(payload) {
    if (!payload.wallet_id) return t('Vui lòng chọn ví.', 'Please choose a wallet.');
    if (!payload.category_id) return t('Vui lòng chọn danh mục.', 'Please choose a category.');
    if (!payload.transaction_type) return t('Vui lòng chọn loại giao dịch.', 'Please choose a transaction type.');
    if (!payload.amount || payload.amount <= 0) return t('Số tiền phải lớn hơn 0.', 'Amount must be greater than 0.');
    if (!payload.transaction_date) return t('Vui lòng chọn ngày giao dịch.', 'Please choose the transaction date.');
    if (payload.is_recurring && !payload.recur_interval) return t('Vui lòng chọn chu kỳ lặp.', 'Please choose recurrence interval.');
    return null;
  }

  async function jsonFetch(url, options = {}) {
    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
      },
      ...options
    });

    const data = await response.json().catch(() => ({ success: false, message: t('Có lỗi xảy ra.', 'Something went wrong.') }));
    if (!response.ok || data.success === false) {
      throw new Error(data.message || t('Có lỗi xảy ra.', 'Something went wrong.'));
    }
    return data;
  }

  function showToast(id) {
    const element = qs(id);
    if (!element) return;
    bootstrap.Toast.getOrCreateInstance(element).show();
  }

  async function createTransaction() {
    const payload = buildCreatePayload();
    const validationError = validatePayload(payload);
    if (validationError) {
      alert(validationError);
      return;
    }

    try {
      const result = await jsonFetch('/Transaction/CreateAjax', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      replaceTransaction(result.data);
      if (addModal) addModal.hide();
      resetAddForm();
      showToast('transactionToastSaved');
      refresh();
    } catch (error) {
      alert(error.message || t('Không thể thêm giao dịch.', 'Cannot create transaction.'));
    }
  }

  async function updateTransaction() {
    if (!state.detailId) return;
    const payload = buildUpdatePayload();
    const validationError = validatePayload(payload);
    if (validationError) {
      alert(validationError);
      return;
    }

    try {
      const result = await jsonFetch(`/Transaction/UpdateAjax?id=${encodeURIComponent(state.detailId)}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      replaceTransaction(result.data);
      if (detailModal) detailModal.hide();
      showToast('transactionToastUpdated');
      refresh();
    } catch (error) {
      alert(error.message || t('Không thể cập nhật giao dịch.', 'Cannot update transaction.'));
    }
  }

  async function deleteTransaction(transactionId) {
    const id = transactionId || state.detailId;
    if (!id) return;
    const confirmed = window.confirm(t('Bạn có chắc muốn xóa giao dịch này?', 'Are you sure you want to delete this transaction?'));
    if (!confirmed) return;

    try {
      await jsonFetch(`/Transaction/DeleteAjax?id=${encodeURIComponent(id)}`, {
        method: 'DELETE'
      });
      removeTransaction(id);
      if (detailModal && state.detailId === id) detailModal.hide();
      state.detailId = null;
      showToast('transactionToastDeleted');
      refresh();
    } catch (error) {
      alert(error.message || t('Không thể xóa giao dịch.', 'Cannot delete transaction.'));
    }
  }

  function exportAllTransactions() {
    if (!transactions.length) {
      alert(t('Không có dữ liệu để xuất.', 'No data to export.'));
      return;
    }

    if (typeof XLSX === 'undefined') {
      alert(t('Thiếu thư viện xuất Excel.', 'Excel export library is missing.'));
      return;
    }

    const rows = transactions
      .slice()
      .sort((left, right) => `${right.transactionDate}${right.id}`.localeCompare(`${left.transactionDate}${left.id}`))
      .map((item, index) => {
        const exportType = item.isTransfer
          ? t('Chuyển ví', 'Transfer')
          : (item.transactionType === 'income' ? t('Thu nhập', 'Income') : t('Chi tiêu', 'Expense'));

        return {
          [t('STT', 'No.')]: index + 1,
          [t('Ngày', 'Date')]: item.transactionDateText || item.transactionDate,
          [t('Loại giao dịch', 'Transaction type')]: exportType,
          [t('Danh mục', 'Category')]: item.categoryName,
          [t('Ví', 'Wallet')]: item.walletName,
          [t('Số tiền', 'Amount')]: item.transactionType === 'income' ? item.amountValue : -item.amountValue,
          [t('Tiền tệ', 'Currency')]: item.currency || 'VND',
          [t('Chu kỳ', 'Repeat')]: item.recurringBadgeText,
          [t('Ghi chú', 'Note')]: item.note === t('Không có ghi chú', 'No note') ? '' : item.note
        };
      });

    const worksheet = XLSX.utils.json_to_sheet(rows);
    worksheet['!cols'] = [
      { wch: 8 },
      { wch: 14 },
      { wch: 18 },
      { wch: 22 },
      { wch: 22 },
      { wch: 16 },
      { wch: 12 },
      { wch: 14 },
      { wch: 36 }
    ];

    for (let rowIndex = 1; rowIndex <= rows.length; rowIndex += 1) {
      const amountCell = worksheet[XLSX.utils.encode_cell({ r: rowIndex, c: 5 })];
      if (amountCell) {
        amountCell.t = 'n';
        amountCell.z = '#,##0';
      }
    }

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, t('Giao dịch', 'Transactions'));

    const timestamp = new Date();
    const pad = (value) => String(value).padStart(2, '0');
    const fileName = `transactions-${timestamp.getFullYear()}${pad(timestamp.getMonth() + 1)}${pad(timestamp.getDate())}-${pad(timestamp.getHours())}${pad(timestamp.getMinutes())}${pad(timestamp.getSeconds())}.xlsx`;

    XLSX.writeFile(workbook, fileName, { compression: true });
  }

  function openDay(dateText) {
    state.selectedCalendarDate = dateText;
    state.anchorDate = parseIso(dateText) || state.anchorDate;
    state.calendarMonth = monthStart(state.anchorDate);
    renderPeriodAssist();
    renderCalendar();
    renderDayDrawer(dateText);
    if (dayOffcanvas) dayOffcanvas.show();
  }

  function openDetail(transactionId) {
    populateDetailForm(transactionId);
    if (detailModal) detailModal.show();
  }

  function moveAnchor(direction) {
    if (state.period === 'today') {
      state.anchorDate = addDays(state.anchorDate, direction);
    } else if (state.period === 'week') {
      state.anchorDate = addDays(state.anchorDate, direction * 7);
    } else if (state.period === 'month') {
      state.anchorDate = addMonths(state.anchorDate, direction);
    }
    refresh();
  }

  function refresh() {
    refreshFilteredState();
    renderPeriodAssist();
    renderBreakdownChart();
    renderList();
    renderCalendar();
    if (state.selectedCalendarDate && dayOffcanvasEl?.classList.contains('show')) renderDayDrawer(state.selectedCalendarDate);
    syncAddPreview();
    if (state.detailId) syncDetailPreview();
  }

    function bindEvents() {
        qsa('#breakdownTypeTabs button[data-type]').forEach((button) => {
            button.addEventListener('click', () => {
                qsa('#breakdownTypeTabs button[data-type]').forEach((item) => item.classList.remove('active'));
                button.classList.add('active');
                state.breakdownType = button.dataset.type || 'expense';
                refresh();
            });
        });

        qsa('#ledgerTypeTabs button[data-type]').forEach((button) => {
            button.addEventListener('click', () => {
                qsa('#ledgerTypeTabs button[data-type]').forEach((item) => item.classList.remove('active'));
                button.classList.add('active');
                state.ledgerType = button.dataset.type || 'all';
                state.page = 1;
                refresh();
            });
        });

        qs('walletFilter')?.addEventListener('change', () => {
            state.walletId = qs('walletFilter').value || 'all';
            state.page = 1;
            refresh();
        });

        qs('transactionSearchInput')?.addEventListener('input', () => {
            state.keyword = (qs('transactionSearchInput').value || '').trim().toLowerCase();
            state.page = 1;
            refresh();
        });

        qs('periodFilter')?.addEventListener('change', () => {
            state.period = qs('periodFilter').value || 'today';
            state.page = 1;
            if (state.period === 'custom' && !state.from && !state.to) {
                state.from = formatIso(addDays(state.anchorDate, -6));
                state.to = formatIso(state.anchorDate);
                if (fromPicker) fromPicker.setDate(state.from, true);
                if (toPicker) toPicker.setDate(state.to, true);
            }
            refresh();
        });

        qs('dayPrevBtn')?.addEventListener('click', () => moveAnchor(-1));
        qs('dayNextBtn')?.addEventListener('click', () => moveAnchor(1));
        qs('calendarPrevBtn')?.addEventListener('click', () => {
            state.calendarMonth = addMonths(state.calendarMonth, -1);
            renderCalendar();
        });
        qs('calendarNextBtn')?.addEventListener('click', () => {
            state.calendarMonth = addMonths(state.calendarMonth, 1);
            renderCalendar();
        });

        qsa('#addModalModeTabs .tx-mode-tab').forEach((tab) => {
            tab.addEventListener('click', () => {
                switchAddModalMode(tab.dataset.mode);
            });
        });

        addModalEl?.addEventListener('hidden.bs.modal', () => {
            switchAddModalMode('transaction');
        });

        applyTypeToggle('addTypeSwitch', 'addTransactionType', 'expense', (value) => {
          filterCategoryGrid('add', value);
          syncAddPreview();
        });
        applyTypeToggle('detailTypeSwitch', 'detailEditType', 'expense', (value) => {
          filterCategoryGrid('detail', value);
          syncDetailPreview();
        });

        ['addTransactionWallet', 'addTransactionAmount', 'addTransactionDate', 'addTransactionRecurrence', 'addTransactionNote']
            .forEach((id) => qs(id)?.addEventListener('input', syncAddPreview));
        ['addTransactionWallet', 'addTransactionRecurrence'].forEach((id) => qs(id)?.addEventListener('change', syncAddPreview));

        ['detailEditWallet', 'detailEditAmount', 'detailEditDate', 'detailEditRecurrence', 'detailEditNote']
            .forEach((id) => qs(id)?.addEventListener('input', syncDetailPreview));
        ['detailEditWallet', 'detailEditRecurrence'].forEach((id) => qs(id)?.addEventListener('change', syncDetailPreview));

        ['transferFromWallet', 'transferToWallet', 'transferAmount', 'transferDate', 'transferNote']
            .forEach((id) => {
                qs(id)?.addEventListener('input', syncTransferPreview);
                qs(id)?.addEventListener('change', syncTransferPreview);
            });

        qsa('#addCategoryGrid .tx-category-chip').forEach((button) => {
            button.addEventListener('click', () => selectCategory('add', button));
        });
        qsa('#detailCategoryGrid .tx-category-chip').forEach((button) => {
            button.addEventListener('click', () => selectCategory('detail', button));
        });

        qs('btnSaveTransactionStatic')?.addEventListener('click', createTransaction);
        qs('btnSaveTransferStatic')?.addEventListener('click', createTransfer);
        qs('btnUpdateTransactionStatic')?.addEventListener('click', updateTransaction);
        qs('btnDeleteTransactionStatic')?.addEventListener('click', () => deleteTransaction());
        qsa('.tx-export-btn').forEach((button) => button.addEventListener('click', exportAllTransactions));

        detailModalEl?.addEventListener('hidden.bs.modal', () => {
            if (state.reopenDayDrawerOnDetailClose && state.selectedCalendarDate) {
                renderDayDrawer(state.selectedCalendarDate);
                if (dayOffcanvas) dayOffcanvas.show();
            }
            state.reopenDayDrawerOnDetailClose = false;
        });

        addModalEl?.addEventListener('shown.bs.modal', () => {
            if (qs('addTransactionPanel').classList.contains('d-none')) {
                syncTransferPreview();
            } else {
                syncAddPreview();
            }
        });

        detailModalEl?.addEventListener('shown.bs.modal', syncDetailPreview);
    }

    const transferDatePicker = qs('transferDate') ? flatpickr(qs('transferDate'), {
        ...fpBaseConfig,
        defaultDate: state.anchorDate
    }) : null;

    function syncTransferPreview() {
        const fromSel = qs('transferFromWallet');
        const toSel = qs('transferToWallet');
        const amount = Number(qs('transferAmount')?.value || 0);

        const fromName = fromSel?.options[fromSel.selectedIndex]?.text || '---';
        const toName = toSel?.options[toSel.selectedIndex]?.text || '---';

        if (qs('transferPreviewFromName')) qs('transferPreviewFromName').textContent = fromName;
        if (qs('transferPreviewToName')) qs('transferPreviewToName').textContent = toName;
        if (qs('transferPreviewAmount')) qs('transferPreviewAmount').textContent = formatMoney(amount);

        const sameWallet = fromSel?.value && toSel?.value && fromSel.value === toSel.value;
        qs('transferSameWalletError')?.classList.toggle('d-none', !sameWallet);
    }

    function resetTransferForm() {
        const fromSel = qs('transferFromWallet');
        const toSel = qs('transferToWallet');

        if (fromSel) {
            const defaultOpt = Array.from(fromSel.options).find(opt => opt.dataset.isDefault === 'true');
            if (defaultOpt) {
                fromSel.value = defaultOpt.value;
            } else {
                fromSel.selectedIndex = 0;
            }
        }

        if (toSel && fromSel) {
            const otherOpt = Array.from(toSel.options).find(opt => opt.value !== fromSel.value);
            if (otherOpt) {
                toSel.value = otherOpt.value;
            } else {
                toSel.selectedIndex = 1;
            }
        }

        if (qs('transferAmount')) qs('transferAmount').value = '';
        if (qs('transferNote')) qs('transferNote').value = '';
        if (transferDatePicker) transferDatePicker.setDate(state.anchorDate, true);

        qs('transferSameWalletError')?.classList.add('d-none');

        syncTransferPreview();
    }

    async function createTransfer() {
        const payload = {
            from_wallet_id: qs('transferFromWallet')?.value,
            to_wallet_id: qs('transferToWallet')?.value,
            amount: Number(qs('transferAmount')?.value || 0),
            transfer_date: qs('transferDate')?.value,
            note: (qs('transferNote')?.value || '').trim() || null
        };

        if (payload.from_wallet_id === payload.to_wallet_id) {
            alert(t('Ví nguồn và đích không được trùng nhau.', 'Source and destination wallets must be different.'));
            return;
        }
        if (payload.amount <= 0) {
            alert(t('Vui lòng nhập số tiền hợp lệ.', 'Please enter a valid amount.'));
            return;
        }

        try {
            const result = await jsonFetch('/Transaction/TransferAjax', {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            if (result.success) {
                if (result.data.expense) replaceTransaction(result.data.expense);
                if (result.data.income) replaceTransaction(result.data.income);

                addModal.hide();
                showToast('transactionToastSaved');
                refresh();
            }
        } catch (error) {
            alert(error.message);
        }
    }

  resetAddForm();
  bindEvents();
  state.selectedCalendarDate = formatIso(state.anchorDate);
  state.calendarMonth = monthStart(state.anchorDate);
  refresh();
});
