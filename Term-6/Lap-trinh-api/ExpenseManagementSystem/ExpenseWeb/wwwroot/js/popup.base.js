
(function () {
  function q(root, selector) {
    if (!root || !selector) return null;
    try { return root.querySelector(selector); } catch (e) { return null; }
  }

  function cloneVisible(el) {
    if (!el) return null;
    var clone = el.cloneNode(true);
    clone.removeAttribute('id');
    return clone;
  }

  function createCloseButton() {
    var btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'popup-base-close';
    btn.setAttribute('data-bs-dismiss', 'modal');
    btn.setAttribute('aria-label', 'Close');
    btn.innerHTML = '<i class="bx bx-x"></i>';
    return btn;
  }

  function markSourceHidden(node) {
    if (node) node.classList.add('popup-base-source-hidden');
  }

  function buildHeader(modal) {
    var content = modal.querySelector('.modal-content');
    if (!content || content.querySelector('.popup-base-head')) return;

    var titleNode = q(content, modal.dataset.popupTitle || '.modal-title, .wallet-modal-title');
    var subtitleNode = q(content, modal.dataset.popupSubtitle || '.wallet-modal-subtitle, .text-body-secondary, .tx-modal-subtitle');
    var iconNode = q(content, modal.dataset.popupIcon || '');
    var extraNode = q(content, modal.dataset.popupExtra || '');
    var sourceHeader = q(content, modal.dataset.popupSourceHeader || '');
    var sourceClose = q(content, modal.dataset.popupSourceClose || '.btn-close, .wallet-modal-close, .category-modal-close-solid, .tx-close-btn');

    var head = document.createElement('div');
    head.className = 'popup-base-head' + ((modal.dataset.popupHeadCompact === 'true') ? ' popup-base-head--compact' : '');

    var main = document.createElement('div');
    main.className = 'popup-base-head__main';

    if (iconNode) {
      var iconWrap = document.createElement('div');
      iconWrap.className = 'popup-base-head__icon';
      iconWrap.appendChild(cloneVisible(iconNode));
      main.appendChild(iconWrap);
    }

    var copy = document.createElement('div');
    copy.className = 'popup-base-head__copy';

    var title = document.createElement('h3');
    title.className = 'popup-base-title';
    title.textContent = titleNode ? titleNode.textContent.trim() : '';
    copy.appendChild(title);

    if (subtitleNode) {
      var subtitle = document.createElement('p');
      subtitle.className = 'popup-base-subtitle';
      subtitle.textContent = subtitleNode.textContent.trim();
      copy.appendChild(subtitle);
    }

    main.appendChild(copy);
    head.appendChild(main);

    var meta = document.createElement('div');
    meta.className = 'popup-base-head__meta';

    if (extraNode && extraNode.textContent.trim()) {
      var extraWrap = document.createElement('div');
      extraWrap.className = 'popup-base-head__extra';
      extraWrap.appendChild(cloneVisible(extraNode));
      meta.appendChild(extraWrap);
    }

    meta.appendChild(createCloseButton());
    head.appendChild(meta);

    var accent = document.createElement('div');
    accent.className = 'popup-base-accent';
    content.insertBefore(accent, content.firstChild);
    content.insertBefore(head, accent.nextSibling);

    markSourceHidden(sourceClose);
    if (sourceHeader) markSourceHidden(sourceHeader);
  }

  function enhanceModal(modal) {
    if (!modal || modal.dataset.popupBaseReady === 'true') return;
    modal.dataset.popupBaseReady = 'true';

    modal.classList.add('popup-base-modal');
    var dialog = modal.querySelector('.modal-dialog');
    var content = modal.querySelector('.modal-content');
    var body = modal.querySelector('.modal-body');
    var footer = modal.querySelector('.modal-footer');

    if (dialog) dialog.classList.add('popup-base-dialog');
    if (content) content.classList.add('popup-base-content');
    if (body) body.classList.add('popup-base-body');
    if (footer) footer.classList.add('popup-base-footer');

    if (modal.dataset.popupUseHeader !== 'false') {
      buildHeader(modal);
    }
  }

  function init() {
    document.querySelectorAll('[data-popup-base="true"]').forEach(enhanceModal);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
