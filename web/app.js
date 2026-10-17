const $ = selector => document.querySelector(selector);
const state = { cases: [], selected: new Set(), category: '全部', query: '', resultFilter: 'all', run: null, busy: false };
const escapeHtml = value => String(value).replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
const pageInfo = { run: ['运行与测试', '选择测试用例，开始一次可靠的设备验证。'], results: ['测试结果', '每一项结果清晰可见，每一次执行都有记录。'], report: ['报告分析', '汇总测试表现，快速识别需要关注的问题。'] };
function navigate(view) {
  if (!pageInfo[view]) view = 'run';
  document.querySelectorAll('.view').forEach(element => { element.hidden = element.id !== `view-${view}`; });
  document.querySelectorAll('.nav-item').forEach(button => { button.classList.toggle('active', button.dataset.view === view); if (button.dataset.view === view) button.setAttribute('aria-current', 'page'); else button.removeAttribute('aria-current'); });
  $('#page-title').textContent = $('#breadcrumb').textContent = pageInfo[view][0];
  $('#page-description').textContent = pageInfo[view][1];
  if (location.hash !== `#${view}`) location.hash = view;
  if (view === 'results') renderResults();
  if (view === 'report') renderReport();
}
function notify(message, error = false) { $('#notice').textContent = message; $('#notice').hidden = !message; $('#notice').classList.toggle('error', error); }
function visibleCases() { return state.cases.filter(item => (state.category === '全部' || item.category === state.category) && item.name.includes(state.query)); }
function selectedExecutionMode() {
  const value = $('input[name="ExecutionMode"]:checked')?.value;
  return ['REAL_DEVICE', 'SIMULATION'].includes(value) ? value : null;
}
function updateSelection() {
  $('#selected-count').textContent = state.selected.size;
  $('#start-test').disabled = state.busy || !state.selected.size || !selectedExecutionMode();
  $('#execution-mode').disabled = state.busy;
  const visible = visibleCases();
  const count = visible.filter(item => state.selected.has(item.id)).length;
  $('#select-all').checked = !!visible.length && count === visible.length;
  $('#select-all').indeterminate = count > 0 && count < visible.length;
  $('#select-all').disabled = state.busy || !visible.length;
}
function renderCases() {
  $('#case-total').textContent = state.cases.length.toString().padStart(2, '0');
  $('#case-grid').innerHTML = visibleCases().map(item => `<label class="case-card ${state.selected.has(item.id) ? 'selected' : ''}"><div class="case-top"><span class="case-id">${escapeHtml(item.id)}</span><input type="checkbox" data-id="${escapeHtml(item.id)}" aria-label="选择${escapeHtml(item.name)}" ${state.selected.has(item.id) ? 'checked' : ''} ${state.busy ? 'disabled' : ''}></div><h3>${escapeHtml(item.name)}</h3><p>${escapeHtml(item.description)}</p><div class="case-bottom"><span class="category-tag">${escapeHtml(item.category)}</span><span>预计 ${item.seconds} 秒</span></div></label>`).join('') || '<div class="empty"><strong>没有匹配的用例</strong><p>请尝试其他关键词或分类。</p></div>';
  updateSelection();
}
function totals() { const results = state.run?.results || []; const passed = results.filter(item => item.status === 'passed').length; return { total: results.length, passed, failed: results.length - passed, score: results.length ? Math.round(passed / results.length * 100) : 0 }; }
function empty() { return '<div class="empty"><strong>还没有测试记录</strong><p>请前往「运行与测试」选择用例并开始测试。</p><button class="button secondary" data-go-run>选择测试用例 →</button></div>'; }
function renderResults() {
  const t = totals();
  $('#result-summary').innerHTML = [['执行用例', t.total, '项', ''], ['通过用例', t.passed, '项', 'green'], ['失败用例', t.failed, '项', 'red'], ['总得分', t.total ? t.score : '—', '/ 100', '']].map(([label, value, unit, color]) => `<div class="stat"><span class="stat-label">${label}</span><strong class="stat-value ${color}">${value} <small>${unit}</small></strong></div>`).join('');
  $('#run-meta').textContent = state.run ? `${state.run.runId} · ${new Date(state.run.completedAt).toLocaleString('zh-CN')} · 模拟执行结果` : '尚未执行测试';
  const items = (state.run?.results || []).filter(item => state.resultFilter === 'all' || item.status === state.resultFilter);
  $('#results-list').innerHTML = !state.run ? empty() : items.map(item => `<article class="result-card"><div class="result-title"><h3>${escapeHtml(item.name)}</h3><span class="${item.status === 'passed' ? 'green' : 'red'}"><i class="status-dot ${item.status === 'passed' ? 'pass' : 'fail'}" aria-hidden="true"></i>${item.status === 'passed' ? '通过' : '失败'}</span></div><p>${escapeHtml(item.message)}</p><div class="result-meta"><span>${escapeHtml(item.id)} / ${escapeHtml(item.category)}</span><span>模拟耗时 ${(item.durationMs / 1000).toFixed(1)} s</span></div></article>`).join('') || '<div class="empty"><strong>没有符合条件的结果</strong><p>可切换到全部结果查看本次执行。</p></div>';
  $('#to-report').disabled = !state.run;
}
function renderReport() {
  if (!state.run) { $('#report-content').innerHTML = `<div class="panel">${empty()}</div>`; return; }
  const t = totals();
  const failed = state.run.results.filter(item => item.status === 'failed');
  $('#report-content').innerHTML = `<div class="report-heading"><span class="subtle">${escapeHtml(state.run.runId)} · 本次测试分析（模拟数据）</span><button id="download-report" class="button secondary">↓ 导出报告 JSON</button></div><div class="report-layout"><div class="report-box"><h2>整体通过率</h2><div class="donut" style="--rate:${t.score}%"><div class="donut-center">${t.score}%<small>${t.passed} / ${t.total} 项用例通过</small></div></div><div class="legend"><span><i class="status-dot pass"></i>通过 ${t.passed}</span><span><i class="status-dot fail"></i>失败 ${t.failed}</span></div></div><div class="report-box"><h2>分类测试表现</h2><p>按本次实际选择的用例统计通过率</p>${['连接管理', '数据交互', '安全验证'].map(category => { const items = state.run.results.filter(item => item.category === category); const pass = items.filter(item => item.status === 'passed').length; return `<div class="bar-row"><div class="bar-label"><span>${category}</span><span>${items.length ? `${pass} / ${items.length} 通过` : '未执行'}</span></div><div class="bar"><span style="width:${items.length ? pass / items.length * 100 : 0}%"></span></div></div>`; }).join('')}</div></div><div class="report-box"><h2>${failed.length ? '需要关注的检查项' : '所有检查项均已通过'}</h2>${failed.length ? failed.map(item => `<div class="finding"><strong class="red">${escapeHtml(item.name)}</strong><p>${escapeHtml(item.message)} ${item.id === 'OPC-006' ? '建议检查节点写权限、变量类型及写入值。' : '建议检查证书有效期及客户端信任列表。'}</p></div>`).join('') : '<p>本次所选用例的模拟结果全部通过，可继续选择其他用例进行验证。</p>'}<p>此报告仅用于界面与流程演示，不代表真实设备测试结论。</p></div>`;
}
document.querySelectorAll('[data-view]').forEach(button => button.addEventListener('click', () => navigate(button.dataset.view)));
window.addEventListener('hashchange', () => navigate(location.hash.slice(1)));
$('#case-filters').addEventListener('click', event => { const button = event.target.closest('[data-category]'); if (!button) return; state.category = button.dataset.category; $('#case-filters').querySelectorAll('button').forEach(item => item.classList.toggle('active', item === button)); renderCases(); });
$('#search').addEventListener('input', event => { state.query = event.target.value.trim(); renderCases(); });
$('#execution-mode').addEventListener('change', updateSelection);
$('#case-grid').addEventListener('change', event => { if (!event.target.matches('[data-id]')) return; const id = event.target.dataset.id; if (event.target.checked) state.selected.add(id); else state.selected.delete(id); event.target.closest('.case-card').classList.toggle('selected', event.target.checked); updateSelection(); });
$('#select-all').addEventListener('change', event => { visibleCases().forEach(item => event.target.checked ? state.selected.add(item.id) : state.selected.delete(item.id)); renderCases(); });
$('#result-filters').addEventListener('click', event => { const button = event.target.closest('[data-status]'); if (!button) return; state.resultFilter = button.dataset.status; $('#result-filters').querySelectorAll('button').forEach(item => item.classList.toggle('active', item === button)); renderResults(); });
$('#back-select').addEventListener('click', () => navigate('run'));
$('#to-report').addEventListener('click', () => navigate('report'));
document.addEventListener('click', event => {
  if (event.target.closest('[data-go-run]')) navigate('run');
  if (event.target.closest('#download-report') && state.run) {
    const blob = new Blob([JSON.stringify({ ...state.run, summary: totals() }, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob); const anchor = document.createElement('a'); anchor.href = url; anchor.download = `${state.run.runId}.json`; anchor.click(); setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
});
$('#start-test').addEventListener('click', async () => {
  if (state.busy || !state.selected.size) return;
  const mode = selectedExecutionMode();
  if (!mode) {
    notify('请选择测试模式后再开始测试。', true);
    updateSelection();
    return;
  }
  // 从所有已选用例构造请求，包含被搜索或分类筛选隐藏的选中项。
  const codelist = state.cases.filter(item => state.selected.has(item.id)).map(item => item.id);
  state.busy = true; renderCases(); $('#start-test').textContent = '正在测试…';
  notify(`正在以 ${mode} 模式提交 ${codelist.length} 项用例，请稍候…`);
  const controller = new AbortController(); const timeout = setTimeout(() => controller.abort(), 30000);
  try {
    const response = await fetch('index/test', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ codelist, mode }), signal: controller.signal });
    const responseText = await response.text();
    if (!response.ok) throw new Error(`测试接口返回 HTTP ${response.status}`);
    // 当前 Java Controller 返回 void；空响应只表示请求成功，不生成虚假测试结果。
    if (!responseText.trim()) {
      notify('测试请求已成功返回；后端尚未提供测试结果数据。');
      return;
    }
    const data = JSON.parse(responseText);
    if (!Array.isArray(data.results) || data.results.length !== codelist.length || new Set(data.results.map(item => item.id)).size !== codelist.length || data.results.some(item => !codelist.includes(item.id) || !['passed', 'failed'].includes(item.status))) throw new Error('后端返回的测试结果格式不正确');
    state.run = data; state.resultFilter = 'all'; $('#result-filters').querySelectorAll('button').forEach(button => button.classList.toggle('active', button.dataset.status === 'all'));
    notify(`测试完成：已收到 ${data.results.length} 项模拟结果。`); navigate('results');
  } catch (error) { notify(error.name === 'AbortError' ? '请求超时，后端任务状态未知，请检查服务后再操作。' : `提交失败：${error.message}。请检查 Tomcat 服务及测试接口响应。`, true); }
  finally { clearTimeout(timeout); state.busy = false; $('#start-test').innerHTML = '<span>▷</span> 开始测试 <span>→</span>'; renderCases(); }
});
$('#today').textContent = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' });
navigate(location.hash.slice(1));
(async () => { try { const response = await fetch('index/cases'); if (!response.ok) throw new Error('获取用例失败'); state.cases = await response.json(); renderCases(); } catch { notify('无法加载用例。请确认 Tomcat 已启动、index/cases 接口可访问，并通过应用地址打开页面。', true); $('#case-grid').innerHTML = '<div class="empty"><strong>用例加载失败</strong><p>检查 Tomcat 服务及用例接口后刷新页面重试。</p></div>'; } })();
