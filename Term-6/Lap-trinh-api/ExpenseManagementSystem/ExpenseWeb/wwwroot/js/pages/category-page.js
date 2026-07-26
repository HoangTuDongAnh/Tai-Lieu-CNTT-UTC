document.addEventListener("DOMContentLoaded", function () {
    const addModalEl = document.getElementById("addCategoryModal");
    const detailModalEl = document.getElementById("categoryDetailModal");
    const addModal = addModalEl ? new bootstrap.Modal(addModalEl) : null;
    const detailModal = detailModalEl ? new bootstrap.Modal(detailModalEl) : null;

    const toastSaved = document.getElementById("categorySavedToast") ? new bootstrap.Toast(document.getElementById("categorySavedToast")) : null;
    const toastUpdated = document.getElementById("categoryUpdatedToast") ? new bootstrap.Toast(document.getElementById("categoryUpdatedToast")) : null;
    const toastDeleted = document.getElementById("categoryDeletedToast") ? new bootstrap.Toast(document.getElementById("categoryDeletedToast")) : null;

    const createUrl = document.getElementById("categoryCreateUrl")?.value || "";
    const updateBaseUrl = document.getElementById("categoryUpdateBaseUrl")?.value || "";
    const deleteBaseUrl = document.getElementById("categoryDeleteBaseUrl")?.value || "";
    const saveBudgetUrl = document.getElementById("categorySaveBudgetUrl")?.value || "";
    const budgetsByCategoryUrl = document.getElementById("categoryBudgetsByCategoryUrl")?.value || "";
    const deleteBudgetsByCategoryUrl = document.getElementById("categoryDeleteBudgetsByCategoryUrl")?.value || "/Category/DeleteBudgetsByCategory";

    const state = {
        currentCategory: null,
        budgets: [],
        activeBudgetId: null,
        defaultIcon: document.querySelector(".category-icon-option-detail")?.dataset.icon || document.getElementById("detailHeadIcon")?.getAttribute("src") || "",
        detailInitialType: "expense",
        pendingBudgetRemoval: false,
        warningDialogOpen: false,
        originalBudgets: [],
        draftBudgets: [],
        budgetCache: new Map(),
        budgetLoadToken: 0
    };

    function getCurrentCategoryCard() {
        const categoryId = state.currentCategory?.id || "";
        if (!categoryId) return null;
        return document.querySelector(`.category-card[data-category-id="${categoryId}"]`);
    }

    function getCurrentTransactionCount() {
        const card = getCurrentCategoryCard();
        return parseInt(card?.getAttribute("data-category-transaction-count") || "0", 10) || 0;
    }

    function currency(value) {
        return Number(value || 0).toLocaleString("vi-VN") + " đ";
    }

    function parseBudgetAmount(rawValue) {
        const normalized = String(rawValue || "").trim().replace(/\s/g, "").replace(/,/g, "");
        if (!normalized) return NaN;
        return Number(normalized);
    }

    function hexToRgba(hex, alpha) {
        if (!hex || !hex.startsWith("#") || hex.length !== 7) return `rgba(255,171,0,${alpha})`;
        const r = parseInt(hex.slice(1, 3), 16);
        const g = parseInt(hex.slice(3, 5), 16);
        const b = parseInt(hex.slice(5, 7), 16);
        return `rgba(${r}, ${g}, ${b}, ${alpha})`;
    }

    function setActiveColor(selector, color) {
        document.querySelectorAll(selector).forEach(function (btn) {
            btn.classList.toggle("active", (btn.dataset.color || "").toUpperCase() === (color || "").toUpperCase());
        });
    }

    function normalizeCategoryType(value) {
        return String(value || '').toLowerCase() === 'income' ? 'income' : 'expense';
    }

    function categoryTypeLabel(value) {
        return normalizeCategoryType(value) === 'income' ? 'Thu nhập' : 'Chi tiêu';
    }

    function setCategoryTypeToggle(target, value, canEdit = true) {
        const normalized = normalizeCategoryType(value);
        const hiddenInput = document.getElementById(target === 'detail' ? 'detailCategoryType' : 'addCategoryType');
        if (hiddenInput) hiddenInput.value = normalized;

        document.querySelectorAll(`[data-category-type-target="${target}"]`).forEach(function (button) {
            const isActive = (button.dataset.categoryTypeValue || '') === normalized;
            button.classList.toggle('active', isActive);
            button.setAttribute('aria-pressed', isActive ? 'true' : 'false');
            button.disabled = !canEdit;
        });

        if (target === 'detail') {
            syncDetailBudgetAvailability();
            updateDetailPreview();
        }
    }

    function syncDetailBudgetAvailability() {
        const currentType = normalizeCategoryType(document.getElementById('detailCategoryType')?.value);
        const locked = currentType === 'income';
        const noBudgetState = document.getElementById('noBudgetState');
        const hasBudgetState = document.getElementById('hasBudgetState');
        const lockedState = document.getElementById('incomeBudgetLockedState');
        const budgetPanel = document.querySelector('.category-budget-panel');
        const addBudgetButton = document.getElementById('btnOpenBudgetEditor');

        budgetPanel?.classList.toggle('is-disabled', locked);
        if (addBudgetButton) addBudgetButton.disabled = locked;
        if (lockedState) lockedState.classList.toggle('d-none', !locked);

        if (locked) {
            noBudgetState?.classList.add('d-none');
            hasBudgetState?.classList.add('d-none');
            closeBudgetChildDialog();
            return;
        }

        const workingBudgets = getWorkingBudgets();
        state.budgets = workingBudgets;
        const hasItems = Array.isArray(workingBudgets) && workingBudgets.length > 0;
        noBudgetState?.classList.toggle('d-none', hasItems);
        hasBudgetState?.classList.toggle('d-none', !hasItems);
    }

    function buildUpdateUrl(id) {
        return updateBaseUrl.replace("__id__", encodeURIComponent(id));
    }

    function buildDeleteUrl(id) {
        return deleteBaseUrl.replace("__id__", encodeURIComponent(id));
    }

    function buildCategoryBudgetsUrl(categoryId) {
        const connector = budgetsByCategoryUrl.includes("?") ? "&" : "?";
        return `${budgetsByCategoryUrl}${connector}categoryId=${encodeURIComponent(categoryId)}`;
    }

    function escapeHtml(value) {
        return String(value || "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function ensureTypeWarningDialog() {
        let overlay = document.getElementById("categoryTypeWarningOverlay");
        if (overlay) return overlay;

        const host = detailModalEl?.querySelector('.modal-content') || detailModalEl || document.body;
        overlay = document.createElement("div");
        overlay.id = "categoryTypeWarningOverlay";
        overlay.className = "category-subdialog category-warning-subdialog d-none";
        overlay.setAttribute("aria-hidden", "true");
        overlay.innerHTML = `
            <div class="category-subdialog__backdrop" data-type-warning-close="true"></div>
            <div class="category-subdialog__panel category-warning-subdialog__panel" role="dialog" aria-modal="true" aria-labelledby="categoryTypeWarningTitle">
                <div class="category-subdialog__header category-warning-subdialog__header">
                    <div>
                        <div class="category-section-title mb-1">
                            <i class="bx bx-error-circle"></i>
                            <span id="categoryTypeWarningTitle">Xác nhận thay đổi loại</span>
                        </div>
                        <div class="text-muted small">Thay đổi này chỉ là tạm thời cho đến khi bạn bấm <strong>Lưu thay đổi</strong> ở popup chi tiết.</div>
                    </div>
                    <button type="button" class="btn category-subdialog__close" id="btnCloseTypeWarning">&times;</button>
                </div>
                <div class="category-subdialog__body">
                    <div class="category-warning-subdialog__hero">
                        <div class="category-warning-subdialog__icon"><i class="bx bx-transfer-alt"></i></div>
                        <div>
                            <div class="category-warning-subdialog__eyebrow">Cảnh báo thay đổi loại</div>
                            <h5 class="category-warning-subdialog__title mb-2">Chuyển danh mục sang Thu nhập?</h5>
                            <p class="category-warning-subdialog__text mb-0">Các hạn mức hiện có của <strong id="categoryTypeWarningName">danh mục này</strong> sẽ chỉ bị xóa khi bạn bấm <strong>Lưu thay đổi</strong> ở popup chi tiết.</p>
                        </div>
                    </div>
                    <div class="category-warning-subdialog__note">
                        <i class="bx bx-info-circle"></i>
                        <span>Bạn vẫn có thể đổi lại về Chi tiêu trước khi lưu và sẽ không mất hạn mức.</span>
                    </div>
                </div>
                <div class="category-subdialog__footer category-warning-subdialog__footer">
                    <button type="button" class="btn btn-outline-secondary" id="btnKeepExpenseType">Giữ nguyên</button>
                    <button type="button" class="btn btn-warning" id="btnConfirmIncomeType">Vẫn chuyển sang Thu nhập</button>
                </div>
            </div>`;

        host.appendChild(overlay);
        overlay.querySelectorAll('[data-type-warning-close="true"], #btnCloseTypeWarning, #btnKeepExpenseType').forEach(function (el) {
            el.addEventListener('click', function () {
                closeTypeWarningDialog(true);
            });
        });
        overlay.querySelector('#btnConfirmIncomeType')?.addEventListener('click', function () {
            applyIncomeTypeChange();
        });
        return overlay;
    }

    function openTypeWarningDialog() {
        const overlay = ensureTypeWarningDialog();
        const categoryName = (document.getElementById('detailCategoryName')?.value || state.currentCategory?.name || 'danh mục').trim();
        const nameEl = overlay.querySelector('#categoryTypeWarningName');
        if (nameEl) nameEl.textContent = categoryName;
        overlay.classList.remove('d-none');
        overlay.setAttribute('aria-hidden', 'false');
        document.body.classList.add('category-subdialog-open');
        detailModalEl?.classList.add('category-warning-open');
        state.warningDialogOpen = true;
    }

    function closeTypeWarningDialog(restoreExpense) {
        const overlay = document.getElementById('categoryTypeWarningOverlay');
        if (!overlay) return;
        overlay.classList.add('d-none');
        overlay.setAttribute('aria-hidden', 'true');
        state.warningDialogOpen = false;
        if (!document.getElementById('budgetChildOverlay')?.classList.contains('d-none') && !document.getElementById('deleteChildOverlay')?.classList.contains('d-none')) {
            // no-op; body class handled below
        }
        const anyOpen = [ 'budgetChildOverlay', 'deleteChildOverlay', 'categoryTypeWarningOverlay' ].some(function (id) {
            const el = document.getElementById(id);
            return el && !el.classList.contains('d-none');
        });
        if (!anyOpen) document.body.classList.remove('category-subdialog-open');
        detailModalEl?.classList.remove('category-warning-open');
        if (restoreExpense) {
            setCategoryTypeToggle('detail', 'expense', document.getElementById('detailCategoryCanEdit')?.value === 'true');
            state.pendingBudgetRemoval = false;
        }
    }

    function applyIncomeTypeChange() {
        closeTypeWarningDialog(false);
        state.pendingBudgetRemoval = true;
        setCategoryTypeToggle('detail', 'income', document.getElementById('detailCategoryCanEdit')?.value === 'true');
    }

    function resetDetailDraftState() {
        state.pendingBudgetRemoval = false;
        state.warningDialogOpen = false;
        state.originalBudgets = [];
        state.draftBudgets = [];
        const overlay = document.getElementById('categoryTypeWarningOverlay');
        if (overlay) {
            overlay.classList.add('d-none');
            overlay.setAttribute('aria-hidden', 'true');
        }
    }

    async function deleteBudgetsForCategory(categoryId) {
        if (!categoryId) return;
        await sendJson(deleteBudgetsByCategoryUrl, 'POST', { category_id: categoryId });
    }

    function cloneBudgetItem(item) {
        return item ? JSON.parse(JSON.stringify(item)) : item;
    }

    function normalizeBudgetDraft(item) {
        const spent = Number(item?.spent_amount || 0);
        const total = Number(item?.limit_amount || 0);
        const percentage = total > 0 ? (spent * 100 / total) : 0;
        let status = 'normal';
        if (percentage > 100) status = 'over';
        else if (percentage >= 100) status = 'reached';
        return {
            ...item,
            spent_amount: spent,
            limit_amount: total,
            percentage_used: Number(percentage.toFixed(2)),
            status
        };
    }

    function getWorkingBudgets() {
        return state.pendingBudgetRemoval ? [] : (state.draftBudgets || []);
    }

    async function persistDetailDraftChanges(categoryId, nextType) {
        if (!categoryId) return;

        if (state.detailInitialType === 'expense' && nextType === 'income' && state.pendingBudgetRemoval) {
            await deleteBudgetsForCategory(categoryId);
            return;
        }

        if (nextType === 'income') return;

        for (const item of (state.draftBudgets || [])) {
            await sendJson(saveBudgetUrl, 'POST', {
                category_id: categoryId,
                budget_id: item?.budget_id && !String(item.budget_id).startsWith('draft-') ? item.budget_id : null,
                period_type: item.period_type,
                period_year: Number(item.period_year),
                period_month: item.period_month != null ? Number(item.period_month) : null,
                period_week: item.period_week != null ? Number(item.period_week) : null,
                limit_amount: Number(item.limit_amount || 0)
            });
        }
    }

    async function sendJson(url, method, payload) {
        const response = await fetch(url, {
            method,
            headers: {
                "Content-Type": "application/json",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: payload ? JSON.stringify(payload) : null
        });

        const raw = await response.text();
        let data = null;
        if (raw) {
            try {
                data = JSON.parse(raw);
            } catch {
                data = { message: raw };
            }
        }

        if (!response.ok) {
            throw new Error(data?.message || raw || "Request failed.");
        }

        return data || {};
    }

    async function fetchJson(url) {
        const response = await fetch(url, {
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        });

        const raw = await response.text();
        let data = null;
        if (raw) {
            try {
                data = JSON.parse(raw);
            } catch {
                data = { message: raw };
            }
        }

        if (!response.ok) {
            throw new Error(data?.message || raw || "Request failed.");
        }

        return data || {};
    }

    function formatDate(raw) {
        if (!raw) return "--";
        const d = new Date(raw);
        if (Number.isNaN(d.getTime())) return raw;
        return d.toLocaleDateString("vi-VN");
    }

    function formatDateFromObj(dateObj) {
        return new Date(dateObj).toLocaleDateString("vi-VN", { timeZone: "UTC" });
    }

    function getWeekNumber(dateObj) {
        const date = new Date(Date.UTC(dateObj.getFullYear(), dateObj.getMonth(), dateObj.getDate()));
        const dayNum = date.getUTCDay() || 7;
        date.setUTCDate(date.getUTCDate() + 4 - dayNum);
        const yearStart = new Date(Date.UTC(date.getUTCFullYear(), 0, 1));
        return Math.ceil((((date - yearStart) / 86400000) + 1) / 7);
    }

    function getIsoWeekStartDate(year, week) {
        const jan4 = new Date(Date.UTC(year, 0, 4));
        const jan4Day = jan4.getUTCDay() || 7;
        const firstMonday = new Date(jan4);
        firstMonday.setUTCDate(jan4.getUTCDate() - (jan4Day - 1));
        const start = new Date(firstMonday);
        start.setUTCDate(firstMonday.getUTCDate() + ((week - 1) * 7));
        return start;
    }

    function periodLabel(periodType) {
        switch ((periodType || "").toLowerCase()) {
            case "week": return "Theo tuần";
            case "month": return "Theo tháng";
            case "year": return "Theo năm";
            default: return "Theo tháng";
        }
    }

    function periodShortLabel(item) {
        if (!item) return "";
        if (item.period_type === "week") return `Tuần ${item.period_week}/${item.period_year}`;
        if (item.period_type === "month") return `Tháng ${item.period_month}/${item.period_year}`;
        return `Năm ${item.period_year}`;
    }

    function getBudgetTimeState(item) {
        const now = new Date();
        const start = item?.start_date ? new Date(item.start_date) : null;
        const end = item?.end_date ? new Date(item.end_date) : null;
        if (!start || !end || Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) return "upcoming";
        if (now < start) return "upcoming";
        if (now > end) return "ended";
        return "active";
    }

    function sortBudgets(items) {
        const stateRank = { active: 0, upcoming: 1, ended: 2 };
        return [...(items || [])].sort(function (a, b) {
            const timeStateDiff = (stateRank[getBudgetTimeState(a)] ?? 9) - (stateRank[getBudgetTimeState(b)] ?? 9);
            if (timeStateDiff !== 0) return timeStateDiff;

            const aStart = a?.start_date ? new Date(a.start_date).getTime() : 0;
            const bStart = b?.start_date ? new Date(b.start_date).getTime() : 0;

            if (getBudgetTimeState(a) === 'ended' && getBudgetTimeState(b) === 'ended') {
                return bStart - aStart;
            }
            return aStart - bStart;
        });
    }

    function getPreferredBudget(items) {
        const sorted = sortBudgets(items);
        return sorted.length ? sorted[0] : null;
    }

    function getDateRangeFromValues(periodType, year, month, week) {
        if (!year || year < 2000) return { start: "--/--/----", end: "--/--/----" };

        if (periodType === "week") {
            const start = getIsoWeekStartDate(year, week || 1);
            const end = new Date(start);
            end.setUTCDate(start.getUTCDate() + 6);
            return { start: formatDateFromObj(start), end: formatDateFromObj(end) };
        }

        if (periodType === "month") {
            const start = new Date(Date.UTC(year, (month || 1) - 1, 1));
            const end = new Date(Date.UTC(year, month || 1, 0));
            return { start: formatDateFromObj(start), end: formatDateFromObj(end) };
        }

        const start = new Date(Date.UTC(year, 0, 1));
        const end = new Date(Date.UTC(year, 11, 31));
        return { start: formatDateFromObj(start), end: formatDateFromObj(end) };
    }

    function getDateRangeFromForm() {
        const periodType = document.getElementById("budgetModalTimeType")?.value || "month";
        const year = parseInt(document.getElementById("budgetModalYear")?.value || "0", 10);
        const month = parseInt(document.getElementById("budgetModalMonth")?.value || "0", 10);
        let week = parseInt(document.getElementById("budgetModalWeek")?.value || "0", 10);
        const weekDateValue = document.getElementById("budgetModalWeekDate")?.value || "";
        if (periodType === 'week' && weekDateValue) {
            const weekDate = new Date(weekDateValue);
            if (!Number.isNaN(weekDate.getTime())) {
                week = getWeekNumber(weekDate);
            }
        }
        return getDateRangeFromValues(periodType, year, month, week);
    }

    function updateAddPreview() {
        const color = document.getElementById("addCategoryColor")?.value || "#FFAB00";
        setActiveColor(".category-color-preset", color);
    }

    function updateDetailPreview() {
        const name = document.getElementById("detailCategoryName")?.value || "Danh mục";
        const color = document.getElementById("detailCategoryColor")?.value || "#FFAB00";
        const iconInputValue = document.getElementById("detailCategoryIcon")?.value || "";
        const icon = iconInputValue || state.defaultIcon;

        const headIcon = document.getElementById("detailHeadIcon");
        const headWrap = document.getElementById("detailHeadIconWrap");
        const title = document.getElementById("detailCategoryTitle");
        const budgetText = document.getElementById("budgetCurrentCategoryText");
        const budgetDot = document.getElementById("budgetCurrentColorDot");

        if (title) title.textContent = name;
        if (budgetText) budgetText.textContent = name;
        if (headIcon && icon) headIcon.src = icon;
        if (headWrap) headWrap.style.background = hexToRgba(color, 0.14);
        if (budgetDot) budgetDot.style.background = color;

        const currentType = normalizeCategoryType(document.getElementById("detailCategoryType")?.value || state.currentCategory?.type || "expense");
        const typeBadge = document.getElementById("detailCategoryTypeBadge");
        if (typeBadge) {
            typeBadge.textContent = categoryTypeLabel(currentType);
            typeBadge.className = currentType === "income" ? "badge category-type-badge category-type-badge--income" : "badge category-type-badge category-type-badge--expense";
        }

        setActiveColor(".category-color-preset-detail", color);
        document.querySelectorAll(".category-icon-option-detail").forEach(function (btn) {
            btn.classList.toggle("active", btn.dataset.icon === icon);
        });
    }

    function syncBudgetPeriodPreview() {
        const periodType = document.getElementById("budgetModalTimeType")?.value || "month";
        const yearEl = document.getElementById("budgetModalYear");
        const monthWrap = document.getElementById("budgetMonthWrap");
        const weekWrap = document.getElementById("budgetWeekWrap");
        const weekDateEl = document.getElementById("budgetModalWeekDate");
        const weekEl = document.getElementById("budgetModalWeek");
        const periodBadge = document.getElementById("budgetModalPeriodLabel");
        const periodHint = document.getElementById("budgetModalPeriodHint");

        if (monthWrap) monthWrap.classList.toggle("d-none", periodType !== "month");
        if (weekWrap) weekWrap.classList.toggle("d-none", periodType !== "week");

        if (periodType === 'week' && weekDateEl?.value) {
            const chosen = new Date(weekDateEl.value);
            if (!Number.isNaN(chosen.getTime())) {
                if (yearEl) yearEl.value = chosen.getFullYear();
                if (weekEl) weekEl.value = getWeekNumber(chosen);
            }
        }

        const range = getDateRangeFromForm();
        if (periodBadge) periodBadge.textContent = periodLabel(periodType);
        if (periodHint) {
            periodHint.textContent = periodType === "week"
                ? "Chọn một ngày bất kỳ trong tuần cần áp dụng."
                : periodType === "month"
                    ? "Tự động lấy ngày đầu và cuối của tháng đã chọn."
                    : "Tự động lấy toàn bộ năm đã chọn.";
        }

        const startEl = document.getElementById("budgetModalStartDate");
        const endEl = document.getElementById("budgetModalEndDate");
        if (startEl) startEl.textContent = range.start;
        if (endEl) endEl.textContent = range.end;
    }

    function setBudgetFormToDate(periodType, dateObj) {
        const date = dateObj instanceof Date ? dateObj : new Date();
        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        const week = getWeekNumber(date);

        const typeEl = document.getElementById("budgetModalTimeType");
        const yearEl = document.getElementById("budgetModalYear");
        const monthEl = document.getElementById("budgetModalMonth");
        const weekEl = document.getElementById("budgetModalWeek");
        const weekDateEl = document.getElementById("budgetModalWeekDate");

        if (typeEl) typeEl.value = periodType;
        if (yearEl) yearEl.value = year;
        if (monthEl) monthEl.value = month;
        if (weekEl) weekEl.value = week;
        if (weekDateEl) weekDateEl.value = date.toISOString().slice(0, 10);

        syncBudgetPeriodPreview();
    }

    function fillBudgetForm(budget) {
        const spentEl = document.getElementById("budgetModalSpent");
        const modeBadge = document.getElementById("budgetModalModeBadge");
        const weekDateEl = document.getElementById("budgetModalWeekDate");

        document.getElementById("budgetModalBudgetId").value = budget?.budget_id || "";
        document.getElementById("budgetModalAmount").value = budget?.limit_amount ?? "";
        if (spentEl) spentEl.value = currency(budget?.spent_amount ?? 0);
        document.getElementById("budgetModalYear").value = budget?.period_year || new Date().getFullYear();
        document.getElementById("budgetModalMonth").value = budget?.period_month || (new Date().getMonth() + 1);
        document.getElementById("budgetModalWeek").value = budget?.period_week || getWeekNumber(new Date());
        document.getElementById("budgetModalTimeType").value = budget?.period_type || "month";
        if (weekDateEl) {
            if (budget?.start_date) {
                const d = new Date(budget.start_date);
                if (!Number.isNaN(d.getTime())) weekDateEl.value = d.toISOString().slice(0, 10);
            } else {
                weekDateEl.value = new Date().toISOString().slice(0, 10);
            }
        }

        if (modeBadge) {
            modeBadge.textContent = budget?.budget_id ? "Đang chỉnh sửa" : "Tạo mới";
            modeBadge.className = budget?.budget_id ? "badge bg-label-warning" : "badge bg-label-primary";
        }

        state.activeBudgetId = budget?.budget_id || null;
        syncBudgetPeriodPreview();
        highlightBudgetItem();
    }

    function resetBudgetForm() {
        const spentEl = document.getElementById("budgetModalSpent");
        const amountEl = document.getElementById("budgetModalAmount");
        const modeBadge = document.getElementById("budgetModalModeBadge");

        document.getElementById("budgetModalBudgetId").value = "";
        if (amountEl) amountEl.value = "";
        if (spentEl) spentEl.value = currency(0);
        state.activeBudgetId = null;
        setBudgetFormToDate("month", new Date());

        if (modeBadge) {
            modeBadge.textContent = "Tạo mới";
            modeBadge.className = "badge bg-label-primary";
        }

        highlightBudgetItem();
    }

    function openBudgetChildDialog() {
        const overlay = document.getElementById("budgetChildOverlay");
        if (!overlay) return;

        overlay.classList.remove("d-none");
        overlay.setAttribute("aria-hidden", "false");
        document.body.classList.add("category-subdialog-open");

        window.setTimeout(function () {
            document.getElementById("budgetModalAmount")?.focus();
        }, 30);
    }

    function closeBudgetChildDialog() {
        const overlay = document.getElementById("budgetChildOverlay");
        if (!overlay) return;

        overlay.classList.add("d-none");
        overlay.setAttribute("aria-hidden", "true");
        const anyOpen = ['budgetChildOverlay','deleteChildOverlay','categoryTypeWarningOverlay'].some(function (id) { const el = document.getElementById(id); return el && !el.classList.contains('d-none'); });
        if (!anyOpen) document.body.classList.remove("category-subdialog-open");
    }

    function revealBudgetEditor(resetToNew) {
        if (resetToNew) {
            resetBudgetForm();
        }
        openBudgetChildDialog();
    }

    function highlightBudgetItem() {
        document.querySelectorAll(".category-budget-tile").forEach(function (item) {
            item.classList.toggle("active", item.dataset.budgetId === state.activeBudgetId);
        });
    }

    function renderBudgetList(items) {
        const grid = document.getElementById("detailBudgetList");
        const countBadge = document.getElementById("budgetModalListCount");
        const sorted = sortBudgets(items);

        if (countBadge) {
            countBadge.textContent = `${sorted.length} mục`;
        }

        if (!grid) return;

        if (!sorted.length) {
            grid.innerHTML = "";
            highlightBudgetItem();
            return;
        }

        const preferredId = getPreferredBudget(sorted)?.budget_id || null;

        grid.innerHTML = sorted.map(function (item) {
            const spent = Number(item.spent_amount || 0);
            const total = Number(item.limit_amount || 0);
            const percentage = Number(item.percentage_used || 0);
            const progress = Math.min(Math.max(percentage, 0), 100);
            const timeState = getBudgetTimeState(item);

            let timeClass = 'is-upcoming';
            let timeBadge = 'Sắp diễn ra';
            let statusText = 'Chưa bắt đầu';
            if (timeState === 'active') {
                timeClass = 'is-active';
                timeBadge = 'Đang thực hiện';
                statusText = item.status === 'over' ? 'Đang vượt mức' : item.status === 'reached' ? 'Đã chạm hạn mức' : 'Đang trong mức';
            } else if (timeState === 'ended') {
                timeClass = 'is-ended';
                timeBadge = 'Đã kết thúc';
                statusText = item.status === 'over' ? 'Kết thúc - vượt mức' : item.status === 'reached' ? 'Kết thúc - chạm mức' : 'Đã kết thúc';
            }

            const progressClass = item.status === 'over' ? 'bg-danger' : item.status === 'reached' ? 'bg-warning' : 'bg-primary';
            const preferredClass = item.budget_id === preferredId ? ' is-preferred' : '';

            return `
                <button type="button" class="category-budget-tile ${timeClass}${preferredClass}" data-budget-id="${item.budget_id}">
                    <div class="category-budget-tile__header">
                        <div>
                            <div class="category-budget-tile__title">${periodShortLabel(item)}</div>
                            <div class="category-budget-tile__date">${formatDate(item.start_date)} - ${formatDate(item.end_date)}</div>
                        </div>
                        <span class="badge bg-label-secondary">${timeBadge}</span>
                    </div>

                    <div class="category-budget-tile__amount">${currency(spent)} / ${currency(total)}</div>

                    <div class="progress category-budget-tile__progress">
                        <div class="progress-bar ${progressClass}" style="width:${progress}%"></div>
                    </div>

                    <div class="category-budget-tile__footer">
                        <span>${statusText}</span>
                        <strong>${percentage.toFixed(1)}%</strong>
                    </div>
                </button>
            `;
        }).join("");

        grid.querySelectorAll(".category-budget-tile").forEach(function (button) {
            button.addEventListener("click", function () {
                const budgetId = button.dataset.budgetId || "";
                const budget = getWorkingBudgets().find(function (item) {
                    return item.budget_id === budgetId;
                });

                if (budget) {
                    fillBudgetForm(budget);
                    openBudgetChildDialog();
                }
            });
        });

        highlightBudgetItem();
    }

    function renderDetailBudgetSummary() {
        syncDetailBudgetAvailability();
    }

    async function loadBudgetsForCurrentCategory() {
        if (!state.currentCategory?.id) return;
        const categoryId = state.currentCategory.id;
        const loadToken = ++state.budgetLoadToken;

        const cached = state.budgetCache.get(categoryId);
        if (Array.isArray(cached)) {
            state.originalBudgets = cached.map(cloneBudgetItem);
            state.draftBudgets = cached.map(cloneBudgetItem);
            state.budgets = state.draftBudgets;
            renderBudgetList(getWorkingBudgets());
            renderDetailBudgetSummary();
            syncDetailBudgetAvailability();
        }

        const response = await fetchJson(buildCategoryBudgetsUrl(categoryId));
        if (loadToken !== state.budgetLoadToken || state.currentCategory?.id !== categoryId) return;

        const fetched = Array.isArray(response.items) ? response.items.map(cloneBudgetItem) : [];
        state.budgetCache.set(categoryId, fetched.map(cloneBudgetItem));
        state.originalBudgets = fetched.map(cloneBudgetItem);
        state.draftBudgets = fetched.map(cloneBudgetItem);
        state.budgets = state.draftBudgets;
        renderBudgetList(getWorkingBudgets());
        renderDetailBudgetSummary();
        syncDetailBudgetAvailability();
    }

    function setDetailStateFromTrigger(trigger) {
        const categoryId = trigger.getAttribute("data-category-id") || "";
        const categoryName = trigger.getAttribute("data-category-name") || "Danh mục";
        const categoryColor = trigger.getAttribute("data-category-color") || "#FFAB00";
        const categoryIcon = trigger.getAttribute("data-category-icon") || state.defaultIcon;
        const categoryType = normalizeCategoryType(trigger.getAttribute("data-category-type") || "expense");
        const canEdit = trigger.getAttribute("data-category-can-edit") === "true";
        const canDelete = trigger.getAttribute("data-category-can-delete") === "true";
        const isDefault = trigger.getAttribute("data-category-is-default") === "true";

        state.currentCategory = { id: categoryId, name: categoryName, color: categoryColor, icon: categoryIcon, type: categoryType, isDefault };
        state.detailInitialType = categoryType;
        state.pendingBudgetRemoval = false;
        state.originalBudgets = [];
        state.draftBudgets = [];

        document.getElementById("detailCategoryId").textContent = categoryId;
        document.getElementById("detailCategoryName").value = categoryName;
        document.getElementById("detailCategoryColor").value = categoryColor;
        document.getElementById("detailCategoryIcon").value = categoryIcon || state.defaultIcon;
        document.getElementById("detailCategoryCanEdit").value = canEdit ? "true" : "false";
        document.getElementById("detailCategoryCanDelete").value = canDelete ? "true" : "false";
        setCategoryTypeToggle('detail', categoryType, canEdit);

        const typeBadge = document.getElementById("detailCategoryTypeBadge");
        if (typeBadge) {
            typeBadge.textContent = categoryTypeLabel(categoryType);
            typeBadge.className = categoryType === 'income' ? "badge category-type-badge category-type-badge--income" : "badge category-type-badge category-type-badge--expense";
        }

        const sourceText = document.getElementById("detailCategorySourceText");
        if (sourceText) {
            const currentLang = (document.documentElement.getAttribute("lang") || "vi").toLowerCase();
            const defaultText = currentLang.startsWith("en")
                ? (sourceText.dataset.defaultEn || sourceText.textContent || "")
                : (sourceText.dataset.defaultVi || sourceText.textContent || "");
            const customText = currentLang.startsWith("en")
                ? (sourceText.dataset.customEn || sourceText.textContent || "")
                : (sourceText.dataset.customVi || sourceText.textContent || "");
            sourceText.textContent = isDefault ? defaultText : customText;
        }

        const deleteButton = document.getElementById("btnDeleteCategoryStatic");
        const updateButton = document.getElementById("btnUpdateCategoryStatic");
        const detailNameInput = document.getElementById("detailCategoryName");
        const detailColorInput = document.getElementById("detailCategoryColor");

        if (deleteButton) deleteButton.disabled = !canDelete;
        if (updateButton) updateButton.disabled = false;
        if (detailNameInput) detailNameInput.readOnly = !canEdit;
        if (detailColorInput) detailColorInput.disabled = !canEdit;

        document.querySelectorAll(".category-color-preset-detail, .category-icon-option-detail").forEach(function (el) {
            if (!canEdit) {
                el.classList.add("disabled");
                el.style.pointerEvents = "none";
                el.style.opacity = "0.65";
            } else {
                el.classList.remove("disabled");
                el.style.pointerEvents = "";
                el.style.opacity = "";
            }
        });

        updateDetailPreview();
        resetBudgetForm();
    }

    document.querySelectorAll("[data-category-type-target]").forEach(function (button) {
        button.addEventListener("click", function () {
            const target = button.dataset.categoryTypeTarget || "add";
            const value = button.dataset.categoryTypeValue || "expense";
            const canEdit = target !== "detail" || document.getElementById("detailCategoryCanEdit")?.value === "true";
            if (!canEdit) return;

            if (target === 'detail') {
                const currentType = normalizeCategoryType(document.getElementById('detailCategoryType')?.value || state.detailInitialType);
                const nextType = normalizeCategoryType(value);
                if (currentType === nextType) return;

                if (nextType === 'income' && state.detailInitialType === 'expense' && Array.isArray(state.originalBudgets) && state.originalBudgets.length > 0) {
                    openTypeWarningDialog();
                    return;
                }

                if (nextType === 'expense') {
                    state.pendingBudgetRemoval = false;
                }

                setCategoryTypeToggle(target, nextType, canEdit);
                return;
            }

            setCategoryTypeToggle(target, value, canEdit);
        });
    });

    document.querySelectorAll(".category-color-preset").forEach(function (btn) {
        btn.addEventListener("click", function () {
            const color = btn.dataset.color || "#FFAB00";
            const input = document.getElementById("addCategoryColor");
            if (input) input.value = color;
            updateAddPreview();
        });
    });

    document.querySelectorAll(".category-color-preset-detail").forEach(function (btn) {
        btn.addEventListener("click", function () {
            const color = btn.dataset.color || "#FFAB00";
            const input = document.getElementById("detailCategoryColor");
            if (input) input.value = color;
            updateDetailPreview();
        });
    });

    document.querySelectorAll(".category-icon-option-add").forEach(function (btn) {
        btn.addEventListener("click", function () {
            document.querySelectorAll(".category-icon-option-add").forEach(function (item) { item.classList.remove("active"); });
            btn.classList.add("active");
            const iconInput = document.getElementById("addCategoryIcon");
            if (iconInput) iconInput.value = btn.dataset.icon || "";
        });
    });

    document.querySelectorAll(".category-icon-option-detail").forEach(function (btn) {
        btn.addEventListener("click", function () {
            if (document.getElementById("detailCategoryCanEdit")?.value !== "true") return;
            document.querySelectorAll(".category-icon-option-detail").forEach(function (item) { item.classList.remove("active"); });
            btn.classList.add("active");
            const iconInput = document.getElementById("detailCategoryIcon");
            if (iconInput) iconInput.value = btn.dataset.icon || "";
            updateDetailPreview();
        });
    });

    ["addCategoryColor"].forEach(function (id) {
        const el = document.getElementById(id);
        if (el) el.addEventListener("input", updateAddPreview);
    });

    ["detailCategoryName", "detailCategoryColor"].forEach(function (id) {
        const el = document.getElementById(id);
        if (el) el.addEventListener("input", updateDetailPreview);
    });

    ["budgetModalTimeType", "budgetModalYear", "budgetModalMonth", "budgetModalWeek", "budgetModalWeekDate"].forEach(function (id) {
        const el = document.getElementById(id);
        if (!el) return;
        el.addEventListener("input", syncBudgetPeriodPreview);
        el.addEventListener("change", syncBudgetPeriodPreview);
    });

    document.querySelectorAll("[data-budget-shortcut]").forEach(function (btn) {
        btn.addEventListener("click", function () {
            const shortcut = btn.dataset.budgetShortcut || "current-month";
            const now = new Date();
            if (shortcut === "current-week") {
                setBudgetFormToDate("week", now);
            } else if (shortcut === "current-month") {
                setBudgetFormToDate("month", now);
            } else if (shortcut === "current-year") {
                setBudgetFormToDate("year", now);
            } else if (shortcut === "next-month") {
                setBudgetFormToDate("month", new Date(now.getFullYear(), now.getMonth() + 1, 1));
            }
            revealBudgetEditor(false);
        });
    });
    document.getElementById("btnOpenBudgetEditor")?.addEventListener("click", function () {
        if (normalizeCategoryType(document.getElementById('detailCategoryType')?.value) === 'income') return;
        revealBudgetEditor(true);
    });

    document.getElementById("btnCreateNewBudgetEntry")?.addEventListener("click", function () {
        resetBudgetForm();
    });

    document.getElementById("btnCloseBudgetChild")?.addEventListener("click", function () {
        closeBudgetChildDialog();
    });

    document.querySelectorAll("[data-budget-overlay-close='true']").forEach(function (el) {
        el.addEventListener("click", function () {
            closeBudgetChildDialog();
        });
    });

    const btnSave = document.getElementById("btnSaveCategoryStatic");
    if (btnSave) {
        btnSave.addEventListener("click", async function () {
            const categoryName = (document.getElementById("addCategoryName")?.value || "").trim();
            const color = document.getElementById("addCategoryColor")?.value || "#FFAB00";
            const icon = document.getElementById("addCategoryIcon")?.value || "";

            if (!categoryName) {
                alert("Vui lòng nhập tên danh mục.");
                return;
            }

            btnSave.disabled = true;
            try {
                const categoryType = normalizeCategoryType(document.getElementById('addCategoryType')?.value || 'expense');
                await sendJson(createUrl, "POST", { category_name: categoryName, category_type: categoryType, color, icon });
                addModal?.hide();
                toastSaved?.show();
                setTimeout(function () { window.location.reload(); }, 700);
            } catch (error) {
                alert(error.message || "Tạo danh mục thất bại.");
            } finally {
                btnSave.disabled = false;
            }
        });
    }

    const btnUpdate = document.getElementById("btnUpdateCategoryStatic");
    if (btnUpdate) {
        btnUpdate.addEventListener("click", async function () {
            const canEdit = document.getElementById("detailCategoryCanEdit")?.value === "true";
            const categoryId = document.getElementById("detailCategoryId")?.textContent || "";
            const categoryName = (document.getElementById("detailCategoryName")?.value || "").trim();
            const color = document.getElementById("detailCategoryColor")?.value || "#FFAB00";
            const icon = document.getElementById("detailCategoryIcon")?.value || state.defaultIcon;
            const originalName = state.currentCategory?.name || categoryName;
            const originalColor = state.currentCategory?.color || color;
            const originalIcon = state.currentCategory?.icon || icon;

            if (!categoryId || !(canEdit ? categoryName : originalName)) {
                alert("Dữ liệu danh mục chưa hợp lệ.");
                return;
            }

            btnUpdate.disabled = true;
            try {
                const categoryType = canEdit
                    ? normalizeCategoryType(document.getElementById('detailCategoryType')?.value || state.currentCategory?.type || 'expense')
                    : normalizeCategoryType(state.currentCategory?.type || document.getElementById('detailCategoryType')?.value || 'expense');

                if (canEdit) {
                    await sendJson(buildUpdateUrl(categoryId), "PUT", {
                        category_name: categoryName,
                        category_type: categoryType,
                        color,
                        icon
                    });
                }

                await persistDetailDraftChanges(categoryId, categoryType);
                toastUpdated?.show();
                setTimeout(function () { window.location.reload(); }, 700);
            } catch (error) {
                alert(error.message || "Cập nhật danh mục thất bại.");
            } finally {
                btnUpdate.disabled = false;
                if (!canEdit) {
                    const nameInput = document.getElementById("detailCategoryName");
                    const colorInput = document.getElementById("detailCategoryColor");
                    const iconInput = document.getElementById("detailCategoryIcon");
                    if (nameInput) nameInput.value = originalName;
                    if (colorInput) colorInput.value = originalColor;
                    if (iconInput) iconInput.value = originalIcon;
                    updateDetailPreview();
                }
            }
        });
    }

    function syncDeleteChoiceCards() {
        const mode = document.querySelector("input[name='deleteCategoryMode']:checked")?.value || "default";
        document.querySelectorAll("[data-delete-choice-card]").forEach(function (card) {
            card.classList.toggle("active", card.getAttribute("data-delete-choice-card") === mode);
        });
    }

    function openDeleteChildDialog() {
        const overlay = document.getElementById("deleteChildOverlay");
        if (!overlay) return;
        overlay.classList.remove("d-none");
        overlay.setAttribute("aria-hidden", "false");
        document.body.classList.add("category-subdialog-open");
        syncDeleteChoiceCards();
    }

    function closeDeleteChildDialog() {
        const overlay = document.getElementById("deleteChildOverlay");
        if (!overlay) return;
        overlay.classList.add("d-none");
        overlay.setAttribute("aria-hidden", "true");
        const anyOpen = ['budgetChildOverlay','deleteChildOverlay','categoryTypeWarningOverlay'].some(function (id) { const el = document.getElementById(id); return el && !el.classList.contains('d-none'); });
        if (!anyOpen) document.body.classList.remove("category-subdialog-open");
    }

    function buildReplacementCategoryOptions() {
        const select = document.getElementById("deleteReplacementCategoryId");
        if (!select) return;

        const currentId = state.currentCategory?.id || "";
        const cards = Array.from(document.querySelectorAll(".category-card[data-category-id]"));
        const options = cards
            .map(function (card) {
                return {
                    id: card.getAttribute("data-category-id") || "",
                    name: card.getAttribute("data-category-name") || "",
                    canDelete: card.getAttribute("data-category-can-delete") === "true"
                };
            })
            .filter(function (item) {
                return item.id && item.id !== currentId;
            })
            .sort(function (a, b) {
                return a.name.localeCompare(b.name, "vi");
            });

        select.innerHTML = `<option value="">-- Chọn danh mục thay thế --</option>` + options.map(function (item) {
            return `<option value="${item.id}">${item.name}</option>`;
        }).join("");
    }

    function prepareDeleteDialog() {
        const categoryName = document.getElementById("detailCategoryName")?.value || state.currentCategory?.name || "Danh mục";
        const txCount = getCurrentTransactionCount();
        const txCountEl = document.getElementById("deleteCategoryTransactionCount");
        const nameEl = document.getElementById("deleteCategoryNameText");
        const select = document.getElementById("deleteReplacementCategoryId");
        const error = document.getElementById("deleteCategoryError");
        const defaultRadio = document.getElementById("deleteModeDefault");

        if (nameEl) nameEl.textContent = categoryName;
        if (txCountEl) txCountEl.textContent = String(txCount);
        if (defaultRadio) defaultRadio.checked = true;
        if (select) {
            buildReplacementCategoryOptions();
            select.disabled = true;
            select.value = "";
        }
        if (error) error.classList.add("d-none");
        syncDeleteChoiceCards();
    }

    document.getElementById("deleteModeDefault")?.addEventListener("change", function () {
        const select = document.getElementById("deleteReplacementCategoryId");
        const error = document.getElementById("deleteCategoryError");
        if (select) {
            select.disabled = true;
            select.value = "";
        }
        if (error) error.classList.add("d-none");
        syncDeleteChoiceCards();
    });

    document.getElementById("deleteModeReplacement")?.addEventListener("change", function () {
        const select = document.getElementById("deleteReplacementCategoryId");
        if (select) select.disabled = false;
        syncDeleteChoiceCards();
    });

    document.getElementById("btnCloseDeleteChild")?.addEventListener("click", closeDeleteChildDialog);
    document.getElementById("btnCancelDeleteChild")?.addEventListener("click", closeDeleteChildDialog);
    document.querySelectorAll("[data-delete-overlay-close='true']").forEach(function (el) {
        el.addEventListener("click", closeDeleteChildDialog);
    });

    const btnDelete = document.getElementById("btnDeleteCategoryStatic");
    if (btnDelete) {
        btnDelete.addEventListener("click", function () {
            if (document.getElementById("detailCategoryCanDelete")?.value !== "true") {
                alert("Danh mục mặc định không thể xóa.");
                return;
            }

            const categoryId = document.getElementById("detailCategoryId")?.textContent || "";
            if (!categoryId) return;

            prepareDeleteDialog();
            openDeleteChildDialog();
        });
    }

    const btnConfirmDelete = document.getElementById("btnConfirmDeleteCategory");
    if (btnConfirmDelete) {
        btnConfirmDelete.addEventListener("click", async function () {
            const categoryId = document.getElementById("detailCategoryId")?.textContent || "";
            if (!categoryId) return;

            const mode = document.querySelector("input[name='deleteCategoryMode']:checked")?.value || "default";
            const replacementId = document.getElementById("deleteReplacementCategoryId")?.value || "";
            const error = document.getElementById("deleteCategoryError");

            if (mode === "replacement" && !replacementId) {
                error?.classList.remove("d-none");
                return;
            }

            btnConfirmDelete.disabled = true;
            try {
                const payload = mode === "replacement" ? { replacement_category_id: replacementId } : {};
                await sendJson(buildDeleteUrl(categoryId), "DELETE", payload);
                closeDeleteChildDialog();
                detailModal?.hide();
                toastDeleted?.show();
                setTimeout(function () { window.location.reload(); }, 700);
            } catch (error) {
                alert(error.message || "Xóa danh mục thất bại.");
            } finally {
                btnConfirmDelete.disabled = false;
            }
        });
    }

    function applyCategoryFilter(filterValue) {
        const normalizedFilter = normalizeCategoryType(filterValue) === filterValue ? filterValue : (filterValue || 'all');
        const cards = Array.from(document.querySelectorAll('.category-card-col'));
        let visibleCount = 0;

        cards.forEach(function (col) {
            const card = col.querySelector('.category-card[data-category-id]');
            if (!card) return;
            const cardType = normalizeCategoryType(card.getAttribute('data-category-type') || 'expense');
            const shouldShow = normalizedFilter === 'all' || cardType === normalizedFilter;
            col.classList.toggle('d-none', !shouldShow);
            if (shouldShow) visibleCount += 1;
        });

        document.querySelectorAll('[data-category-filter]').forEach(function (button) {
            const isActive = (button.getAttribute('data-category-filter') || 'all') === normalizedFilter;
            button.classList.toggle('active', isActive);
            button.setAttribute('aria-selected', isActive ? 'true' : 'false');
        });

        const emptyState = document.getElementById('categoryFilterEmptyState');
        if (emptyState) {
            const shouldHideEmptyState = cards.length === 0 || visibleCount > 0;
            emptyState.classList.toggle('d-none', shouldHideEmptyState);
        }
    }

    document.querySelectorAll('[data-category-filter]').forEach(function (button) {
        button.addEventListener('click', function () {
            applyCategoryFilter(button.getAttribute('data-category-filter') || 'all');
        });
    });

    const btnSaveBudget = document.getElementById("btnSaveBudget");
    if (btnSaveBudget) {
        btnSaveBudget.addEventListener("click", async function () {
            if (normalizeCategoryType(document.getElementById('detailCategoryType')?.value) === 'income') {
                alert('Danh mục thu nhập không thể thiết lập hạn mức.');
                return;
            }
            const categoryId = (document.getElementById("detailCategoryId")?.textContent || "").trim();
            const budgetId = document.getElementById("budgetModalBudgetId")?.value || "";
            const periodType = document.getElementById("budgetModalTimeType")?.value || "month";
            const amount = parseBudgetAmount(document.getElementById("budgetModalAmount")?.value || "");
            const year = parseInt(document.getElementById("budgetModalYear")?.value || "0", 10);
            const month = periodType === "month" ? parseInt(document.getElementById("budgetModalMonth")?.value || "0", 10) : null;
            const week = periodType === "week" ? parseInt(document.getElementById("budgetModalWeek")?.value || "0", 10) : null;

            if (!categoryId || !Number.isFinite(amount) || amount <= 0 || !year) {
                alert("Vui lòng nhập hạn mức hợp lệ.");
                return;
            }
            if (periodType === "month" && (!month || month < 1 || month > 12)) {
                alert("Vui lòng chọn tháng hợp lệ.");
                return;
            }
            if (periodType === "week" && (!week || week < 1 || week > 53)) {
                alert("Vui lòng chọn tuần ISO hợp lệ.");
                return;
            }

            btnSaveBudget.disabled = true;
            try {
                const currentSpent = budgetId
                    ? Number((state.draftBudgets || []).find(function (item) { return item.budget_id === budgetId; })?.spent_amount || 0)
                    : 0;
                const rawRange = (() => {
                    if (periodType === 'week') {
                        const start = getIsoWeekStartDate(year, week || 1);
                        const end = new Date(start);
                        end.setUTCDate(start.getUTCDate() + 6);
                        return { start: start.toISOString().slice(0, 10), end: end.toISOString().slice(0, 10) };
                    }
                    if (periodType === 'month') {
                        const start = new Date(Date.UTC(year, (month || 1) - 1, 1));
                        const end = new Date(Date.UTC(year, month || 1, 0));
                        return { start: start.toISOString().slice(0, 10), end: end.toISOString().slice(0, 10) };
                    }
                    const start = new Date(Date.UTC(year, 0, 1));
                    const end = new Date(Date.UTC(year, 11, 31));
                    return { start: start.toISOString().slice(0, 10), end: end.toISOString().slice(0, 10) };
                })();
                const existingIndex = (state.draftBudgets || []).findIndex(function (item) {
                    if (budgetId) return item.budget_id === budgetId;
                    return item.period_type === periodType
                        && Number(item.period_year || 0) === year
                        && Number(item.period_month || 0) === Number(month || 0)
                        && Number(item.period_week || 0) === Number(week || 0);
                });

                const draftBudget = normalizeBudgetDraft({
                    budget_id: budgetId || (existingIndex >= 0 ? state.draftBudgets[existingIndex].budget_id : `draft-${Date.now()}`),
                    category_id: categoryId,
                    period_type: periodType,
                    period_year: year,
                    period_month: month,
                    period_week: week,
                    start_date: rawRange.start,
                    end_date: rawRange.end,
                    limit_amount: amount,
                    spent_amount: existingIndex >= 0 ? Number(state.draftBudgets[existingIndex].spent_amount || 0) : currentSpent
                });

                if (existingIndex >= 0) {
                    state.draftBudgets.splice(existingIndex, 1, draftBudget);
                } else {
                    state.draftBudgets.push(draftBudget);
                }

                state.budgets = state.draftBudgets;
                renderBudgetList(getWorkingBudgets());
                renderDetailBudgetSummary();
                syncDetailBudgetAvailability();
                fillBudgetForm(draftBudget);
                toastSaved?.show();
                closeBudgetChildDialog();
            } catch (error) {
                alert(error.message || "Lưu hạn mức thất bại.");
            } finally {
                btnSaveBudget.disabled = false;
            }
        });
    }

    if (detailModalEl) {
        detailModalEl.addEventListener("show.bs.modal", async function (event) {
            const trigger = event.relatedTarget;
            if (!trigger) return;
            setDetailStateFromTrigger(trigger);
            await loadBudgetsForCurrentCategory();
        });

        detailModalEl.addEventListener("hidden.bs.modal", function () {
            state.currentCategory = null;
            state.budgets = [];
            state.originalBudgets = [];
            state.draftBudgets = [];
            state.activeBudgetId = null;
            state.detailInitialType = 'expense';
            resetDetailDraftState();
            closeBudgetChildDialog();
            document.body.classList.remove('category-subdialog-open');
        });
    }

    applyCategoryFilter('all');
    setCategoryTypeToggle('add', document.getElementById('addCategoryType')?.value || 'expense', true);
    setCategoryTypeToggle('detail', document.getElementById('detailCategoryType')?.value || 'expense', true);
    updateAddPreview();
    updateDetailPreview();
    resetBudgetForm();
    syncDetailBudgetAvailability();
});
