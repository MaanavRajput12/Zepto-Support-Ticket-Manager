import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  API_BASE_URL,
  approveHumanReview,
  getDecisions,
  getTickets,
  overrideHumanReview,
  resolveTicket,
  resolveUnprocessedTickets,
} from './services/ticketApi'

const Icon = ({ children, className = '' }) => <span className={`icon ${className}`}>{children}</span>
const ticketLabel = (id) => `TKT-${String(id).padStart(4, '0')}`
const actionLabel = (action) => action ? action.replaceAll('_', ' ') : 'Not assessed'
const money = (value) => typeof value === 'number' ? `Rs ${value}` : 'Not exposed'

function decisionFields(decision) {
  if (!decision) return {}
  const { id, ticketId, ...rest } = decision
  return { ...rest, decisionLogId: id, ticketId }
}

function Confidence({ value }) {
  if (typeof value !== 'number') return <span className="confidence low"><i />Not assessed</span>
  const tone = value >= 90 ? 'high' : value >= 75 ? 'medium' : 'low'
  return <span className={`confidence ${tone}`}><i />{value}% confident</span>
}

function statusFor(ticket) {
  if (ticket.decision === 'AUTO_RESOLVED' || (ticket.decision === 'HUMAN_REVIEW' && ticket.executedAction && ticket.executedAction !== 'NONE')) return 'auto'
  if (ticket.decision === 'HUMAN_REVIEW') return 'human'
  return 'pending'
}

function TicketCard({ ticket, selected, onSelect }) {
  const status = statusFor(ticket)
  const human = status !== 'auto'
  const statusText = status === 'auto'
    ? ticket.decision === 'HUMAN_REVIEW' ? 'Human resolved' : 'Auto-resolved'
    : status === 'human' ? 'Human review' : 'Awaiting analysis'
  const action = ticket.suggestedAction ?? ticket.selectedAction

  return (
    <button className={`ticket-card ${selected ? 'selected' : ''} ${human ? 'review' : ''}`} onClick={() => onSelect(ticket)}>
      <div className="ticket-top">
        <span className="ticket-id">{ticketLabel(ticket.id)}</span>
        <span className={`status ${human ? 'status-review' : 'status-auto'}`}><Icon>{human ? 'o' : 'OK'}</Icon>{statusText}</span>
      </div>
      <p className="ticket-description">{ticket.description}</p>
      <div className="ticket-meta"><span>Support ticket</span><span>-</span><span>Order #{ticket.orderId}</span></div>
      <div className="ticket-bottom">
        <span className="action-label">{status === 'human' ? 'Suggested' : 'Action'}: <strong>{actionLabel(action)}</strong></span>
        <Confidence value={ticket.confidence} />
      </div>
    </button>
  )
}

function HumanReviewControls({ ticket, busy, onApprove, onOverride }) {
  const suggestedAction = ticket.suggestedAction ?? ticket.selectedAction ?? 'NONE'
  const canApproveSuggestion = suggestedAction !== 'NONE'
  const [action, setAction] = useState(canApproveSuggestion ? suggestedAction : 'REFUND')
  const [reviewNote, setReviewNote] = useState('')

  useEffect(() => {
    const nextSuggestion = ticket.suggestedAction ?? ticket.selectedAction ?? 'NONE'
    setAction(nextSuggestion !== 'NONE' ? nextSuggestion : 'REFUND')
    setReviewNote('')
  }, [ticket.id, ticket.suggestedAction, ticket.selectedAction])

  return (
    <div className="human-controls">
      <div className="section-title">Human review action</div>
      <div className="review-form">
        <button className="primary" disabled={busy || !canApproveSuggestion} onClick={() => onApprove(ticket)}>
          {busy ? 'Working...' : canApproveSuggestion ? `Approve ${actionLabel(suggestedAction)}` : 'No suggestion to approve'}
        </button>
        <select value={action} onChange={(event) => setAction(event.target.value)}>
          <option value="REFUND">Refund</option>
          <option value="REDELIVERY">Redelivery</option>
          <option value="COUPON">Coupon</option>
        </select>
        <textarea
          placeholder="Reason for override"
          value={reviewNote}
          onChange={(event) => setReviewNote(event.target.value)}
        />
        <button className="secondary" disabled={busy} onClick={() => onOverride(ticket, action, reviewNote)}>
          Override and execute
        </button>
      </div>
    </div>
  )
}

