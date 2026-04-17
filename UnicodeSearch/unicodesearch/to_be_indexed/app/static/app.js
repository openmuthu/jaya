import { convertScript, toDevanagari } from './script-converter.js';

// ── State ─────────────────────────────────────────────────────────────────────
let currentScript = 'devanagari';
let currentFilePath = null;
let treeData = null;
let searchScript = 'devanagari';
let fuzzyMode = false;
let regexMode = false;
let selectedScopes = [];       // [] means all
let overlaySearchTimeout = null;

// ── Outline state ────────────────────────────────────────────────────────────
let outlineData = null;
let activeOutlineLeaf = null;

// ── Progressive loading state ─────────────────────────────────────────────────
const CHUNK_SIZE = 300;      // lines fetched per request
const SCROLL_BUFFER = 100;   // lines to load before the target line
let loadedStart = 0;         // 0-based index of first loaded line
let loadedEnd = 0;           // 0-based index past last loaded line (exclusive)
let totalFileLines = 0;
let loadedDevaLines = {};    // lineIdx (0-based) → raw Devanagari text
let isLoadingChunk = false;
let topSentinel = null;
let bottomSentinel = null;
let chunkObserver = null;

// ── DOM refs ──────────────────────────────────────────────────────────────────
const treeContainer    = document.getElementById('tree-container');
const contentBody      = document.getElementById('content-body');
const contentTitle     = document.getElementById('content-title');
const sidebarSearch    = document.getElementById('sidebar-search');
const globalSearch     = document.getElementById('global-search');
const searchOverlay    = document.getElementById('search-overlay');
const searchResultsList= document.getElementById('search-results-list');
const noResultsMsg     = document.getElementById('no-results-msg');
const resultCount      = document.getElementById('result-count');
const viewAllBtn       = document.getElementById('view-all-btn');
const scopeSelector    = document.getElementById('scope-selector');
const scopeWrap        = document.getElementById('scope-wrap');
const outlinePanel     = document.getElementById('outline-panel');
const outlineTree      = document.getElementById('outline-tree');
const outlineToggleBtn = document.getElementById('outline-toggle-btn');
const fuzzyToggle      = document.getElementById('fuzzy-toggle');
const regexToggle      = document.getElementById('regex-toggle');
const toastEl          = document.getElementById('toast');

// ── Toast ─────────────────────────────────────────────────────────────────────
let toastTimer;
function showToast(msg, duration = 2500) {
  toastEl.textContent = msg;
  toastEl.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toastEl.classList.remove('show'), duration);
}

// ── Script switcher ───────────────────────────────────────────────────────────
document.querySelectorAll('.script-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.script-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    currentScript = btn.dataset.script;
    rerenderScript();
  });
});

// ── Sidebar toggle ────────────────────────────────────────────────────────────
document.getElementById('sidebar-toggle').addEventListener('click', () => {
  document.getElementById('sidebar').classList.toggle('mobile-open');
});

// ── Resize handle ─────────────────────────────────────────────────────────────
(function setupResize() {
  const handle = document.getElementById('resize-handle');
  const sidebar = document.getElementById('sidebar');
  let dragging = false, startX = 0, startW = 0;

  handle.addEventListener('mousedown', e => {
    dragging = true; startX = e.clientX; startW = sidebar.offsetWidth;
    handle.classList.add('dragging');
    document.body.style.userSelect = 'none';
  });
  document.addEventListener('mousemove', e => {
    if (!dragging) return;
    const w = Math.max(180, Math.min(520, startW + e.clientX - startX));
    sidebar.style.width = w + 'px';
  });
  document.addEventListener('mouseup', () => {
    if (!dragging) return;
    dragging = false; handle.classList.remove('dragging');
    document.body.style.userSelect = '';
  });
})();

// ── Load & render file tree ───────────────────────────────────────────────────
async function loadTree() {
  try {
    const res = await fetch('/api/tree');
    treeData = await res.json();
    renderTree(treeData.children || [], treeContainer, '');
    buildScopeSelector(treeData.children || []);
  } catch (e) {
    treeContainer.innerHTML = `<div style="padding:16px;color:red">Failed to load tree: ${e.message}</div>`;
  }
}


function renderTree(nodes, container, indent) {
  container.innerHTML = '';
  for (const node of nodes) {
    container.appendChild(createTreeNode(node));
  }
}

