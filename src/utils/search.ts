export function buildSearchParams(
  search: Record<string, unknown>,
  pageNum: number,
  pageSize: number,
  stringFields?: string[]
): Record<string, unknown> {
  const params: Record<string, unknown> = {
    page: pageNum || 1,
    size: pageSize || 10,
  }
  Object.entries(search).forEach(([k, v]) => {
    if (v !== '' && v !== null && v !== undefined) {
      if (stringFields?.includes(k)) {
        params[k] = String(v)
      } else {
        params[k] = typeof v === 'string' ? v.trim() : v
      }
    }
  })
  return params
}

export function buildTimeRangeParams(
  range: [string, string] | null,
  startKey: string,
  endKey: string
): Record<string, string> {
  if (range && range.length === 2) {
    return {
      [startKey]: range[0] + 'T00:00:00',
      [endKey]: range[1] + 'T23:59:59',
    }
  }
  return {}
}

export function resetSearch<T extends Record<string, unknown>>(search: T, defaults: T) {
  Object.keys(defaults).forEach((k) => {
    ;(search as Record<string, unknown>)[k] = defaults[k]
  })
}