function TicketDetails({ ticket, resolving, reviewing, onResolve, onApprove, onOverride }) {
  const [copied, setCopied] = useState(false)
  if (!ticket) {
    return <aside className="details-panel"><div className="empty-details">Select a ticket to inspect its decision trail.</div></aside>
  }

  const status = statusFor(ticket)
  const human = status === 'human'
  const hasDecision = Boolean(ticket.decision)
  const suggestedAction = ticket.suggestedAction ?? ticket.selectedAction
  const executedAction = ticket.executedAction ?? ticket.actionResult?.action ?? (ticket.actionResult?.success ? suggestedAction : 'NONE')
  const order = ticket.order ?? {}

  const copyReply = async () => {
    if (!ticket.draftedReply) return
    await navigator.clipboard?.writeText(ticket.draftedReply)
    setCopied(true)
    setTimeout(() => setCopied(false), 1500)
  }

  return (
    <aside className="details-panel">
      <div className="details-head">
        <div>
          <div className="eyebrow">TICKET DETAIL</div>
          <h2>{ticketLabel(ticket.id)}</h2>
          <p>Order #{ticket.orderId}</p>
        </div>
        <span className={`status ${human || status === 'pending' ? 'status-review' : 'status-auto'}`}>
          <Icon>{human || status === 'pending' ? 'o' : 'OK'}</Icon>
          {hasDecision ? (human ? 'Needs human' : 'Resolved') : 'Unassessed'}
        </span>
      </div>

      <div className="detail-section issue">
        <div className="section-title">Customer issue</div>
        <p>{ticket.description}</p>
      </div>

      <div className="order-card">
        <div className="section-title">Order information</div>
        <div className="order-grid">
          <div><span>Order ID</span><b>#{ticket.orderId}</b></div>
          <div><span>Order value</span><b>{money(order.value)}</b></div>
          <div><span>Items</span><b>{order.items ?? 'Not exposed'}</b></div>
          <div><span>Delivery status</span><b>{order.status ?? 'Not exposed'}</b></div>
          <div><span>Delivery time</span><b>{order.deliveryTime ? `${order.deliveryTime} min` : 'Not exposed'}</b></div>
        </div>
      </div>

      {hasDecision ? (
        <>
          <div className={`decision ${human ? 'decision-review' : ''}`}>
            <div className="decision-header">
              <div>
                <span className="section-title">{human ? 'Recommended resolution' : 'Executed resolution'}</span>
                <strong>{human ? actionLabel(suggestedAction) : actionLabel(executedAction)}</strong>
              </div>
              <Confidence value={ticket.confidence} />
            </div>
            <div className="action-grid">
              <div><span>Suggested</span><b>{actionLabel(suggestedAction)}</b></div>
              <div><span>Executed</span><b>{actionLabel(executedAction)}</b></div>
              <div><span>Result</span><b>{ticket.actionResult?.message ?? ticket.actionMessage ?? 'Not executed'}</b></div>
            </div>
            {human && (
              <div className="conflict">
                <Icon>!</Icon>
                <span>This ticket needs human review. No customer action has been executed automatically.</span>
              </div>
            )}
            <div className="why">
              <span className="why-title"><Icon>*</Icon>Why this action?</span>
              <p>{ticket.reasoning}</p>
            </div>
          </div>

          <div className="detail-section">
            <div className="section-row">
              <div className="section-title">Top similar tickets</div>
              <span className="subtle">Historical outcomes</span>
            </div>
            <div className="similar-list">
              {ticket.topPrecedents?.length ? ticket.topPrecedents.map((item, index) => (
                <div className="similar" key={item.ticketId}>
                  <span className="rank">{index + 1}</span>
                  <div>
                    <b>{ticketLabel(item.ticketId)}</b>
                    <p>{item.description}</p>
                    <span>{actionLabel(item.action)}{item.resolutionNote ? ` - ${item.resolutionNote}` : ''}{item.csat ? ` - CSAT ${item.csat}` : ''}</span>
                  </div>
                  <strong>{item.similarity}%<small>match</small></strong>
                </div>
              )) : <p className="empty-copy">Run the decision again to load the current top precedents.</p>}
            </div>
          </div>

          <div className="reply">
            <div className="section-row">
              <div className="section-title">Drafted customer reply</div>
              <button className="text-button" onClick={copyReply}>{copied ? 'Copied' : 'Copy'}</button>
            </div>
            <p>{ticket.draftedReply}</p>
          </div>

          {human && (
            <HumanReviewControls
              ticket={ticket}
              busy={reviewing}
              onApprove={onApprove}
              onOverride={onOverride}
            />
          )}
        </>
      ) : (
        <div className="decision decision-review">
          <div className="why">
            <span className="why-title"><Icon>*</Icon>Decision pending</span>
            <p>Run the backend decision engine to retrieve the recommendation, confidence, reasoning, reply, and historical precedents.</p>
          </div>
        </div>
      )}

      <div className="review-actions">
        <button className="primary" disabled={resolving} onClick={() => onResolve(ticket)}>
          {resolving ? 'Running decision...' : hasDecision ? 'Run decision again' : 'Run decision'} <span>-&gt;</span>
        </button>
      </div>
    </aside>
  )
}

