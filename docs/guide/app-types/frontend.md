# Frontend SPA Architecture

The **Frontend SPA** architecture (`PackoraAppType.FRONTEND`) is engineered specifically for Single Page Applications created with modern JavaScript and TypeScript frameworks such as React, Vue, Vite, Svelte, Angular, Nuxt, and Next.js static exports.

---

## Client History Routing Fallback

In traditional WebViews, navigating directly to deep-linked URLs (e.g. `/dashboard/settings` or `/profile/overview`) in a Single Page Application can trigger `404 Not Found` or `net::ERR_FILE_NOT_FOUND` because the physical file does not exist on the file system or server.

With **Frontend SPA** mode in Packora:
- **`spaRoutingFallback = true`**: Automatically redirects missing path requests back to the single `index.html` entrypoint while preserving the URL path and query parameters for client-side routers (React Router, Vue Router, TanStack Router).
- **Persistent Storage**: Configures IndexedDB, local storage, session storage, and Service Worker caching policies to ensure user authentication and offline state remain intact across application restarts.

---

## Best For

- React, Vue, Svelte, and Vite web applications.
- Next.js and Nuxt.js static exports (`output: 'export'`).
- Web applications with client-side history navigation (`createBrowserHistory`).
- Modern progressive web apps with heavy local state.
