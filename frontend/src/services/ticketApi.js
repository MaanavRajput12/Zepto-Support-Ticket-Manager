export const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

async function request(path, options) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({}))
    throw new Error(error.message ?? `Request failed (${response.status})`)
  }

  return response.json()
}

export const getTickets = () => request('/api/tickets')
export const getDecisions = () => request('/api/decisions')
export const getPrecedents = (ticketId) => request(`/api/tickets/${ticketId}/precedents`)
export const resolveTicket = (ticketId) => request(`/api/tickets/${ticketId}/resolve`, { method: 'POST' })
export const resolveUnprocessedTickets = () => request('/api/tickets/resolve-unprocessed', { method: 'POST' })
export const approveHumanReview = (ticketId) => request(`/api/tickets/${ticketId}/human-review/approve`, { method: 'POST' })
export const overrideHumanReview = (ticketId, payload) => request(`/api/tickets/${ticketId}/human-review/override`, {
  method: 'POST',
  body: JSON.stringify(payload),
})
