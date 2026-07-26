(function () {
  function qs(id) {
    return document.getElementById(id);
  }

  function formatFileSize(bytes) {
    const size = Number(bytes || 0);
    if (!size) return '0 B';
    const units = ['B', 'KB', 'MB', 'GB'];
    const exponent = Math.min(Math.floor(Math.log(size) / Math.log(1024)), units.length - 1);
    const value = size / (1024 ** exponent);
    return `${value >= 10 || exponent === 0 ? value.toFixed(0) : value.toFixed(1)} ${units[exponent]}`;
  }

  function escapeHtml(value) {
    return String(value || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function detectKind(fileName, fileType) {
    const name = String(fileName || '').toLowerCase();
    const type = String(fileType || '').toLowerCase();
    if (type.startsWith('image/')) return 'image';
    if (type.startsWith('video/')) return 'video';
    if (type === 'application/pdf' || name.endsWith('.pdf')) return 'pdf';
    if (type === 'application/msword'
      || type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      || name.endsWith('.doc') || name.endsWith('.docx')) return 'document';
    return 'file';
  }

  function buildSelectedFileCard(file, index, objectUrl) {
    const kind = detectKind(file.name, file.type);
    const preview = kind === 'image'
      ? `<img src="${objectUrl}" alt="${escapeHtml(file.name)}" class="support-selected-file-thumb" />`
      : kind === 'video'
        ? `<video class="support-selected-file-thumb" muted preload="metadata"><source src="${objectUrl}" type="${escapeHtml(file.type)}"></video>`
        : kind === 'pdf'
          ? `<div class="support-selected-file-thumb support-selected-file-thumb--icon"><i class="bx bx-file-blank"></i><span>PDF</span></div>`
          : kind === 'document'
            ? `<div class="support-selected-file-thumb support-selected-file-thumb--icon"><i class="bx bx-file-doc"></i><span>DOC</span></div>`
            : `<div class="support-selected-file-thumb support-selected-file-thumb--icon"><i class="bx bx-file"></i></div>`;

    return `
      <div class="support-selected-file-card" data-file-index="${index}">
        <button type="button" class="support-selected-file-preview" data-selected-preview="${index}">
          ${preview}
        </button>
        <button type="button" class="support-selected-file-remove" data-remove-file="${index}" aria-label="Remove file"><i class="bx bx-x"></i></button>
        <div class="support-selected-file-copy">
          <strong>${escapeHtml(file.name)}</strong>
          <span>${formatFileSize(file.size)}</span>
        </div>
      </div>`;
  }

  function initLightbox() {
    const shell = qs('supportLightbox');
    const content = qs('supportLightboxContent');
    const fileName = qs('supportLightboxFileName');
    const download = qs('supportLightboxDownload');
    const prevButton = qs('supportLightboxPrev');
    const nextButton = qs('supportLightboxNext');

    if (!shell || !content || !fileName) {
      return { open() {}, openGallery() {}, close() {} };
    }

    let galleryItems = [];
    let currentIndex = 0;

    function normalizeKind(kind) {
      return kind === 'doc' ? 'document' : (kind || 'file');
    }

    function renderCurrent() {
      const item = galleryItems[currentIndex];
      if (!item || !item.src) return;

      const kind = normalizeKind(item.kind);
      const safeType = item.fileType ? ` type="${escapeHtml(item.fileType)}"` : '';
      const safeName = escapeHtml(item.name || '');
      const safeSrc = item.src;

      fileName.textContent = item.name || '';

      if (download) {
        download.href = safeSrc;
        download.setAttribute('download', item.name || 'attachment');
      }

      if (prevButton) prevButton.disabled = galleryItems.length <= 1 || currentIndex <= 0;
      if (nextButton) nextButton.disabled = galleryItems.length <= 1 || currentIndex >= galleryItems.length - 1;
      shell.classList.toggle('is-single', galleryItems.length <= 1);

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
            <p>Tệp DOC/DOCX hiện chưa được trình duyệt render ổn định như PDF. Bạn có thể tải xuống hoặc mở trực tiếp từ đây.</p>
            <div class="support-lightbox-doc-actions">
              <a href="${safeSrc}" target="_blank" rel="noopener" class="btn btn-sm btn-outline-primary">Mở tệp</a>
              <a href="${safeSrc}" download="${safeName}" class="btn btn-sm btn-primary">Tải xuống</a>
            </div>
          </div>`;
      } else {
        content.innerHTML = `
          <div class="support-lightbox-doc-card">
            <i class="bx bx-file"></i>
            <strong>${safeName}</strong>
            <div class="support-lightbox-doc-actions">
              <a href="${safeSrc}" target="_blank" rel="noopener" class="btn btn-sm btn-outline-primary">Mở tệp</a>
              <a href="${safeSrc}" download="${safeName}" class="btn btn-sm btn-primary">Tải xuống</a>
            </div>
          </div>`;
      }
    }

    function close() {
      shell.classList.remove('is-open');
      shell.setAttribute('aria-hidden', 'true');
      content.innerHTML = '';
      galleryItems = [];
      currentIndex = 0;
      document.body.classList.remove('support-lightbox-open');
    }

    function openGallery(items, index) {
      galleryItems = Array.isArray(items) ? items.filter((item) => item && item.src) : [];
      currentIndex = Math.max(0, Math.min(Number(index || 0), galleryItems.length - 1));
      if (!galleryItems.length) return;
      renderCurrent();
      shell.classList.add('is-open');
      shell.setAttribute('aria-hidden', 'false');
      document.body.classList.add('support-lightbox-open');
    }

    function open(options) {
      openGallery([options], 0);
    }

    prevButton?.addEventListener('click', () => {
      if (currentIndex > 0) {
        currentIndex -= 1;
        renderCurrent();
      }
    });

    nextButton?.addEventListener('click', () => {
      if (currentIndex < galleryItems.length - 1) {
        currentIndex += 1;
        renderCurrent();
      }
    });

    shell.addEventListener('click', (event) => {
      if (event.target.closest('[data-lightbox-close]')) {
        close();
      }
    });

    document.addEventListener('keydown', (event) => {
      if (!shell.classList.contains('is-open')) return;
      if (event.key === 'Escape') {
        close();
      } else if (event.key === 'ArrowLeft' && currentIndex > 0) {
        currentIndex -= 1;
        renderCurrent();
      } else if (event.key === 'ArrowRight' && currentIndex < galleryItems.length - 1) {
        currentIndex += 1;
        renderCurrent();
      }
    });

    return { open, openGallery, close };
  }

  function initFileSelectionPreview(lightbox) {
    const input = qs('supportFilesInput');
    const previewShell = qs('supportSelectedFiles');
    const form = qs('supportRequestForm');
    const config = window.supportHistoryConfig || {};
    const labels = config.labels || {};
    const maxFiles = Number(config.maxFiles || 10);

    if (!input || !previewShell) return;

    let currentFiles = [];
    let objectUrls = [];

    function cleanupObjectUrls() {
      objectUrls.forEach((url) => URL.revokeObjectURL(url));
      objectUrls = [];
    }

    function syncInputFiles() {
      if (typeof DataTransfer === 'undefined') return;
      const dataTransfer = new DataTransfer();
      currentFiles.forEach((file) => dataTransfer.items.add(file));
      input.files = dataTransfer.files;
    }

    function getFileKey(file) {
      return [file.name, file.size, file.lastModified, file.type].join('::');
    }

    function appendFiles(fileList) {
      const additions = Array.from(fileList || []);
      if (!additions.length) return;

      const existingKeys = new Set(currentFiles.map(getFileKey));
      additions.forEach((file) => {
        const key = getFileKey(file);
        if (!existingKeys.has(key) && currentFiles.length < maxFiles) {
          currentFiles.push(file);
          existingKeys.add(key);
        }
      });

      syncInputFiles();
      render();
    }

    function buildGalleryItems() {
      return currentFiles.map((file, index) => ({
        kind: detectKind(file.name, file.type),
        src: objectUrls[index],
        name: file.name,
        fileType: file.type
      }));
    }

    function render() {
      cleanupObjectUrls();
      if (!currentFiles.length) {
        previewShell.classList.add('d-none');
        previewShell.innerHTML = '';
        return;
      }

      const cards = currentFiles.map((file, index) => {
        const objectUrl = URL.createObjectURL(file);
        objectUrls.push(objectUrl);
        return buildSelectedFileCard(file, index, objectUrl);
      }).join('');

      previewShell.classList.remove('d-none');
      previewShell.innerHTML = `
        <div class="support-selected-file-head">
          <span>${currentFiles.length} ${labels.selectedFiles || 'selected files'}</span>
        </div>
        <div class="support-selected-file-grid">${cards}</div>`;
    }

    currentFiles = Array.from(input.files || []);
    render();

    input.addEventListener('click', () => {
      input.value = '';
    });

    input.addEventListener('change', () => {
      appendFiles(input.files || []);
    });

    previewShell.addEventListener('click', (event) => {
      const removeBtn = event.target.closest('[data-remove-file]');
      if (removeBtn) {
        const index = Number(removeBtn.getAttribute('data-remove-file'));
        currentFiles = currentFiles.filter((_, fileIndex) => fileIndex !== index);
        syncInputFiles();
        render();
        return;
      }

      const previewBtn = event.target.closest('[data-selected-preview]');
      if (!previewBtn) return;
      const index = Number(previewBtn.getAttribute('data-selected-preview'));
      lightbox.openGallery(buildGalleryItems(), index);
    });

    form?.addEventListener('reset', () => {
      currentFiles = [];
      syncInputFiles();
      window.setTimeout(render, 0);
    });

    window.addEventListener('beforeunload', cleanupObjectUrls);
  }

  function initInlineViewer(scope, lightbox) {
    if (!scope) return;

    scope.querySelectorAll('[data-support-viewer]').forEach((viewer) => {
      if (viewer.dataset.bound === 'true') return;
      viewer.dataset.bound = 'true';

      viewer.addEventListener('click', (event) => {
        const lightboxTrigger = event.target.closest('[data-lightbox-trigger]');
        if (!lightboxTrigger) return;

        const triggers = Array.from(viewer.querySelectorAll('[data-lightbox-trigger]'));
        const items = triggers.map((trigger) => ({
          kind: trigger.getAttribute('data-lightbox-type'),
          src: trigger.getAttribute('data-lightbox-src'),
          name: trigger.getAttribute('data-lightbox-name'),
          fileType: trigger.getAttribute('data-lightbox-filetype')
        }));
        const index = Math.max(0, triggers.indexOf(lightboxTrigger));
        lightbox.openGallery(items, index);
      });
    });
  }

  function initSupportHistory() {
    const config = window.supportHistoryConfig || {};
    const rowsPerPage = Number(config.rowsPerPage || 5);
    const labels = config.labels || {};

    const listShell = qs('supportHistoryListShell');
    const emptyState = qs('supportEmptyState');
    const paginationBar = qs('supportPaginationBar');
    const paginationControls = qs('paginationControls');
    const paginationInfo = qs('paginationInfo');
    const modalElement = qs('requestUserDetailModal');
    const modalBody = qs('modalUserDetailContent');
    const lightbox = initLightbox();

    initFileSelectionPreview(lightbox);

    if (!listShell) return;

    const rows = Array.from(listShell.querySelectorAll('.support-row-item'));
    const totalRows = rows.length;
    let currentPage = 1;

    function renderPagination(totalPages) {
      if (!paginationControls) return;

      if (totalRows <= rowsPerPage) {
        paginationControls.innerHTML = '';
        return;
      }

      const startPage = Math.max(1, Math.min(currentPage - 1, totalPages - 2));
      const endPage = Math.min(totalPages, startPage + 2);
      const buttons = [];

      buttons.push(`<button type="button" class="tx-page-nav" data-nav="prev" ${currentPage === 1 ? 'disabled' : ''}><i class="bx bx-chevron-left"></i></button>`);
      for (let page = startPage; page <= endPage; page += 1) {
        buttons.push(`<button type="button" class="${page === currentPage ? 'active' : ''}" data-page="${page}">${page}</button>`);
      }
      buttons.push(`<button type="button" class="tx-page-nav" data-nav="next" ${currentPage === totalPages ? 'disabled' : ''}><i class="bx bx-chevron-right"></i></button>`);

      paginationControls.innerHTML = buttons.join('');

      paginationControls.querySelectorAll('[data-page]').forEach((button) => {
        button.addEventListener('click', () => renderPage(Number(button.dataset.page || '1')));
      });

      const prevButton = paginationControls.querySelector('[data-nav="prev"]');
      const nextButton = paginationControls.querySelector('[data-nav="next"]');

      if (prevButton && currentPage > 1) {
        prevButton.addEventListener('click', () => renderPage(currentPage - 1));
      }
      if (nextButton && currentPage < totalPages) {
        nextButton.addEventListener('click', () => renderPage(currentPage + 1));
      }
    }

    function renderPage(page) {
      const totalPages = Math.max(1, Math.ceil(totalRows / rowsPerPage));
      currentPage = Math.min(Math.max(page, 1), totalPages);

      rows.forEach((row, index) => {
        const pageIndex = Math.floor(index / rowsPerPage) + 1;
        row.classList.toggle('d-none', pageIndex !== currentPage);
      });

      const visibleCount = rows.filter((row) => !row.classList.contains('d-none')).length;
      if (emptyState) emptyState.classList.toggle('d-none', visibleCount > 0);
      if (paginationBar) paginationBar.classList.toggle('d-none', totalRows === 0);

      if (paginationInfo) {
        const start = totalRows === 0 ? 0 : ((currentPage - 1) * rowsPerPage) + 1;
        const end = Math.min(currentPage * rowsPerPage, totalRows);
        paginationInfo.innerHTML = `${labels.showing || 'Showing'} <strong>${start} - ${end}</strong> ${labels.ofTotal || 'of total'} <strong>${totalRows}</strong> ${labels.requests || 'requests'}`;
      }

      renderPagination(totalPages);
    }

    listShell.addEventListener('click', (event) => {
      const button = event.target.closest('.support-view-btn');
      if (!button || !modalElement || !modalBody) return;

      const requestId = button.getAttribute('data-id');
      const template = qs(`tpl_detail_${requestId}`);
      if (!template) return;

      modalBody.innerHTML = template.innerHTML;
      initInlineViewer(modalBody, lightbox);
      if (window.bootstrap && window.bootstrap.Modal) {
        window.bootstrap.Modal.getOrCreateInstance(modalElement).show();
      }
    });

    if (modalElement && modalBody) {
      modalElement.addEventListener('hidden.bs.modal', () => {
        modalBody.innerHTML = '';
      });
    }

    renderPage(1);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initSupportHistory);
  } else {
    initSupportHistory();
  }
})();