export default function App() {
  const [tickets, setTickets] = useState([])
  const [selected, setSelected] = useState(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [actionError, setActionError] = useState('')
  const [view, setView] = useState('tickets')
  const [filter, setFilter] = useState('All tickets')
  const [search, setSearch] = useState('')
  const [resolving, setResolving] = useState(false)
  const [reviewing, setReviewing] = useState(false)
  const [toast, setToast] = useState('')

  const loadData = useCallback(async (active = true) => {
    setLoadError('')
    await resolveUnprocessedTickets()
    return Promise.all([getTickets(), getDecisions()])
      .then(([ticketData, decisions]) => {
        if (!active) return
        const latestByTicket = new Map()
        decisions.forEach((decision) => {
          const known = latestByTicket.get(decision.ticketId)
          if (!known || new Date(decision.createdAt) > new Date(known.createdAt)) latestByTicket.set(decision.ticketId, decision)
        })
        const mapped = ticketData.map((ticket) => ({
          ...ticket,
          ...decisionFields(latestByTicket.get(ticket.id)),
          id: ticket.id,
          topPrecedents: [],
        }))
        setTickets(mapped)
        setSelected((current) => mapped.find((ticket) => ticket.id === current?.id) ?? mapped[0] ?? null)
      })
      .catch((requestError) => active && setLoadError(requestError.message))
      .finally(() => active && setLoading(false))
  }, [])

  useEffect(() => {
    let active = true
    loadData(active)
    return () => { active = false }
  }, [loadData])

  const updateTicket = (ticket, response) => {
    const updated = { ...ticket, ...response, id: response.ticketId ?? ticket.id }
    setTickets((current) => current.map((item) => item.id === ticket.id ? updated : item))
    setSelected(updated)
    return updated
  }

  const resolve = async (ticket) => {
    setResolving(true)
    setActionError('')
    try {
      updateTicket(ticket, await resolveTicket(ticket.id))
      setToast(`${ticketLabel(ticket.id)} decision generated`)
      setTimeout(() => setToast(''), 2600)
    } catch (requestError) {
      setActionError(requestError.message)
      if (requestError.message.includes('No ticket exists')) await loadData()
    } finally {
      setResolving(false)
    }
  }

  const approve = async (ticket) => {
    setReviewing(true)
    setActionError('')
    try {
      updateTicket(ticket, await approveHumanReview(ticket.id))
      setToast(`${ticketLabel(ticket.id)} approved and executed`)
      setTimeout(() => setToast(''), 2600)
    } catch (requestError) {
      setActionError(requestError.message)
      if (requestError.message.includes('No ticket exists')) await loadData()
    } finally {
      setReviewing(false)
    }
  }

  const override = async (ticket, action, reviewNote) => {
    setReviewing(true)
    setActionError('')
    try {
      updateTicket(ticket, await overrideHumanReview(ticket.id, { action, reviewNote }))
      setToast(`${ticketLabel(ticket.id)} override executed`)
      setTimeout(() => setToast(''), 2600)
    } catch (requestError) {
      setActionError(requestError.message)
      if (requestError.message.includes('No ticket exists')) await loadData()
    } finally {
      setReviewing(false)
    }
  }

  const filtered = useMemo(() => tickets.filter((ticket) => (
    filter === 'All tickets' || statusFor(ticket) === filter
  ) && `${ticket.id} ${ticket.orderId} ${ticket.description}`.toLowerCase().includes(search.toLowerCase())), [tickets, filter, search])
  const auto = filtered.filter((ticket) => statusFor(ticket) === 'auto')
  const review = filtered.filter((ticket) => statusFor(ticket) !== 'auto')
  const averageConfidence = tickets.filter((ticket) => typeof ticket.confidence === 'number')
    .reduce((sum, ticket, _, list) => sum + ticket.confidence / list.length, 0)
  const knowledgeActions = [...new Set(tickets.map((ticket) => ticket.suggestedAction ?? ticket.selectedAction).filter(Boolean))]

  return (
    <main className="app-shell">
      <nav className="sidebar">
        <div className="brand"><div className="brand-mark">z</div><span>zepto</span></div>
        <div className="workspace-label">WORKSPACE</div>
        <button className={`nav-item ${view === 'tickets' ? 'active' : ''}`} onClick={() => setView('tickets')}><Icon>TI</Icon>Ticket inbox<span className="nav-count">{tickets.length}</span></button>
        <button className={`nav-item ${view === 'analytics' ? 'active' : ''}`} onClick={() => setView('analytics')}><Icon>AN</Icon>Analytics</button>
        <button className={`nav-item ${view === 'knowledge' ? 'active' : ''}`} onClick={() => setView('knowledge')}><Icon>KB</Icon>Knowledge base</button>
        <div className="sidebar-foot">
          <button className="nav-item"><Icon>*</Icon>Settings</button>
          <div className="avatar">SK</div>
          <div className="agent"><b>Siddhi Kulkarni</b><span>Support lead</span></div>
        </div>
      </nav>

      <section className="workspace">
        <header className="topbar">
          <div><div className="eyebrow">SUPPORT OPERATIONS</div><h1>{view === 'tickets' ? 'Ticket inbox' : view === 'analytics' ? 'Analytics' : 'Knowledge base'}</h1></div>
          <div className="live"><span className="live-dot" />AI triage live</div>
        </header>

        <div className="summary">
          <div><span>Total tickets</span><b>{tickets.length}</b><small>Seeded and created tickets</small></div>
          <div><span>Auto/human resolved</span><b>{tickets.filter((ticket) => statusFor(ticket) === 'auto').length}</b><small>Action executed</small></div>
          <div><span>Human review queue</span><b>{tickets.filter((ticket) => statusFor(ticket) === 'human').length}</b><small className="amber">Awaiting reviewer</small></div>
          <div><span>Average confidence</span><b>{averageConfidence ? Math.round(averageConfidence) : 0}%</b><small>Latest decisions</small></div>
        </div>

        {actionError && <div className="action-error">Action failed: {actionError}</div>}

        {view === 'tickets' && (
          <div className="toolbar">
            <div className="search"><Icon>?</Icon><input placeholder="Search ticket, order, issue..." value={search} onChange={(event) => setSearch(event.target.value)} /></div>
            <select value={filter} onChange={(event) => setFilter(event.target.value)}>
              <option value="All tickets">All tickets</option>
              <option value="auto">Resolved</option>
              <option value="human">Human review</option>
              <option value="pending">Awaiting assessment</option>
            </select>
          </div>
        )}

        {loading ? <div className="loading">Loading tickets from the support API...</div>
          : loadError ? <div className="loading">Support API error: {loadError}</div>
            : !tickets.length ? <div className="loading">No tickets are currently available.</div>
              : view === 'analytics' ? (
                <div className="insight-grid">
                  <div><span className="section-title">API connection</span><b>{API_BASE_URL}</b><p>Frontend requests are routed through the centralized ticket API client.</p></div>
                  <div><span className="section-title">Resolution split</span><b>{auto.length} resolved / {tickets.filter((ticket) => statusFor(ticket) === 'human').length} human</b><p>Resolved includes automatic and reviewer-executed tickets.</p></div>
                  <div><span className="section-title">Average confidence</span><b>{averageConfidence ? Math.round(averageConfidence) : 0}%</b><p>Calculated from the latest decision logs returned by the backend.</p></div>
                </div>
              ) : view === 'knowledge' ? (
                <div className="knowledge-list">
                  <div><span className="section-title">Resolved-ticket precedents</span><p>The backend ranks historical resolved tickets with TF-IDF similarity and returns the top three cases for every decision.</p></div>
                  <div><span className="section-title">Known recommended actions</span><p>{knowledgeActions.length ? knowledgeActions.map(actionLabel).join(', ') : 'Run decisions to populate recommended actions.'}</p></div>
                  <div><span className="section-title">Safety rules</span><p>Cancelled orders cannot be redelivered, refunds are capped by order value, and uncertain/conflicting evidence stays in human review.</p></div>
                </div>
              ) : (
                <div className="board">
                  <section className="lane auto-lane">
                    <div className="lane-head"><div><span className="lane-kicker"><i />AUTO-RESOLVED</span><h3>Ready to send</h3></div><span className="lane-count">{auto.length}</span></div>
                    <p className="lane-desc">Backend decisions with a simulated action and drafted reply.</p>
                    <div className="cards">{auto.map((ticket) => <TicketCard key={ticket.id} ticket={ticket} selected={selected?.id === ticket.id} onSelect={setSelected} />)}</div>
                  </section>
                  <section className="lane human-lane">
                    <div className="lane-head"><div><span className="lane-kicker review-kicker"><i />NEEDS HUMAN</span><h3>Review or assess</h3></div><span className="lane-count review-count">{review.length}</span></div>
                    <p className="lane-desc">Human-review decisions and tickets that still need assessment.</p>
                    <div className="cards">{review.map((ticket) => <TicketCard key={ticket.id} ticket={ticket} selected={selected?.id === ticket.id} onSelect={setSelected} />)}</div>
                  </section>
                </div>
              )}
      </section>

      <TicketDetails
        ticket={selected}
        resolving={resolving}
        reviewing={reviewing}
        onResolve={resolve}
        onApprove={approve}
        onOverride={override}
      />
      {toast && <div className="toast"><Icon>OK</Icon>{toast}</div>}
    </main>
  )
}