function createTreeNode(node) {
  const wrapper = document.createElement('div');
  wrapper.className = 'tree-node';

  if (node.type === 'dir') {
    const header = document.createElement('div');
    header.className = 'tree-folder-header';
    header.innerHTML = `<span class="tree-arrow">▶</span><span class="tree-folder-icon">📁</span><span class="tree-label">${escHtml(node.name)}</span>`;

    const children = document.createElement('div');
    children.className = 'tree-children';
    for (const child of (node.children || [])) {
      children.appendChild(createTreeNode(child));
    }

    header.addEventListener('click', () => {
      const arrow = header.querySelector('.tree-arrow');
      const isOpen = children.classList.toggle('open');
      arrow.classList.toggle('open', isOpen);
      header.querySelector('.tree-folder-icon').textContent = isOpen ? '📂' : '📁';
    });

    wrapper.appendChild(header);
    wrapper.appendChild(children);
  } else {
    const item = document.createElement('div');
    item.className = 'tree-file';
    item.dataset.path = node.path;
    item.innerHTML = `<span class="tree-file-icon">📄</span><span class="tree-label">${escHtml(node.name.replace(/\.txt$/, ''))}</span>`;
    item.addEventListener('click', () => openFile(node.path, item));
    wrapper.appendChild(item);
  }

  return wrapper;
}

// ── Sidebar filter ────────────────────────────────────────────────────────────
sidebarSearch.addEventListener('input', () => {
  const query = sidebarSearch.value.trim().toLowerCase();
  if (!query) {
    renderTree(treeData?.children || [], treeContainer);
    return;
  }
  const flat = flattenFiles(treeData?.children || []);
  const matches = flat.filter(f => f.name.toLowerCase().includes(query) || f.path.toLowerCase().includes(query));
  renderFlatFileList(matches);
});

function flattenFiles(nodes) {
  const list = [];
  for (const n of nodes) {
    if (n.type === 'file') list.push(n);
    else if (n.children) list.push(...flattenFiles(n.children));
  }
  return list;
}

function renderFlatFileList(files) {
  treeContainer.innerHTML = '';
  if (!files.length) {
    treeContainer.innerHTML = '<div style="padding:12px;color:#999;font-size:.85rem">No matches</div>';
    return;
  }
  for (const f of files) {
    const item = document.createElement('div');
    item.className = 'tree-file';
    item.dataset.path = f.path;
    item.innerHTML = `<span class="tree-file-icon">📄</span><span class="tree-label">${escHtml(f.path)}</span>`;
    item.addEventListener('click', () => openFile(f.path, item));
    treeContainer.appendChild(item);
  }
}

// ── Open & display a file ─────────────────────────────────────────────────────
async function openFile(filePath, itemEl, targetLine = 0) {
  // Highlight in sidebar
  document.querySelectorAll('.tree-file.active').forEach(el => el.classList.remove('active'));
  if (itemEl) itemEl.classList.add('active');

  currentFilePath = filePath;
  contentTitle.textContent = filePath;
  contentBody.innerHTML = '<div class="spinner" style="display:block;margin:40px auto;width:28px;height:28px;"></div>';

  // Reset progressive loading state
  loadedDevaLines = {};
  isLoadingChunk = false;
  destroyChunkObserver();

  // Start loading SCROLL_BUFFER lines before the target so it's centred in the initial view
  const startOffset = targetLine > 0 ? Math.max(0, targetLine - 1 - SCROLL_BUFFER) : 0;

  try {
    const [fileRes, outlineRes] = await Promise.all([
      fetch(`/api/file?path=${encodeURIComponent(filePath)}&offset=${startOffset}&limit=${CHUNK_SIZE}`),
      fetch(`/api/outline?path=${encodeURIComponent(filePath)}`)
    ]);
    if (!fileRes.ok) throw new Error(`HTTP ${fileRes.status}`);
    const fileData = await fileRes.json();

    totalFileLines = fileData.totalLines;
    loadedStart    = startOffset;
    loadedEnd      = startOffset + fileData.lines.length;

    outlineData = outlineRes.ok ? await outlineRes.json() : null;

    // Build sentinel elements for IntersectionObserver-driven chunk loading
    topSentinel    = document.createElement('div');
    topSentinel.className = 'chunk-sentinel';
    bottomSentinel = document.createElement('div');
    bottomSentinel.className = 'chunk-sentinel';

    // Annotate sentinels so the user knows more content exists
    if (loadedStart > 0) {
      topSentinel.textContent = `↑ ${loadedStart} more lines above`;
      topSentinel.classList.add('chunk-sentinel-label');
    }
    if (loadedEnd < totalFileLines) {
      bottomSentinel.textContent = `↓ ${totalFileLines - loadedEnd} more lines below`;
      bottomSentinel.classList.add('chunk-sentinel-label');
    }

    const frag = renderChunk(fileData.lines, startOffset);
    contentBody.innerHTML = '';
    contentBody.appendChild(topSentinel);
    contentBody.appendChild(frag);
    contentBody.appendChild(bottomSentinel);

    if (targetLine > 0) {
      // Double-rAF ensures layout is flushed before we try to scroll
      requestAnimationFrame(() => requestAnimationFrame(() => scrollToLine(targetLine)));
    }

    setupChunkObserver();
    renderOutlineFromData(outlineData);
  } catch (e) {
    contentBody.innerHTML = `<div style="padding:24px;color:red">Error loading file: ${e.message}</div>`;
  }
}

