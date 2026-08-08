import { useEffect, useMemo, useState } from 'react'
import { getTickets, resolveTicket } from './services/ticketApi'

const Icon = ({ children, className = '' }) => <span className={`icon ${className}`}>{children}</span>

function Confidence({ value }) {
  const tone = value >= 90 ? 'high' : value >= 75 ? 'medium' : 'low'
  return <span className={`confidence ${tone}`}><i />{value}% confident</span>
}

function TicketCard({ ticket, selected, onSelect }) {
  const human = ticket.status === 'human'
  return (
    <button className={`ticket-card ${selected ? 'selected' : ''} ${human ? 'review' : ''}`} onClick={() => onSelect(ticket)}>
      <div className="ticket-top"><span className="ticket-id">{ticket.id}</span><span className={`status ${human ? 'status-review' : 'status-auto'}`}><Icon>{human ? '◷' : '✓'}</Icon>{human ? 'Human review' : 'Auto-resolved'}</span></div>
      <p className="ticket-description">{ticket.description}</p>
      <div className="ticket-meta"><span>{ticket.category}</span><span>•</span><span>{ticket.order.id}</span></div>
      <div className="ticket-bottom"><span className="action-label">{human ? 'Suggested' : 'Action'}: <strong>{human ? ticket.suggestedAction : ticket.action}</strong></span><Confidence value={ticket.confidence} /></div>
    </button>
  )
}

function TicketDetails({ ticket, onResolve }) {
  const [copied, setCopied] = useState(false)
  if (!ticket) return <div className="empty-details">Select a ticket to inspect its decision trail.</div>
  const human = ticket.status === 'human'
  const copyReply = async () => { await navigator.clipboard?.writeText(ticket.reply); setCopied(true); setTimeout(() => setCopied(false), 1500) }
  return (
    <aside className="details-panel">
      <div className="details-head">
        <div><div className="eyebrow">TICKET DETAIL</div><h2>{ticket.id}</h2><p>{ticket.customer} · {ticket.category}</p></div>
        <span className={`status ${human ? 'status-review' : 'status-auto'}`}><Icon>{human ? '◷' : '✓'}</Icon>{human ? 'Needs human' : 'Resolved'}</span>
      </div>
      <div className="detail-section issue"><div className="section-title">Customer issue</div><p>{ticket.description}</p></div>
      <div className="order-card"><div className="section-title">Order information</div><div className="order-grid"><div><span>Order</span><b>{ticket.order.id}</b></div><div><span>Value</span><b>{ticket.order.value}</b></div><div><span>Store</span><b>{ticket.order.store}</b></div><div><span>Placed</span><b>{ticket.order.placed}</b></div><div><span>ETA</span><b>{ticket.order.eta}</b></div><div><span>Delivery</span><b className={ticket.order.delay === 'On time' ? 'positive' : 'warning'}>{ticket.order.delay}</b></div></div></div>
      <div className={`decision ${human ? 'decision-review' : ''}`}><div className="decision-header"><div><span className="section-title">{human ? 'Suggested resolution' : 'Chosen resolution'}</span><strong>{human ? ticket.suggestedAction : ticket.action}</strong></div><Confidence value={ticket.confidence} /></div>{ticket.conflict && <div className="conflict"><Icon>!</Icon><span>Conflicting precedents detected — review the affected items before acting.</span></div>}<div className="why"><span className="why-title"><Icon>✦</Icon>Why this action?</span><p>{ticket.reason}</p></div></div>
      <div className="detail-section"><div className="section-row"><div className="section-title">Top similar tickets</div><span className="subtle">Historical outcomes</span></div><div className="similar-list">{ticket.similarities.map((item, index) => <div className="similar" key={item.id}><span className="rank">{index + 1}</span><div><b>{item.id}</b><p>{item.text}</p><span>{item.outcome}</span></div><strong>{item.match}%<small>match</small></strong></div>)}</div></div>
      <div className="reply"><div className="section-row"><div className="section-title">Drafted customer reply</div><button className="text-button" onClick={copyReply}>{copied ? 'Copied' : 'Copy'}</button></div><p>{ticket.reply}</p></div>
      {human && <div className="review-actions"><button className="secondary">Edit decision</button><button className="primary" onClick={() => onResolve(ticket)}>Approve & resolve <span>→</span></button></div>}
    </aside>
  )
}

