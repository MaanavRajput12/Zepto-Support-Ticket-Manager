import { useEffect, useMemo, useState } from 'react'
import { getDecisions, getTickets, resolveTicket } from './services/ticketApi'

const Icon = ({ children, className = '' }) => <span className={`icon ${className}`}>{children}</span>
const ticketLabel = (id) => `TKT-${String(id).padStart(4, '0')}`
const actionLabel = (action) => action ? action.replaceAll('_', ' ') : 'Not assessed'

function Confidence({ value }) {
  if (typeof value !== 'number') return <span className="confidence low"><i />Not assessed</span>
  const tone = value >= 90 ? 'high' : value >= 75 ? 'medium' : 'low'
  return <span className={`confidence ${tone}`}><i />{value}% confident</span>
}

function statusFor(ticket) {
  if (ticket.decision === 'AUTO_RESOLVED') return 'auto'
  if (ticket.decision === 'HUMAN_REVIEW') return 'human'
  return 'pending'
}

function TicketCard({ ticket, selected, onSelect }) {
  const status = statusFor(ticket)
  const human = status !== 'auto'
  const statusText = status === 'auto' ? 'Auto-resolved' : status === 'human' ? 'Human review' : 'Awaiting analysis'
  return (
    <button className={`ticket-card ${selected ? 'selected' : ''} ${human ? 'review' : ''}`} onClick={() => onSelect(ticket)}>
      <div className="ticket-top"><span className="ticket-id">{ticketLabel(ticket.id)}</span><span className={`status ${human ? 'status-review' : 'status-auto'}`}><Icon>{human ? '○' : '✓'}</Icon>{statusText}</span></div>
      <p className="ticket-description">{ticket.description}</p>
      <div className="ticket-meta"><span>Support ticket</span><span>•</span><span>Order #{ticket.orderId}</span></div>
      <div className="ticket-bottom"><span className="action-label">{status === 'human' ? 'Suggested' : 'Action'}: <strong>{actionLabel(ticket.selectedAction)}</strong></span><Confidence value={ticket.confidence} /></div>
    </button>
  )
}

function TicketDetails({ ticket, resolving, onResolve }) {
  const [copied, setCopied] = useState(false)
  if (!ticket) return <aside className="details-panel"><div className="empty-details">Select a ticket to inspect its decision trail.</div></aside>
  const status = statusFor(ticket)
  const human = status !== 'auto'
  const hasDecision = Boolean(ticket.decision)
  const copyReply = async () => {
    if (!ticket.draftedReply) return
    await navigator.clipboard?.writeText(ticket.draftedReply)
    setCopied(true)
    setTimeout(() => setCopied(false), 1500)
  }
  return (
    <aside className="details-panel">
      <div className="details-head"><div><div className="eyebrow">TICKET DETAIL</div><h2>{ticketLabel(ticket.id)}</h2><p>Order #{ticket.orderId} · Customer data is not exposed by this API</p></div><span className={`status ${human ? 'status-review' : 'status-auto'}`}><Icon>{human ? '○' : '✓'}</Icon>{hasDecision ? (human ? 'Needs human' : 'Resolved') : 'Unassessed'}</span></div>
      <div className="detail-section issue"><div className="section-title">Customer issue</div><p>{ticket.description}</p></div>
      <div className="order-card"><div className="section-title">Order information</div><div className="order-grid"><div><span>Order ID</span><b>#{ticket.orderId}</b></div><div><span>Order value</span><b>Not exposed</b></div><div><span>Items</span><b>Not exposed</b></div><div><span>Delivery status</span><b>Not exposed</b></div></div></div>
      {hasDecision ? <><div className={`decision ${human ? 'decision-review' : ''}`}><div className="decision-header"><div><span className="section-title">{human ? 'Recommended resolution' : 'Executed resolution'}</span><strong>{actionLabel(ticket.selectedAction)}</strong></div><Confidence value={ticket.confidence} /></div>{human && <div className="conflict"><Icon>!</Icon><span>This decision requires a human review. The backend does not provide an approve/reject API.</span></div>}<div className="why"><span className="why-title"><Icon>✦</Icon>Why this action?</span><p>{ticket.reasoning}</p></div></div>
      <div className="detail-section"><div className="section-row"><div className="section-title">Top similar tickets</div><span className="subtle">Historical outcomes</span></div><div className="similar-list">{ticket.topPrecedents?.map((item, index) => <div className="similar" key={item.ticketId}><span className="rank">{index + 1}</span><div><b>{ticketLabel(item.ticketId)}</b><p>{item.description}</p><span>{actionLabel(item.action)}{item.resolutionNote ? ` · ${item.resolutionNote}` : ''}</span></div><strong>{item.similarity}%<small>match</small></strong></div>)}</div></div>
      <div className="reply"><div className="section-row"><div className="section-title">Drafted customer reply</div><button className="text-button" onClick={copyReply}>{copied ? 'Copied' : 'Copy'}</button></div><p>{ticket.draftedReply}</p></div></> : <div className="decision decision-review"><div className="why"><span className="why-title"><Icon>✦</Icon>Decision pending</span><p>Run the backend decision engine to retrieve the real recommendation, confidence, reasoning, reply, and historical precedents.</p></div></div>}
      <div className="review-actions"><button className="primary" disabled={resolving} onClick={() => onResolve(ticket)}>{resolving ? 'Running decision…' : hasDecision ? 'Run decision again' : 'Run decision'} <span>→</span></button></div>
    </aside>
  )
}