function renderOutlineFromData(data) {
  if (!data || data.style === 'none' || !data.nodes?.length) {
    outlinePanel.style.display = 'none';
    outlineToggleBtn.style.display = 'none';
    return;
  }
  outlineToggleBtn.style.display = '';
  renderOutline(data);
  const shouldAutoShow = data.style !== 'flat' || data.nodes.length > 5;
  if (shouldAutoShow) {
    outlinePanel.style.display = '';
    outlineToggleBtn.classList.add('active');
  }
}

// ── Render a chunk of lines into a DocumentFragment ──────────────────────────
// lines:        array of raw strings from the server
// startLineIdx: 0-based index of the first line in the full file
// Side-effect:  populates loadedDevaLines so rerenderScript() works later.
function renderChunk(lines, startLineIdx) {
  const frag = document.createDocumentFragment();
  let prevWasBlank = true;  // treat chunk start as after a blank (safe default)

  for (let i = 0; i < lines.length; i++) {
    const rawLine = lines[i];
    const trimmed = rawLine.trim();
    const lineIdx = startLineIdx + i;  // 0-based absolute position in the file

    if (!trimmed) { prevWasBlank = true; continue; }

    // Store Devanagari text so script-switching can update in-place
    loadedDevaLines[lineIdx] = trimmed;

    const displayed = currentScript === 'devanagari' ? trimmed : convertScript(trimmed, currentScript);

    // Section-header heuristic: short numbered lines without a daṇḍa
    const isHeader = /^[०-९\d]+[.\s]/.test(trimmed) && trimmed.length < 120 && !trimmed.includes('\u0964');

    const block = document.createElement('div');
    block.dataset.line = lineIdx + 1;  // 1-based for scrollToLine / data-line lookups

    if (isHeader && prevWasBlank) {
      block.className = 'section-title';
    } else {
      // Use lineIdx for stable even/odd colouring across chunk boundaries
      block.className = `verse-block ${lineIdx % 2 === 0 ? 'even' : 'odd'}`;
    }

    block.textContent = displayed;
    frag.appendChild(block);
    prevWasBlank = false;
  }

  return frag;
}

// Re-render text of already-loaded blocks when the display script changes.
// Much cheaper than re-fetching or rebuilding the DOM.
function rerenderScript() {
  contentBody.querySelectorAll('[data-line]').forEach(block => {
    const lineIdx = parseInt(block.dataset.line, 10) - 1;
    const devaText = loadedDevaLines[lineIdx];
    if (devaText !== undefined) {
      block.textContent = currentScript === 'devanagari'
        ? devaText
        : convertScript(devaText, currentScript);
    }
  });
}

function highlightMatches(html, query) {
  if (!query) return html;
  try {
    const escaped = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    return html.replace(new RegExp(escaped, 'gi'), m => `<mark>${m}</mark>`);
  } catch { return html; }
}

// ── Outline panel ─────────────────────────────────────────────────────────────

outlineToggleBtn.addEventListener('click', () => {
  const open = outlinePanel.style.display !== 'none';
  outlinePanel.style.display = open ? 'none' : '';
  outlineToggleBtn.classList.toggle('active', !open);
});
document.getElementById('outline-close-btn').addEventListener('click', () => {
  outlinePanel.style.display = 'none';
  outlineToggleBtn.classList.remove('active');
});


