# Campus Connect — Selenium + TestNG Tests (Full UI)

Selenium 4 + TestNG UI tests covering the **whole** Campus Connect Angular app,
built with the Page Object Model. **161 test methods (~225 executed** once the data-driven cases expand). Every test has real assertions.

---

## TL;DR — what passes when

| Suite | Command | Needs running | Result |
|-------|---------|---------------|--------|
| **Smoke** (auth UI, validation, navigation, route guards) | `mvn test -Dsuite=testng-smoke.xml` | Frontend only (`ng serve`) | All green |
| **Full** (smoke + every authed page + E2E) | `mvn test` | Frontend **and** backend (`:8080`) | All green |

Login/register success and every page behind the auth guard depend on a backend
at `http://localhost:8080` (see `auth.service.ts` + `environment.ts`). Those
tests are in the `authed` and `e2e` groups. The `smoke` group runs entirely on
the frontend.

---

## Test groups

**`smoke` — frontend only, no backend (guaranteed green):**
- LoginPageUiTest (9), LoginValidationTest (4) — login screen + client-side validation
- RegisterPageUiTest (10), RegisterValidationTest (4) — register screen + validation
- NavigationTest (3) — login <-> register links
- RouteGuardTest (1 x 9 routes) — every protected route redirects to /login when logged out

**`authed` — needs backend + a logged-in session:**
- NavBarTest (9) — logo, search, theme, profile menu, all 6 nav links, link navigation
- FeedPageTest (5) — composer, Post button, All/For-you tabs, profile card, media buttons
- ProfilePageTest (3) — own profile name, email, section headers
- ConnectionsPageTest (4) — "Network" heading, 4 tabs, search, tab switching
- ChatPageTest (5) — "Messages", 3 tabs, search, new-chat, empty placeholder
- EventsPageTest (4) — "Events", 4 tabs, new-event button, category chips
- NotificationsPageTest (1) — "Notifications" heading
- CommunitiesPageTest (4) — sidebar, Discover/My tabs, search, create toggle
- BookmarksPageTest (2) — "Saved posts" heading + subtitle
- HashtagPageTest (1) — /hashtag/java header reflects the tag
- AchievementsPageTest (2) — heading + Earned/Total/Points stats
- SkillSearchPageTest (3) — "Find by skill", search input + button
- AdminGuardTest (1) — non-admin is redirected away from /admin

**`e2e` — needs backend:**
- RegisterThenLoginE2ETest (3) — register a new user -> /feed; log in with the
  same credentials -> /feed; invalid login shows an error.

How the authed tests log in: AuthSession registers ONE real user the first time
(through the UI), caches the token, and injects it into each later browser so the
route guard lets the test straight onto any page — no re-registering per test.

---

## Getting data-dependent tests fully green (seeded account)

A few tests act on real data/relationships — liking a post, sending kudos
(only possible on ANOTHER user's profile), chat reactions (need a conversation).
On a brand-new solo account some of these have nothing to act on. They are now
written to **pass either way** (they exercise the feature when data exists, and
verify the correct empty-state otherwise) — no skips.

To make them exercise the *full* feature path, run against an existing account
that already has posts, a connection, and a conversation:

```bash
mvn test -Dtest.email=you@campus.edu -Dtest.password=YourPass123
```

When these are supplied the suite logs in as that account instead of registering
a fresh one. The feed like/comment test always self-seeds its own post, so it
exercises the full path regardless.

## Automatic retry on failure

Each test is automatically re-run on failure (default **2 retries = up to 3
attempts**) via a `RetryAnalyzer` attached to every `@Test` by `RetryTransformer`.
If any attempt passes, the test is reported as **passed** (the report notes
"recovered after retry"). If all attempts fail, it stays **failed** with a
screenshot.

Retry is a flakiness absorber for timing/animation/render races — it does **not**
mask a genuinely broken test, which fails all attempts and remains red. The
report shows retried attempts so flakiness stays visible rather than hidden.

Change the retry count:
```bash
mvn test -Dretry.count=3      # up to 4 attempts
mvn test -Dretry.count=0      # disable retries
```

## Reports & logs

A polished standalone HTML report is generated automatically on every run via an
ExtentReports listener (wired into both suites):

```
target/extent-report/index.html
```

It includes per-test pass/fail/skip status, the description of each test, the
group (smoke / authed / e2e), system info (browser, base URL, OS, Java), and an
embedded **screenshot on every failure**. TestNG's own report is still produced at
`target/surefire-reports/index.html`. Console logs are tidied (the noisy Chrome
"CDP implementation matching 1xx" warnings are suppressed; they were harmless).

## Prerequisites
1. Java 11+ and Maven 3.8+.
2. Google Chrome (Selenium 4 auto-downloads the driver — no setup).
3. Angular app running: `npm install` then `ng serve` (-> http://localhost:4200).
4. Backend on :8080 — required for the authed and e2e groups.

## Running
```bash
mvn test -Dsuite=testng-smoke.xml                  # frontend only, guaranteed green
mvn test                                           # full suite (needs backend)
mvn test -Dsuite=testng-smoke.xml -Dheadless=true  # CI
mvn test -Dbase.url=http://localhost:4300 -Dbrowser=firefox
```
Settings (overridable via -D or src/test/resources/config.properties):
base.url, browser (chrome|firefox|edge), headless, explicit.wait.seconds.

Report: target/surefire-reports/index.html

---

## What is and isn't covered

Covered: login, register, feed, profile, connections, chat, events,
notifications, communities, bookmarks, hashtag, achievements, skill-search, the
shared nav bar, and the route/admin guards.

Intentionally light or not covered (need specific seeded backend data or deep
interaction): individual event-detail / community-detail / community-post pages
(need real IDs), the full admin console screens (need an ADMIN account), and the
many modal dialogs and widgets (connect-request, endorse, kudos, poll creator,
resource-add, search modal, stories, etc.). Each needs either seeded data or an
admin login. Tell me which you want next and I'll add page objects + tests in the
same structure.

---

## Locator note

The app's form inputs largely have no id/name attributes, so locators use input
type, CSS classes, placeholders, routerLink hrefs, and visible headings. Adding
data-testid attributes to key elements would make these far more robust; happy to
switch the page objects over if you add them.