export default function App() {
  const [tickets, setTickets] = useState([])
  const [selected, setSelected] = useState(null)
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('All tickets')
  const [search, setSearch] = useState('')
  const [toast, setToast] = useState('')
  useEffect(() => { getTickets().then((data) => { setTickets(data); setSelected(data[0]); setLoading(false) }) }, [])
  const filtered = useMemo(() => tickets.filter((ticket) => (filter === 'All tickets' || ticket.category === filter) && `${ticket.id} ${ticket.customer} ${ticket.description}`.toLowerCase().includes(search.toLowerCase())), [tickets, filter, search])
  const auto = filtered.filter((ticket) => ticket.status === 'auto')
  const human = filtered.filter((ticket) => ticket.status === 'human')
  const approve = async (ticket) => { await resolveTicket(ticket.id); const next = tickets.map((item) => item.id === ticket.id ? { ...item, status: 'auto', action: item.suggestedAction, confidence: Math.max(item.confidence, 85) } : item); setTickets(next); setSelected(next.find((item) => item.id === ticket.id)); setToast(`${ticket.id} approved and resolved`); setTimeout(() => setToast(''), 2600) }
  return <main className="app-shell">
    <nav className="sidebar"><div className="brand"><div className="brand-mark">z</div><span>zepto</span></div><div className="workspace-label">WORKSPACE</div><button className="nav-item active"><Icon>⌑</Icon>Ticket inbox<span className="nav-count">{tickets.length}</span></button><button className="nav-item"><Icon>◫</Icon>Analytics</button><button className="nav-item"><Icon>⌘</Icon>Knowledge base</button><div className="sidebar-foot"><button className="nav-item"><Icon>⚙</Icon>Settings</button><div className="avatar">SK</div><div className="agent"><b>Siddhi Kulkarni</b><span>Support lead</span></div></div></nav>
    <section className="workspace"><header className="topbar"><div><div className="eyebrow">SUPPORT OPERATIONS</div><h1>Ticket inbox</h1></div><div className="live"><span className="live-dot"/>AI triage live</div></header>
      <div className="summary"><div><span>Resolved today</span><b>128</b><small>+18% from yesterday</small></div><div><span>Human review queue</span><b>12</b><small className="amber">4 need attention</small></div><div><span>AI resolution rate</span><b>86%</b><small>Across 1,026 tickets</small></div><div className="summary-note"><Icon>✦</Icon><p>AI is handling routine tickets with policy-aware decisions.</p></div></div>
      <div className="toolbar"><div className="search"><Icon>⌕</Icon><input placeholder="Search ticket, customer, order…" value={search} onChange={(e) => setSearch(e.target.value)} /></div><select value={filter} onChange={(e) => setFilter(e.target.value)}><option>All tickets</option><option>Late delivery</option><option>Product freshness</option><option>Missing item</option><option>Damaged item</option><option>Delivery experience</option></select><button className="icon-button">⋮</button></div>
      {loading ? <div className="loading">Loading ticket intelligence…</div> : <div className="board"><section className="lane auto-lane"><div className="lane-head"><div><span className="lane-kicker"><i/>AUTO-RESOLVED</span><h3>Ready to send</h3></div><span className="lane-count">{auto.length}</span></div><p className="lane-desc">High-confidence decisions with a drafted reply.</p><div className="cards">{auto.map((ticket) => <TicketCard key={ticket.id} ticket={ticket} selected={selected?.id === ticket.id} onSelect={setSelected} />)}</div></section><section className="lane human-lane"><div className="lane-head"><div><span className="lane-kicker review-kicker"><i/>NEEDS HUMAN</span><h3>Review required</h3></div><span className="lane-count review-count">{human.length}</span></div><p className="lane-desc">Low confidence or ambiguous precedents.</p><div className="cards">{human.map((ticket) => <TicketCard key={ticket.id} ticket={ticket} selected={selected?.id === ticket.id} onSelect={setSelected} />)}</div></section></div>}
    </section>
    <TicketDetails ticket={selected} onResolve={approve} />
    {toast && <div className="toast"><Icon>✓</Icon>{toast}</div>}
  </main>
}