function renderOutline(data) {
  outlineTree.innerHTML = '';
  activeOutlineLeaf = null;

  const { style, nodes } = data;

  if (style === 'flat') {
    // Simple flat list of verse numbers
    for (const node of nodes) {
      outlineTree.appendChild(makeOutlineLeaf(node.label, node.lineNumber, 0));
    }
    return;
  }

  if (style === 'sectioned') {
    for (const section of nodes) {
      const wrapper = document.createElement('div');
      wrapper.className = 'ol-section';

      if (section.children?.length) {
        // Section with verses — collapsible
        const header = document.createElement('div');
        header.className = 'ol-section-header';
        const arrow = document.createElement('span');
        arrow.className = 'ol-arrow';
        arrow.textContent = '▶';
        const labelSpan = document.createElement('span');
        labelSpan.className = 'ol-section-label';
        labelSpan.textContent = section.label + '.';
        const titleSpan = document.createElement('span');
        titleSpan.className = 'ol-section-title';
        titleSpan.textContent = section.title || '';
        header.append(arrow, labelSpan, titleSpan);

        const childWrap = document.createElement('div');
        childWrap.className = 'ol-children';

        // Clicking the header itself scrolls to the section line
        header.addEventListener('click', () => {
          const isOpen = childWrap.classList.toggle('open');
          arrow.classList.toggle('open', isOpen);
          scrollToLine(section.lineNumber);
        });

        for (const v of section.children) {
          childWrap.appendChild(makeOutlineLeaf(v.label, v.lineNumber, 0));
        }

        wrapper.appendChild(header);
        wrapper.appendChild(childWrap);
      } else {
        // Sectionless heading — clickable leaf
        const header = document.createElement('div');
        header.className = 'ol-section-header';
        const labelSpan = document.createElement('span');
        labelSpan.className = 'ol-section-label';
        labelSpan.textContent = section.label + '.';
        const titleSpan = document.createElement('span');
        titleSpan.className = 'ol-section-title';
        titleSpan.textContent = section.title || '';
        header.append(document.createTextNode(' '), labelSpan, titleSpan);
        header.addEventListener('click', () => scrollToLine(section.lineNumber));
        wrapper.appendChild(header);
      }

      outlineTree.appendChild(wrapper);
    }
    return;
  }

  if (style === 'dotted') {
    // Multi-level: root → mid → leaf
    for (const root of nodes) {
      const rootWrapper = document.createElement('div');
      rootWrapper.className = 'ol-section';

      const rootHeader = document.createElement('div');
      rootHeader.className = 'ol-section-header';
      const arrow = document.createElement('span');
      arrow.className = 'ol-arrow';
      arrow.textContent = '▶';
      const labelEl = document.createElement('span');
      labelEl.className = 'ol-section-label';
      labelEl.textContent = root.label;
      rootHeader.append(arrow, labelEl);

      const rootChildren = document.createElement('div');
      rootChildren.className = 'ol-children';

      rootHeader.addEventListener('click', () => {
        const isOpen = rootChildren.classList.toggle('open');
        arrow.classList.toggle('open', isOpen);
        scrollToLine(root.lineNumber);
      });

      for (const mid of (root.children || [])) {
        if (!mid.children?.length) {
          // Leaf at mid level
          rootChildren.appendChild(makeOutlineLeaf(mid.label, mid.lineNumber, 0));
        } else {
          // Mid level with children
          const midWrapper = document.createElement('div');
          midWrapper.className = 'ol-section';

          const midHeader = document.createElement('div');
          midHeader.className = 'ol-mid';
          const midArrow = document.createElement('span');
          midArrow.className = 'ol-arrow';
          midArrow.textContent = '▶';
          const midLabel = document.createElement('span');
          midLabel.className = 'ol-mid-label';
          midLabel.textContent = mid.label;
          midHeader.append(midArrow, midLabel);

          const midChildren = document.createElement('div');
          midChildren.className = 'ol-children';

          midHeader.addEventListener('click', () => {
            const isOpen = midChildren.classList.toggle('open');
            midArrow.classList.toggle('open', isOpen);
            scrollToLine(mid.lineNumber);
          });

          for (const leaf of mid.children) {
            midChildren.appendChild(makeOutlineLeaf(leaf.label, leaf.lineNumber, 0));
          }

          midWrapper.append(midHeader, midChildren);
          rootChildren.appendChild(midWrapper);
        }
      }

      rootWrapper.append(rootHeader, rootChildren);
      outlineTree.appendChild(rootWrapper);
    }
  }
}

