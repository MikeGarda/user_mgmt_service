import { NextResponse } from "next/server"

// Kurzer Timeout, damit ein langsames/unerreichbares Backend die
// Readiness-Probe nicht blockiert (Next.js Server soll zuegig antworten).
const BACKEND_HEALTH_TIMEOUT_MS = 2000

/**
 * Readiness-Check: Neben "Server laeuft" wird zusaetzlich geprueft, ob das
 * Backend erreichbar ist. Nur wenn beides stimmt, kann der Pod sinnvoll
 * Traffic bedienen (Login/Dashboard haengen vom Backend ab).
 *
 * Nutzt den Actuator-Endpoint des Backends, der in WebSecurityConfig
 * bewusst ohne Authentifizierung freigegeben ist (/actuator/health/**).
 */
export async function GET() {
  const apiUrl = process.env.NEXT_PUBLIC_API_URL

  if (!apiUrl) {
    return NextResponse.json(
      { status: "down", reason: "NEXT_PUBLIC_API_URL not configured" },
      { status: 503 }
    )
  }

  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), BACKEND_HEALTH_TIMEOUT_MS)

  try {
    const res = await fetch(`${apiUrl}/actuator/health/readiness`, {
      method: "GET",
      signal: controller.signal,
      cache: "no-store",
    })

    if (!res.ok) {
      return NextResponse.json(
        { status: "down", reason: `backend responded with ${res.status}` },
        { status: 503 }
      )
    }

    return NextResponse.json({ status: "ok" }, { status: 200 })
  } catch (err) {
    return NextResponse.json(
      {
        status: "down",
        reason: err instanceof Error ? err.message : "backend unreachable",
      },
      { status: 503 }
    )
  } finally {
    clearTimeout(timeout)
  }
}
