document.addEventListener("DOMContentLoaded", function () {
    const addWalletModalEl = document.getElementById("addWalletModal");
    const walletDetailModalEl = document.getElementById("walletDetailModal");
    const addWalletModal = addWalletModalEl ? new bootstrap.Modal(addWalletModalEl) : null;
    const walletDetailModal = walletDetailModalEl ? new bootstrap.Modal(walletDetailModalEl) : null;


    const walletActionAlert = document.getElementById("walletActionAlert");
    const detailWalletActionAlert = document.getElementById("detailWalletActionAlert");
    const replacementWalletGroup = document.getElementById("replacementWalletGroup");
    const replacementWalletId = document.getElementById("replacementWalletId");

    const lang = window.AppToast?.getLang?.() || "vi";
    const dict = lang === "en"
        ? {
            unknownError: "Something went wrong.",
            requestFailed: "Request failed.",
            emptyName: "Wallet name is required.",
            invalidBalance: "Initial balance cannot be negative.",
            unknownWalletUpdate: "Unable to identify the wallet to update.",
            unknownWalletDelete: "Unable to identify the wallet to delete.",
            chooseReplacement: "Please choose a replacement wallet.",
            createSuccess: "Wallet created successfully.",
            updateSuccess: "Wallet updated successfully.",
            deleteSuccess: "Wallet deleted successfully.",
            standardWallet: "Standard wallet",
            defaultWallet: "Default wallet",
            switchDefaultConfirm: "This will replace the current default wallet. Continue?",
            unsetDefaultBlocked: "A default wallet must always exist. Choose another wallet as default instead."
          }
        : {
            unknownError: "Có lỗi xảy ra.",
            requestFailed: "Yêu cầu không thành công.",
            emptyName: "Tên ví không được để trống.",
            invalidBalance: "Số dư ban đầu không được âm.",
            unknownWalletUpdate: "Không xác định được ví cần cập nhật.",
            unknownWalletDelete: "Không xác định được ví cần xóa.",
            chooseReplacement: "Hãy chọn ví nhận giao dịch.",
            createSuccess: "Tạo ví thành công.",
            updateSuccess: "Cập nhật ví thành công.",
            deleteSuccess: "Xóa ví thành công.",
            standardWallet: "Ví thường",
            defaultWallet: "Ví mặc định",
            switchDefaultConfirm: "Thao tác này sẽ đổi ví mặc định hiện tại sang ví này. Bạn có muốn tiếp tục không?",
            unsetDefaultBlocked: "Hệ thống luôn phải có 1 ví mặc định. Hãy chọn ví khác làm mặc định thay vì bỏ chọn trực tiếp."
          };

    function queueToast(type, message) {
        try {
            sessionStorage.setItem("dashboard.wallet.toast", JSON.stringify({ type, message }));
        } catch (e) {}
    }

    function flushQueuedToast() {
        try {
            const raw = sessionStorage.getItem("dashboard.wallet.toast");
            if (!raw) return;
            sessionStorage.removeItem("dashboard.wallet.toast");
            const toast = JSON.parse(raw);
            if (toast?.type && toast?.message && window.AppToast?.[toast.type]) {
                window.AppToast[toast.type](toast.message);
            }
        } catch (e) {}
    }

    function formatCurrency(value, currencyCode) {
        return `${Number(value || 0).toLocaleString(lang === "en" ? "en-US" : "vi-VN")} ${currencyCode || "VND"}`;
    }

    function showAlert(el, message) {
        if (!el) return;
        const finalMessage = message || dict.unknownError;
        el.textContent = finalMessage;
        el.classList.remove("d-none");
        window.AppToast?.error?.(finalMessage);
    }

    function hideAlert(el) {
        if (!el) return;
        el.textContent = "";
        el.classList.add("d-none");
    }

    function getDeleteMode() {
        const checked = document.querySelector('input[name="deleteWalletMode"]:checked');
        return checked ? checked.value : "delete_all";
    }

    function toggleReplacementWallet() {
        const mode = getDeleteMode();
        replacementWalletGroup?.classList.toggle("d-none", mode !== "move_transactions");
    }

    function fillReplacementWalletOptions(currentWalletId) {
        if (!replacementWalletId) return;
        const options = Array.from(replacementWalletId.querySelectorAll("option"));
        options.forEach(opt => {
            if (!opt.value) return;
            opt.hidden = opt.value === currentWalletId;
        });
        replacementWalletId.value = "";
    }

    function hasAnotherDefaultWallet(currentWalletId) {
        return Array.from(document.querySelectorAll('[data-wallet-is-default="true"]'))
            .some(el => (el.getAttribute('data-wallet-id') || '') !== (currentWalletId || ''));
    }

    function setDetailWalletData(trigger) {
        if (!trigger) return;
        const walletId = trigger.getAttribute("data-wallet-id") || "";
        const walletName = trigger.getAttribute("data-wallet-name") || "";
        const walletBalance = trigger.getAttribute("data-wallet-balance") || "0";
        const walletInitial = trigger.getAttribute("data-wallet-initial") || "0";
        const walletCurrency = trigger.getAttribute("data-wallet-currency") || "VND";
        const walletIsDefault = (trigger.getAttribute("data-wallet-is-default") || "false") === "true";

        const setText = (id, value) => {
            const el = document.getElementById(id);
            if (el) el.textContent = value;
        };
        const setValue = (id, value) => {
            const el = document.getElementById(id);
            if (el) el.value = value;
        };

        setValue("detailWalletIdValue", walletId);
        setText("detailWalletId", walletId || "-");
        setText("detailWalletTitle", walletName || "Ví");
        setValue("detailWalletName", walletName);
        setValue("detailWalletCurrencySelect", walletCurrency);
        setValue("detailCurrentBalanceInput", formatCurrency(walletBalance, walletCurrency));
        setValue("detailInitialBalance", formatCurrency(walletInitial, walletCurrency));

        const defaultCheckbox = document.getElementById("detailWalletDefault");
        if (defaultCheckbox) defaultCheckbox.checked = walletIsDefault;

        const badge = document.getElementById("detailWalletBadge");
        if (badge) badge.textContent = walletIsDefault ? dict.defaultWallet : dict.standardWallet;

        fillReplacementWalletOptions(walletId);
        hideAlert(detailWalletActionAlert);
        document.getElementById("deleteWalletModeDeleteAll")?.click();
        toggleReplacementWallet();
    }

    walletDetailModalEl?.addEventListener("show.bs.modal", function (event) {
        setDetailWalletData(event.relatedTarget);
    });

    document.querySelectorAll('input[name="deleteWalletMode"]').forEach(r => r.addEventListener("change", toggleReplacementWallet));

    async function sendJson(url, method, payload) {
        const response = await fetch(url, {
            method,
            headers: {
                "Content-Type": "application/json",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: payload ? JSON.stringify(payload) : null
        });

        let data = null;
        try { data = await response.json(); } catch {}
        if (!response.ok || !data?.success) {
            throw new Error(data?.message || dict.requestFailed);
        }
        return data;
    }

    document.getElementById("btnSaveWalletApi")?.addEventListener("click", async function () {
        hideAlert(walletActionAlert);
        const wallet_name = (document.getElementById("addWalletName")?.value || "").trim();
        const initial_balance = Number(document.getElementById("addInitialBalance")?.value || 0);
        const currency = document.getElementById("addWalletCurrencyCode")?.value || "VND";
        const is_default = !!document.getElementById("addWalletDefault")?.checked;

        if (!wallet_name) return showAlert(walletActionAlert, dict.emptyName);
        if (initial_balance < 0) return showAlert(walletActionAlert, dict.invalidBalance);
        if (is_default && hasAnotherDefaultWallet('') && !window.confirm(dict.switchDefaultConfirm)) return;

        try {
            const data = await sendJson("/Dashboard/CreateWalletAjax", "POST", { wallet_name, initial_balance, currency, is_default });
            addWalletModal?.hide();
            queueToast("success", data?.message || dict.createSuccess);
            window.location.reload();
        } catch (error) {
            showAlert(walletActionAlert, error.message);
        }
    });

    document.getElementById("btnUpdateWalletApi")?.addEventListener("click", async function () {
        hideAlert(detailWalletActionAlert);
        const walletId = document.getElementById("detailWalletIdValue")?.value || "";
        const wallet_name = (document.getElementById("detailWalletName")?.value || "").trim();
        const currency = document.getElementById("detailWalletCurrencySelect")?.value || "VND";
        const is_default = !!document.getElementById("detailWalletDefault")?.checked;
        const currentIsDefault = Array.from(document.querySelectorAll('[data-wallet-id]'))
            .some(el => (el.getAttribute('data-wallet-id') || '') === walletId && (el.getAttribute('data-wallet-is-default') || 'false') === 'true');

        if (!walletId) return showAlert(detailWalletActionAlert, dict.unknownWalletUpdate);
        if (!wallet_name) return showAlert(detailWalletActionAlert, dict.emptyName);
        if (!is_default && currentIsDefault) return showAlert(detailWalletActionAlert, dict.unsetDefaultBlocked);
        if (is_default && !currentIsDefault && hasAnotherDefaultWallet(walletId) && !window.confirm(dict.switchDefaultConfirm)) return;

        try {
            const data = await sendJson(`/Dashboard/UpdateWalletAjax/${encodeURIComponent(walletId)}`, "PUT", { wallet_name, currency, is_default });
            queueToast("success", data?.message || dict.updateSuccess);
            window.location.reload();
        } catch (error) {
            showAlert(detailWalletActionAlert, error.message);
        }
    });

    document.getElementById("btnDeleteWalletApi")?.addEventListener("click", async function () {
        hideAlert(detailWalletActionAlert);
        const walletId = document.getElementById("detailWalletIdValue")?.value || "";
        const mode = getDeleteMode();
        const replacement_wallet_id = replacementWalletId?.value || null;

        if (!walletId) return showAlert(detailWalletActionAlert, dict.unknownWalletDelete);
        if (mode === "move_transactions" && !replacement_wallet_id) return showAlert(detailWalletActionAlert, dict.chooseReplacement);

        try {
            const data = await sendJson(`/Dashboard/DeleteWalletAjax/${encodeURIComponent(walletId)}`, "DELETE", { mode, replacement_wallet_id });
            walletDetailModal?.hide();
            queueToast("success", data?.message || dict.deleteSuccess);
            window.location.reload();
        } catch (error) {
            showAlert(detailWalletActionAlert, error.message);
        }
    });


    flushQueuedToast();
});