function makeOutlineLeaf(label, lineNumber, _indent) {
  const el = document.createElement('div');
  el.className = 'ol-leaf';
  el.dataset.line = lineNumber;
  const num = document.createElement('span');
  num.className = 'ol-leaf-num';
  num.textContent = label;
  el.appendChild(num);
  el.addEventListener('click', () => {
    // Deactivate previous
    if (activeOutlineLeaf) activeOutlineLeaf.classList.remove('active');
    el.classList.add('active');
    activeOutlineLeaf = el;
    scrollToLine(lineNumber);
  });
  return el;
}

function scrollToLine(lineNumber) {
  const target = contentBody.querySelector(`[data-line="${lineNumber}"]`);
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'center' });
    target.style.outline = '2px solid var(--accent-light)';
    setTimeout(() => target.style.outline = '', 1800);
  } else {
    // Line is outside the currently-loaded window — fetch the right chunk
    jumpToLine(lineNumber);
  }
}

// Fetch and render a fresh window of lines centred on lineNumber,
// replacing whatever is currently in the content body.
async function jumpToLine(lineNumber) {
  if (!currentFilePath) return;
  isLoadingChunk = false;
  destroyChunkObserver();

  const startOffset = Math.max(0, lineNumber - 1 - SCROLL_BUFFER);
  try {
    const res = await fetch(
      `/api/file?path=${encodeURIComponent(currentFilePath)}&offset=${startOffset}&limit=${CHUNK_SIZE}`
    );
    if (!res.ok) return;
    const data = await res.json();

    loadedDevaLines = {};
    loadedStart = startOffset;
    loadedEnd   = startOffset + data.lines.length;

    topSentinel    = document.createElement('div');
    topSentinel.className = 'chunk-sentinel';
    bottomSentinel = document.createElement('div');
    bottomSentinel.className = 'chunk-sentinel';

    if (loadedStart > 0) {
      topSentinel.textContent = `↑ ${loadedStart} more lines above`;
      topSentinel.classList.add('chunk-sentinel-label');
    }
    if (loadedEnd < totalFileLines) {
      bottomSentinel.textContent = `↓ ${totalFileLines - loadedEnd} more lines below`;
      bottomSentinel.classList.add('chunk-sentinel-label');
    }

    const frag = renderChunk(data.lines, startOffset);
    contentBody.innerHTML = '';
    contentBody.appendChild(topSentinel);
    contentBody.appendChild(frag);
    contentBody.appendChild(bottomSentinel);

    // Scroll after the browser has painted the new content
    requestAnimationFrame(() => requestAnimationFrame(() => {
      const el = contentBody.querySelector(`[data-line="${lineNumber}"]`);
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
        el.style.outline = '2px solid var(--accent-light)';
        setTimeout(() => el.style.outline = '', 1800);
      }
    }));

    setupChunkObserver();
  } catch (e) {
    console.error('jumpToLine:', e);
  }
}

// ── Progressive chunk loading ─────────────────────────────────────────────────

function destroyChunkObserver() {
  if (chunkObserver) { chunkObserver.disconnect(); chunkObserver = null; }
}

function setupChunkObserver() {
  destroyChunkObserver();
  // No observer needed when the whole file fits in one chunk
  if (totalFileLines <= CHUNK_SIZE) return;

  chunkObserver = new IntersectionObserver(entries => {
    for (const entry of entries) {
      if (!entry.isIntersecting || isLoadingChunk) continue;
      if (entry.target === bottomSentinel && loadedEnd < totalFileLines) {
        loadNextChunk();
      } else if (entry.target === topSentinel && loadedStart > 0) {
        loadPrevChunk();
      }
    }
  }, { root: contentBody, threshold: 0 });

  if (topSentinel)    chunkObserver.observe(topSentinel);
  if (bottomSentinel) chunkObserver.observe(bottomSentinel);
}