export default function App() {
  const [tickets, setTickets] = useState([])
  const [selected, setSelected] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filter, setFilter] = useState('All tickets')
  const [search, setSearch] = useState('')
  const [resolving, setResolving] = useState(false)
  const [toast, setToast] = useState('')

  useEffect(() => {
    let active = true
    Promise.all([getTickets(), getDecisions()]).then(([ticketData, decisions]) => {
      if (!active) return
      const latestByTicket = new Map()
      decisions.forEach((decision) => {
        const known = latestByTicket.get(decision.ticketId)
        if (!known || new Date(decision.createdAt) > new Date(known.createdAt)) latestByTicket.set(decision.ticketId, decision)
      })
      const mapped = ticketData.map((ticket) => ({ ...ticket, ...latestByTicket.get(ticket.id), topPrecedents: [] }))
      setTickets(mapped)
      setSelected(mapped[0] ?? null)
    }).catch((requestError) => active && setError(requestError.message)).finally(() => active && setLoading(false))
    return () => { active = false }
  }, [])

  const filtered = useMemo(() => tickets.filter((ticket) => (filter === 'All tickets' || statusFor(ticket) === filter) && `${ticket.id} ${ticket.orderId} ${ticket.description}`.toLowerCase().includes(search.toLowerCase())), [tickets, filter, search])
  const auto = filtered.filter((ticket) => statusFor(ticket) === 'auto')
  const review = filtered.filter((ticket) => statusFor(ticket) !== 'auto')
  const resolve = async (ticket) => {
    setResolving(true)
    setError('')
    try {
      const decision = await resolveTicket(ticket.id)
      const updated = { ...ticket, ...decision }
      setTickets((current) => current.map((item) => item.id === ticket.id ? updated : item))
      setSelected(updated)
      setToast(`${ticketLabel(ticket.id)} decision generated`)
      setTimeout(() => setToast(''), 2600)
    } catch (requestError) { setError(requestError.message) } finally { setResolving(false) }
  }

  return <main className="app-shell"><nav className="sidebar"><div className="brand"><div className="brand-mark">z</div><span>zepto</span></div><div className="workspace-label">WORKSPACE</div><button className="nav-item active"><Icon>⌑</Icon>Ticket inbox<span className="nav-count">{tickets.length}</span></button><button className="nav-item"><Icon>○</Icon>Analytics</button><button className="nav-item"><Icon>⌘</Icon>Knowledge base</button><div className="sidebar-foot"><button className="nav-item"><Icon>⚙</Icon>Settings</button><div className="avatar">SK</div><div className="agent"><b>Siddhi Kulkarni</b><span>Support lead</span></div></div></nav>
    <section className="workspace"><header className="topbar"><div><div className="eyebrow">SUPPORT OPERATIONS</div><h1>Ticket inbox</h1></div><div className="live"><span className="live-dot"/>AI triage live</div></header><div className="summary"><div><span>Auto-resolved</span><b>{tickets.filter((ticket) => statusFor(ticket) === 'auto').length}</b><small>Recorded by decision API</small></div><div><span>Human review queue</span><b>{tickets.filter((ticket) => statusFor(ticket) === 'human').length}</b><small className="amber">Recorded review decisions</small></div><div><span>Awaiting assessment</span><b>{tickets.filter((ticket) => !ticket.decision).length}</b><small>Run a decision to classify</small></div><div className="summary-note"><Icon>✦</Icon><p>Decisions and confidence come directly from the ticket-resolution API.</p></div></div>
      <div className="toolbar"><div className="search"><Icon>⌕</Icon><input placeholder="Search ticket, order, issue…" value={search} onChange={(event) => setSearch(event.target.value)} /></div><select value={filter} onChange={(event) => setFilter(event.target.value)}><option value="All tickets">All tickets</option><option value="auto">Auto-resolved</option><option value="human">Human review</option><option value="pending">Awaiting assessment</option></select></div>
      {loading ? <div className="loading">Loading tickets from the support API…</div> : error ? <div className="loading">Could not load the support API: {error}</div> : !tickets.length ? <div className="loading">No tickets are currently available.</div> : <div className="board"><section className="lane auto-lane"><div className="lane-head"><div><span className="lane-kicker"><i/>AUTO-RESOLVED</span><h3>Ready to send</h3></div><span className="lane-count">{auto.length}</span></div><p className="lane-desc">Backend decisions with a simulated action and drafted reply.</p><div className="cards">{auto.map((ticket) => <TicketCard key={ticket.id} ticket={ticket} selected={selected?.id === ticket.id} onSelect={setSelected} />)}</div></section><section className="lane human-lane"><div className="lane-head"><div><span className="lane-kicker review-kicker"><i/>NEEDS HUMAN</span><h3>Review or assess</h3></div><span className="lane-count review-count">{review.length}</span></div><p className="lane-desc">Human-review decisions and tickets that still need assessment.</p><div className="cards">{review.map((ticket) => <TicketCard key={ticket.id} ticket={ticket} selected={selected?.id === ticket.id} onSelect={setSelected} />)}</div></section></div>}</section>
    <TicketDetails ticket={selected} resolving={resolving} onResolve={resolve} />{toast && <div className="toast"><Icon>✓</Icon>{toast}</div>}</main>
}
