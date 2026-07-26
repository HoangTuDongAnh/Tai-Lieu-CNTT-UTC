(function () {
  function qs(id) { return document.getElementById(id); }

  function escapeHtml(value) {
    return String(value || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function decodeHtmlEntities(value) {
    const text = String(value || '');
    const textarea = document.createElement('textarea');
    textarea.innerHTML = text;
    return textarea.value;
  }

  function initLightbox() {
    const shell = qs('adminSupportLightbox');
    const content = qs('adminSupportLightboxContent');
    const fileName = qs('adminSupportLightboxFileName');
    const download = qs('adminSupportLightboxDownload');
    const prevButton = qs('adminSupportLightboxPrev');
    const nextButton = qs('adminSupportLightboxNext');
    const cfg = window.adminSupportConfig || {};

    if (!shell || !content || !fileName) {
      return { openGallery() {}, close() {}, isOpen() { return false; } };
    }

    let items = [];
    let currentIndex = 0;

    function normalizeKind(kind) {
      return kind === 'doc' ? 'document' : (kind || 'file');
    }

    function renderCurrent() {
      const item = items[currentIndex];
      if (!item) return;
      const kind = normalizeKind(item.kind);
      const safeName = escapeHtml(item.name || '');
      const safeSrc = item.src || '#';
      const safeType = item.fileType ? ` type="${escapeHtml(item.fileType)}"` : '';

      fileName.textContent = item.name || '';
      if (download) {
        download.href = safeSrc;
        download.setAttribute('download', item.name || 'attachment');
      }

      shell.classList.toggle('is-single', items.length <= 1);
      if (prevButton) prevButton.disabled = items.length <= 1 || currentIndex <= 0;
      if (nextButton) nextButton.disabled = items.length <= 1 || currentIndex >= items.length - 1;

      if (kind === 'image') {
        content.innerHTML = `<img src="${safeSrc}" alt="${safeName}" class="support-lightbox-image" />`;
      } else if (kind === 'video') {
        content.innerHTML = `<video class="support-lightbox-video" controls autoplay><source src="${safeSrc}"${safeType}></video>`;
      } else if (kind === 'pdf') {
        content.innerHTML = `<iframe src="${safeSrc}" class="support-lightbox-pdf" title="${safeName}"></iframe>`;
      } else if (kind === 'document') {
        content.innerHTML = `
          <div class="support-lightbox-doc-card">
            <i class="bx bx-file-doc"></i>
            <strong>${safeName}</strong>
            <p>${escapeHtml(cfg.documentNote || '')}</p>
            <div class="support-lightbox-doc-actions">
              <a href="${safeSrc}" target="_blank" rel="noopener" class="btn btn-sm btn-outline-primary">${escapeHtml(cfg.openFileText || 'Open file')}</a>
              <a href="${safeSrc}" download="${safeName}" class="btn btn-sm btn-primary">${escapeHtml(cfg.downloadText || 'Download')}</a>
            </div>
          </div>`;
      } else {
        content.innerHTML = `
          <div class="support-lightbox-doc-card">
            <i class="bx bx-file"></i>
            <strong>${safeName}</strong>
            <div class="support-lightbox-doc-actions">
              <a href="${safeSrc}" target="_blank" rel="noopener" class="btn btn-sm btn-outline-primary">${escapeHtml(cfg.openFileText || 'Open file')}</a>
              <a href="${safeSrc}" download="${safeName}" class="btn btn-sm btn-primary">${escapeHtml(cfg.downloadText || 'Download')}</a>
            </div>
          </div>`;
      }
    }

    function close() {
      shell.classList.remove('is-open');
      shell.setAttribute('aria-hidden', 'true');
      content.innerHTML = '';
      items = [];
      currentIndex = 0;
      document.body.classList.remove('support-lightbox-open');
    }

    function openGallery(galleryItems, index) {
      items = Array.isArray(galleryItems) ? galleryItems.filter((item) => item && item.src) : [];
      currentIndex = Math.max(0, Math.min(Number(index || 0), items.length - 1));
      if (!items.length) return;
      renderCurrent();
      shell.classList.add('is-open');
      shell.setAttribute('aria-hidden', 'false');
      document.body.classList.add('support-lightbox-open');
    }

    prevButton?.addEventListener('click', function () {
      if (currentIndex > 0) { currentIndex -= 1; renderCurrent(); }
    });

    nextButton?.addEventListener('click', function () {
      if (currentIndex < items.length - 1) { currentIndex += 1; renderCurrent(); }
    });

    shell.addEventListener('click', function (event) {
      if (event.target.closest('[data-lightbox-close]')) close();
    });

    document.addEventListener('keydown', function (event) {
      if (!shell.classList.contains('is-open')) return;
      if (event.key === 'Escape') close();
      if (event.key === 'ArrowLeft' && currentIndex > 0) { currentIndex -= 1; renderCurrent(); }
      if (event.key === 'ArrowRight' && currentIndex < items.length - 1) { currentIndex += 1; renderCurrent(); }
    });

    return {
      openGallery,
      close,
      isOpen() { return shell.classList.contains('is-open'); }
    };
  }

  function initScopeLightbox(scope, lightbox) {
    if (!scope || scope.dataset.lightboxBound === 'true') return;
    scope.dataset.lightboxBound = 'true';

    scope.addEventListener('click', function (event) {
      const trigger = event.target.closest('[data-lightbox-trigger]');
      if (!trigger) return;
      event.preventDefault();
      event.stopPropagation();
      const triggers = Array.from(scope.querySelectorAll('[data-lightbox-trigger]'));
      const items = triggers.map((node) => ({
        kind: node.getAttribute('data-lightbox-type'),
        src: node.getAttribute('data-lightbox-src'),
        name: node.getAttribute('data-lightbox-name'),
        fileType: node.getAttribute('data-lightbox-filetype')
      }));
      lightbox.openGallery(items, Math.max(0, triggers.indexOf(trigger)));
    });
  }

  function buildPagination() {
    const shell = qs('adminSupportListShell') || document.querySelector('.admin-support-list-shell');
    const info = qs('adminSupportPaginationInfo');
    const controls = qs('adminSupportPagination');
    const bar = qs('adminSupportPaginationBar');
    const config = window.adminSupportConfig || {};

    if (!shell || !info || !controls || !bar) return;

    const rows = Array.from(shell.querySelectorAll('.admin-support-row-item'));
    const pageSize = Math.max(1, Number(config.listPageSize || 6));
    const showingText = decodeHtmlEntities(config.showingText || 'Showing');
    const ofTotalText = decodeHtmlEntities(config.ofTotalText || 'of total');
    const requestsText = decodeHtmlEntities(config.requestsText || 'requests');
    const previousPageText = decodeHtmlEntities(config.previousPageText || 'Previous page');
    const nextPageText = decodeHtmlEntities(config.nextPageText || 'Next page');
    let currentPage = 1;

    function renderControls(totalItems, totalPages, startIndex) {
      if (!totalItems) {
        info.textContent = '';
        controls.innerHTML = '';
        bar.classList.add('d-none');
        return;
      }

      const from = startIndex + 1;
      const to = Math.min(startIndex + pageSize, totalItems);
      info.innerHTML = `${escapeHtml(showingText)} <strong>${from} - ${to}</strong> ${escapeHtml(ofTotalText)} <strong>${totalItems}</strong> ${escapeHtml(requestsText)}`;
      bar.classList.remove('d-none');

      if (totalPages <= 1) {
        controls.innerHTML = '';
        return;
      }

      const pages = [];
      const windowStart = Math.max(1, Math.min(currentPage - 1, totalPages - 2));
      const windowEnd = Math.min(totalPages, windowStart + 2);
      for (let page = windowStart; page <= windowEnd; page += 1) pages.push(page);

      controls.innerHTML = [
        `<button type="button" class="admin-page-nav" data-page="${currentPage - 1}" ${currentPage === 1 ? 'disabled' : ''} aria-label="${escapeHtml(previousPageText)}"><i class="bx bx-chevron-left"></i></button>`,
        ...pages.map((page) => `<button type="button" data-page="${page}" class="${page === currentPage ? 'active' : ''}">${page}</button>`),
        `<button type="button" class="admin-page-nav" data-page="${currentPage + 1}" ${currentPage === totalPages ? 'disabled' : ''} aria-label="${escapeHtml(nextPageText)}"><i class="bx bx-chevron-right"></i></button>`
      ].join('');
    }

    function renderPage(page) {
      const totalItems = rows.length;
      const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));
      currentPage = Math.min(Math.max(Number(page || 1), 1), totalPages);
      const startIndex = totalItems ? (currentPage - 1) * pageSize : 0;
      const endIndex = startIndex + pageSize;

      rows.forEach(function (row, index) {
        row.classList.toggle('d-none', index < startIndex || index >= endIndex);
      });

      renderControls(totalItems, totalPages, startIndex);
    }

    controls.onclick = function (event) {
      const button = event.target.closest('[data-page]');
      if (!button || button.disabled) return;
      renderPage(Number(button.getAttribute('data-page')) || 1);
    };

    renderPage(1);
  }

  function replaceSectionFromDoc(doc, selector) {
    const current = document.querySelector(selector);
    const incoming = doc.querySelector(selector);
    if (current && incoming) current.replaceWith(incoming);
  }

  function syncFormValuesFromDoc(doc) {
    const incomingForm = doc.querySelector('#adminSupportFilterForm');
    const form = qs('adminSupportFilterForm');
    if (!incomingForm || !form) return;

    Array.from(incomingForm.elements).forEach(function (sourceInput) {
      if (!sourceInput.name) return;
      const targetInput = form.elements[sourceInput.name];
      if (!targetInput) return;
      if (targetInput instanceof RadioNodeList) {
        Array.from(targetInput).forEach(function (node) {
          node.checked = node.value === sourceInput.value;
        });
      } else {
        targetInput.value = sourceInput.value;
      }
    });
  }

  function initAjaxFilters() {
    const form = qs('adminSupportFilterForm');
    const config = window.adminSupportConfig || {};
    if (!form || form.dataset.ajaxBound === 'true') return;
    form.dataset.ajaxBound = 'true';

    let abortController = null;

    function loadResults(url, pushState) {
      const listCard = qs('adminSupportListCard');
      const filterErrorText = decodeHtmlEntities(config.filterErrorText || 'Unable to load the list. Please try again.');

      if (abortController) abortController.abort();
      abortController = new AbortController();

      if (listCard) {
        listCard.classList.add('position-relative');
        const cardBody = listCard.querySelector('.card-body');
        if (cardBody) {
          cardBody.style.opacity = '0.6';
          cardBody.style.pointerEvents = 'none';
        }
      }

      fetch(url, {
        headers: { 'X-Requested-With': 'XMLHttpRequest' },
        signal: abortController.signal
      })
        .then(function (response) { return response.text(); })
        .then(function (html) {
          const parser = new DOMParser();
          const doc = parser.parseFromString(html, 'text/html');

          replaceSectionFromDoc(doc, '#adminSupportOverview');
          replaceSectionFromDoc(doc, '#adminSupportFilterCard');
          replaceSectionFromDoc(doc, '#adminSupportListCard');
          syncFormValuesFromDoc(doc);
          buildPagination();
          if (pushState) window.history.pushState({ url: url }, '', url);
          initAjaxFilters();
        })
        .catch(function (error) {
          if (error && error.name === 'AbortError') return;
          const cardBody = document.querySelector('#adminSupportListCard .card-body');
          if (cardBody) {
            cardBody.insertAdjacentHTML('afterbegin', `<div class="alert alert-warning mb-3">${escapeHtml(filterErrorText)}</div>`);
          }
        })
        .finally(function () {
          const currentBody = document.querySelector('#adminSupportListCard .card-body');
          if (currentBody) {
            currentBody.style.opacity = '';
            currentBody.style.pointerEvents = '';
          }
        });
    }

    function submitAjax() {
      const url = new URL(form.getAttribute('action') || window.location.pathname, window.location.origin);
      const formData = new FormData(form);
      formData.forEach(function (value, key) {
        const normalized = String(value || '').trim();
        if (normalized) url.searchParams.set(key, normalized);
      });
      const lang = new URL(window.location.href).searchParams.get('lang');
      if (lang && !url.searchParams.has('lang')) url.searchParams.set('lang', lang);
      loadResults(url.toString(), true);
    }

    form.addEventListener('submit', function (event) {
      event.preventDefault();
      submitAjax();
    });

    form.querySelectorAll('select').forEach(function (select) {
      select.addEventListener('change', function () { submitAjax(); });
    });

    const keywordInput = form.querySelector('input[name="keyword"]');
    let debounceTimer = null;
    keywordInput?.addEventListener('input', function () {
      window.clearTimeout(debounceTimer);
      debounceTimer = window.setTimeout(submitAjax, 320);
    });

    window.onpopstate = function (event) {
      const targetUrl = event.state && event.state.url ? event.state.url : window.location.href;
      loadResults(targetUrl, false);
    };
  }

  document.addEventListener('DOMContentLoaded', function () {
    const config = window.adminSupportConfig || {};
    const modalElement = qs('requestDetailModal');
    const modalBody = qs('modalDetailContent');
    const lightbox = initLightbox();
    const detailModal = modalElement ? bootstrap.Modal.getOrCreateInstance(modalElement, { backdrop: false, focus: true, keyboard: true }) : null;

    buildPagination();
    initAjaxFilters();

    function removeBootstrapBackdrop() {
      document.querySelectorAll('.modal-backdrop').forEach(function (node) { node.remove(); });
    }

    modalElement?.addEventListener('show.bs.modal', function () {
      if (lightbox.isOpen()) lightbox.close();
      document.body.classList.remove('support-lightbox-open');
      removeBootstrapBackdrop();
    });

    modalElement?.addEventListener('shown.bs.modal', function () {
      removeBootstrapBackdrop();
    });

    modalElement?.addEventListener('hidden.bs.modal', function () {
      if (modalBody) modalBody.innerHTML = '';
      if (lightbox.isOpen()) lightbox.close();
      document.body.classList.remove('support-lightbox-open');
      removeBootstrapBackdrop();
    });

    document.addEventListener('click', function (event) {
      const trigger = event.target.closest('.admin-support-open, .admin-support-row-item');
      if (!trigger) return;
      if (event.target.closest('[data-lightbox-trigger]')) return;
      const requestId = trigger.getAttribute('data-id') || trigger.closest('[data-id]')?.getAttribute('data-id');
      if (!requestId || !modalBody || !detailModal) return;

      modalBody.innerHTML = `
        <div class="d-flex flex-column align-items-center justify-content-center py-5">
          <div class="spinner-border text-primary mb-2" role="status"></div>
          <span class="text-muted">${escapeHtml(config.loadingText || 'Loading details...')}</span>
        </div>`;
      detailModal.show();

      fetch(`${config.detailUrl}?id=${encodeURIComponent(requestId)}`, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
        .then(function (response) { return response.text(); })
        .then(function (html) {
          modalBody.innerHTML = String(html || '').trim();
          initScopeLightbox(modalBody.querySelector('[data-admin-lightbox-scope]'), lightbox);
        })
        .catch(function () {
          modalBody.innerHTML = `<div class="alert alert-danger mt-3">${escapeHtml(config.loadErrorText || 'Failed to load data. Please try again.')}</div>`;
        });
    });
  });
})();