async function loadNextChunk() {
  if (isLoadingChunk || loadedEnd >= totalFileLines) return;
  isLoadingChunk = true;
  if (bottomSentinel) bottomSentinel.textContent = 'Loading…';
  try {
    const res = await fetch(`/api/file?path=${encodeURIComponent(currentFilePath)}&offset=${loadedEnd}&limit=${CHUNK_SIZE}`);
    if (!res.ok) return;
    const data = await res.json();
    const frag = renderChunk(data.lines, loadedEnd);
    contentBody.insertBefore(frag, bottomSentinel);
    loadedEnd += data.lines.length;
    if (bottomSentinel) {
      const rem = totalFileLines - loadedEnd;
      bottomSentinel.textContent = rem > 0 ? `↓ ${rem} more lines below` : '';
      bottomSentinel.classList.toggle('chunk-sentinel-label', rem > 0);
    }
  } catch (e) {
    console.error('loadNextChunk:', e);
  } finally {
    isLoadingChunk = false;
  }
}

async function loadPrevChunk() {
  if (isLoadingChunk || loadedStart <= 0) return;
  isLoadingChunk = true;
  if (topSentinel) topSentinel.textContent = 'Loading…';
  try {
    const newStart = Math.max(0, loadedStart - CHUNK_SIZE);
    const limit    = loadedStart - newStart;
    const res = await fetch(`/api/file?path=${encodeURIComponent(currentFilePath)}&offset=${newStart}&limit=${limit}`);
    if (!res.ok) return;
    const data = await res.json();
    const frag = renderChunk(data.lines, newStart);
    // Maintain scroll position: record before-height, inject, then correct scrollTop
    const prevScrollTop = contentBody.scrollTop;
    const prevHeight    = contentBody.scrollHeight;
    contentBody.insertBefore(frag, topSentinel.nextSibling);
    contentBody.scrollTop = prevScrollTop + (contentBody.scrollHeight - prevHeight);
    loadedStart = newStart;
    if (topSentinel) {
      const rem = loadedStart;
      topSentinel.textContent = rem > 0 ? `↑ ${rem} more lines above` : '';
      topSentinel.classList.toggle('chunk-sentinel-label', rem > 0);
    }
  } catch (e) {
    console.error('loadPrevChunk:', e);
  } finally {
    isLoadingChunk = false;
  }
}

// Update active outline leaf as user scrolls
let scrollDebounce;
contentBody.addEventListener('scroll', () => {
  clearTimeout(scrollDebounce);
  scrollDebounce = setTimeout(syncOutlineToScroll, 120);
});

function syncOutlineToScroll() {
  if (!outlineData || outlineData.style === 'none') return;
  // Find topmost visible block
  const blocks = [...contentBody.querySelectorAll('[data-line]')];
  const containerTop = contentBody.getBoundingClientRect().top;
  let closest = null, closestDist = Infinity;
  for (const b of blocks) {
    const rect = b.getBoundingClientRect();
    const dist = Math.abs(rect.top - containerTop);
    if (dist < closestDist) { closestDist = dist; closest = b; }
  }
  if (!closest) return;
  const lineNum = closest.dataset.line;
  const leafEl = outlineTree.querySelector(`.ol-leaf[data-line="${lineNum}"]`);
  if (leafEl && leafEl !== activeOutlineLeaf) {
    if (activeOutlineLeaf) activeOutlineLeaf.classList.remove('active');
    leafEl.classList.add('active');
    activeOutlineLeaf = leafEl;
    // Scroll leaf into view within the outline panel
    leafEl.scrollIntoView({ block: 'nearest' });
  }
}

// ── Global search bar ─────────────────────────────────────────────────────────
globalSearch.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    openSearchOverlay(globalSearch.value.trim());
  } else if (e.key === 'Escape') {
    globalSearch.value = '';
  }
});
document.getElementById('search-open-btn').addEventListener('click', () => {
  openSearchOverlay(globalSearch.value.trim());
});

// Ctrl+K shortcut
document.addEventListener('keydown', e => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault();
    globalSearch.focus();
    openSearchOverlay('');
  }
  if (e.key === 'Escape') closeSearchOverlay();
});

// ── Voice search ──────────────────────────────────────────────────────────────
const voiceBtn = document.getElementById('voice-btn');
let recognition = null;

voiceBtn.addEventListener('click', () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) {
    showToast('Voice search not supported in this browser.');
    return;
  }
  if (recognition) {
    recognition.stop();
    recognition = null;
    voiceBtn.classList.remove('mic-recording');
    return;
  }
  recognition = new SpeechRecognition();
  recognition.lang = 'hi-IN';     // Hindi/Sanskrit
  recognition.interimResults = false;
  recognition.maxAlternatives = 1;

  recognition.onstart = () => voiceBtn.classList.add('mic-recording');
  recognition.onend   = () => { voiceBtn.classList.remove('mic-recording'); recognition = null; };
  recognition.onerror = () => { showToast('Voice recognition error.'); voiceBtn.classList.remove('mic-recording'); recognition = null; };

  recognition.onresult = e => {
    const transcript = e.results[0][0].transcript.trim();
    if (transcript) {
      globalSearch.value = transcript;
      openSearchOverlay(transcript);
    }
  };

  recognition.start();
  showToast('Listening… speak now.');
});

// ── Search overlay ────────────────────────────────────────────────────────────
function openSearchOverlay(query = '') {
  searchOverlay.classList.add('open');
  // Populate the search field in the panel if a query was passed
  if (query) {
    globalSearch.value = query;
    triggerSearch(query);
  }
}

function closeSearchOverlay() {
  searchOverlay.classList.remove('open');
}

document.getElementById('search-backdrop').addEventListener('click', closeSearchOverlay);
document.getElementById('search-close-btn').addEventListener('click', closeSearchOverlay);

// Script toggle buttons in the panel
document.querySelectorAll('[data-search-script]').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('[data-search-script]').forEach(b => b.classList.remove('on'));
    btn.classList.add('on');
    searchScript = btn.dataset.searchScript;
    const q = globalSearch.value.trim();
    if (q) triggerSearch(q);
  });
});

fuzzyToggle.addEventListener('click', () => {
  if (regexMode) { regexMode = false; regexToggle.classList.remove('on'); }
  fuzzyMode = !fuzzyMode;
  fuzzyToggle.classList.toggle('on', fuzzyMode);
  const q = globalSearch.value.trim();
  if (q) triggerSearch(q);
});

regexToggle.addEventListener('click', () => {
  if (fuzzyMode) { fuzzyMode = false; fuzzyToggle.classList.remove('on'); }
  regexMode = !regexMode;
  regexToggle.classList.toggle('on', regexMode);
  const q = globalSearch.value.trim();
  if (q) triggerSearch(q);
});

// Scope toggle
document.getElementById('scope-toggle-btn').addEventListener('click', () => {
  const visible = scopeWrap.style.display !== 'none';
  scopeWrap.style.display = visible ? 'none' : 'block';
});

// Live search as user types in global search box while overlay is open
globalSearch.addEventListener('input', () => {
  if (!searchOverlay.classList.contains('open')) return;
  clearTimeout(overlaySearchTimeout);
  const q = globalSearch.value.trim();
  overlaySearchTimeout = setTimeout(() => { if (q) triggerSearch(q); else clearResults(); }, 400);
});

async function triggerSearch(query) {
  if (!query) return;

  searchResultsList.innerHTML = '<div style="padding:20px;text-align:center"><div class="spinner" style="display:inline-block;width:20px;height:20px"></div></div>';
  noResultsMsg.style.display = 'none';
  viewAllBtn.style.display = 'none';
  resultCount.textContent = '';

  const params = new URLSearchParams({
    q: query,
    script: searchScript,
    fuzzy: fuzzyMode ? 'true' : 'false',
    regex: regexMode ? 'true' : 'false',
    limit: '10',
    offset: '0'
  });

  if (selectedScopes.length) params.set('paths', selectedScopes.join(','));

  try {
    const res = await fetch(`/api/search?${params}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    renderSearchResults(data, query);
  } catch (e) {
    searchResultsList.innerHTML = `<div style="padding:16px;color:red">Search error: ${e.message}</div>`;
  }
}

function renderSearchResults(data, query) {
  searchResultsList.innerHTML = '';

  const { results = [], total = 0, devaQuery = query } = data;

  resultCount.textContent = total ? `${total.toLocaleString()} result${total !== 1 ? 's' : ''}` : '';

  if (!results.length) {
    noResultsMsg.style.display = 'block';
    searchResultsList.appendChild(noResultsMsg);
    return;
  }

  // Show "view all" if there are more results than shown
  if (total > results.length) {
    viewAllBtn.style.display = 'inline-block';
    viewAllBtn.onclick = () => {
      const params = new URLSearchParams({
        q: globalSearch.value.trim(),
        script: searchScript,
        fuzzy: fuzzyMode ? 'true' : 'false',
        regex: regexMode ? 'true' : 'false'
      });
      if (selectedScopes.length) params.set('paths', selectedScopes.join(','));
      window.open(`/search?${params}`, '_blank');
    };
  }

  for (const item of results) {
    const el = document.createElement('div');
    el.className = 'search-result-item';

    const displayedLine = currentScript === 'devanagari'
      ? item.lineText
      : convertScript(item.lineText, currentScript);

    const highlighted = highlightMatches(escHtml(displayedLine), devaQuery);

    el.innerHTML = `
      <div class="search-result-file">${escHtml(item.file)} — line ${item.lineNumber}</div>
      <div class="search-result-text">${highlighted}</div>`;

    el.addEventListener('click', () => {
      closeSearchOverlay();
      navigateToResult(item);
    });

    searchResultsList.appendChild(el);
  }
}

async function navigateToResult(item) {
  const sidebarItem = treeContainer.querySelector(`[data-path="${CSS.escape(item.file)}"]`);

  if (currentFilePath !== item.file) {
    // openFile fetches the chunk centred around the target line and scrolls there
    await openFile(item.file, sidebarItem, item.lineNumber);
    expandParentsOf(item.file);
    return;
  }

  // Same file — try to scroll to the line directly
  const target = contentBody.querySelector(`[data-line="${item.lineNumber}"]`);
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'center' });
    target.style.outline = '2px solid var(--accent)';
    setTimeout(() => target.style.outline = '', 2000);
  } else {
    // Line exists but isn't in the loaded window; reload the file at that position
    await openFile(item.file, sidebarItem, item.lineNumber);
  }
}

function expandParentsOf(filePath) {
  const parts = filePath.split('/');
  let cumPath = '';
  for (let i = 0; i < parts.length - 1; i++) {
    cumPath = cumPath ? `${cumPath}/${parts[i]}` : parts[i];
    // Find folder header whose path matches
    treeContainer.querySelectorAll('.tree-folder-header').forEach(h => {
      const children = h.nextElementSibling;
      if (children && !children.classList.contains('open')) {
        // Check if this subtree contains our file
        if (children.querySelector(`[data-path="${CSS.escape(filePath)}"]`)) {
          children.classList.add('open');
          h.querySelector('.tree-arrow')?.classList.add('open');
          h.querySelector('.tree-folder-icon').textContent = '📂';
        }
      }
    });
  }
}

function clearResults() {
  searchResultsList.innerHTML = '';
  noResultsMsg.style.display = 'none';
  viewAllBtn.style.display = 'none';
  resultCount.textContent = '';
}

// ── Build scope selector ──────────────────────────────────────────────────────
function buildScopeSelector(nodes) {
  scopeSelector.innerHTML = '';
  for (const node of nodes) {
    if (node.type !== 'dir') continue;
    const label = document.createElement('label');
    const cb = document.createElement('input');
    cb.type = 'checkbox';
    cb.value = node.path;
    cb.addEventListener('change', () => {
      selectedScopes = Array.from(scopeSelector.querySelectorAll('input:checked')).map(i => i.value);
      const q = globalSearch.value.trim();
      if (q && searchOverlay.classList.contains('open')) triggerSearch(q);
    });
    label.appendChild(cb);
    label.appendChild(document.createTextNode(' ' + node.name));
    scopeSelector.appendChild(label);
  }
}

// ── Utility ───────────────────────────────────────────────────────────────────
function escHtml(str) {
  return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

// ── Handle URL params (e.g. ?file=...&line=...) ───────────────────────────────
async function handleUrlParams() {
  const params = new URLSearchParams(location.search);
  const file = params.get('file');
  const line = parseInt(params.get('line'), 10) || 0;

  if (file) {
    // openFile fetches the chunk around `line` and scrolls there automatically
    await openFile(file, null, line);
    history.replaceState({}, '', '/');
  }
}

// ── Init ──────────────────────────────────────────────────────────────────────
loadTree().then(handleUrlParams);
